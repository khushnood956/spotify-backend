package com.spotify.backend.controller;

import com.spotify.backend.model.Song;
import com.spotify.backend.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/songs")
@CrossOrigin(origins = "*")
public class SongController {

    @Autowired
    private SongService songService;  // Use Service instead of Repository

    // 1️⃣ Get all songs (with enriched artist/album data)
    @GetMapping
    public List<Song> getAllSongs() {
        return songService.getAll();  // This will call the enrich method
    }

    // 2️⃣ Get song by ID (with enriched data)
    @GetMapping("/{id}")
    public ResponseEntity<?> getSongById(@PathVariable String id) {
        Optional<Song> song = songService.getById(id);
        return song.isPresent()
                ? ResponseEntity.ok(song.get())
                : ResponseEntity.status(404).body("Song not found");
    }

    // 3️⃣ Get songs by genre
    @GetMapping("/genre/{genre}")
    public List<Song> getSongsByGenre(@PathVariable String genre) {
        // You'll need to add this method to SongService
        return songService.getByGenre(genre);
    }

    // 4️⃣ Get songs by artist ID
    @GetMapping("/artist/{artistId}")
    public List<Song> getSongsByArtist(@PathVariable String artistId) {
        // You'll need to add this method to SongService
        return songService.getByArtistId(artistId);
    }

    // 5️⃣ Get songs by album ID
    @GetMapping("/album/{albumId}")
    public List<Song> getSongsByAlbum(@PathVariable String albumId) {
        // You'll need to add this method to SongService
        return songService.getByAlbumId(albumId);
    }

    // 6️⃣ Create new song
    @PostMapping
    public Song createSong(@RequestBody Song song) {
        return songService.create(song);
    }

    // 7️⃣ Update song
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSong(@PathVariable String id, @RequestBody Song song) {
        Song updated = songService.update(id, song);
        return updated != null
                ? ResponseEntity.ok(updated)
                : ResponseEntity.status(404).body("Song not found");
    }

    // 8️⃣ Delete song
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSong(@PathVariable String id) {
        boolean deleted = songService.delete(id);
        return deleted
                ? ResponseEntity.ok("Song deleted")
                : ResponseEntity.status(404).body("Song not found");
    }
}