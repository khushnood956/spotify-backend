package com.spotify.backend.controller;

import com.spotify.backend.model.Artist;
import com.spotify.backend.repository.ArtistRepository;

import com.spotify.backend.service.ArtistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/artists")
@CrossOrigin(origins = "*")
public class ArtistController {

    @Autowired
    private ArtistRepository artistRepository;
    @Autowired
    private ArtistService artistService;

    // ▶ Get all artists
    @GetMapping
    public ResponseEntity<?> getAllArtists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;

        return ResponseEntity.ok(artistService.getPaginated(page, size, search));
    }


    // ▶ Get artist by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getArtistById(@PathVariable String id) {
        Optional<Artist> artist = artistRepository.findById(id);
        return artist.isPresent()
                ? ResponseEntity.ok(artist.get())
                : ResponseEntity.status(404).body("Artist not found");
    }

    // ▶ Add new artist
    @PostMapping
    public Artist createArtist(@RequestBody Artist artist) {
        return artistRepository.save(artist);
    }

    // ▶ Update artist
    @PutMapping("/{id}")
    public ResponseEntity<?> updateArtist(
            @PathVariable String id,
            @RequestBody Artist payload) {

        Optional<Artist> existingOpt = artistRepository.findById(id);

        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Artist not found");
        }

        Artist existing = existingOpt.get();

        existing.setName(payload.getName());
        existing.setBio(payload.getBio());
        existing.setGenre(payload.getGenre());
        existing.setPicture(payload.getPicture());

        artistRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    // ▶ Delete artist
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArtist(@PathVariable String id) {
        if (!artistRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Artist not found");
        }
        artistRepository.deleteById(id);
        return ResponseEntity.ok("Artist deleted");
    }
}
