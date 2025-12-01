package com.spotify.backend.repository;

import com.spotify.backend.model.Album;
import com.spotify.backend.model.Artist;
import com.spotify.backend.model.Playlist;
import com.spotify.backend.model.Song;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface PlaylistRepository extends MongoRepository<Playlist, String> {

    // Updated method name from findByUserId to findByCreatedBy
    List<Playlist> findByCreatedBy(String createdBy);

    // Other methods...
    long countByCreatedBy(String createdBy);
    List<Playlist> findByCreatedByAndNameContaining(String createdBy, String name);





//    @Repository
//    public interface SongRepository extends MongoRepository<Song, String> {
//        // MongoDB queries
//    }
//
//    @Repository
//    public interface ArtistRepository extends MongoRepository<Artist, String> {
//    }
//
//    @Repository
//    public interface AlbumRepository extends MongoRepository<Album, String> {
//    }


}