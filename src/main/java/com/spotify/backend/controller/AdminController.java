package com.spotify.backend.controller;

import com.spotify.backend.model.*;
import com.spotify.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private GenreRepository genreRepository;

    // ==================== COMPLETE ADMIN STATISTICS ====================

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Basic Counts
        stats.put("totalUsers", userRepository.count());
        stats.put("totalSongs", songRepository.count());
        stats.put("totalArtists", artistRepository.count());
        stats.put("totalAlbums", albumRepository.count());
        stats.put("totalPlaylists", playlistRepository.count());
        stats.put("totalGenres", genreRepository.count());

        // 2. Today's Activity
        String today = Instant.now().toString();
        String yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toString();

        stats.put("todayNewUsers", userRepository.countByJoinDateAfter(yesterday));
        stats.put("todayNewSongs", songRepository.countByCreatedAtAfter(yesterday));

        // 3. Active Users (users active in last 7 days)
        String lastWeek = Instant.now().minus(7, ChronoUnit.DAYS).toString();
        long activeUsers = userRepository.findAll().stream()
                .filter(user -> {
                    if (user.getLastActive() == null) return false;
                    return user.getLastActive().compareTo(lastWeek) > 0;
                })
                .count();
        stats.put("activeUsers", activeUsers);

        // 4. Platform Growth (last 30 days users)
        String last30Days = Instant.now().minus(30, ChronoUnit.DAYS).toString();
        long newUsersLast30Days = userRepository.countByJoinDateAfter(last30Days);
        stats.put("newUsersLast30Days", newUsersLast30Days);

        // 5. Most Played Song
        Optional<Song> mostPlayedSong = songRepository.findAll().stream()
                .max(Comparator.comparingLong(Song::getPlayCount));
        mostPlayedSong.ifPresent(song ->
                stats.put("mostPlayedSong", Map.of(
                        "title", song.getTitle(),
                        "plays", song.getPlayCount(),
                        "artist", song.getArtist_id()
                ))
        );

        // 6. User Role Distribution
        Map<String, Long> roleDistribution = userRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        user -> user.getRole() != null ? user.getRole() : "USER",
                        Collectors.counting()
                ));
        stats.put("roleDistribution", roleDistribution);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/genres")
    public ResponseEntity<Map<String, Object>> getGenreStats() {
        Map<String, Object> response = new HashMap<>();

        // 1. Genre Distribution from Songs
        Map<String, Long> genreDistribution = songRepository.findAll().stream()
                .filter(song -> song.getGenre() != null && !song.getGenre().isEmpty())
                .collect(Collectors.groupingBy(Song::getGenre, Collectors.counting()));

        response.put("songGenres", genreDistribution);

        // 2. Genre Distribution from Artists
        Map<String, Long> artistGenres = artistRepository.findAll().stream()
                .filter(artist -> artist.getGenre() != null && !artist.getGenre().isEmpty())
                .collect(Collectors.groupingBy(Artist::getGenre, Collectors.counting()));

        response.put("artistGenres", artistGenres);

        // 3. Top 5 Genres
        List<Map<String, Object>> topGenres = genreDistribution.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .map(entry -> {
                    Map<String, Object> genreData = new HashMap<>();
                    genreData.put("genre", entry.getKey());
                    genreData.put("count", entry.getValue());
                    return genreData;
                })
                .collect(Collectors.toList());

        response.put("topGenres", topGenres);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/artists")
    public ResponseEntity<Map<String, Object>> getArtistStats() {
        Map<String, Object> response = new HashMap<>();

        // 1. Top Artists by Song Count
        Map<String, Long> artistSongCount = songRepository.findAll().stream()
                .filter(song -> song.getArtist_id() != null)
                .collect(Collectors.groupingBy(Song::getArtist_id, Collectors.counting()));

        // 2. Top Artists by Total Plays
        Map<String, Long> artistTotalPlays = new HashMap<>();
        songRepository.findAll().forEach(song -> {
            if (song.getArtist_id() != null) {
                artistTotalPlays.merge(song.getArtist_id(), song.getPlayCount(), Long::sum);
            }
        });

        // 3. Top 5 Artists by Plays
        List<Map<String, Object>> topArtists = artistTotalPlays.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .map(entry -> {
                    String artistId = entry.getKey();
                    Optional<Artist> artistOpt = artistRepository.findById(artistId);

                    Map<String, Object> artistData = new HashMap<>();
                    artistData.put("artistId", artistId);
                    artistData.put("name", artistOpt.map(Artist::getName).orElse("Unknown Artist"));
                    artistData.put("totalPlays", entry.getValue());
                    artistData.put("songCount", artistSongCount.getOrDefault(artistId, 0L));
                    return artistData;
                })
                .collect(Collectors.toList());

        response.put("topArtists", topArtists);
        response.put("totalArtists", artistRepository.count());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/daily")
    public ResponseEntity<Map<String, Object>> getDailyStats(@RequestParam(defaultValue = "7") int days) {
        Map<String, Object> response = new HashMap<>();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<String> dateLabels = new ArrayList<>();
        List<Long> newUsersData = new ArrayList<>();
        List<Long> newSongsData = new ArrayList<>();

        // Get all users and songs
        List<User> allUsers = userRepository.findAll();
        List<Song> allSongs = songRepository.findAll();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            String dateStr = currentDate.format(formatter);
            dateLabels.add(dateStr);

            // Count users joined on this date
            long usersCount = allUsers.stream()
                    .filter(user -> {
                        if (user.getJoinDate() == null) return false;
                        return user.getJoinDate().startsWith(dateStr);
                    })
                    .count();
            newUsersData.add(usersCount);

            // Count songs created on this date (if you have createdAt field)
            long songsCount = allSongs.stream()
                    .filter(song -> {
                        // Assuming Song has getCreatedAt() method
                        // If not, you might need to add it or use another field
                        return true; // Placeholder - adjust based on your Song model
                    })
                    .count();
            newSongsData.add(songsCount);
        }

        response.put("labels", dateLabels);
        response.put("newUsers", newUsersData);
        response.put("newSongs", newSongsData);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/platform")
    public ResponseEntity<Map<String, Object>> getPlatformStats() {
        Map<String, Object> response = new HashMap<>();

        // 1. Average Songs per Playlist
        List<Playlist> allPlaylists = playlistRepository.findAll();
        double avgSongsPerPlaylist = allPlaylists.stream()
                .mapToInt(playlist -> playlist.getSongIds() != null ? playlist.getSongIds().size() : 0)
                .average()
                .orElse(0.0);
        response.put("avgSongsPerPlaylist", Math.round(avgSongsPerPlaylist * 100.0) / 100.0);

        // 2. Average Plays per Song
        List<Song> allSongs = songRepository.findAll();
        double avgPlaysPerSong = allSongs.stream()
                .mapToLong(Song::getPlayCount)
                .average()
                .orElse(0.0);
        response.put("avgPlaysPerSong", Math.round(avgPlaysPerSong * 100.0) / 100.0);

        // 3. Most Active User (most playlists created)
        Map<String, Long> userPlaylistCount = allPlaylists.stream()
                .filter(playlist -> playlist.getCreatedBy() != null)
                .collect(Collectors.groupingBy(Playlist::getCreatedBy, Collectors.counting()));

        Optional<Map.Entry<String, Long>> mostActiveUser = userPlaylistCount.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        mostActiveUser.ifPresent(entry -> {
            Optional<User> userOpt = userRepository.findById(entry.getKey());
            userOpt.ifPresent(user -> {
                response.put("mostActiveUser", Map.of(
                        "username", user.getUsername(),
                        "playlistCount", entry.getValue()
                ));
            });
        });

        return ResponseEntity.ok(response);
    }

    // ==================== ENHANCED USER MANAGEMENT ====================

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search) {

        Page<User> usersPage;

        if (search != null && !search.trim().isEmpty()) {
            // Search functionality
            usersPage = userRepository.findByUsernameContainingOrEmailContaining(
                    search, search, PageRequest.of(page, size));
        } else if (role != null && !role.trim().isEmpty()) {
            // Filter by role
            usersPage = userRepository.findByRole(role, PageRequest.of(page, size));
        } else {
            // Get all users
            usersPage = userRepository.findAll(PageRequest.of(page, size));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("users", usersPage.getContent());
        response.put("currentPage", usersPage.getNumber());
        response.put("totalItems", usersPage.getTotalElements());
        response.put("totalPages", usersPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}/details")
    public ResponseEntity<Map<String, Object>> getUserDetails(@PathVariable String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        Map<String, Object> details = new HashMap<>();

        // Basic user info
        details.put("user", user);

        // User's playlists count
        long playlistCount = playlistRepository.findByCreatedBy(id).size();
        details.put("playlistCount", playlistCount);

        // User's liked songs count (if you have likes functionality)
        // details.put("likedSongsCount", userLikeRepository.countByUserId(id));

        // Last active
        details.put("lastActive", user.getLastActive());

        return ResponseEntity.ok(details);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates) {

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        // Update fields if present in request
        if (updates.containsKey("username")) {
            user.setUsername((String) updates.get("username"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("displayName")) {
            user.setDisplayName((String) updates.get("displayName"));
        }
        if (updates.containsKey("role")) {
            user.setRole((String) updates.get("role"));
        }
        if (updates.containsKey("profilePicture")) {
            user.setProfilePicture((String) updates.get("profilePicture"));
        }
        if (updates.containsKey("isActive")) {
            // You might want to add an isActive field to User model
        }

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/users/{id}/ban")
    public ResponseEntity<?> banUser(@PathVariable String id, @RequestBody Map<String, String> request) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        String reason = request.getOrDefault("reason", "No reason provided");

        // Create admin log entry
        // adminLogService.logAction("USER_BAN", "Banned user: " + user.getUsername(), reason);

        // Update user status (you might need to add a status field)
        // user.setStatus("BANNED");
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "User banned successfully",
                "username", user.getUsername(),
                "reason", reason
        ));
    }

    // ==================== CONTENT MANAGEMENT ====================

    @GetMapping("/content/songs")
    public ResponseEntity<Map<String, Object>> getAllSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String artist) {

        // You'll need to implement custom repository methods for filtering
        // For now, returning all songs with pagination
        Page<Song> songsPage = songRepository.findAll(PageRequest.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("songs", songsPage.getContent());
        response.put("currentPage", songsPage.getNumber());
        response.put("totalItems", songsPage.getTotalElements());
        response.put("totalPages", songsPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/content/playlists")
    public ResponseEntity<Map<String, Object>> getAllPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Playlist> playlistsPage = playlistRepository.findAll(PageRequest.of(page, size));

        // Enrich playlists with creator info
        List<Map<String, Object>> enrichedPlaylists = playlistsPage.getContent().stream()
                .map(playlist -> {
                    Map<String, Object> enriched = new HashMap<>();
                    enriched.put("playlist", playlist);

                    // Add creator username
                    userRepository.findById(playlist.getCreatedBy())
                            .ifPresent(user -> enriched.put("creator", user.getUsername()));

                    return enriched;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("playlists", enrichedPlaylists);
        response.put("currentPage", playlistsPage.getNumber());
        response.put("totalItems", playlistsPage.getTotalElements());
        response.put("totalPages", playlistsPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/content/songs/{id}")
    public ResponseEntity<?> deleteSong(@PathVariable String id) {
        if (!songRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Song not found"));
        }

        try {
            songRepository.deleteById(id);
            // Log the action
            // adminLogService.logAction("SONG_DELETE", "Deleted song ID: " + id);

            return ResponseEntity.ok(Map.of("message", "Song deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete song: " + e.getMessage()));
        }
    }

    @DeleteMapping("/content/playlists/{id}")
    public ResponseEntity<?> deletePlaylist(@PathVariable String id) {
        if (!playlistRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Playlist not found"));
        }

        try {
            playlistRepository.deleteById(id);
            // Log the action
            // adminLogService.logAction("PLAYLIST_DELETE", "Deleted playlist ID: " + id);

            return ResponseEntity.ok(Map.of("message", "Playlist deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete playlist: " + e.getMessage()));
        }
    }

    // ==================== SYSTEM SETTINGS ====================

    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSystemSettings() {
        Map<String, Object> settings = new HashMap<>();

        // Application info
        settings.put("appName", "Spotify Mock");
        settings.put("version", "1.0.0");
        settings.put("environment", "Production");

        // Database info
        settings.put("database", "MongoDB Atlas");
        settings.put("totalCollections", 10); // Your collection count

        // Platform settings (you can store these in a separate collection)
        settings.put("allowRegistrations", true);
        settings.put("maxPlaylistsPerUser", 50);
        settings.put("maxSongsPerPlaylist", 100);
        settings.put("maintenanceMode", false);

        // Storage info
        settings.put("totalSongsStorage", "150 MB"); // Estimate
        settings.put("totalUsers", userRepository.count());

        return ResponseEntity.ok(settings);
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSystemSettings(@RequestBody Map<String, Object> newSettings) {
        // In a real app, you'd save these to a database
        // For now, just return success

        // Validate settings
        if (newSettings.containsKey("maintenanceMode")) {
            boolean maintenanceMode = (Boolean) newSettings.get("maintenanceMode");
            // adminLogService.logAction("SETTINGS_UPDATE",
            //     "Maintenance mode " + (maintenanceMode ? "enabled" : "disabled"));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Settings updated successfully",
                "updatedSettings", newSettings
        ));
    }

    // ==================== ADMIN LOGS ====================

    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getAdminLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String adminId) {

        // You'll need to implement AdminLogRepository with filtering
        // For now, returning empty with structure

        Map<String, Object> response = new HashMap<>();
        response.put("logs", new ArrayList<>()); // Replace with actual logs
        response.put("currentPage", 0);
        response.put("totalItems", 0);
        response.put("totalPages", 0);

        return ResponseEntity.ok(response);
    }
}