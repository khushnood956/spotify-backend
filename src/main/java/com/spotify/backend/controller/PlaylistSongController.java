package com.spotify.backend.controller;

import com.spotify.backend.model.PlaylistSong;
import com.spotify.backend.service.PlaylistSongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/playlistsSong")
@CrossOrigin(origins = "*")
public class PlaylistSongController {

    @Autowired
    private PlaylistSongService playlistSongService;

    // ========== CREATE ==========
    @PostMapping("/{playlistId}/songs/add")
    public ResponseEntity<PlaylistSong> addSongToPlaylist(
            @PathVariable String playlistId,
            @RequestBody AddSongRequest request) {

        PlaylistSong playlistSong = new PlaylistSong();
        playlistSong.setPlaylistId(playlistId);
        playlistSong.setSongId(request.getSongId());
//        playlistSong..setAddedAt(Instant.now());
//        playlistSong.setId();setAddedBy(request.getAddedBy());
//        playlistSong.setPosition(request.getPosition());

        PlaylistSong created = playlistSongService.create(playlistSong);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ========== READ ==========
    @GetMapping("/{playlistId}/songs")
    public ResponseEntity<List<PlaylistSong>> getSongsInPlaylist(@PathVariable String playlistId) {
        List<PlaylistSong> songs = playlistSongService.getByPlaylistId(playlistId);
        return ResponseEntity.ok(songs);
    }

    @GetMapping("/songs/all")
    public ResponseEntity<List<PlaylistSong>> getAllPlaylistSongs() {
        List<PlaylistSong> allRelations = playlistSongService.getAll();
        return ResponseEntity.ok(allRelations);
    }

    @GetMapping("/songs/{id}")
    public ResponseEntity<PlaylistSong> getPlaylistSongById(@PathVariable String id) {
        Optional<PlaylistSong> playlistSong = playlistSongService.getById(id);
        return playlistSong.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/songs/song/{songId}")
    public ResponseEntity<List<PlaylistSong>> getPlaylistsBySongId(@PathVariable String songId) {
        List<PlaylistSong> playlists = playlistSongService.getBySongId(songId);
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/{playlistId}/songs/count")
    public ResponseEntity<Long> getSongCount(@PathVariable String playlistId) {
        long count = playlistSongService.getCountByPlaylistId(playlistId);
        return ResponseEntity.ok(count);
    }

    // ========== UPDATE ==========
    @PutMapping("/songs/{id}")
    public ResponseEntity<PlaylistSong> updatePlaylistSong(
            @PathVariable String id,
            @RequestBody PlaylistSong playlistSong) {
        PlaylistSong updated = playlistSongService.update(id, playlistSong);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    // ========== DELETE ==========
    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Void> removeSongFromPlaylist(
            @PathVariable String playlistId,
            @PathVariable String songId) {
        boolean deleted = playlistSongService.deleteByPlaylistIdAndSongId(playlistId, songId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{playlistId}/songs")
    public ResponseEntity<Void> removeAllSongsFromPlaylist(@PathVariable String playlistId) {
        boolean deleted = playlistSongService.deleteAllByPlaylistId(playlistId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/songs/{id}")
    public ResponseEntity<Void> deletePlaylistSong(@PathVariable String id) {
        boolean deleted = playlistSongService.delete(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ========== DTO ==========
    public static class AddSongRequest {
        private String songId;
        private String addedBy;
        private int position;

        public AddSongRequest() {}

        public AddSongRequest(String songId, String addedBy, int position) {
            this.songId = songId;
            this.addedBy = addedBy;
            this.position = position;
        }

        public String getSongId() { return songId; }
        public void setSongId(String songId) { this.songId = songId; }

        public String getAddedBy() { return addedBy; }
        public void setAddedBy(String addedBy) { this.addedBy = addedBy; }

        public int getPosition() { return position; }
        public void setPosition(int position) { this.position = position; }
    }
}