package com.example.supernews.data.manager;

import com.example.supernews.data.model.News;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class BookmarkManager {

    private static BookmarkManager instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private BookmarkManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized BookmarkManager getInstance() {
        if (instance == null) {
            instance = new BookmarkManager();
        }
        return instance;
    }

    // Interface trả về kết quả
    public interface BookmarkCallback {
        void onSuccess(boolean isSaved);
        void onFailure(String error);
    }

    public interface CheckCallback {
        void onResult(boolean isSaved);
    }

    // --- 1. HÀM THÔNG MINH (Dùng cho DetailActivity) ---
    // Tự động kiểm tra: Nếu có rồi thì xóa, chưa có thì thêm
    public void toggleBookmark(News news, BookmarkCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onFailure("Vui lòng đăng nhập!");
            return;
        }

        checkIsBookmarked(news.getId(), isBookmarked -> {
            if (isBookmarked) {
                removeBookmark(news.getId(), callback); // Có rồi -> Xóa
            } else {
                addBookmark(news, callback); // Chưa -> Thêm
            }
        });
    }

    // --- 2. CÁC HÀM CƠ BẢN (Dùng cho SavedNewsActivity & Logic bên trong) ---

    public void checkIsBookmarked(String newsId, CheckCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onResult(false);
            return;
        }
        db.collection("users").document(auth.getUid())
                .collection("bookmarks").document(newsId)
                .get()
                .addOnSuccessListener(doc -> callback.onResult(doc.exists()))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    // Thêm bookmark
    public void addBookmark(News news, BookmarkCallback callback) {
        if (auth.getCurrentUser() == null) return;

        db.collection("users").document(auth.getUid())
                .collection("bookmarks").document(news.getId())
                .set(news)
                .addOnSuccessListener(v -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Xóa bookmark
    public void removeBookmark(String newsId, BookmarkCallback callback) {
        // 1. Lấy User hiện tại một cách an toàn
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // 2. Kiểm tra Null kỹ càng
        if (currentUser == null) {
            // Nếu chưa đăng nhập thì không làm gì cả (hoặc báo lỗi)
            return;
        }

        String uid = currentUser.getUid(); // Lấy UID an toàn

        // 3. Thực hiện xóa
        db.collection("users").document(uid)
                .collection("bookmarks").document(newsId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(false); // false nghĩa là "đã bỏ lưu"
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // Lấy Query để lắng nghe (Dùng cho SavedNewsActivity)
    public Query getBookmarksQuery() {
        if (auth.getCurrentUser() == null) return null;
        return db.collection("users").document(auth.getUid())
                .collection("bookmarks");
    }
}