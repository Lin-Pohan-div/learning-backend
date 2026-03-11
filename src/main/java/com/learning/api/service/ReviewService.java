package com.learning.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.learning.api.entity.Reviews;
import com.learning.api.repo.ReviewRepository;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private static final int MAX_COMMENT_LENGTH = 500;

    public List<Reviews> findAll() {
        return reviewRepository.findAll();
    }

    public Optional<Reviews> findById(Long id) {
        return reviewRepository.findById(id);
    }

    public List<Reviews> findByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    public List<Reviews> findByCourseId(Long courseId) {
        return reviewRepository.findByCourseId(courseId);
    }

    public Double getAverageRating(Long courseId) {
        Double average = reviewRepository.findAverageRatingByCourseId(courseId);
        return average != null ? average : 0.0;
    }

    public Reviews save(Reviews review) {
        validateReview(review);
        return reviewRepository.save(review);
    }

    public Optional<Reviews> update(Long id, Reviews updatedReview) {
        return reviewRepository.findById(id).map(existing -> {
            validateReview(updatedReview);
            existing.setFocusScore(updatedReview.getFocusScore());
            existing.setComprehensionScore(updatedReview.getComprehensionScore());
            existing.setConfidenceScore(updatedReview.getConfidenceScore());
            existing.setComment(updatedReview.getComment());
            return reviewRepository.save(existing);
        });
    }

    public boolean deleteById(Long id) {
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void validateReview(Reviews review) {
        if (review.getFocusScore() == null) {
            throw new IllegalArgumentException("專注分數不能為空");
        }
        if (review.getComprehensionScore() == null) {
            throw new IllegalArgumentException("理解分數不能為空");
        }
        if (review.getConfidenceScore() == null) {
            throw new IllegalArgumentException("自信分數不能為空");
        }
        if (review.getComment() != null && review.getComment().length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("評論不能超過 " + MAX_COMMENT_LENGTH + " 個字元");
        }
    }
}
