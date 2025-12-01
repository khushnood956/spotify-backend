package com.spotify.backend.service;

import com.spotify.backend.model.Genre;
import com.spotify.backend.repository.GenreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GenreService {

    private final GenreRepository genreRepo;

    public GenreService(GenreRepository genreRepo) {
        this.genreRepo = genreRepo;
    }

    public List<Genre> getAll() {
        return genreRepo.findAll();
    }

    public Optional<Genre> getById(String id) {
        return genreRepo.findById(id);
    }

    public Genre create(Genre g) {
        return genreRepo.save(g);
    }

    public Genre update(String id, Genre g) {
        return genreRepo.findById(id).map(existing -> {
            existing.setName(g.getName());
            existing.setDescription(g.getDescription());
            return genreRepo.save(existing);
        }).orElse(null);
    }

    public boolean delete(String id) {
        if (!genreRepo.existsById(id)) return false;
        genreRepo.deleteById(id);
        return true;
    }
}
