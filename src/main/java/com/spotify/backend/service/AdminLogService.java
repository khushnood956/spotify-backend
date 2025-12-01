package com.spotify.backend.service;

import com.spotify.backend.model.AdminLog;
import com.spotify.backend.repository.AdminLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminLogService {

    private final AdminLogRepository repo;

    public AdminLogService(AdminLogRepository repo) {
        this.repo = repo;
    }

    public List<AdminLog> getAll() { return repo.findAll(); }

    public Optional<AdminLog> getById(String id) { return repo.findById(id); }

    public List<AdminLog> getByUser(String userId) {
        return repo.findByUserId(userId);
    }

    public AdminLog create(AdminLog log) {
        return repo.save(log);
    }

    public AdminLog update(String id, AdminLog updated) {
        return repo.findById(id).map(log -> {
            log.setUserId(updated.getUserId());
            log.setAction(updated.getAction());
            log.setDetails(updated.getDetails());
            log.setTimestamp(updated.getTimestamp());
            return repo.save(log);
        }).orElse(null);
    }

    public boolean delete(String id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}
