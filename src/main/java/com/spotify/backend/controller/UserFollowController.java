package com.spotify.backend.controller;

import com.spotify.backend.model.UserFollow;
import com.spotify.backend.service.UserFollowService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-follows")
public class UserFollowController {

    private final UserFollowService service;

    public UserFollowController(UserFollowService service) {
        this.service = service;
    }

    @GetMapping
    public Object getAll() {
        return new Response(true, service.getAll(), null);
    }

    @GetMapping("/{id}")
    public Object getById(@PathVariable String id) {
        return service.getById(id)
                .map(obj -> new Response(true, obj, null))
                .orElse(new Response(false, null, "Not found"));
    }

    @PostMapping
    public Object create(@RequestBody UserFollow payload) {
        return new Response(true, service.create(payload), "Created");
    }

    @PutMapping("/{id}")
    public Object update(@PathVariable String id, @RequestBody UserFollow payload) {
        var updated = service.update(id, payload);
        return updated != null
                ? new Response(true, updated, "Updated")
                : new Response(false, null, "Not found");
    }

    @DeleteMapping("/{id}")
    public Object delete(@PathVariable String id) {
        return service.delete(id)
                ? new Response(true, null, "Deleted")
                : new Response(false, null, "Not found");
    }

    record Response(boolean success, Object data, String message) {}
}
