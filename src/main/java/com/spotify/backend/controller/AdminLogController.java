package com.spotify.backend.controller;

import com.spotify.backend.model.AdminLog;
import com.spotify.backend.service.AdminLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adminlogs")
public class AdminLogController {

    private final AdminLogService service;

    public AdminLogController(AdminLogService service) {
        this.service = service;
    }

    @GetMapping
    public List<AdminLog> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public AdminLog getById(@PathVariable String id) {
        return service.getById(id).orElse(null);
    }

    @GetMapping("/user/{userId}")
    public List<AdminLog> getByUser(@PathVariable String userId) {
        return service.getByUser(userId);
    }

    @PostMapping
    public AdminLog create(@RequestBody AdminLog log) {
        return service.create(log);
    }

    @PutMapping("/{id}")
    public AdminLog update(@PathVariable String id, @RequestBody AdminLog log) {
        return service.update(id, log);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable String id) {
        return service.delete(id);
    }
}
