package com.spotify.backend.controller;

import com.spotify.backend.model.Song;
import com.spotify.backend.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/songs")
@CrossOrigin(origins = "*")
public class SongController {

    @Autowired
    private SongService songService;  // Use Service instead of Repository

    @GetMapping
    public ResponseEntity<?> getAllSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;

        return ResponseEntity.ok(songService.getPaginated(page, size, search));
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

    // Add to SongController.java
    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamSong(@PathVariable String id) {
        try {
            // 1. Get song from database
            Optional<Song> optionalSong = songService.getById(id);
            if (!optionalSong.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Song song = optionalSong.get();

            // 2. Get file path (assuming song has 'filePath' field)
            String filePath = song.getFileUrl(); // e.g., "songs/song1.mp3"
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());

            // 3. Stream the file
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
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

    // 9️⃣ Increment play count for a song
    @PostMapping("/{id}/play")
    public ResponseEntity<?> incrementPlayCount(@PathVariable String id) {
        boolean updated = songService.incrementPlayCount(id);
        return updated
                ? ResponseEntity.ok(Map.of("success", true, "message", "Play count updated"))
                : ResponseEntity.status(404).body(Map.of("success", false, "message", "Song not found"));
    }

}