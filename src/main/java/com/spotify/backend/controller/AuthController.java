package com.spotify.backend.controller;

import com.spotify.backend.config.JwtUtil;
import com.spotify.backend.model.User;
import com.spotify.backend.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest authRequest) { // Changed to LoginRequest
        try {
            System.out.println("Login attempt for username/email: " + authRequest.getUsername());

            // Try to find user by username first, then by email
            User user = userRepository.findByUsername(authRequest.getUsername())
                    .orElseGet(() -> userRepository.findByEmail(authRequest.getUsername())
                            .orElse(null));

            if (user == null) {
                System.out.println("User not found with identifier: " + authRequest.getUsername());
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid username/email or password");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            System.out.println("User found: " + user.getEmail() + ", role: " + user.getRole());

            try {
                // Authenticate using email (since UserDetailsService uses email)
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getEmail(), authRequest.getPassword())
                );
            } catch (Exception e) {
                System.out.println("Authentication failed: " + e.getMessage());
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid password");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Generate JWT token
            final String jwt = jwtUtil.generateToken(user.getEmail());
            System.out.println("JWT Token generated successfully");

            // Return response with token
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("email", user.getEmail());
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("displayName", user.getDisplayName());
            response.put("role", user.getRole());
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("Registration attempt for: " + registerRequest.getEmail());

            // Validate required fields
            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Email is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Username is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Password is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Check if email already exists
            if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Email already exists");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Check if username already exists
            if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Username already exists");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Create new user
            User user = new User();
            user.setUsername(registerRequest.getUsername().trim());
            user.setEmail(registerRequest.getEmail().trim().toLowerCase());
            user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
            user.setDisplayName(registerRequest.getDisplayName() != null ?
                    registerRequest.getDisplayName().trim() : registerRequest.getUsername());
            user.setProfilePicture(registerRequest.getProfilePicture());
            user.setJoinDate(Instant.now().toString());
            user.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : "USER");

            User savedUser = userRepository.save(user);
            System.out.println("User registered successfully: " + savedUser.getEmail());

            // Generate JWT token for auto-login after registration
            final String jwt = jwtUtil.generateToken(user.getEmail());

            // Return response
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("message", "User registered successfully");
            response.put("userId", savedUser.getId());
            response.put("email", savedUser.getEmail());
            response.put("username", savedUser.getUsername());
            response.put("role", savedUser.getRole());
            response.put("displayName", savedUser.getDisplayName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                response.put("error", "Invalid authorization header");
                return ResponseEntity.ok(response);
            }

            String jwt = token.substring(7);
            String email = jwtUtil.extractUsername(jwt);

            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                response.put("error", "User not found");
                return ResponseEntity.ok(response);
            }

            // Validate token
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            boolean isValid = jwtUtil.validateToken(jwt, userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            if (isValid) {
                response.put("email", email);
                response.put("userId", user.getId());
                response.put("username", user.getUsername());
                response.put("displayName", user.getDisplayName());
                response.put("role", user.getRole());
            } else {
                response.put("error", "Token validation failed");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", "Invalid token: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid authorization header");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String jwt = token.substring(7);
            String email = jwtUtil.extractUsername(jwt);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getId());
            userInfo.put("email", user.getEmail());
            userInfo.put("username", user.getUsername());
            userInfo.put("displayName", user.getDisplayName());
            userInfo.put("role", user.getRole());
            userInfo.put("profilePicture", user.getProfilePicture());
            userInfo.put("joinDate", user.getJoinDate());

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get user info: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // DTO Classes as static inner classes
    @Data
    public static class LoginRequest {
        private String username; // Can be username or email
        private String password;
    }

    @Data
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String displayName;
        private String profilePicture;
        private String role;
    }
}