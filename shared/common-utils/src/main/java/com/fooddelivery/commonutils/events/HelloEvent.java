package com.fooddelivery.commonutils.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelloEvent {

    private String userId;
    private String message;
    private LocalDateTime sentAt;

    public static HelloEvent of(String userId) {
        return HelloEvent.builder()
                .userId(userId)
                .message("Hello from order-service!")
                .sentAt(LocalDateTime.now())
                .build();
    }
}