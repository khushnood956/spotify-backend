package com.spotify.backend.controller;

import com.spotify.backend.model.User;
import com.spotify.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    @Autowired
    private UserRepository userRepository;

    // Get current user profile
    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update current user profile
    @PutMapping("/profile")
    public ResponseEntity<User> updateUserProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody User updatedUser) {

        String email = userDetails.getUsername();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setDisplayName(updatedUser.getDisplayName());
            user.setProfilePicture(updatedUser.getProfilePicture());
            // Don't update email, username, or role from here

            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        }

        return ResponseEntity.notFound().build();
    }

    // Get user playlists, likes, history etc.
    @GetMapping("/playlists")
    public ResponseEntity<Object> getUserPlaylists(@AuthenticationPrincipal UserDetails userDetails) {
        // You'll implement this based on your playlist logic
        String email = userDetails.getUsername();
        return ResponseEntity.ok().body(
                java.util.Map.of(
                        "message", "User playlists endpoint",
                        "user", email
                )
        );
    }
}