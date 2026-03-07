package com.learning.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.learning.api.entity.LessonFeedback;
import java.util.List;
import java.util.Optional;

public interface LessonFeedbackRepository extends JpaRepository<LessonFeedback, Long> {
    List<LessonFeedback> findByLessonId(Long lessonId);
    Optional<LessonFeedback> findFirstByLessonId(Long lessonId);

    @Query("SELECT AVG(f.rating) FROM LessonFeedback f WHERE f.lessonId = :lessonId")
    Double findAverageRatingByLessonId(@Param("lessonId") Long lessonId);
}
