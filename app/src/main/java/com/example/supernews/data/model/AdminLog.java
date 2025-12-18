package com.example.supernews.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class AdminLog {
    private String id;
    private String adminId;      // ID người thực hiện
    private String adminName;    // Tên người thực hiện
    private String action;       // Hành động: CREATE, UPDATE, DELETE
    private String targetId;     // ID đối tượng bị tác động (ID bài viết)
    private String targetTitle;  // Tiêu đề bài viết (để dễ đọc log)
    private String details;      // Mô tả thêm (nếu cần)
    private Timestamp timestamp; // Thời gian

    public AdminLog() {} // Constructor rỗng cho Firebase

    public AdminLog(String adminId, String adminName, String action, String targetId, String targetTitle, String details) {
        this.adminId = adminId;
        this.adminName = adminName;
        this.action = action;
        this.targetId = targetId;
        this.targetTitle = targetTitle;
        this.details = details;
    }

    // --- GETTER & SETTER ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAdminId() { return adminId; }
    public String getAdminName() { return adminName; }
    public String getAction() { return action; }
    public String getTargetId() { return targetId; }
    public String getTargetTitle() { return targetTitle; }
    public String getDetails() { return details; }

    @ServerTimestamp
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}