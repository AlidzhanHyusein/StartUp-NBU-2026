package com.StartUp.service;

import com.StartUp.entity.*;
import com.StartUp.repository.ReviewRepository;
import com.StartUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public Review createReview(Long jobApplicationId, User reviewer, User reviewedUser,
                               Integer rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Review review = Review.builder()
                .jobApplicationId(jobApplicationId)
                .reviewer(reviewer)
                .reviewedUser(reviewedUser)
                .rating(rating)
                .comment(comment)
                .build();

        Review savedReview = reviewRepository.save(review);

        // Автоматично update на avg_rating на потребителя
        updateUserAverageRating(reviewedUser.getId());

        // Нотификация за потребителя
        notificationService.createNotification(
                reviewedUser,
                Notification.NotificationType.NEW_REVIEW,
                "Ново ревю",
                reviewer.getFullName() + " ви остави ревю с рейтинг " + rating + "/5",
                savedReview.getId(),
                "Review"
        );

        return savedReview;
    }

    @Transactional
    public void updateUserAverageRating(Long userId) {
        Double avgRating = reviewRepository.calculateAverageRating(userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Закръгляне до цяло число
        int roundedRating = avgRating != null ? (int) Math.round(avgRating) : 0;
        user.setRating(roundedRating);
        
        userRepository.save(user);
    }

    public List<Review> getReviewsForUser(Long userId) {
        return reviewRepository.findByReviewedUserId(userId);
    }

    public List<Review> getReviewsByReviewer(Long reviewerId) {
        return reviewRepository.findByReviewerId(reviewerId);
    }

    public Double getUserAverageRating(Long userId) {
        return reviewRepository.calculateAverageRating(userId);
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
    }

    @Transactional
    public Review updateReview(Long reviewId, Integer rating, String comment) {
        Review review = getReviewById(reviewId);
        
        if (rating != null) {
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            review.setRating(rating);
        }
        
        if (comment != null) {
            review.setComment(comment);
        }

        Review updatedReview = reviewRepository.save(review);
        
        // Обновяване на средния рейтинг
        updateUserAverageRating(review.getReviewedUser().getId());
        
        return updatedReview;
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = getReviewById(reviewId);
        Long userId = review.getReviewedUser().getId();
        
        reviewRepository.delete(review);
        
        // Обновяване на средния рейтинг след изтриване
        updateUserAverageRating(userId);
    }
}
