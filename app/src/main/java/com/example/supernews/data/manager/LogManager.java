package com.example.supernews.data.manager;

import com.example.supernews.data.model.AdminLog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LogManager {

    private static final String COLLECTION_LOGS = "admin_logs";
    private static LogManager instance;
    private final FirebaseFirestore db;

    private LogManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    // Hàm ghi log duy nhất bạn cần gọi
    public void log(String action, String targetId, String targetTitle, String details) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return; // Không đăng nhập thì thôi

        String adminName = (user.getDisplayName() != null && !user.getDisplayName().isEmpty())
                ? user.getDisplayName() : user.getEmail();

        AdminLog log = new AdminLog(
                user.getUid(),
                adminName,
                action,
                targetId,
                targetTitle,
                details
        );

        // Ghi vào Firestore (Fire & Forget - Ghi xong không cần chờ kết quả để App đỡ lag)
        db.collection(COLLECTION_LOGS).add(log);
    }
}