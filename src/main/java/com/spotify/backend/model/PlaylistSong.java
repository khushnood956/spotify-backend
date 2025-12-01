package com.spotify.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "playlist_songs")
public class PlaylistSong {
    @Id
    private String id;
    private String playlistId;
    private String songId;
    private LocalDateTime addedAt;
    private Integer position; // Optional: for ordering

    public PlaylistSong() {
        this.addedAt = LocalDateTime.now();
    }

    public PlaylistSong(String playlistId, String songId) {
        this();
        this.playlistId = playlistId;
        this.songId = songId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}