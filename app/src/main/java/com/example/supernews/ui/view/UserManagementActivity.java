package com.example.supernews.ui.view;

import android.os.Bundle;
import android.view.View;
import android.view.ViewParent; // Thêm import này
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.supernews.R;
import com.example.supernews.data.manager.UserManager;
import com.example.supernews.data.model.User;
import com.example.supernews.ui.adapter.UserManagementAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private UserManagementAdapter adapter;
    private List<User> userList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. CHỐT CHẶN BẢO MẬT (Chỉ Admin mới được vào)
        if (!UserManager.getInstance().isAdmin()) {
            Toast.makeText(this, "⛔ Bạn không có quyền truy cập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tái sử dụng layout của màn hình Admin Log
        setContentView(R.layout.activity_admin_log);

        // Setup Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarLog);
        if(toolbar != null) {
            toolbar.setTitle("Quản lý tài khoản");
            setSupportActionBar(toolbar);
            if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());

            // Ẩn bộ lọc của màn hình Log đi (Chỉ hiện cho Admin)
            try {
                View chipAll = findViewById(R.id.chipAll);
                if (chipAll != null) {
                    ViewParent parent = chipAll.getParent(); // LinearLayout
                    if (parent != null) {
                        ViewParent grandParent = parent.getParent(); // HorizontalScrollView
                        if (grandParent instanceof View) {
                            ((View) grandParent).setVisibility(View.GONE);
                        }
                    }
                }
            } catch (Exception e) {
                // Bỏ qua nếu không tìm thấy view, không ảnh hưởng chức năng chính
            }
        }

        recyclerView = findViewById(R.id.rvLogs);
        progressBar = findViewById(R.id.progressBarLog);

        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();

        adapter = new UserManagementAdapter(this, userList, this::confirmChangeRole);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);

        // Lấy toàn bộ User
        db.collection("users")
                .addSnapshotListener((value, error) -> {
                    if (isFinishing()) return;
                    progressBar.setVisibility(View.GONE);

                    if (value != null) {
                        userList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            User user = doc.toObject(User.class);
                            user.setId(doc.getId());
                            userList.add(user);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void confirmChangeRole(User user) {
        String currentUid = FirebaseAuth.getInstance().getUid();

        // Không cho phép tự hủy quyền của chính mình
        if (user.getId().equals(currentUid)) {
            Toast.makeText(this, "Không thể tự hủy quyền Admin của chính mình!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isAdmin = User.ROLE_ADMIN.equals(user.getRole());
        String action = isAdmin ? "HỦY QUYỀN ADMIN" : "THĂNG CHỨC ADMIN";
        String message = "Bạn có chắc chắn muốn " + action + " cho " + user.getName() + "?";

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận phân quyền")
                .setMessage(message)
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    // Logic đảo ngược quyền
                    String newRole = isAdmin ? User.ROLE_USER : User.ROLE_ADMIN;
                    updateRoleOnServer(user, newRole);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateRoleOnServer(User user, String newRole) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(user.getId())
                .update("role", newRole)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();

                    // Ghi log hoạt động
                    com.example.supernews.data.manager.LogManager.getInstance()
                            .log("UPDATE", user.getId(), user.getName(), "Đã thay đổi quyền thành: " + newRole);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}