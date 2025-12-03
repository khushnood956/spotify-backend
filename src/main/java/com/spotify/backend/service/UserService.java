package com.spotify.backend.service;

import com.spotify.backend.model.User;
import com.spotify.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // -----------------------------------
    // GET ALL USERS
    // -----------------------------------
    public List<User> getAll() {
        return userRepo.findAll();
    }

    // -----------------------------------
    // GET USER BY ID
    // -----------------------------------
    public Optional<User> getById(String id) {
        return userRepo.findById(id);
    }

    // -----------------------------------
    // REGISTER USER
    // -----------------------------------
    public User register(User user) {

        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Hash password
        user.setPasswordHash(encoder.encode(user.getPasswordHash()));

        return userRepo.save(user);
    }

    public Map<String, Object> getPaginated(int page, int size, String search) {

        Pageable pageable = PageRequest.of(page, size);

        Page<User> userPage;

        if (search != null && !search.isEmpty()) {
            userPage = userRepo.findByUsernameContainingIgnoreCase(search, pageable);
        } else {
            userPage = userRepo.findAll(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", userPage.getTotalPages());
        response.put("totalElements", userPage.getTotalElements());
        response.put("data", userPage.getContent());

        return response;
    }


    // -----------------------------------
    // LOGIN USER
    // -----------------------------------
    public User login(String username, String password) {

        Optional<User> userOpt = userRepo.findByUsername(username);

        if (userOpt.isEmpty()) return null;

        User user = userOpt.get();

        if (!encoder.matches(password, user.getPasswordHash())) {
            return null;
        }

        return user; // success
    }

    // -----------------------------------
    // UPDATE USER
    // -----------------------------------
    public User update(String id, User u) {
        return userRepo.findById(id).map(existing -> {

            existing.setUsername(u.getUsername());
            existing.setEmail(u.getEmail());
            existing.setDisplayName(u.getDisplayName());
            existing.setProfilePicture(u.getProfilePicture());
            existing.setRole(u.getRole());

            return userRepo.save(existing);
        }).orElse(null);
    }

    // -----------------------------------
    // DELETE USER
    // -----------------------------------
    public boolean delete(String id) {
        if (!userRepo.existsById(id)) return false;
        userRepo.deleteById(id);
        return true;
    }
}
