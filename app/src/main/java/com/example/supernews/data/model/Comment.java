package com.example.supernews.data.model;

import com.google.firebase.Timestamp; // Lưu ý chọn đúng gói này

public class Comment {
    private String content;     // Nội dung bình luận
    private String userId;      // Ai bình luận?
    private String userName;    // Tên người đó (để hiển thị cho nhanh)
    private Timestamp timestamp; // Thời gian

    public Comment() { } // Bắt buộc cho Firebase

    public Comment(String content, String userId, String userName, Timestamp timestamp) {
        this.content = content;
        this.userId = userId;
        this.userName = userName;
        this.timestamp = timestamp;
    }

    // Getter & Setter
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}