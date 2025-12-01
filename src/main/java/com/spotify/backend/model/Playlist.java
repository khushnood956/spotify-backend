package com.spotify.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "playlists")
public class Playlist {
    @Id
    private String id;

    private String name;
    private String description;
    private String createdBy;  // User ID who created the playlist
    private Boolean isPublic = true;
    private String coverImage;

    // List of song IDs (for quick access)
    private List<String> songIds = new ArrayList<>();

    // List of full song objects (for enriched responses)
    private List<Song> songs = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor without id (for new playlists)
    public Playlist(String name, String description, String createdBy, Boolean isPublic, String coverImage) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.isPublic = isPublic;
        this.coverImage = coverImage;
        this.songIds = new ArrayList<>();
        this.songs = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Pre-persist method to set timestamps
    @org.springframework.data.annotation.Transient
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();

        // Ensure lists are initialized
        if (this.songIds == null) {
            this.songIds = new ArrayList<>();
        }
        if (this.songs == null) {
            this.songs = new ArrayList<>();
        }
        if (this.isPublic == null) {
            this.isPublic = true;
        }
    }
}