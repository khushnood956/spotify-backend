package com.spotify.backend.service;

import com.spotify.backend.model.Album;
import com.spotify.backend.repository.AlbumRepository;
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
public class AlbumService {

    @Autowired
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

    public Map<String, Object> getPaginated(int page, int size, String search) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Album> albumPage;

        if (search != null && !search.isEmpty()) {
            albumPage = albumRepo.findByTitleContainingIgnoreCase(search, pageable);
        } else {
            albumPage = albumRepo.findAll(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", albumPage.getTotalPages());
        response.put("totalElements", albumPage.getTotalElements());
        response.put("data", albumPage.getContent());

        return response;
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
