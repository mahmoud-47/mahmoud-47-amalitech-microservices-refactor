package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.commonutils.dto.SharedCustomerResponse;
import com.fooddelivery.commonutils.dto.SharedMenuItemResponse;
import com.fooddelivery.commonutils.dto.SharedRestaurantResponse;
import com.fooddelivery.restaurantservice.client.CustomerClient;
import com.fooddelivery.restaurantservice.dto.*;
import com.fooddelivery.restaurantservice.model.MenuItem;
import com.fooddelivery.restaurantservice.model.Restaurant;
import com.fooddelivery.restaurantservice.repository.MenuItemRepository;
import com.fooddelivery.restaurantservice.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final CustomerClient customerClient;   // Feign — replaces CustomerRepository

    public RestaurantService(RestaurantRepository restaurantRepository,
                             MenuItemRepository menuItemRepository,
                             CustomerClient customerClient) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.customerClient = customerClient;
    }

    @Transactional
    public RestaurantResponse createRestaurant(String ownerUsername, RestaurantRequest request) {
        // Feign call to Customer Service — no direct DB access
        SharedCustomerResponse owner = customerClient.getByUsername(ownerUsername);

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .cuisineType(request.getCuisineType())
                .address(request.getAddress())
                .city(request.getCity())
                .phone(request.getPhone())
                .estimatedDeliveryMinutes(request.getEstimatedDeliveryMinutes())
                .ownerId(owner.getId())   // store ID only
                .build();

        return RestaurantResponse.fromEntity(restaurantRepository.save(restaurant));
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getById(Long id) {
        return RestaurantResponse.fromEntity(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> searchByCity(String city) {
        return restaurantRepository.findByCityIgnoreCaseAndActiveTrue(city)
                .stream().map(RestaurantResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> searchByCuisine(String cuisineType) {
        return restaurantRepository.findByCuisineTypeIgnoreCaseAndActiveTrue(cuisineType)
                .stream().map(RestaurantResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllActive() {
        return restaurantRepository.findByActiveTrue()
                .stream().map(RestaurantResponse::fromEntity).toList();
    }

    // ── Menu item management ──────────────────────────────────────────────────
    @Transactional
    public MenuItemResponse addMenuItem(Long restaurantId, String ownerUsername,
                                        MenuItemRequest request) {
        Restaurant restaurant = findEntityById(restaurantId);
        validateOwnership(restaurant, ownerUsername);

        MenuItem item = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .restaurant(restaurant)
                .build();

        return MenuItemResponse.fromEntity(menuItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenu(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId)
                .stream().map(MenuItemResponse::fromEntity).toList();
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long itemId, String ownerUsername,
                                           MenuItemRequest request) {
        MenuItem item = findMenuItemById(itemId);
        validateOwnership(item.getRestaurant(), ownerUsername);

        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getCategory() != null) item.setCategory(request.getCategory());

        return MenuItemResponse.fromEntity(menuItemRepository.save(item));
    }

    @Transactional
    public void toggleAvailability(Long itemId, String ownerUsername) {
        MenuItem item = findMenuItemById(itemId);
        validateOwnership(item.getRestaurant(), ownerUsername);
        item.setAvailable(!item.isAvailable());
        menuItemRepository.save(item);
    }

    // ── Internal — called by Order Service via Feign ──────────────────────────
    public SharedMenuItemResponse getSharedMenuItem(Long menuItemId) {
        MenuItem item = findMenuItemById(menuItemId);
        return SharedMenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .price(item.getPrice())
                .available(item.isAvailable())
                .restaurantId(item.getRestaurant().getId())
                .build();
    }

    public SharedRestaurantResponse getSharedRestaurant(Long restaurantId) {
        Restaurant r = findEntityById(restaurantId);
        return SharedRestaurantResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .address(r.getAddress())
                .active(r.isActive())
                .estimatedDeliveryMinutes(r.getEstimatedDeliveryMinutes())
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Restaurant findEntityById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + id));
    }

    private MenuItem findMenuItemById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MenuItem not found: " + id));
    }

    private void validateOwnership(Restaurant restaurant, String username) {
        // Feign call to verify ownership — no local Customer entity
        SharedCustomerResponse owner = customerClient.getById(restaurant.getOwnerId());
        if (!owner.getUsername().equals(username)) {
            throw new IllegalStateException("You don't own this restaurant");
        }
    }
}