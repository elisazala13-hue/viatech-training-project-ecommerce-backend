package com.ecommerce.services;

import com.ecommerce.models.DTO.requests.CheckoutRequest;
import com.ecommerce.models.DTO.responses.OrderResponse;
import com.ecommerce.exceptions.BadRequestException;
import com.ecommerce.exceptions.ConflictException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.entities.Address;
import com.ecommerce.models.entities.Cart;
import com.ecommerce.models.entities.CartItem;
import com.ecommerce.models.entities.Order;
import com.ecommerce.models.entities.OrderItem;
import com.ecommerce.models.entities.Product;
import com.ecommerce.models.entities.SellerOrder;
import com.ecommerce.models.enums.OrderStatus;
import com.ecommerce.models.enums.ProductStatus;
import com.ecommerce.models.enums.SellerStatus;
import com.ecommerce.repositories.AddressRepository;
import com.ecommerce.repositories.CartRepository;
import com.ecommerce.repositories.OrderRepository;
import com.ecommerce.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest req) {
        Cart cart = cartRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Address address = addressRepository.findByIdAndUserId(req.addressId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        List<CartItem> items = cart.getItems().stream()
                .sorted(Comparator.comparing((CartItem i) -> i.getProduct().getId()))
                .toList();

        Order order = new Order();
        order.setCustomer(cart.getUser());
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        Map<Long, SellerOrder> bySeller = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : items) {
            Product product = cartItem.getProduct();

            if (product.getStatus() != ProductStatus.APPROVED
                    || product.getSeller().getStatus() != SellerStatus.APPROVED) {
                throw new ConflictException("Product is no longer available: " + product.getName());
            }

            int updated = productRepository.decreaseStock(product.getId(), cartItem.getQuantity());
            if (updated == 0) {
                throw new ConflictException("Not enough stock for: " + product.getName());
            }

            SellerOrder sellerOrder = bySeller.computeIfAbsent(product.getSeller().getId(), id -> {
                SellerOrder so = new SellerOrder();
                so.setOrder(order);
                so.setSeller(product.getSeller());
                so.setStatus(OrderStatus.PENDING_PAYMENT);
                so.setSubtotal(BigDecimal.ZERO);
                order.getSellerOrders().add(so);
                return so;
            });

            OrderItem orderItem = new OrderItem();
            orderItem.setSellerOrder(sellerOrder);
            orderItem.setProduct(product);
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            sellerOrder.getItems().add(orderItem);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            sellerOrder.setSubtotal(sellerOrder.getSubtotal().add(lineTotal));
            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);
        orderRepository.save(order);

        cart.getItems().clear();

        return OrderResponse.from(order);
    }
}
