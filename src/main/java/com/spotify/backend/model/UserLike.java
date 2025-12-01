package com.spotify.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_likes")
public class UserLike {

    @Id
    private String id;

    @Transient
    private Song song;

    @Transient
    private Playlist playlist;


    private String userId;
    private String targetId;
    private String targetType; // "song" or "playlist"
}
