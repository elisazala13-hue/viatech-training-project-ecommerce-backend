package com.ecommerce.services;

import com.ecommerce.models.DTO.responses.ProductImageResponse;
import com.ecommerce.exceptions.ConflictException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.entities.Product;
import com.ecommerce.models.entities.ProductImage;
import com.ecommerce.models.entities.SellerProfile;
import com.ecommerce.models.enums.ProductStatus;
import com.ecommerce.repositories.ProductImageRepository;
import com.ecommerce.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private static final int MAX_IMAGES = 8;

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final SellerService sellerService;
    private final FileStorageService fileStorageService;

    @Transactional
    public ProductImageResponse upload(Long userId, Long productId, MultipartFile file) {
        Product product = findOwned(userId, productId);

        if (product.getImages().size() >= MAX_IMAGES) {
            throw new ConflictException("A product can have at most " + MAX_IMAGES + " images");
        }

        String url = fileStorageService.store(file, "products");

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl(url);
        image.setPrimaryImage(product.getImages().isEmpty());

        ProductImage saved = productImageRepository.save(image);
        product.getImages().add(saved);

        return ProductImageResponse.from(saved);
    }

    @Transactional
    public void delete(Long userId, Long productId, Long imageId) {
        Product product = findOwned(userId, productId);

        ProductImage image = product.getImages().stream()
                .filter(i -> i.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        boolean wasPrimary = image.isPrimaryImage();
        product.getImages().remove(image);

        if (wasPrimary && !product.getImages().isEmpty()) {
            product.getImages().get(0).setPrimaryImage(true);
        }

        fileStorageService.delete(image.getUrl());
    }

    private Product findOwned(Long userId, Long productId) {
        SellerProfile seller = sellerService.getApprovedSeller(userId);
        return productRepository
                .findByIdAndSellerIdAndStatusNot(productId, seller.getId(), ProductStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }
}
