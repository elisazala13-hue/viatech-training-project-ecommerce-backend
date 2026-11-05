package com.ecommerce.services;

import com.ecommerce.models.DTO.responses.ProductResponse;
import com.ecommerce.exceptions.ConflictException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.entities.Product;
import com.ecommerce.models.enums.ProductStatus;
import com.ecommerce.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(ProductStatus status, Pageable pageable) {
        Page<Product> page = (status == null)
                ? productRepository.findByStatusNot(ProductStatus.DELETED, pageable)
                : productRepository.findByStatus(status, pageable);
        return page.map(ProductResponse::from);
    }

    @Transactional
    public ProductResponse approve(Long id) {
        Product product = findNotDeleted(id);
        if (product.getStatus() == ProductStatus.APPROVED) {
            throw new ConflictException("Product is already approved");
        }
        product.setStatus(ProductStatus.APPROVED);
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse reject(Long id) {
        Product product = findNotDeleted(id);
        if (product.getStatus() == ProductStatus.REJECTED) {
            throw new ConflictException("Product is already rejected");
        }
        product.setStatus(ProductStatus.REJECTED);
        return ProductResponse.from(product);
    }

    private Product findNotDeleted(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (product.getStatus() == ProductStatus.DELETED) {
            throw new ResourceNotFoundException("Product has been deleted");
        }
        return product;
    }
}
