package com.fooddelivery.customerservice.service;

import com.fooddelivery.commonutils.dto.SharedCustomerResponse;
import com.fooddelivery.customerservice.dto.*;
import com.fooddelivery.customerservice.model.Customer;
import com.fooddelivery.customerservice.repository.CustomerRepository;
import com.fooddelivery.customerservice.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public CustomerService(CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (customerRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Customer customer = Customer.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .city(request.getCity())
                .role(Customer.Role.CUSTOMER)
                .build();

        customerRepository.save(customer);
        String token = jwtUtil.generateToken(customer.getUsername(), customer.getRole().name());
        return new AuthResponse(token, customer.getId(), customer.getUsername(), customer.getRole().name());
    }

    public AuthResponse login(AuthRequest request) {
        Customer customer = customerRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(customer.getUsername(), customer.getRole().name());
        return new AuthResponse(token, customer.getId(), customer.getUsername(), customer.getRole().name());
    }

    @Transactional(readOnly = true)
    public CustomerResponse getProfile(String username) {
        return CustomerResponse.fromEntity(findByUsername(username));
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
        return CustomerResponse.fromEntity(customer);
    }

    @Transactional
    public CustomerResponse updateProfile(String username, RegisterRequest request) {
        Customer customer = findByUsername(username);
        if (request.getFirstName() != null) customer.setFirstName(request.getFirstName());
        if (request.getLastName() != null) customer.setLastName(request.getLastName());
        if (request.getPhone() != null) customer.setPhone(request.getPhone());
        if (request.getDeliveryAddress() != null) customer.setDeliveryAddress(request.getDeliveryAddress());
        if (request.getCity() != null) customer.setCity(request.getCity());
        return CustomerResponse.fromEntity(customerRepository.save(customer));
    }

    // ── Internal endpoint — called by other services via Feign ───────────────
    // Returns SharedCustomerResponse (from common-utils), not CustomerResponse
    @Transactional(readOnly = true)
    public SharedCustomerResponse getSharedById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
        return toShared(customer);
    }

    @Transactional(readOnly = true)
    public SharedCustomerResponse getSharedByUsername(String username) {
        Customer customer = findByUsername(username);
        return toShared(customer);
    }

    private Customer findByUsername(String username) {
        return customerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + username));
    }

    private SharedCustomerResponse toShared(Customer c) {
        return SharedCustomerResponse.builder()
                .id(c.getId())
                .username(c.getUsername())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .deliveryAddress(c.getDeliveryAddress())
                .city(c.getCity())
                .role(c.getRole().name())
                .build();
    }
}