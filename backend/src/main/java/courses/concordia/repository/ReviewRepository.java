package courses.concordia.repository;

import courses.concordia.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    Optional<Review> findByInstructorIdAndUserIdAndType(String instructorId, String userId, String type);

    Optional<Review> findByCourseIdAndUserIdAndType(String courseId, String userId, String type);

    List<Review> findAllByInstructorIdAndType(String instructorId, String type);

    List<Review> findAllByCourseIdAndType(String courseId, String type);
}
