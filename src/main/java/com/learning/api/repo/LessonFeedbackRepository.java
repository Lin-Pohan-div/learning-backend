package com.learning.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.learning.api.entity.LessonFeedback;
import java.util.List;
import java.util.Optional;

public interface LessonFeedbackRepository extends JpaRepository<LessonFeedback, Long> {
    // 檢查這堂課是不是已經寫過回饋了 (防呆)
    boolean existsByBookingId(Long bookingId);

    List<LessonFeedback> findByBookingId(Long bookingId);

    Optional<LessonFeedback> findFirstByBookingId(Long bookingId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.bookingId = :bookingId")
    Double findAverageRatingByBookingId(@Param("bookingId") Long bookingId);

    List<LessonFeedback> findByBookingIdIn(List<Long> bookingIds);

    @Query("SELECT AVG(f.rating) FROM LessonFeedback f WHERE f.bookingId IN :bookingIds")
    Double findAverageRatingByBookingIdIn(@Param("bookingIds") List<Long> bookingIds);
}
