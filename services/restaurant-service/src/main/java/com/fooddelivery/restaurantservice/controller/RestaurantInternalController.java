package com.fooddelivery.restaurantservice.controller;

import com.fooddelivery.commonutils.dto.SharedMenuItemResponse;
import com.fooddelivery.commonutils.dto.SharedRestaurantResponse;
import com.fooddelivery.restaurantservice.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
public class RestaurantInternalController {

    private final RestaurantService restaurantService;

    public RestaurantInternalController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/restaurants/{id}")
    public ResponseEntity<SharedRestaurantResponse> getSharedRestaurant(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getSharedRestaurant(id));
    }

    @GetMapping("/menu-items/{id}")
    public ResponseEntity<SharedMenuItemResponse> getSharedMenuItem(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getSharedMenuItem(id));
    }
}