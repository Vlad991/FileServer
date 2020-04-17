package com.filesynch.repository;

import com.filesynch.entity.ServerSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServerSettingsRepository extends JpaRepository<ServerSettings, Long> {
    Optional<ServerSettings> findById(Long id);
}
