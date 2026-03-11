package com.learning.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import com.learning.api.entity.Reviews;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Reviews, Long> {
    List<Reviews> findByUserId(Long userId);
    List<Reviews> findByCourseId(Long courseId);

    @Query("SELECT AVG((r.focusScore + r.comprehensionScore + r.confidenceScore) / 3.0) FROM Reviews r WHERE r.courseId = :courseId")
    Double findAverageRatingByCourseId(@Param("courseId") Long courseId);
}
