package com.learning.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.learning.api.entity.Review;
import com.learning.api.repo.ReviewRepository;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    private static final int MAX_COMMENT_LENGTH = 500;

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }

    public List<Review> findByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    public List<Review> findByCourseId(Long courseId) {
        return reviewRepository.findByCourseId(courseId);
    }

    public Double getAverageRating(Long courseId) {
        Double average = reviewRepository.findAverageRatingByCourseId(courseId);
        return average != null ? average : 0.0;
    }

    public Review save(Review review) {
        validateReview(review);
        return reviewRepository.save(review);
    }

    public Optional<Review> update(Long id, Review updatedReview) {
        return reviewRepository.findById(id).map(existing -> {
            validateReview(updatedReview);
            existing.setRating(updatedReview.getRating());
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

    private void validateReview(Review review) {
        if (review.getRating() == null) {
            throw new IllegalArgumentException("評分不能為空");
        }
        if (review.getRating() < MIN_RATING || review.getRating() > MAX_RATING) {
            throw new IllegalArgumentException("評分必須在 " + MIN_RATING + " 到 " + MAX_RATING + " 之間");
        }
        if (review.getComment() != null && review.getComment().length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("評論不能超過 " + MAX_COMMENT_LENGTH + " 個字元");
        }
    }
}
