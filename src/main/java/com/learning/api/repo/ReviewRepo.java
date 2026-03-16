package com.learning.api.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.learning.api.entity.Reviews;

@Repository
public interface ReviewRepo extends JpaRepository<Reviews, Long> {
    List<Reviews> findByCourseId(Long courseId);
}
