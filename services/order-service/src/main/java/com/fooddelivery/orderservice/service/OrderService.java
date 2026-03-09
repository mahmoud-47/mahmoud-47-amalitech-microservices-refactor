package com.fooddelivery.orderservice.service;

import com.fooddelivery.commonutils.dto.SharedCustomerResponse;
import com.fooddelivery.commonutils.dto.SharedMenuItemResponse;
import com.fooddelivery.commonutils.dto.SharedRestaurantResponse;
import com.fooddelivery.commonutils.events.OrderCancelledEvent;
import com.fooddelivery.commonutils.events.OrderPlacedEvent;
import com.fooddelivery.orderservice.client.CustomerClient;
import com.fooddelivery.orderservice.client.RestaurantClient;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.dto.PlaceOrderRequest;
import com.fooddelivery.orderservice.messaging.OrderEventPublisher;
import com.fooddelivery.orderservice.model.Order;
import com.fooddelivery.orderservice.model.OrderItem;
import com.fooddelivery.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;       // Feign — replaces CustomerService
    private final RestaurantClient restaurantClient;   // Feign — replaces RestaurantService
    private final OrderEventPublisher eventPublisher;  // Async — replaces DeliveryService call

    public OrderService(OrderRepository orderRepository,
                        CustomerClient customerClient,
                        RestaurantClient restaurantClient,
                        OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.customerClient = customerClient;
        this.restaurantClient = restaurantClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse placeOrder(String customerUsername, PlaceOrderRequest request) {
        // Feign: validate customer exists and get their address
        SharedCustomerResponse customer = customerClient.getByUsername(customerUsername);

        // Feign: validate restaurant exists and is active
        SharedRestaurantResponse restaurant = restaurantClient.getById(request.getRestaurantId());

        if (!restaurant.isActive()) {
            throw new IllegalStateException("Restaurant is currently not accepting orders");
        }

        String deliveryAddress = request.getDeliveryAddress() != null
                ? request.getDeliveryAddress()
                : customer.getDeliveryAddress();

        Order order = Order.builder()
                .customerId(customer.getId())       // ID only — no entity reference
                .restaurantId(restaurant.getId())   // ID only — no entity reference
                .deliveryAddress(deliveryAddress)
                .specialInstructions(request.getSpecialInstructions())
                .estimatedDeliveryTime(
                        LocalDateTime.now().plusMinutes(restaurant.getEstimatedDeliveryMinutes()))
                .build();

        // Feign: validate each menu item + snapshot name and price
        BigDecimal total = BigDecimal.ZERO;
        for (var itemReq : request.getItems()) {
            SharedMenuItemResponse menuItem = restaurantClient.getMenuItem(itemReq.getMenuItemId());

            if (!menuItem.isAvailable()) {
                throw new IllegalStateException("Menu item is not available: " + menuItem.getName());
            }
            if (!menuItem.getRestaurantId().equals(restaurant.getId())) {
                throw new IllegalStateException("Menu item does not belong to this restaurant");
            }

            BigDecimal subtotal = menuItem.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItemId(menuItem.getId())
                    .itemName(menuItem.getName())   // snapshot — stored locally
                    .quantity(itemReq.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .subtotal(subtotal)
                    .specialInstructions(itemReq.getSpecialInstructions())
                    .build();

            order.getItems().add(orderItem);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);

        // ASYNC: publish event — Delivery Service subscribes and creates delivery
        // Order response is returned immediately, delivery assignment happens in background
        eventPublisher.publishOrderPlaced(OrderPlacedEvent.builder()
                .orderId(savedOrder.getId())
                .customerId(customer.getId())
                .restaurantId(restaurant.getId())
                .restaurantName(restaurant.getName())
                .deliveryAddress(deliveryAddress)
                .restaurantAddress(restaurant.getAddress())  // snapshot
                .totalAmount(total)
                .placedAt(LocalDateTime.now())
                .build());

        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long orderId) {
        return OrderResponse.fromEntity(findById(orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrders(String username) {
        SharedCustomerResponse customer = customerClient.getByUsername(username);
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId())
                .stream().map(OrderResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getRestaurantOrders(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
                .stream().map(OrderResponse::fromEntity).toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, String status) {
        Order order = findById(orderId);
        order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        return OrderResponse.fromEntity(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String username) {
        Order order = findById(orderId);

        // Feign: verify ownership without entity traversal
        SharedCustomerResponse customer = customerClient.getByUsername(username);
        if (!order.getCustomerId().equals(customer.getId())) {
            throw new IllegalStateException("You can only cancel your own orders");
        }

        if (order.getStatus() != Order.OrderStatus.PLACED
                && order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        // ASYNC: publish event — Delivery Service subscribes and cancels delivery
        eventPublisher.publishOrderCancelled(OrderCancelledEvent.builder()
                .orderId(saved.getId())
                .customerId(customer.getId())
                .reason("Cancelled by customer")
                .cancelledAt(LocalDateTime.now())
                .build());

        return OrderResponse.fromEntity(saved);
    }

    // ── Internal endpoint — called by Delivery Service via Feign ─────────────
    public com.fooddelivery.commonutils.dto.SharedOrderResponse getSharedById(Long orderId) {
        Order order = findById(orderId);
        return com.fooddelivery.commonutils.dto.SharedOrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .customerId(order.getCustomerId())
                .restaurantId(order.getRestaurantId())
                .deliveryAddress(order.getDeliveryAddress())
                .build();
    }

    private Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }
}