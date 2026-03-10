package com.learning.api.repo;

import com.learning.api.entity.LessonFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonFeedbackRepo extends JpaRepository<LessonFeedback, Long> {
    // 檢查這堂課是不是已經寫過回饋了 (防呆)
    boolean existsByBookingId(Long bookingId);
}