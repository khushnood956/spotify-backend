package com.spotify.backend.controller;

import com.spotify.backend.model.Playlist;
import com.spotify.backend.repository.PlaylistRepository;
import com.spotify.backend.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final PlaylistRepository playlistRepository;

//    // Helper to extract user ID from token (implement your logic)
//    private String extractUserIdFromToken(String authHeader) {
//        // Your JWT token extraction logic here
//        return "user-id-from-token"; // Replace with actual implementation
//    }

    // Replace this dummy method with actual JWT extraction
    private String extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid Authorization header");
        }

        String token = authHeader.substring(7);

        // If you have a JWT service, use it:
        // return jwtUtil.extractUsername(token);

        // TEMPORARY FIX: For testing, return the token itself
        // This assumes your token format includes user ID
        // Remove this when you implement proper JWT parsing

        System.out.println("📝 Token received: " + token);

        // If you're storing user ID in token, parse it
        // For example, if token is "user123|timestamp"
        if (token.contains("|")) {
            return token.split("\\|")[0];
        }

        return token; // Fallback: use token as user ID
    }

    private String getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        // Option 1: If principal is the user ID string directly
        if (authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }

        // Option 2: If principal is a UserDetails object
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal()).getUsername();
        }

        // Option 3: If you have a custom User object
        // return ((YourCustomUserClass) authentication.getPrincipal()).getId();

        throw new RuntimeException("Unable to extract user ID from authentication");
    }

    // Create playlist
    @PostMapping
    public ResponseEntity<?> createPlaylist(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Playlist playlist) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            playlist.setCreatedBy(userId);

            // Ensure lists are initialized
            if (playlist.getSongIds() == null) playlist.setSongIds(new ArrayList<>());
            if (playlist.getSongs() == null) playlist.setSongs(new ArrayList<>());

            Playlist created = playlistService.createPlaylist(playlist);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return errorResponse("Failed to create playlist: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Get user's playlists
    @GetMapping("/all")
    public ResponseEntity<?> getUserPlaylists(@RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            List<Playlist> playlists = playlistService.getUserPlaylists(userId);
            return ResponseEntity.ok(playlists);
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;

        return ResponseEntity.ok(playlistService.getPaginated(page, size, search));
    }


    // Get playlist by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlaylistById(@PathVariable String id) {
        try {
            Playlist playlist = playlistService.getPlaylistById(id);
            if (playlist == null) {
                return errorResponse("Playlist not found", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(playlist);
        } catch (Exception e) {
            return errorResponse("Playlist not found", HttpStatus.NOT_FOUND);
        }
    }

    // Update playlist
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlaylist(
            @PathVariable String id,
            @RequestBody Playlist playlist) {
        try {
            Playlist updated = playlistService.updatePlaylist(id, playlist);
            if (updated == null) {
                return errorResponse("Playlist not found", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return errorResponse("Failed to update playlist: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Delete playlist
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlaylist(@PathVariable String id) {
        try {
            boolean deleted = playlistService.deletePlaylist(id);
            if (!deleted) {
                return errorResponse("Playlist not found", HttpStatus.NOT_FOUND);
            }
            return successResponse("Playlist deleted successfully");
        } catch (Exception e) {
            return errorResponse("Failed to delete playlist: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Add song to playlist
    @PostMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<?> addSongToPlaylist(
            @PathVariable String playlistId,
            @PathVariable String songId) {
        try {
            Playlist updated = playlistService.addSongToPlaylist(playlistId, songId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return errorResponse("Failed to add song: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Remove song from playlist
    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<?> removeSongFromPlaylist(
            @PathVariable String playlistId,
            @PathVariable String songId) {
        try {
            Playlist updated = playlistService.removeSongFromPlaylist(playlistId, songId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return errorResponse("Failed to remove song: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Add multiple songs
    @PostMapping("/{playlistId}/songs/batch")
    public ResponseEntity<?> addSongsToPlaylist(
            @PathVariable String playlistId,
            @RequestBody Map<String, List<String>> request) {
        try {
            List<String> songIds = request.get("songIds");
            if (songIds == null || songIds.isEmpty()) {
                return errorResponse("No songs provided", HttpStatus.BAD_REQUEST);
            }

            Playlist updated = playlistService.addSongsToPlaylist(playlistId, songIds);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return errorResponse("Failed to add songs: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Helper methods for responses
    private ResponseEntity<Map<String, String>> errorResponse(String message, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<Map<String, String>> successResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debug-db")
    public ResponseEntity<Map<String, Object>> debugDatabase(Authentication authentication) {
        try {
            String userId = getUserIdFromAuthentication(authentication);

            // Get ALL playlists from database (no user filter)
            List<Playlist> allPlaylists = playlistRepository.findAll();

            // Get playlists for current user
            List<Playlist> userPlaylists = playlistRepository.findByCreatedBy(userId);

            // Debug: Check what's in the database
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("currentUserId", userId);
            debugInfo.put("totalPlaylistsInDB", allPlaylists.size());
            debugInfo.put("userPlaylistsCount", userPlaylists.size());
            debugInfo.put("allPlaylists", allPlaylists.stream()
                    .map(p -> Map.of(
                            "id", p.getId(),
                            "name", p.getName(),
                            "createdBy", p.getCreatedBy(),
                            "songCount", p.getSongIds() != null ? p.getSongIds().size() : 0
                    ))
                    .collect(Collectors.toList()));
            debugInfo.put("userPlaylists", userPlaylists);

            System.out.println("🐛 DEBUG DATABASE:");
            System.out.println("🐛 Current User ID: " + userId);
            System.out.println("🐛 Total playlists in DB: " + allPlaylists.size());
            System.out.println("🐛 Playlists for current user: " + userPlaylists.size());

            // Log all playlists with their createdBy fields
            allPlaylists.forEach(playlist -> {
                System.out.println("🐛 Playlist: " + playlist.getName() +
                        " | CreatedBy: " + playlist.getCreatedBy() +
                        " | Matches current user: " + playlist.getCreatedBy().equals(userId));
            });

            return ResponseEntity.ok(debugInfo);

        } catch (Exception e) {
            System.out.println("❌ Debug database error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }


}