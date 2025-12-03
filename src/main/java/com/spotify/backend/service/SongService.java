package com.spotify.backend.service;

import com.spotify.backend.model.Album;
import com.spotify.backend.model.Artist;
import com.spotify.backend.model.Song;
import com.spotify.backend.repository.AlbumRepository;
import com.spotify.backend.repository.ArtistRepository;
import com.spotify.backend.repository.SongRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SongService {

    private final SongRepository songRepo;
    private final ArtistRepository artistRepo;
    private final AlbumRepository albumRepo;

    public SongService(SongRepository songRepo, ArtistRepository artistRepo, AlbumRepository albumRepo) {
        this.songRepo = songRepo;
        this.artistRepo = artistRepo;
        this.albumRepo = albumRepo;
    }

    private Song enrich(Song s) {
        if (s == null) return null;

        Artist artist = artistRepo.findById(s.getArtist_id()).orElse(null);
        Album album = albumRepo.findById(s.getAlbum_id()).orElse(null);

        s.setArtist(artist);
        s.setAlbum(album);

        return s;
    }

    public List<Song> getAll() {
        return songRepo.findAll()
                .stream()
                .map(this::enrich)
                .toList();
    }

    public Optional<Song> getById(String id) {
        return songRepo.findById(id)
                .map(this::enrich);
    }

    public Song create(Song s) {
        return songRepo.save(s);
    }

    public Song update(String id, Song s) {
        return songRepo.findById(id).map(existing -> {
            existing.setTitle(s.getTitle());
            existing.setArtist_id(s.getArtist_id());
            existing.setAlbum_id(s.getAlbum_id());
            existing.setGenre(s.getGenre());
            existing.setDuration(s.getDuration());
            existing.setFileUrl(s.getFileUrl());
            existing.setPlayCount(s.getPlayCount());
            return songRepo.save(existing);
        }).orElse(null);
    }

    // Add these methods to your existing SongService class

    public List<Song> getByGenre(String genre) {
        return songRepo.findByGenreIgnoreCase(genre)
                .stream()
                .map(this::enrich)
                .toList();
    }

    public List<Song> getByArtistId(String artistId) {
        return songRepo.findByArtistId(artistId)
                .stream()
                .map(this::enrich)
                .toList();
    }

    public List<Song> getByAlbumId(String albumId) {
        return songRepo.findByAlbumId(albumId)
                .stream()
                .map(this::enrich)
                .toList();
    }

    public Map<String, Object> getPaginated(int page, int size, String search) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Song> songPage;

        if (search != null && !search.isEmpty()) {
            songPage = songRepo.findByTitleContainingIgnoreCase(search, pageable);
        } else {
            songPage = songRepo.findAll(pageable);
        }

        // Enrich each song
        List<Song> enriched = songPage.getContent()
                .stream()
                .map(this::enrich)
                .collect(Collectors.toList());

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", songPage.getTotalPages());
        response.put("totalElements", songPage.getTotalElements());
        response.put("data", enriched);

        return response;
    }


    public boolean incrementPlayCount(String songId) {
        Optional<Song> optionalSong = songRepo.findById(songId);
        if (optionalSong.isPresent()) {
            Song song = optionalSong.get();
            song.setPlayCount(song.getPlayCount() + 1);
            songRepo.save(song);
            return true;
        }
        return false;
    }

    public boolean delete(String id) {
        if (!songRepo.existsById(id)) return false;
        songRepo.deleteById(id);
        return true;
    }
}
