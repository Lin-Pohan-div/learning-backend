package com.learning.api.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.learning.api.dto.feedback.FeedbackEmailDTO;
import com.learning.api.entity.Bookings;
import com.learning.api.entity.Course;
import com.learning.api.entity.LessonFeedback;
import com.learning.api.entity.Order;
import com.learning.api.entity.User;
import com.learning.api.repo.BookingRepo;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.LessonFeedbackRepository;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.UserRepo;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonFeedbackService {

    private static final Logger log = LoggerFactory.getLogger(LessonFeedbackService.class);

    private final LessonFeedbackRepository lessonFeedbackRepository;
    private final BookingRepo bookingRepo;
    private final UserRepo userRepo;
    private final OrderRepository orderRepository;
    private final CourseRepo courseRepo;
    private final EmailService emailService;

    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;
    private static final int MAX_COMMENT_LENGTH = 1000;

    public List<LessonFeedback> findAll() {
        return lessonFeedbackRepository.findAll();
    }

    public Optional<LessonFeedback> findById(Long id) {
        return lessonFeedbackRepository.findById(id);
    }

    public List<LessonFeedback> findByBookingId(Long bookingId) {
        return lessonFeedbackRepository.findByBookingId(bookingId);
    }

    public Double getAverageRating(Long bookingId) {
        Double average = lessonFeedbackRepository.findAverageRatingByBookingId(bookingId);
        return average != null ? average : 0.0;
    }

    public LessonFeedback save(LessonFeedback feedback) {
        if (lessonFeedbackRepository.existsByBookingId(feedback.getBookingId())) {
            throw new IllegalArgumentException("這堂課已經填寫過回饋囉！");
        }
        validate(feedback);
        LessonFeedback saved = lessonFeedbackRepository.save(feedback);
        buildAndSendFeedbackEmail(saved);
        return saved;
    }

    private void buildAndSendFeedbackEmail(LessonFeedback saved) {
        // Step 1: 查 Booking（需要 date, hour, studentId, tutorId, orderId）
        Bookings booking = bookingRepo.findById(saved.getBookingId()).orElse(null);
        if (booking == null) {
            log.warn("Feedback email 略過：找不到 booking，bookingId={}", saved.getBookingId());
            return;
        }

        // Step 2: 查 Student（需要 email, name）
        User student = userRepo.findById(booking.getStudentId()).orElse(null);
        // Step 3: 查 Tutor（只需要 name，Tutor.id = User.id）
        User tutor = userRepo.findById(booking.getTutorId()).orElse(null);
        // Step 4: 查 Order（需要 courseId）
        Order order = orderRepository.findById(booking.getOrderId()).orElse(null);
        if (student == null || tutor == null || order == null) {
            log.warn("Feedback email 略過：缺少相關資料，bookingId={}", booking.getId());
            return;
        }

        // Step 5: 查 Course（需要 name）
        Course course = courseRepo.findById(order.getCourseId()).orElse(null);
        if (course == null) {
            log.warn("Feedback email 略過：找不到課程，courseId={}", order.getCourseId());
            return;
        }

        // Step 6: 組裝 FeedbackEmailDTO（comment 可為 null，轉為空字串）
        FeedbackEmailDTO dto = new FeedbackEmailDTO();
        dto.setStudentEmail(student.getEmail());
        dto.setStudentName(student.getName());
        dto.setTutorName(tutor.getName());
        dto.setCourseName(course.getName());
        dto.setDate(booking.getDate());
        dto.setHour(booking.getHour());
        dto.setFocusScore(saved.getFocusScore());
        dto.setComprehensionScore(saved.getComprehensionScore());
        dto.setConfidenceScore(saved.getConfidenceScore());
        dto.setComment(saved.getComment() != null ? saved.getComment() : "");

        // Step 7: 呼叫現有的 sendFeedbackEmail（內部已有 try-catch）
        emailService.sendFeedbackEmail(dto);
    }

    public Optional<LessonFeedback> update(Long id, LessonFeedback updated) {
        return lessonFeedbackRepository.findById(id).map(existing -> {
            validate(updated);
            existing.setFocusScore(updated.getFocusScore());
            existing.setComprehensionScore(updated.getComprehensionScore());
            existing.setConfidenceScore(updated.getConfidenceScore());
            existing.setRating(updated.getRating());
            existing.setComment(updated.getComment());
            return lessonFeedbackRepository.save(existing);
        });
    }

    public boolean deleteById(Long id) {
        if (lessonFeedbackRepository.existsById(id)) {
            lessonFeedbackRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void validate(LessonFeedback feedback) {
        validateScore("專注度", feedback.getFocusScore());
        validateScore("理解度", feedback.getComprehensionScore());
        validateScore("自信度", feedback.getConfidenceScore());
        validateScore("評分", feedback.getRating());
        if (feedback.getComment() != null && feedback.getComment().length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("評論不能超過 " + MAX_COMMENT_LENGTH + " 個字元");
        }
    }
    private void validateScore(String fieldName, Integer score) {
        if (score == null) {
            throw new IllegalArgumentException(fieldName + "不能為空");
        }
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new IllegalArgumentException(fieldName + "必須在 " + MIN_SCORE + " 到 " + MAX_SCORE + " 之間");
        }
    }
}
