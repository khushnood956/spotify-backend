package com.spotify.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/songs")
    public ResponseEntity<Object> getPublicSongs() {
        return ResponseEntity.ok().body(
                Map.of("message", "Public songs endpoint - no authentication required")
        );
    }

    @GetMapping("/playlists")
    public ResponseEntity<Object> getPublicPlaylists() {
        return ResponseEntity.ok().body(
                Map.of("message", "Public playlists endpoint - no authentication required")
        );
    }

    @GetMapping("/artists")
    public ResponseEntity<Object> getPublicArtists() {
        return ResponseEntity.ok().body(
                Map.of("message", "Public artists endpoint - no authentication required")
        );
    }
}