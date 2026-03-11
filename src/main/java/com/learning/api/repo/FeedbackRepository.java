package com.learning.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.learning.api.entity.Feedback;
import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    // 檢查這堂課是不是已經寫過回饋了 (防呆)
    boolean existsByBookingId(Long bookingId);

    List<Feedback> findByBookingId(Long bookingId);

    Optional<Feedback> findFirstByBookingId(Long bookingId);

    @Query("SELECT AVG(f.rating) FROM LessonFeedback f WHERE f.bookingId = :bookingId")
    Double findAverageRatingByBookingId(@Param("bookingId") Long bookingId);
}
