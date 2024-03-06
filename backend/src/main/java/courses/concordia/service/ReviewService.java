package courses.concordia.service;
import courses.concordia.dto.model.course.ReviewDto;
import courses.concordia.model.Review;

import java.util.List;

public interface ReviewService {
    ReviewDto addReview(Review review);
    ReviewDto updateReview(Review review);
    void deleteReview(String reviewId);
    List<ReviewDto> getUserReviews(String userId);
}
