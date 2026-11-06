package com.ecommerce.services;

import com.ecommerce.models.DTO.requests.ProductRequest;
import com.ecommerce.models.DTO.responses.ProductResponse;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.entities.Category;
import com.ecommerce.models.entities.Product;
import com.ecommerce.models.entities.SellerProfile;
import com.ecommerce.models.entities.Store;
import com.ecommerce.models.enums.ProductStatus;
import com.ecommerce.repositories.CartItemRepository;
import com.ecommerce.repositories.CategoryRepository;
import com.ecommerce.repositories.ProductRepository;
import com.ecommerce.repositories.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final SellerService sellerService;

    @Transactional
    public ProductResponse create(Long userId, ProductRequest req) {
        SellerProfile seller = sellerService.getApprovedSeller(userId);

        Product product = new Product();
        product.setSeller(seller);
        product.setStatus(ProductStatus.PENDING);
        fill(product, req);

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long userId, Long productId, ProductRequest req) {
        Product product = findOwned(userId, productId);
        fill(product, req);

        if (product.getStatus() == ProductStatus.REJECTED) {
            product.setStatus(ProductStatus.PENDING);
        }
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(Long userId, Long productId) {
        Product product = findOwned(userId, productId);
        product.setStatus(ProductStatus.DELETED);
        cartItemRepository.deleteByProductId(productId);
    }

    @Transactional(readOnly = true)
    public ProductResponse getMine(Long userId, Long productId) {
        return ProductResponse.from(findOwned(userId, productId));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listMine(Long userId, Pageable pageable) {
        SellerProfile seller = sellerService.getApprovedSeller(userId);
        return productRepository
                .findBySellerIdAndStatusNot(seller.getId(), ProductStatus.DELETED, pageable)
                .map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(Long categoryId, String q, Pageable pageable) {
        String text = (q == null || q.isBlank()) ? null : q.trim();
        return productRepository.search(ProductStatus.APPROVED, categoryId, text, pageable)
                .map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse getPublic(Long id) {
        return productRepository.findByIdAndStatus(id, ProductStatus.APPROVED)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listByStore(Long storeId, Pageable pageable) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        return productRepository
                .findBySellerIdAndStatus(store.getSeller().getId(), ProductStatus.APPROVED, pageable)
                .map(ProductResponse::from);
    }

    private Product findOwned(Long userId, Long productId) {
        SellerProfile seller = sellerService.getApprovedSeller(userId);
        return productRepository
                .findByIdAndSellerIdAndStatusNot(productId, seller.getId(), ProductStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private void fill(Product product, ProductRequest req) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        product.setCategory(category);
        product.setName(req.name().trim());
        product.setDescription(req.description().trim());
        product.setPrice(req.price());
        product.setQuantity(req.stockQuantity());
    }
}
