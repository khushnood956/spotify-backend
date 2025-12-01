package com.spotify.backend.repository;

import com.spotify.backend.model.AdminLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AdminLogRepository extends MongoRepository<AdminLog, String> {

    List<AdminLog> findByUserId(String userId);
}
