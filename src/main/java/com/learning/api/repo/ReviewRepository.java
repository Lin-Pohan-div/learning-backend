package com.learning.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import com.learning.api.entity.Review;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserId(Long userId);
    List<Review> findByCourseId(Long courseId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.courseId = :courseId")
    Double findAverageRatingByCourseId(@Param("courseId") Long courseId);
}
