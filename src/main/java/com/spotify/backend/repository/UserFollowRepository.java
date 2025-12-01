package com.spotify.backend.repository;

import com.spotify.backend.model.UserFollow;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserFollowRepository extends MongoRepository<UserFollow, String> {

    List<UserFollow> findByFollowerId(String followerId);

    List<UserFollow> findByFollowingId(String followingId);

    List<UserFollow> findByFollowingType(String followingType);
}
