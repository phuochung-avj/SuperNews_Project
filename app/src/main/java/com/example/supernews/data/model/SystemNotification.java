package com.example.supernews.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class SystemNotification implements Serializable {
    private String id;
    private String title;
    private String body;
    private String newsId;
    private String type;
    private boolean isRead; // Trạng thái đã đọc/chưa đọc
    private Timestamp timestamp;

    public SystemNotification() { } // Bắt buộc cho Firestore

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getNewsId() { return newsId; }
    public Timestamp getTimestamp() { return timestamp; }
    @PropertyName("isRead")
    public boolean isRead() {
        return isRead;
    }

    @PropertyName("isRead")
    public void setRead(boolean read) {
        isRead = read;
    }
}