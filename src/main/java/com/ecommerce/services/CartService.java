package com.ecommerce.services;

import com.ecommerce.exceptions.BadRequestException;
import com.ecommerce.models.DTO.requests.AddToCartRequest;
import com.ecommerce.models.DTO.responses.CartResponse;
import com.ecommerce.models.DTO.requests.UpdateCartItemRequest;
import com.ecommerce.exceptions.ConflictException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.entities.Cart;
import com.ecommerce.models.entities.CartItem;
import com.ecommerce.models.entities.Product;
import com.ecommerce.models.enums.ProductStatus;
import com.ecommerce.repositories.CartItemRepository;
import com.ecommerce.repositories.CartRepository;
import com.ecommerce.repositories.ProductRepository;
import com.ecommerce.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponse getCart(Long userId) {
        return CartResponse.from(getOrCreateCart(userId));
    }

    @Transactional
    public CartResponse addItem(Long userId, AddToCartRequest req) {
        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findByIdAndStatus(req.productId(), ProductStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getSeller().getUser().getId().equals(userId)) {
            throw new BadRequestException("You cannot buy your own products");
        }

        // check if product already exists in cart
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        int newQuantity = (item == null ? 0 : item.getQuantity()) + req.quantity();
        checkStock(product, newQuantity);

        // if product doesn't exist create it, else update qty
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
            cart.getItems().add(item);
        } else {
            item.setQuantity(newQuantity);
        }

        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, UpdateCartItemRequest req) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = findItem(cart, itemId);

        Product product = item.getProduct();
        if (product.getStatus() != ProductStatus.APPROVED) {
            throw new ConflictException("Product is no longer available");
        }
        checkStock(product, req.quantity());

        item.setQuantity(req.quantity());
        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = findItem(cart, itemId);

        cart.getItems().remove(item);
        return CartResponse.from(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(userRepository.getReferenceById(userId));
            return cartRepository.save(cart);
        });
    }

    private CartItem findItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
    }

    private void checkStock(Product product, int quantity) {
        if (quantity > product.getQuantity()) {
            throw new ConflictException("Only " + product.getQuantity() + " in stock");
        }
    }
}
