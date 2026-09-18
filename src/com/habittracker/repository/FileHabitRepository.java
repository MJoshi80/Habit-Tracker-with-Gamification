package com.habittracker.repository;

import com.habittracker.exception.StorageException;
import com.habittracker.model.Frequency;
import com.habittracker.model.Habit;
import com.habittracker.model.HabitCategory;
import com.habittracker.util.CsvUtil;
import com.habittracker.util.DateUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FileHabitRepository implements Repository<Habit, Integer> {
    private final File dataFile;
    private final Map<Integer, Habit> cache = new LinkedHashMap<Integer, Habit>();
    private boolean loaded = false;
    private int nextId = 1;

    public FileHabitRepository(String filePath) {
        this.dataFile = new File(filePath);
    }

    public FileHabitRepository() {
        this("data/habits.csv");
    }

    private synchronized void ensureLoaded() throws StorageException {
        if (loaded) {
            return;
        }
        cache.clear();
        if (!dataFile.exists()) {
            loaded = true;
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line = br.readLine(); // Header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                List<String> tokens = CsvUtil.parseLine(line);
                if (tokens.size() >= 6) {
                    int id = Integer.parseInt(tokens.get(0));
                    String name = tokens.get(1);
                    HabitCategory category = HabitCategory.fromString(tokens.get(2));
                    Frequency frequency = Frequency.fromString(tokens.get(3));
                    LocalDate createdOn = DateUtil.parseDate(tokens.get(4));
                    boolean active = Boolean.parseBoolean(tokens.get(5));

                    Habit habit = new Habit(id, name, category, frequency, createdOn, active);
                    cache.put(id, habit);
                    if (id >= nextId) {
                        nextId = id + 1;
                    }
                }
            }
            loaded = true;
        } catch (Exception e) {
            throw new StorageException("Failed to read habits file: " + dataFile.getPath(), e);
        }
    }

    private synchronized void persistAll() throws StorageException {
        File parent = dataFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        File tempFile = new File(dataFile.getAbsolutePath() + ".tmp");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
            bw.write("id,name,category,frequency,createdOn,active");
            bw.newLine();

            for (Habit habit : cache.values()) {
                String line = CsvUtil.join(
                        String.valueOf(habit.getId()),
                        habit.getName(),
                        habit.getCategory().name(),
                        habit.getFrequency().name(),
                        DateUtil.formatDate(habit.getCreatedOn()),
                        String.valueOf(habit.isActive())
                );
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            if (tempFile.exists()) tempFile.delete();
            throw new StorageException("Failed to write temporary habits file", e);
        }

        try {
            Path tempPath = tempFile.toPath();
            Path targetPath = dataFile.toPath();
            try {
                Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                // Fallback to standard replace
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to atomically update habits file", e);
        }
    }

    @Override
    public synchronized Optional<Habit> findById(Integer id) throws StorageException {
        ensureLoaded();
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public synchronized List<Habit> findAll() throws StorageException {
        ensureLoaded();
        return new ArrayList<Habit>(cache.values());
    }

    @Override
    public synchronized Habit save(Habit habit) throws StorageException {
        ensureLoaded();
        if (habit.getId() <= 0) {
            habit.setId(nextId++);
        } else if (habit.getId() >= nextId) {
            nextId = habit.getId() + 1;
        }
        cache.put(habit.getId(), habit);
        persistAll();
        return habit;
    }

    @Override
    public synchronized void saveAll(List<Habit> entities) throws StorageException {
        ensureLoaded();
        for (Habit h : entities) {
            if (h.getId() <= 0) {
                h.setId(nextId++);
            } else if (h.getId() >= nextId) {
                nextId = h.getId() + 1;
            }
            cache.put(h.getId(), h);
        }
        persistAll();
    }

    @Override
    public synchronized boolean deleteById(Integer id) throws StorageException {
        ensureLoaded();
        if (cache.remove(id) != null) {
            persistAll();
            return true;
        }
        return false;
    }
}
