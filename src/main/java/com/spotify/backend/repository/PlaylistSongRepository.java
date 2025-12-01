package com.spotify.backend.repository;

import com.spotify.backend.model.PlaylistSong;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistSongRepository extends MongoRepository<PlaylistSong, String> {

    // Find specific song in playlist
    Optional<PlaylistSong> findByPlaylistIdAndSongId(String playlistId, String songId);

    // Find all songs in a playlist
    List<PlaylistSong> findByPlaylistId(String playlistId);

    // Find all playlists containing a song
    List<PlaylistSong> findBySongId(String songId);

    // Count songs in playlist
    long countByPlaylistId(String playlistId);

    // Delete specific song from playlist
    void deleteByPlaylistIdAndSongId(String playlistId, String songId);

    // Delete all songs from playlist
    void deleteByPlaylistId(String playlistId);

    // ADD THIS METHOD - Find by multiple playlist IDs
    List<PlaylistSong> findByPlaylistIdIn(List<String> playlistIds);
}