package com.example.supernews.data.repository;

import com.example.supernews.data.model.News;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class NewsRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnFirestoreTaskComplete {
        void onSuccess(List<News> newsList);
        void onError(Exception e);
    }

    // --- HÀM LẤY DỮ LIỆU (ĐÃ CẬP NHẬT LOGIC LỌC SCOPE) ---
    public void getNewsData(String category, String scope, OnFirestoreTaskComplete onComplete) {

        Query query = db.collection("news");

        // 1. Ưu tiên lọc theo Scope (Trong nước / Quốc tế)
        if (scope != null && !scope.isEmpty()) {
            query = query.whereEqualTo("scope", scope);
        }

        // 2. Lọc theo Chuyên mục (Nếu không phải "Mới nhất")
        if (category != null && !category.equals("Mới nhất")) {
            query = query.whereEqualTo("source", category);
        }

        // 3. Sắp xếp tin mới nhất lên đầu
        query = query.orderBy("publishedAt", Query.Direction.DESCENDING);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<News> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot d : queryDocumentSnapshots) {
                            News news = d.toObject(News.class);
                            if (news != null) {
                                news.setId(d.getId());
                                list.add(news);
                            }
                        }
                    }
                    onComplete.onSuccess(list);
                })
                .addOnFailureListener(onComplete::onError);
    }

    // --- GIỮ NGUYÊN CÁC HÀM KHÁC ---
    public void searchNews(String keyword, OnFirestoreTaskComplete onComplete) {
        db.collection("news")
                .orderBy("title")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<News> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot d : queryDocumentSnapshots) {
                            News news = d.toObject(News.class);
                            if (news != null) {
                                news.setId(d.getId());
                                list.add(news);
                            }
                        }
                    }
                    onComplete.onSuccess(list);
                })
                .addOnFailureListener(onComplete::onError);
    }

    public void getSavedNews(String userId, OnFirestoreTaskComplete onComplete) {
        db.collection("users").document(userId).collection("bookmarks").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<News> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot d : queryDocumentSnapshots) {
                            News news = d.toObject(News.class);
                            if (news != null) {
                                news.setId(d.getId());
                                list.add(news);
                            }
                        }
                    }
                    onComplete.onSuccess(list);
                })
                .addOnFailureListener(onComplete::onError);
    }
}