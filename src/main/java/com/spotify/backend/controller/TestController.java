package com.spotify.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is public - no auth needed";
    }

    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "This is protected - auth required";
    }
}