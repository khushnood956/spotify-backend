package com.spotify.backend.repository;

import com.spotify.backend.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface SongRepository extends MongoRepository<Song, String> {

    // Use explicit @Query annotations with the exact field names
    @Query("{ 'album_id' : ?0 }")
    List<Song> findByAlbumId(String albumId);

    @Query("{ 'artist_id' : ?0 }")
    List<Song> findByArtistId(String artistId);

    // This one should work fine as is
    List<Song> findByGenreIgnoreCase(String genre);

    // Optional: Add method for top songs
    @Query("{ }")  // Empty filter to get all
    List<Song> findByOrderByPlayCountDesc();
    Page<Song> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("{'createdAt': {$gt: ?0}}")
    Long countByCreatedAtAfter(String date);
}