package com.spotify.backend.service;

import com.spotify.backend.model.UserLike;
import com.spotify.backend.repository.UserLikeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserLikeService {

    private final UserLikeRepository repo;

    public UserLikeService(UserLikeRepository repo) {
        this.repo = repo;
    }

    public List<UserLike> getAll() {
        return repo.findAll();
    }

    public Optional<UserLike> getById(String id) {
        return repo.findById(id);
    }

    public UserLike create(UserLike like) {
        return repo.save(like);
    }

    // 🔥 Correct update() for embedded Song object
    public UserLike update(String id, UserLike payload) {
        return repo.findById(id).map(existing -> {

            existing.setUserId(payload.getUserId());

            // Since Song is embedded, we directly replace the object
            existing.setSong(payload.getSong());

            return repo.save(existing);

        }).orElse(null);
    }

    public boolean delete(String id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}
