package com.learning.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.learning.api.entity.LessonFeedback;
import com.learning.api.repo.LessonFeedbackRepository;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonFeedbackService {

    private final LessonFeedbackRepository lessonFeedbackRepository;

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
        validate(feedback);
        return lessonFeedbackRepository.save(feedback);
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
