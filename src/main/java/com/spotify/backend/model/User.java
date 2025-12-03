package com.spotify.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "users")
public class User {

    @Id
    private String id;
    private String username;
    private String email;
    private String passwordHash;
    private String displayName;
    private String profilePicture;
    private String joinDate;
    private String lastActive;  // ADD THIS FIELD
    private String role;
    private boolean isActive = true;  // ADD THIS FIELD

    // Constructors
    public User() {
        this.role = "USER";
        this.isActive = true;
        this.joinDate = new Date().toInstant().toString();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }

    public String getLastActive() { return lastActive; }  // ADD GETTER
    public void setLastActive(String lastActive) { this.lastActive = lastActive; }  // ADD SETTER

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return isActive; }  // ADD GETTER
    public void setActive(boolean active) { isActive = active; }  // ADD SETTER
}