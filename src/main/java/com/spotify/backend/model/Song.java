package com.spotify.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;
import org.springframework.objenesis.instantiator.annotations.Instantiator;

import java.util.Date;
import java.util.List;

@Document(collection = "songs")
@CompoundIndex(def = "{'genre': 1, 'playCount': -1}", name = "genre_plays_idx")
@CompoundIndex(def = "{'artist_id': 1, 'album_id': 1}", name = "artist_album_idx")
public class Song {

    @Id
    private String id;

    @Transient
    private List<Song> songs;




    @TextIndexed(weight = 10)
    private String title;
    @Indexed
    private String artist_id;
    @Indexed
    private String album_id;
    private int duration;
    private String fileUrl;
    @Indexed
    private String genre;
    private long playCount;
    private String createdAt;  // ADD THIS FIELD
    private String updatedAt;  // ADD THIS FIELD

    public Float getTextScore() {
        return textScore;
    }

    public void setTextScore(Float textScore) {
        this.textScore = textScore;
    }

    @TextScore
    private Float textScore; // For text search relevance score


    // Constructors
    public Song() {
        this.playCount = 0;
        this.createdAt = new Date().toInstant().toString();
        this.updatedAt = this.createdAt;
    }

    @Transient
    private Artist artist;
    @Transient
    private Album album;

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(String artist_id) {
        this.artist_id = artist_id;
    }

    public String getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(String album_id) {
        this.album_id = album_id;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public long getPlayCount() {
        return playCount;
    }

    public void setPlayCount(long playCount) {
        this.playCount = playCount;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }



    // Getters and Setters
}
