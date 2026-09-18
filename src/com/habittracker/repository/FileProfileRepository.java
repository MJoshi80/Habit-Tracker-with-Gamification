package com.habittracker.repository;

import com.habittracker.exception.StorageException;
import com.habittracker.model.BadgeType;
import com.habittracker.model.UserProfile;
import com.habittracker.util.CsvUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FileProfileRepository implements Repository<UserProfile, String> {
    private final File dataFile;
    private UserProfile cachedProfile = null;

    public FileProfileRepository(String filePath) {
        this.dataFile = new File(filePath);
    }

    public FileProfileRepository() {
        this("data/profile.csv");
    }

    private synchronized void ensureLoaded() throws StorageException {
        if (cachedProfile != null) {
            return;
        }
        if (!dataFile.exists()) {
            cachedProfile = new UserProfile("User", 0, EnumSet.noneOf(BadgeType.class));
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String row1 = br.readLine();
            String row2 = br.readLine();

            String userName = "User";
            int totalXp = 0;
            Set<BadgeType> badges = EnumSet.noneOf(BadgeType.class);

            if (row1 != null && !row1.trim().isEmpty()) {
                List<String> tokens = CsvUtil.parseLine(row1);
                if (tokens.size() >= 1 && !tokens.get(0).trim().isEmpty()) {
                    userName = tokens.get(0);
                }
                if (tokens.size() >= 2) {
                    try {
                        totalXp = Integer.parseInt(tokens.get(1).trim());
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            if (row2 != null && !row2.trim().isEmpty()) {
                List<String> tokens = CsvUtil.parseLine(row2);
                for (String token : tokens) {
                    BadgeType badge = BadgeType.fromString(token);
                    if (badge != null) {
                        badges.add(badge);
                    }
                }
            }

            cachedProfile = new UserProfile(userName, totalXp, badges);
        } catch (Exception e) {
            throw new StorageException("Failed to read profile file: " + dataFile.getPath(), e);
        }
    }

    public synchronized UserProfile getProfile() throws StorageException {
        ensureLoaded();
        return cachedProfile;
    }

    @Override
    public synchronized Optional<UserProfile> findById(String id) throws StorageException {
        ensureLoaded();
        return Optional.ofNullable(cachedProfile);
    }

    @Override
    public synchronized List<UserProfile> findAll() throws StorageException {
        ensureLoaded();
        return Collections.singletonList(cachedProfile);
    }

    @Override
    public synchronized UserProfile save(UserProfile entity) throws StorageException {
        this.cachedProfile = entity;
        File parent = dataFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        File tempFile = new File(dataFile.getAbsolutePath() + ".tmp");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
            // Row 1: userName, totalXp
            bw.write(CsvUtil.join(entity.getUserName(), String.valueOf(entity.getTotalXp())));
            bw.newLine();

            // Row 2: badge1,badge2,...
            List<String> badgeNames = new ArrayList<String>();
            for (BadgeType badge : entity.getBadges()) {
                badgeNames.add(badge.name());
            }
            bw.write(CsvUtil.join(badgeNames));
            bw.newLine();
        } catch (IOException e) {
            if (tempFile.exists()) tempFile.delete();
            throw new StorageException("Failed to write temporary profile file", e);
        }

        try {
            Path tempPath = tempFile.toPath();
            Path targetPath = dataFile.toPath();
            try {
                Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to atomically update profile file", e);
        }

        return entity;
    }

    @Override
    public synchronized void saveAll(List<UserProfile> entities) throws StorageException {
        if (!entities.isEmpty()) {
            save(entities.get(0));
        }
    }

    @Override
    public synchronized boolean deleteById(String id) throws StorageException {
        cachedProfile = new UserProfile("User", 0, EnumSet.noneOf(BadgeType.class));
        save(cachedProfile);
        return true;
    }
}
