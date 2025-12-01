package com.spotify.backend.controller;

import com.spotify.backend.model.User;
import com.spotify.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestTemplate;
import org.springframework.web.bind.annotation.RestController;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/security")
    public ResponseEntity<Map<String, Object>> checkSecurity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();

        response.put("authentication", auth != null ? auth.getName() : "NULL");
        response.put("authorities", auth != null ? auth.getAuthorities() : "NULL");
        response.put("isAuthenticated", auth != null && auth.isAuthenticated());

        System.out.println("🔍 DEBUG Security Context:");
        System.out.println("   User: " + (auth != null ? auth.getName() : "NULL"));
        System.out.println("   Authenticated: " + (auth != null && auth.isAuthenticated()));
        System.out.println("   Authorities: " + (auth != null ? auth.getAuthorities() : "NULL"));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        System.out.println("👥 Total users in DB: " + users.size());
        users.forEach(user -> {
            System.out.println("   - " + user.getEmail() + " (" + user.getUsername() + ") - Role: " + user.getRole());
        });
        return ResponseEntity.ok(users);
    }
}