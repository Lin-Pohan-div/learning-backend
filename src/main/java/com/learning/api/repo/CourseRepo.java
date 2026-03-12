package com.learning.api.repo;

import com.learning.api.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CourseRepo extends JpaRepository<Course, Long> {
    // 找出某個老師所有「已上架」的課程
    List<Course> findByTutorIdAndActive(Long tutorId, boolean active);
    boolean existsByTutorId(Long tutorId);
}
