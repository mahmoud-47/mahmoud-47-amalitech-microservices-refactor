package com.fooddelivery.orderservice.dto;

import com.fooddelivery.orderservice.client.RestaurantClient;
import com.fooddelivery.orderservice.model.Order;
import com.fooddelivery.orderservice.model.OrderItem;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@RequiredArgsConstructor
public class OrderResponse {
    private Long id;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private String deliveryAddress;
    private String specialInstructions;
    private LocalDateTime createdAt;
    private LocalDateTime estimatedDeliveryTime;
    private Long customerId;
    private Long restaurantId;
    private List<OrderItemDetail> items;

    @Data
    public static class OrderItemDetail {
        private Long id;
        private Long menuItemId;
        private String itemName;      // snapshot — stored at order time
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }

    public static OrderResponse fromEntity(Order o) {
        OrderResponse dto = new OrderResponse();
        dto.setId(o.getId());
        dto.setStatus(o.getStatus().name());
        dto.setTotalAmount(o.getTotalAmount());
        dto.setDeliveryFee(o.getDeliveryFee());
        dto.setDeliveryAddress(o.getDeliveryAddress());
        dto.setSpecialInstructions(o.getSpecialInstructions());
        dto.setCreatedAt(o.getCreatedAt());
        dto.setEstimatedDeliveryTime(o.getEstimatedDeliveryTime());
        dto.setCustomerId(o.getCustomerId());
        dto.setRestaurantId(o.getRestaurantId());

        dto.setItems(o.getItems().stream().map(item -> {
            OrderItemDetail detail = new OrderItemDetail();
            detail.setId(item.getId());
            detail.setMenuItemId(item.getMenuItemId());
            detail.setItemName(item.getItemName()); // snapshot, no Feign needed at read time
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(item.getUnitPrice());
            detail.setSubtotal(item.getSubtotal());
            return detail;
        }).toList());

        return dto;
    }
}