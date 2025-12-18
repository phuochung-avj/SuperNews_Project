package com.example.supernews.data.model;

import java.io.Serializable;

public class User implements Serializable {
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";
    private String id;
    private String name;
    private String email;
    private String role;
    private String avatar;

    public User() { } // Bắt buộc cho Firebase

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}