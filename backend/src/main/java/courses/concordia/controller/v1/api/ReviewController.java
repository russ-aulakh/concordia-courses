package courses.concordia.controller.v1.api;

import courses.concordia.dto.model.review.ReviewDto;
import courses.concordia.dto.model.review.ReviewFilterDto;
import courses.concordia.dto.model.review.ReviewPayloadDto;
import courses.concordia.dto.response.Response;
import courses.concordia.model.User;
import courses.concordia.service.InteractionService;
import courses.concordia.service.NotificationService;
import courses.concordia.service.ReviewService;
import courses.concordia.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reviews")
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;
    private final UserService userService;
    private final InteractionService interactionService;
    private final NotificationService notificationService;
    @Value("${beaudelaire.uploadKey}")
    private String uploadKey;

    @PutMapping("/upload")
    public ResponseEntity<String> uploadReviews(@RequestParam("file") MultipartFile file, @RequestParam String key) {
        try {
            if (!key.equals(uploadKey)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid key");
            }
            reviewService.uploadReviews(file);
            return ResponseEntity.ok("Reviews processed successfully");
        } catch (Exception e) {
            log.error("Error processing reviews file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing reviews file");
        }
    }

    @GetMapping
    public Response<?> getReviews(@RequestParam String userId) {
        List<ReviewDto> reviews = reviewService.getUserReviews(userId);
        return Response.ok().setPayload(reviews);
    }

    @GetMapping("/shared")
    public Response<?> getReview(@RequestParam String id) {
        ReviewDto review = reviewService.getReviewById(id);
        return Response.ok().setPayload(review);
    }

    @PostMapping("/filter")
    public Response<?> getReviewsWithFilters(@RequestBody ReviewFilterDto filters, @RequestParam int limit, @RequestParam int offset) {
        List<ReviewDto> reviews = reviewService.getReviewsWithFilter(limit, offset, filters);
        return Response.ok().setPayload(reviews);
    }

    @PostMapping
    public Response<?> addReview(@RequestBody ReviewDto reviewDto) {

        User user = userService.getAuthenticatedUser();
        if(user == null) {
            return Response.unauthorized();
        }
        reviewDto = reviewDto.setUserId(user.get_id());
        ReviewDto addedReview = reviewService.addOrUpdateReview(reviewDto);
        notificationService.addNotifications(reviewDto);
        return Response.ok().setPayload(addedReview);
    }

    @PutMapping
    public Response<?> updateReview(@RequestBody ReviewDto reviewDto) {

        User user = userService.getAuthenticatedUser();
        if(user == null) {
            return Response.unauthorized();
        }
        reviewDto = reviewDto.setUserId(user.get_id());
        ReviewDto addedReview = reviewService.addOrUpdateReview(reviewDto);
        notificationService.updateNotifications(user.get_id(), reviewDto.getCourseId(), reviewDto);
        return Response.ok().setPayload(addedReview);
    }

    @DeleteMapping
    public Response<?> deleteReview(@RequestBody ReviewPayloadDto reviewPayloadDto) {

        User user = userService.getAuthenticatedUser();
        if(user == null) {
            return Response.unauthorized();
        }
        ReviewDto reviewDto = reviewService.getReviewById(reviewPayloadDto.getId());
        interactionService.deleteInteractions(reviewPayloadDto.getId(), user.get_id(), reviewDto.getType());
        notificationService.deleteNotification(user.get_id(), null, reviewDto.getCourseId());
        reviewService.deleteReview(reviewPayloadDto.getId(), reviewPayloadDto.getType(), reviewPayloadDto.getCourseId(), reviewPayloadDto.getInstructorId());
        return Response.ok().setPayload("Review was deleted successfully");
    }
}