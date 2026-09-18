package com.habittracker.repository;

import com.habittracker.exception.StorageException;
import com.habittracker.model.CompletionLog;
import com.habittracker.util.CsvUtil;
import com.habittracker.util.DateUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FileLogRepository implements Repository<CompletionLog, String> {
    private final File dataFile;
    private final List<CompletionLog> cache = new ArrayList<CompletionLog>();
    private boolean loaded = false;

    public FileLogRepository(String filePath) {
        this.dataFile = new File(filePath);
    }

    public FileLogRepository() {
        this("data/logs.csv");
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
                if (tokens.size() >= 4) {
                    int habitId = Integer.parseInt(tokens.get(0));
                    LocalDate date = DateUtil.parseDate(tokens.get(1));
                    boolean completed = Boolean.parseBoolean(tokens.get(2));
                    int xp = Integer.parseInt(tokens.get(3));

                    cache.add(new CompletionLog(habitId, date, completed, xp));
                }
            }
            loaded = true;
        } catch (Exception e) {
            throw new StorageException("Failed to read logs file: " + dataFile.getPath(), e);
        }
    }

    @Override
    public synchronized Optional<CompletionLog> findById(String id) throws StorageException {
        // id formatted as "habitId:date"
        ensureLoaded();
        if (id == null) return Optional.empty();
        String[] parts = id.split(":");
        if (parts.length != 2) return Optional.empty();
        int habitId = Integer.parseInt(parts[0]);
        LocalDate date = DateUtil.parseDate(parts[1]);

        return findByHabitAndDate(habitId, date);
    }

    public synchronized Optional<CompletionLog> findByHabitAndDate(int habitId, LocalDate date) throws StorageException {
        ensureLoaded();
        for (CompletionLog log : cache) {
            if (log.getHabitId() == habitId && log.getDate().equals(date)) {
                return Optional.of(log);
            }
        }
        return Optional.empty();
    }

    public synchronized List<CompletionLog> findByHabitId(int habitId) throws StorageException {
        ensureLoaded();
        List<CompletionLog> results = new ArrayList<CompletionLog>();
        for (CompletionLog log : cache) {
            if (log.getHabitId() == habitId) {
                results.add(log);
            }
        }
        results.sort(Comparator.comparing(CompletionLog::getDate));
        return results;
    }

    @Override
    public synchronized List<CompletionLog> findAll() throws StorageException {
        ensureLoaded();
        return new ArrayList<CompletionLog>(cache);
    }

    @Override
    public synchronized CompletionLog save(CompletionLog log) throws StorageException {
        ensureLoaded();
        Optional<CompletionLog> existing = findByHabitAndDate(log.getHabitId(), log.getDate());
        if (existing.isPresent()) {
            cache.remove(existing.get());
            cache.add(log);
            persistAll();
        } else {
            // O(1) Fast append
            appendLog(log);
        }
        return log;
    }

    public synchronized void appendLog(CompletionLog log) throws StorageException {
        ensureLoaded();
        cache.add(log);

        File parent = dataFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        boolean writeHeader = !dataFile.exists() || dataFile.length() == 0;
        try (FileWriter fw = new FileWriter(dataFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {

            if (writeHeader) {
                pw.println("habitId,date,completed,xpEarned");
            }
            String line = CsvUtil.join(
                    String.valueOf(log.getHabitId()),
                    DateUtil.formatDate(log.getDate()),
                    String.valueOf(log.isCompleted()),
                    String.valueOf(log.getXpEarned())
            );
            pw.println(line);
        } catch (IOException e) {
            throw new StorageException("Failed to append log entry to " + dataFile.getPath(), e);
        }
    }

    @Override
    public synchronized void saveAll(List<CompletionLog> entities) throws StorageException {
        ensureLoaded();
        cache.clear();
        cache.addAll(entities);
        persistAll();
    }

    private synchronized void persistAll() throws StorageException {
        File parent = dataFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        File tempFile = new File(dataFile.getAbsolutePath() + ".tmp");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
            bw.write("habitId,date,completed,xpEarned");
            bw.newLine();

            for (CompletionLog log : cache) {
                String line = CsvUtil.join(
                        String.valueOf(log.getHabitId()),
                        DateUtil.formatDate(log.getDate()),
                        String.valueOf(log.isCompleted()),
                        String.valueOf(log.getXpEarned())
                );
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            if (tempFile.exists()) tempFile.delete();
            throw new StorageException("Failed to write temporary logs file", e);
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
            throw new StorageException("Failed to atomically update logs file", e);
        }
    }

    @Override
    public synchronized boolean deleteById(String id) throws StorageException {
        ensureLoaded();
        Optional<CompletionLog> log = findById(id);
        if (log.isPresent()) {
            cache.remove(log.get());
            persistAll();
            return true;
        }
        return false;
    }

    public synchronized void deleteByHabitId(int habitId) throws StorageException {
        ensureLoaded();
        cache.removeIf(l -> l.getHabitId() == habitId);
        persistAll();
    }
}
