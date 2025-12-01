package com.spotify.backend.service;

import com.spotify.backend.model.*;
import com.spotify.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepo;
    private final SongRepository songRepo;
    private final ArtistRepository artistRepo;
    private final AlbumRepository albumRepo;

    // REMOVED: PlaylistSongRepository - not needed for MongoDB

    /**
     * Enrich playlist with full song details
     */
    public Playlist enrichPlaylist(Playlist playlist) {
        if (playlist == null) return null;

        System.out.println("🎵 Enriching playlist: " + playlist.getName() + " ID: " + playlist.getId());
        System.out.println("📋 Current song IDs in playlist: " + playlist.getSongIds());

        List<Song> enrichedSongs = new ArrayList<>();

        for (String songId : playlist.getSongIds()) {
            Optional<Song> songOpt = songRepo.findById(songId);
            if (songOpt.isPresent()) {
                Song song = songOpt.get();
                System.out.println("✅ Found song: " + song.getTitle() + " for playlist");

                // Enrich song with artist/album
                if (song.getArtist_id() != null) {
                    Optional<Artist> artistOpt = artistRepo.findById(song.getArtist_id());
                    artistOpt.ifPresent(song::setArtist);
                }
                if (song.getAlbum_id() != null) {
                    Optional<Album> albumOpt = albumRepo.findById(song.getAlbum_id());
                    albumOpt.ifPresent(song::setAlbum);
                }

                enrichedSongs.add(song);
            } else {
                System.out.println("❌ Song not found with ID: " + songId);
            }
        }

        playlist.setSongs(enrichedSongs);
        System.out.println("🎉 Final enriched playlist - Songs: " + enrichedSongs.size());

        return playlist;
    }

    // Get all playlists for user
    public List<Playlist> getUserPlaylists(String userId) {
        List<Playlist> playlists = playlistRepo.findByCreatedBy(userId);
        System.out.println("🔍 Found " + playlists.size() + " playlists for user: " + userId);
        return playlists.stream()
                .map(this::enrichPlaylist)
                .toList();
    }

    // Get playlist by ID
    public Playlist getPlaylistById(String id) {
        return playlistRepo.findById(id)
                .map(this::enrichPlaylist)
                .orElse(null);
    }

    // Create playlist
    public Playlist createPlaylist(Playlist playlist) {
        playlist.prePersist(); // Initialize timestamps and lists
        return playlistRepo.save(playlist);
    }

    // Update playlist basic info
    public Playlist updatePlaylist(String id, Playlist playlist) {
        return playlistRepo.findById(id).map(existing -> {
            existing.setName(playlist.getName());
            existing.setDescription(playlist.getDescription());
            existing.setCoverImage(playlist.getCoverImage());
            existing.setIsPublic(playlist.getIsPublic());
            existing.setUpdatedAt(java.time.LocalDateTime.now());
            return playlistRepo.save(existing);
        }).orElse(null);
    }

    // Delete playlist
    public boolean deletePlaylist(String id) {
        if (!playlistRepo.existsById(id)) return false;
        playlistRepo.deleteById(id);
        return true;
    }

    // Add song to playlist - SIMPLIFIED for MongoDB
    public Playlist addSongToPlaylist(String playlistId, String songId) {
        return playlistRepo.findById(playlistId).map(playlist -> {
            // Check if song exists
            if (!songRepo.existsById(songId)) {
                throw new RuntimeException("Song not found: " + songId);
            }

            // Add to songIds list if not already present
            if (!playlist.getSongIds().contains(songId)) {
                playlist.getSongIds().add(songId);
                playlist.setUpdatedAt(java.time.LocalDateTime.now());
                return playlistRepo.save(playlist);
            }
            return playlist; // Already in playlist
        }).orElseThrow(() -> new RuntimeException("Playlist not found: " + playlistId));
    }

    // Remove song from playlist - SIMPLIFIED for MongoDB
    public Playlist removeSongFromPlaylist(String playlistId, String songId) {
        return playlistRepo.findById(playlistId).map(playlist -> {
            playlist.getSongIds().remove(songId);
            playlist.setUpdatedAt(java.time.LocalDateTime.now());
            return playlistRepo.save(playlist);
        }).orElseThrow(() -> new RuntimeException("Playlist not found: " + playlistId));
    }

    // Add multiple songs to playlist - SIMPLIFIED for MongoDB
    public Playlist addSongsToPlaylist(String playlistId, List<String> songIds) {
        return playlistRepo.findById(playlistId).map(playlist -> {
            int addedCount = 0;
            for (String songId : songIds) {
                // Check if song exists and not already in playlist
                if (songRepo.existsById(songId) && !playlist.getSongIds().contains(songId)) {
                    playlist.getSongIds().add(songId);
                    addedCount++;
                }
            }
            if (addedCount > 0) {
                playlist.setUpdatedAt(java.time.LocalDateTime.now());
                playlistRepo.save(playlist);
                System.out.println("✅ Added " + addedCount + " songs to playlist " + playlistId);
            }
            return playlist;
        }).orElseThrow(() -> new RuntimeException("Playlist not found: " + playlistId));
    }
}