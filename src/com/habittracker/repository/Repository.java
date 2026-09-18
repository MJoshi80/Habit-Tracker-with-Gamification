package com.habittracker.repository;

import com.habittracker.exception.StorageException;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    Optional<T> findById(ID id) throws StorageException;
    List<T> findAll() throws StorageException;
    T save(T entity) throws StorageException;
    void saveAll(List<T> entities) throws StorageException;
    boolean deleteById(ID id) throws StorageException;
}
