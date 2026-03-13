package com.learning.api.repo;

import com.learning.api.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepo extends JpaRepository<Course, Long> {
    boolean existsByTutorId(Long tutorId);
    List<Course> findByTutorId(Long tutorId);
}
