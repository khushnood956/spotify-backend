package com.spotify.backend.repository;

import com.spotify.backend.model.Album;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface AlbumRepository extends MongoRepository<Album, String> {

    // ✅ FIXED: Use explicit query to avoid Spring Data misinterpretation
    @Query("{ 'artist_id' : ?0 }")
    List<Album> findByArtist_id(String artist_id);
}