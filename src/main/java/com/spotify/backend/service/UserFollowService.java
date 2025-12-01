package com.spotify.backend.service;

import com.spotify.backend.model.UserFollow;
import com.spotify.backend.repository.UserFollowRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserFollowService {

    private final UserFollowRepository repo;

    public UserFollowService(UserFollowRepository repo) {
        this.repo = repo;
    }

    public List<UserFollow> getAll() {
        return repo.findAll();
    }

    public Optional<UserFollow> getById(String id) {
        return repo.findById(id);
    }

    public UserFollow create(UserFollow follow) {
        return repo.save(follow);
    }

    // 🔥 Add THIS → Fixes your controller error
    public UserFollow update(String id, UserFollow payload) {
        return repo.findById(id).map(existing -> {
            existing.setFollowerId(payload.getFollowerId());
            existing.setFollowingId(payload.getFollowingId());
            return repo.save(existing);
        }).orElse(null);
    }

    public boolean delete(String id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}
