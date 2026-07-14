package com.velvet.sakura.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.velvet.sakura.entity.Reading;

public interface ReadingRepository extends JpaRepository<Reading,Long> {
List<Reading> findByUserId(Long userId);
    
}
