package com.StartUp.controller;

import com.StartUp.dtos.ReviewRequest;
import com.StartUp.entity.Review;
import com.StartUp.entity.User;
import com.StartUp.repository.UserRepository;
import com.StartUp.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsForUser(@PathVariable Long userId) {
        List<Review> reviews = reviewService.getReviewsForUser(userId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody ReviewRequest request) {
        User reviewer = userRepository.findById(request.getReviewerId()).orElseThrow();
        User reviewedUser = userRepository.findById(request.getReviewedUserId()).orElseThrow();
        return ResponseEntity.ok(reviewService.createReview(
                request.getJobApplicationId(), reviewer, reviewedUser,
                request.getRating(), request.getComment()
        ));
    }

    @GetMapping("/reviewer/{reviewerId}")
    public ResponseEntity<List<Review>> getReviewsByReviewer(@PathVariable Long reviewerId) {
        List<Review> reviews = reviewService.getReviewsByReviewer(reviewerId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}/average")
    public ResponseEntity<Map<String, Double>> getUserAverageRating(@PathVariable Long userId) {
        Double avgRating = reviewService.getUserAverageRating(userId);
        return ResponseEntity.ok(Map.of("averageRating", avgRating != null ? avgRating : 0.0));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        Review review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Review> updateReview(
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> request) {
        Integer rating = request.containsKey("rating") ? (Integer) request.get("rating") : null;
        String comment = request.containsKey("comment") ? (String) request.get("comment") : null;
        
        Review updatedReview = reviewService.updateReview(reviewId, rating, comment);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
