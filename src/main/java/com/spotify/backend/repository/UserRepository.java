package com.spotify.backend.repository;

import com.spotify.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    long countByJoinDateAfter(String date);
    Optional<User> findByUsername(String username);
    List<User> findByRole(String role);
    List<User> findByUsernameContainingOrEmailContaining(String username, String email);

}