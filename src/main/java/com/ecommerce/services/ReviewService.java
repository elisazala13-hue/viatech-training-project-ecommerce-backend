package com.ecommerce.services;

import com.ecommerce.exceptions.BadRequestException;
import com.ecommerce.exceptions.ConflictException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.DTO.requests.ReviewRequest;
import com.ecommerce.models.DTO.responses.ReviewResponse;
import com.ecommerce.models.DTO.responses.ReviewSummaryResponse;
import com.ecommerce.models.entities.Product;
import com.ecommerce.models.entities.Review;
import com.ecommerce.models.enums.OrderStatus;
import com.ecommerce.models.enums.ProductStatus;
import com.ecommerce.repositories.OrderItemRepository;
import com.ecommerce.repositories.ProductRepository;
import com.ecommerce.repositories.ReviewRepository;
import com.ecommerce.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> listForProduct(Long productId, Pageable pageable) {
        getApprovedProduct(productId);
        return reviewRepository.findByProductId(productId, pageable).map(ReviewResponse::from);
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse summary(Long productId) {
        getApprovedProduct(productId);
        ReviewRepository.RatingSummary s = reviewRepository.summarize(productId);

        // round to one decimal place
        double average = s.getAverage() == null ? 0 : Math.round(s.getAverage() * 10) / 10.0;
        return new ReviewSummaryResponse(average, s.getCount() == null ? 0 : s.getCount());
    }

    @Transactional
    public ReviewResponse create(Long userId, Long productId, ReviewRequest req) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!orderItemRepository.hasPurchased(productId, userId, OrderStatus.DELIVERED)) {
            throw new BadRequestException("You can review only products you have received");
        }
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ConflictException("You have already reviewed this product");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(userRepository.getReferenceById(userId));
        review.setRating(req.rating());
        review.setComment(clean(req.comment()));

        return ReviewResponse.from(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponse update(Long userId, Long reviewId, ReviewRequest req) {
        Review review = findOwned(userId, reviewId);
        review.setRating(req.rating());
        review.setComment(clean(req.comment()));
        return ReviewResponse.from(review);
    }

    @Transactional
    public void delete(Long userId, Long reviewId) {
        reviewRepository.delete(findOwned(userId, reviewId));
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> listMine(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable).map(ReviewResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> listAll(Pageable pageable) {
        Page<Review> page = reviewRepository.findAll(pageable);
        return page.map(ReviewResponse::from);
    }

    @Transactional
    public void deleteAny(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review not found");
        }
        reviewRepository.deleteById(reviewId);
    }

    private Review findOwned(Long userId, Long reviewId) {
        return reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
    }

    private void getApprovedProduct(Long productId) {
        productRepository.findByIdAndStatus(productId, ProductStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private String clean(String comment) {
        return (comment == null || comment.isBlank()) ? null : comment.trim();
    }
}
