package com.spotify.backend.repository;

import com.spotify.backend.model.UserLike;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserLikeRepository extends MongoRepository<UserLike, String> {

    List<UserLike> findByUserId(String userId);

    List<UserLike> findByTargetId(String targetId);

    List<UserLike> findByTargetType(String targetType);
}
