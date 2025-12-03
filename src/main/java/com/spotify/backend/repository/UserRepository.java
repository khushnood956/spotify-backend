package com.spotify.backend.repository;

import com.spotify.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
    List<User> findByRole(String role);
    List<User> findByUsernameContainingOrEmailContaining(String username, String email);

    Page<User> findByRole(String role, Pageable pageable);

    @Query("{'$or': [{'username': {$regex: ?0, $options: 'i'}}, {'email': {$regex: ?1, $options: 'i'}}]}")
    Page<User> findByUsernameContainingOrEmailContaining(String username, String email, Pageable pageable);

    Long countByJoinDateAfter(String date);

}