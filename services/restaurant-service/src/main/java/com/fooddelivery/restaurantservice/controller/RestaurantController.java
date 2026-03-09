package com.fooddelivery.restaurantservice.controller;

import com.fooddelivery.commonutils.dto.ApiResponse;
import com.fooddelivery.commonutils.dto.SharedMenuItemResponse;
import com.fooddelivery.commonutils.dto.SharedRestaurantResponse;
import com.fooddelivery.restaurantservice.dto.*;
import com.fooddelivery.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;

    // ── Public browsing ───────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("Restaurants fetched",
                restaurantService.getAllActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Restaurant fetched",
                restaurantService.getById(id)));
    }

    @GetMapping("/search/city")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> searchByCity(
            @RequestParam String city) {
        return ResponseEntity.ok(ApiResponse.ok("Results fetched",
                restaurantService.searchByCity(city)));
    }

    @GetMapping("/search/cuisine")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> searchByCuisine(
            @RequestParam String cuisine) {
        return ResponseEntity.ok(ApiResponse.ok("Results fetched",
                restaurantService.searchByCuisine(cuisine)));
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenu(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Menu fetched",
                restaurantService.getMenu(id)));
    }

    // ── Owner management ──────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantResponse>> create(
            @RequestHeader("X-Authenticated-User-Id") String ownerUsername,
            @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Restaurant created",
                restaurantService.createRestaurant(ownerUsername, request)));
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<ApiResponse<MenuItemResponse>> addMenuItem(
            @PathVariable Long id,
            @RequestHeader("X-Authenticated-User-Id") String ownerUsername,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Menu item added",
                restaurantService.addMenuItem(id, ownerUsername, request)));
    }

    @PutMapping("/menu-items/{itemId}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(
            @PathVariable Long itemId,
            @RequestHeader("X-Authenticated-User-Id") String ownerUsername,
            @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Menu item updated",
                restaurantService.updateMenuItem(itemId, ownerUsername, request)));
    }

    @PatchMapping("/menu-items/{itemId}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleAvailability(
            @PathVariable Long itemId,
            @RequestHeader("X-Authenticated-User-Id") String ownerUsername) {
        restaurantService.toggleAvailability(itemId, ownerUsername);
        return ResponseEntity.ok(ApiResponse.ok("Availability toggled"));
    }
}