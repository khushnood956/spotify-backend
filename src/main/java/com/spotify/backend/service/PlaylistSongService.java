package com.spotify.backend.service;

import com.spotify.backend.model.PlaylistSong;
import com.spotify.backend.repository.PlaylistSongRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlaylistSongService {

    private final PlaylistSongRepository playlistSongRepository;

    public PlaylistSongService(PlaylistSongRepository playlistSongRepository) {
        this.playlistSongRepository = playlistSongRepository;
    }

    // Create
    public PlaylistSong create(PlaylistSong playlistSong) {
        return playlistSongRepository.save(playlistSong);
    }

    // Read
    public List<PlaylistSong> getAll() {
        return playlistSongRepository.findAll();
    }

    public Optional<PlaylistSong> getById(String id) {
        return playlistSongRepository.findById(id);
    }

    public List<PlaylistSong> getByPlaylistId(String playlistId) {
        return playlistSongRepository.findByPlaylistId(playlistId);
    }

    public List<PlaylistSong> getBySongId(String songId) {
        return playlistSongRepository.findBySongId(songId);
    }

    public long getCountByPlaylistId(String playlistId) {
        return playlistSongRepository.countByPlaylistId(playlistId);
    }

    public List<PlaylistSong> getByPlaylistIds(List<String> playlistIds) {
        return playlistSongRepository.findByPlaylistIdIn(playlistIds);
    }

    // Update
    public PlaylistSong update(String id, PlaylistSong playlistSong) {
        if (playlistSongRepository.existsById(id)) {
            playlistSong.setId(id);
            return playlistSongRepository.save(playlistSong);
        }
        return null;
    }

    // Delete
    public boolean delete(String id) {
        if (playlistSongRepository.existsById(id)) {
            playlistSongRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean deleteByPlaylistIdAndSongId(String playlistId, String songId) {
        Optional<PlaylistSong> relation = playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId);
        if (relation.isPresent()) {
            playlistSongRepository.delete(relation.get());
            return true;
        }
        return false;
    }

    public boolean deleteAllByPlaylistId(String playlistId) {
        List<PlaylistSong> relations = playlistSongRepository.findByPlaylistId(playlistId);
        if (!relations.isEmpty()) {
            playlistSongRepository.deleteAll(relations);
            return true;
        }
        return false;
    }
}