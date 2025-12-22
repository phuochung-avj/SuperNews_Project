package com.example.supernews.data.manager;

import com.example.supernews.data.model.User; // 🔥 Nhớ Import file User chứa hằng số
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class UserManager {

    private static UserManager instance;
    private boolean isAdmin = false;
    private ListenerRegistration registration; // Biến để hủy lắng nghe khi thoát app

    // Interface để báo về MainActivity biết khi quyền thay đổi
    public interface OnRoleChangeListener {
        void onRoleChanged(boolean isAdmin);
    }

    private UserManager() {}

    public static synchronized UserManager getInstance() {
        if (instance == null) instance = new UserManager();
        return instance;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    // Bắt đầu lắng nghe sự thay đổi quyền từ Firestore
    public void startListeningRole(OnRoleChangeListener listener) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            isAdmin = false;
            if (listener != null) listener.onRoleChanged(false);
            return;
        }

        // addSnapshotListener: Lắng nghe thời gian thực
        registration = FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) {
                        isAdmin = false;
                    } else {
                        String role = snapshot.getString("role");

                        isAdmin = User.ROLE_ADMIN.equals(role);
                    }

                    // Báo cho UI cập nhật
                    if (listener != null) listener.onRoleChanged(isAdmin);
                });
    }

    // Hủy lắng nghe để tránh tốn pin
    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}