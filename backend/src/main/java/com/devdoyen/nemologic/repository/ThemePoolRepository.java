package com.devdoyen.nemologic.repository;

import com.devdoyen.nemologic.model.ThemePool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ThemePoolRepository extends JpaRepository<ThemePool, Long> {

    long countByWidthAndHeightAndUsedFalse(int width, int height);

    boolean existsByNameIgnoreCase(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM ThemePool t WHERE t.width = :width AND t.height = :height AND t.used = false ORDER BY t.id ASC")
    List<ThemePool> findFirstUnused(@Param("width") int width, @Param("height") int height, Pageable pageable);
}
