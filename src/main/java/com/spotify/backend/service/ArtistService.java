package com.spotify.backend.service;

import com.spotify.backend.model.Artist;
import com.spotify.backend.repository.ArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ArtistService {
@Autowired
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

    public Map<String, Object> getPaginated(int page, int size, String search) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Artist> artistPage;

        if (search != null && !search.isEmpty()) {
            artistPage = artistRepo.findByNameContainingIgnoreCase(search, pageable);
        } else {
            artistPage = artistRepo.findAll(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", artistPage.getTotalPages());
        response.put("totalElements", artistPage.getTotalElements());
        response.put("data", artistPage.getContent());

        return response;
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
