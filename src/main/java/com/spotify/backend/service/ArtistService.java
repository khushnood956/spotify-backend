package com.spotify.backend.service;

import com.spotify.backend.model.Artist;
import com.spotify.backend.repository.ArtistRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArtistService {

    private final ArtistRepository artistRepo;

    public ArtistService(ArtistRepository artistRepo) {
        this.artistRepo = artistRepo;
    }

    public List<Artist> getAll() {
        return artistRepo.findAll();
    }

    public Optional<Artist> getById(String id) {
        return artistRepo.findById(id);
    }

    public Artist create(Artist a) {
        return artistRepo.save(a);
    }

    public Artist update(String id, Artist a) {
        return artistRepo.findById(id).map(existing -> {
            existing.setName(a.getName());
            existing.setBio(a.getBio());
            existing.setGenre(a.getGenre());
            existing.setPicture(a.getPicture());
            return artistRepo.save(existing);
        }).orElse(null);
    }

    public boolean delete(String id) {
        if (!artistRepo.existsById(id)) return false;
        artistRepo.deleteById(id);
        return true;
    }
}
