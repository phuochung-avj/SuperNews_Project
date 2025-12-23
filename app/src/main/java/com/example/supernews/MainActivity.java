package com.example.supernews;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.supernews.data.manager.UserManager;
import com.example.supernews.databinding.ActivityMainBinding;
import com.example.supernews.ui.view.SearchActivity;
import com.example.supernews.ui.view.UploadActivity;
import com.example.supernews.ui.view.auth.ProfileFragment;
import com.example.supernews.ui.view.detail.DetailActivity;
import com.example.supernews.ui.view.home.MainNewsContainerFragment;
import com.example.supernews.ui.view.notification.NotificationFragment;
import com.example.supernews.ui.view.saved.SavedFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean isGuest = false; // Biến kiểm tra khách

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Cấu hình Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("SuperNews");
        }

        // 2. Kiểm tra chế độ Khách
        isGuest = getIntent().getBooleanExtra("IS_GUEST", false);

        // Nếu không phải khách -> Lắng nghe quyền Admin để cập nhật Menu
        if (!isGuest) {
            UserManager.getInstance().startListeningRole(isAdmin -> invalidateOptionsMenu());
        }

        // 3. Khởi tạo giao diện mặc định
        loadFragment(new MainNewsContainerFragment());
        setupBottomNavigation();

        // 4. Đăng ký nhận thông báo & Xin quyền
        FirebaseMessaging.getInstance().subscribeToTopic("all_news");
        askNotificationPermission();

        // 5. Kiểm tra thông báo (Deep Link) khi mở App lần đầu
        processDeepLink(getIntent());
    }

    // --- QUAN TRỌNG: Xử lý thông báo khi App đang chạy ngầm ---
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Cập nhật Intent mới nhất
        processDeepLink(intent); // Xử lý lại logic mở bài viết
    }

    // Xử lý logic mở bài viết từ thông báo
    private void processDeepLink(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            String newsId = intent.getStringExtra("newsId");
            String type = intent.getStringExtra("type");

            // Chỉ xử lý khi có ID và đúng loại thông báo
            if (newsId != null && !newsId.isEmpty()) {
                openNewsDetailOnlyId(newsId);
            }
        }
    }

    // Mở màn hình chi tiết NGAY LẬP TỨC (Không query Firestore ở đây để tránh Lag)
    private void openNewsDetailOnlyId(String newsId) {
        Intent intent = new Intent(this, DetailActivity.class);
        // Gửi ID sang, bên DetailActivity sẽ tự tải dữ liệu
        intent.putExtra("news_id_only", newsId);
        startActivity(intent);
    }

    private void setupBottomNavigation() {
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
                title = isGuest ? "Khách tham quan" : "Hồ sơ cá nhân";
            }

            if (fragment != null) {
                loadFragment(fragment);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(title);
                }
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        // Có thể nâng cấp lên show/hide nếu muốn giữ trạng thái cuộn
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // Xin quyền thông báo cho Android 13+
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Dừng lắng nghe khi thoát App để tránh rò rỉ bộ nhớ
        if (!isGuest) {
            UserManager.getInstance().stopListening();
        }
    }

    // --- MENU LOGIC ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem uploadItem = menu.findItem(R.id.action_upload);
        MenuItem notiItem = menu.findItem(R.id.action_notification);

        // Logic hiển thị nút Upload (Chỉ Admin mới thấy)
        if (uploadItem != null) {
            boolean isAdmin = UserManager.getInstance().isAdmin();
            // Nếu là Khách hoặc không phải Admin -> Ẩn nút Upload
            uploadItem.setVisible(!isGuest && isAdmin);
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
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Thông báo");
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}