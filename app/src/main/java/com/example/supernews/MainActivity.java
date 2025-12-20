package com.example.supernews;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.supernews.data.manager.UserManager;
import com.example.supernews.data.model.News;
import com.example.supernews.databinding.ActivityMainBinding;
import com.example.supernews.ui.view.SearchActivity;
import com.example.supernews.ui.view.UploadActivity;
import com.example.supernews.ui.view.auth.ProfileFragment;
import com.example.supernews.ui.view.detail.DetailActivity;
import com.example.supernews.ui.view.home.MainNewsContainerFragment;
import com.example.supernews.ui.view.notification.NotificationFragment;
import com.example.supernews.ui.view.saved.SavedFragment;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Mặc định load tab Trang chủ
        loadFragment(new MainNewsContainerFragment());
        getSupportActionBar().setTitle("SuperNews");

        // Xử lý sự kiện bấm Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment = null;
            String title = "SuperNews";

            if (id == R.id.nav_home) {
                fragment = new MainNewsContainerFragment();
                title = "SuperNews";
            } else if (id == R.id.nav_saved) {
                fragment = new SavedFragment();
                title = "Tin đã lưu";
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
                title = "Hồ sơ cá nhân";
            }

            if (fragment != null) {
                loadFragment(fragment);
                getSupportActionBar().setTitle(title);
                return true;
            }
            return false;
        });

        // 1. Xin quyền & Đăng ký nhận tin
        askNotificationPermission();
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("all_news");

        // 🔥 2. KIỂM TRA DEEP LINK TỪ THÔNG BÁO (LOGIC MỚI) 🔥
        checkDeepLinkFromNotification();
    }

    // --- HÀM MỚI: XỬ LÝ KHI BẤM VÀO THÔNG BÁO ---
    private void checkDeepLinkFromNotification() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            String newsId = getIntent().getStringExtra("newsId");
            String type = getIntent().getStringExtra("type");

            // Nếu đúng là thông báo tin tức và có ID
            if (newsId != null && !newsId.isEmpty() && "news_alert".equals(type)) {
                openNewsDetail(newsId);
            }
        }
    }

    // --- HÀM MỚI: TẢI TIN & MỞ DETAIL ACTIVITY ---
    private void openNewsDetail(String newsId) {
        // Có thể hiện Loading nhẹ ở đây nếu muốn
        FirebaseFirestore.getInstance()
                .collection("news").document(newsId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        News news = documentSnapshot.toObject(News.class);
                        if (news != null) {
                            news.setId(documentSnapshot.getId());

                            // Mở màn hình chi tiết
                            Intent intent = new Intent(this, DetailActivity.class);
                            intent.putExtra("object_news", news);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(this, "Bài viết không tồn tại hoặc đã bị xóa", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Lỗi mạng hoặc lỗi server
                });
    }

    // --- CÁC HÀM CŨ GIỮ NGUYÊN ---

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        UserManager.getInstance().startListeningRole(isAdmin -> invalidateOptionsMenu());
    }

    @Override
    protected void onStop() {
        super.onStop();
        UserManager.getInstance().stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem uploadItem = menu.findItem(R.id.action_upload);
        if (uploadItem != null) {
            uploadItem.setVisible(UserManager.getInstance().isAdmin());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        } else if (id == R.id.action_upload) {
            startActivity(new Intent(this, UploadActivity.class));
            return true;
        } else if (id == R.id.action_notification) {
            loadFragment(new NotificationFragment());
            getSupportActionBar().setTitle("Thông báo");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}