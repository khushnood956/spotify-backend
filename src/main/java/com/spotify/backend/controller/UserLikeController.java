package com.spotify.backend.controller;

import com.spotify.backend.model.UserLike;
import com.spotify.backend.service.UserLikeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-likes")
public class UserLikeController {

    private final UserLikeService service;

    public UserLikeController(UserLikeService service) {
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
    public Object create(@RequestBody UserLike payload) {
        return new Response(true, service.create(payload), "Created");
    }

    @PutMapping("/{id}")
    public Object update(@PathVariable String id, @RequestBody UserLike payload) {
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
