package com.spotify.backend.service;

import com.spotify.backend.model.User;
import com.spotify.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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
