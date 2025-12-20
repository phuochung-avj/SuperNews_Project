package com.example.supernews.ui.view.notification;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.supernews.R;
import com.example.supernews.data.model.News;
import com.example.supernews.data.model.SystemNotification;
import com.example.supernews.ui.adapter.NotificationAdapter;
import com.example.supernews.ui.view.detail.DetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    // 👇 SỬA Ở ĐÂY: Đổi TextView thành View để dùng cho cả cụm LinearLayout
    private View layoutEmpty;

    private FirebaseFirestore db;
    private NotificationAdapter adapter;
    private List<SystemNotification> notiList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nhớ đảm bảo file xml tên đúng là fragment_notification nhé
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rvNotifications);
        progressBar = view.findViewById(R.id.progressBarNoti);

        // 👇 SỬA Ở ĐÂY: Ánh xạ ID mới
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        db = FirebaseFirestore.getInstance();
        notiList = new ArrayList<>();

        setupRecyclerView();
        loadNotifications();
    }

    // ... (Các hàm setupRecyclerView, onNotificationClick, openNewsDetail GIỮ NGUYÊN) ...
    // ... Bạn copy lại từ code cũ nhé ...
    private void setupRecyclerView() {
        adapter = new NotificationAdapter(requireContext(), notiList, this::onNotificationClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void onNotificationClick(SystemNotification noti) {
        // Chỉ xử lý nếu đang là trạng thái chưa đọc
        if (!noti.isRead()) {

            // 1. Cập nhật Server (KÈM BẮT LỖI CHI TIẾT)
            db.collection("system_notifications").document(noti.getId())
                    .update("isRead", true)
                    .addOnSuccessListener(aVoid -> {
                        // Nếu thành công thì tốt, không làm gì cả
                        android.util.Log.d("NOTI_DEBUG", "Đã cập nhật thành công lên Server");
                    })
                    .addOnFailureListener(e -> {
                        // 🔥 QUAN TRỌNG: NẾU LỖI NÓ SẼ HIỆN LÊN MÀN HÌNH 🔥
                        Toast.makeText(requireContext(), "LỖI SERVER: " + e.getMessage(), Toast.LENGTH_LONG).show();

                        // Hoàn tác lại màu xanh để bạn biết là chưa lưu được
                        noti.setRead(false);
                        adapter.notifyDataSetChanged();
                    });

            // 2. Cập nhật giao diện ngay lập tức (để người dùng thấy mượt)
            noti.setRead(true);
            adapter.notifyDataSetChanged();
        }

        // 3. Mở bài viết chi tiết
        if (noti.getNewsId() != null) {
            openNewsDetail(noti.getNewsId());
        }
    }

    private void openNewsDetail(String newsId) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("news").document(newsId).get()
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(View.GONE);
                    if (doc.exists()) {
                        News news = doc.toObject(News.class);
                        if (news != null) {
                            news.setId(doc.getId());
                            Intent intent = new Intent(requireContext(), DetailActivity.class);
                            intent.putExtra("object_news", news);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(requireContext(), "Bài viết không tồn tại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);

        // 1. Tính thời gian giới hạn (Ví dụ: 3 ngày trước)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3); // Trừ đi 3 ngày
        Date threeDaysAgo = calendar.getTime();

        // 2. Tạo Query: Chỉ lấy tin MỚI HƠN 3 ngày trước
        db.collection("system_notifications")
                .whereGreaterThan("timestamp", threeDaysAgo) // <--- LỌC THỜI GIAN
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);

                    if (error != null) {
                        // Xử lý lỗi (nếu cần)
                        return;
                    }

                    if (value != null) {
                        notiList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            SystemNotification item = doc.toObject(SystemNotification.class);
                            item.setId(doc.getId());
                            notiList.add(item);
                        }
                        adapter.notifyDataSetChanged();

                        // Ẩn/Hiện hình rỗng
                        if (notiList.isEmpty()) {
                            layoutEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            layoutEmpty.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
}