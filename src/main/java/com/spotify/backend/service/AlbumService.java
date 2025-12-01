package com.spotify.backend.service;

import com.spotify.backend.model.Album;
import com.spotify.backend.repository.AlbumRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlbumService {

    private final AlbumRepository albumRepo;

    public AlbumService(AlbumRepository albumRepo) {
        this.albumRepo = albumRepo;
    }

    public List<Album> getAll() {
        return albumRepo.findAll();
    }

    public Optional<Album> getById(String id) {
        return albumRepo.findById(id);
    }

    public Album create(Album a) {
        return albumRepo.save(a);
    }

    public Album update(String id, Album a) {
        return albumRepo.findById(id).map(existing -> {
            existing.setTitle(a.getTitle());
            existing.setArtist_id(a.getArtist_id());
            existing.setGenre(a.getGenre());
            existing.setCoverArt(a.getCoverArt());
            existing.setReleaseDate(a.getReleaseDate());
            return albumRepo.save(existing);
        }).orElse(null);
    }

    public boolean delete(String id) {
        if (!albumRepo.existsById(id)) return false;
        albumRepo.deleteById(id);
        return true;
    }
}
