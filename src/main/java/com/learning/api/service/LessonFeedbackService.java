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

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    private static final int MAX_COMMENT_LENGTH = 1000;

    public List<LessonFeedback> findAll() {
        return lessonFeedbackRepository.findAll();
    }

    public Optional<LessonFeedback> findById(Long id) {
        return lessonFeedbackRepository.findById(id);
    }

    public List<LessonFeedback> findByLessonId(Long lessonId) {
        return lessonFeedbackRepository.findByLessonId(lessonId);
    }

    public Double getAverageRating(Long lessonId) {
        Double average = lessonFeedbackRepository.findAverageRatingByLessonId(lessonId);
        return average != null ? average : 0.0;
    }

    public LessonFeedback save(LessonFeedback feedback) {
        validate(feedback);
        return lessonFeedbackRepository.save(feedback);
    }

    public Optional<LessonFeedback> update(Long id, LessonFeedback updated) {
        return lessonFeedbackRepository.findById(id).map(existing -> {
            validate(updated);
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
        if (feedback.getRating() == null) {
            throw new IllegalArgumentException("評分不能為空");
        }
        if (feedback.getRating() < MIN_RATING || feedback.getRating() > MAX_RATING) {
            throw new IllegalArgumentException("評分必須在 " + MIN_RATING + " 到 " + MAX_RATING + " 之間");
        }
        if (feedback.getComment() != null && feedback.getComment().length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("評論不能超過 " + MAX_COMMENT_LENGTH + " 個字元");
        }
    }
}
