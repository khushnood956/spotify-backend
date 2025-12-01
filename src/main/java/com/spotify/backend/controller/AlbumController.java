package com.spotify.backend.controller;

import com.spotify.backend.model.Album;
import com.spotify.backend.repository.AlbumRepository;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/albums")
@CrossOrigin(origins = "*")
public class AlbumController {

    @Autowired
    private AlbumRepository albumRepository;

    // ▶ Get all albums
    @GetMapping
    public List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }

    // ▶ Get album by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getAlbumById(@PathVariable String id) {
        Optional<Album> album = albumRepository.findById(id);
        return album.isPresent()
                ? ResponseEntity.ok(album.get())
                : ResponseEntity.status(404).body("Album not found");
    }

    // ▶ Get albums by artist
    @GetMapping("/artist/{artist_id}")
    public List<Album> getAlbumsByArtist(@PathVariable String artist_id) {
        return albumRepository.findByArtist_id(artist_id);
    }

    // ▶ Create new album
    @PostMapping
    public Album createAlbum(@RequestBody Album album) {
        // Generate consistent String ID
        if (album.getId() == null) {
            album.setId(new ObjectId().toHexString());
        }
        return albumRepository.save(album);
    }

    // ▶ Update album
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAlbum(
            @PathVariable String id,
            @RequestBody Album payload) {

        Optional<Album> existingOpt = albumRepository.findById(id);

        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Album not found");
        }

        Album existing = existingOpt.get();

        existing.setTitle(payload.getTitle());
        existing.setArtist_id(payload.getArtist_id());
        existing.setReleaseDate(payload.getReleaseDate());
        existing.setCoverArt(payload.getCoverArt());
        existing.setGenre(payload.getGenre());

        albumRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    // ▶ Delete album
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAlbum(@PathVariable String id) {
        if (!albumRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Album not found");
        }
        albumRepository.deleteById(id);
        return ResponseEntity.ok("Album deleted");
    }
    // Add this method to debug
    @GetMapping("/debug/count")
    public String debugCount() {
        long count = albumRepository.count();
        return "Total albums in database: " + count;
    }

    @GetMapping("/debug/all")
    public List<Album> debugAll() {
        return albumRepository.findAll();
    }
}
