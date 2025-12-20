package com.example.supernews.ui.view.detail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.supernews.R;
import com.example.supernews.data.manager.BookmarkManager;
import com.example.supernews.data.manager.UserManager;
import com.example.supernews.data.model.Comment;
import com.example.supernews.data.model.News;
import com.example.supernews.databinding.ActivityDetailBinding;
import com.example.supernews.ui.adapter.CommentsAdapter;
import com.example.supernews.ui.adapter.RelatedAdapter;
import com.example.supernews.ui.view.EditActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private News currentNews;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private boolean isSaved = false;
    private boolean isLiked = false;
    private float currentTextSize = 16f;

    private CommentsAdapter commentsAdapter;
    private RelatedAdapter relatedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Toolbar
        setSupportActionBar(binding.toolbarDetail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        loadDisplaySettings(); // Load cỡ chữ

        // Nhận dữ liệu
        currentNews = (News) getIntent().getSerializableExtra("object_news");

        if (currentNews != null) {
            displayNewsData();
            increaseViewCount();

            // Các tính năng tương tác
            checkIsSaved();
            checkIsLiked();

            // Setup danh sách
            setupCommentsRecycler();
            loadCommentsRealtime();
            loadRelatedNews();
        }

        // Sự kiện Click
        binding.fabSave.setOnClickListener(v -> toggleSaveNews());
        binding.btnSendComment.setOnClickListener(v -> postComment());
        binding.btnLike.setOnClickListener(v -> toggleLikeNews());
    }

    // --- PHẦN 1: HIỂN THỊ DỮ LIỆU ---

    private void displayNewsData() {
        binding.tvDetailTitle.setText(currentNews.getTitle());
        binding.tvDetailDate.setText(currentNews.getPublishedAt());
        binding.tvDetailContent.setText(currentNews.getContent());
        binding.tvDetailViews.setText(currentNews.getViews() + "");
        binding.tvLikeCount.setText(String.valueOf(currentNews.getLikes()));

        String sourceText = currentNews.getSource();
        if (currentNews.getAuthor() != null && !currentNews.getAuthor().isEmpty()) {
            sourceText = sourceText + " • " + currentNews.getAuthor();
        }
        binding.tvDetailSource.setText(sourceText);

        if (currentNews.getImageSource() != null && !currentNews.getImageSource().isEmpty()) {
            binding.tvImageCaption.setText(currentNews.getImageSource());
        } else {
            binding.tvImageCaption.setText("Ảnh minh họa / Nguồn Internet");
        }

        // Xử lý ảnh (Tối ưu cho Shared Element Transition)
        if (currentNews.getImageUrl() != null) {
            if (currentNews.getImageUrl().startsWith("http")) {
                Glide.with(this)
                        .load(currentNews.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_foreground)
                        .dontAnimate() // QUAN TRỌNG: Tắt animation để chuyển cảnh mượt
                        .into(binding.imgDetailThumb);
            } else {
                try {
                    byte[] imageBytes = Base64.decode(currentNews.getImageUrl(), Base64.DEFAULT);
                    Glide.with(this)
                            .load(imageBytes)
                            .placeholder(R.drawable.ic_launcher_background)
                            .dontAnimate()
                            .into(binding.imgDetailThumb);
                } catch (Exception e) {}
            }
        }
    }
    private void increaseViewCount() {
        db.collection("news").document(currentNews.getId())
                .update("views", FieldValue.increment(1));
    }

    private void loadRelatedNews() {
        binding.rvRelatedNews.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        db.collection("news")
                .whereEqualTo("source", currentNews.getSource())
                .limit(6)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<News> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        News news = doc.toObject(News.class);
                        news.setId(doc.getId());
                        // Loại trừ bài viết đang đọc
                        if (!news.getId().equals(currentNews.getId())) list.add(news);
                    }

                    // Khởi tạo Adapter với logic chuyển cảnh (2 tham số)
                    relatedAdapter = new RelatedAdapter(this, list, (newsItem, imageView) -> {
                        Intent intent = new Intent(this, DetailActivity.class);
                        intent.putExtra("object_news", newsItem);

                        // Hiệu ứng phóng to ảnh
                        android.app.ActivityOptions options = android.app.ActivityOptions
                                .makeSceneTransitionAnimation(this, imageView, "news_image_transition");

                        startActivity(intent, options.toBundle());
                    });
                    binding.rvRelatedNews.setAdapter(relatedAdapter);
                });
    }

    // --- PHẦN 2: BÌNH LUẬN & TƯƠNG TÁC ---

    private void setupCommentsRecycler() {
        commentsAdapter = new CommentsAdapter();
        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComments.setAdapter(commentsAdapter);
    }

    // Hàm tiện ích: Ẩn bàn phím
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void postComment() {
        String content = binding.edtComment.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để bình luận!", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSendComment.setEnabled(false); // Khóa nút gửi

        Comment comment = new Comment(content, currentUser.getUid(), currentUser.getDisplayName(), Timestamp.now());

        db.collection("news").document(currentNews.getId())
                .collection("comments").add(comment)
                .addOnSuccessListener(doc -> {
                    binding.edtComment.setText(""); // Xóa ô nhập
                    hideKeyboard(); // Ẩn bàn phím
                    binding.btnSendComment.setEnabled(true);

                    Toast.makeText(this, "Đã gửi bình luận!", Toast.LENGTH_SHORT).show();

                    // Cuộn xuống cuối danh sách
                    if (commentsAdapter.getItemCount() > 0) {
                        binding.rvComments.smoothScrollToPosition(commentsAdapter.getItemCount() - 1);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.btnSendComment.setEnabled(true);
                    Toast.makeText(this, "Lỗi gửi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCommentsRealtime() {
        db.collection("news").document(currentNews.getId())
                .collection("comments").orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    List<Comment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) list.add(doc.toObject(Comment.class));
                    commentsAdapter.setComments(list);
                });
    }

    // --- PHẦN 3: LIKE & SAVE ---

    private void checkIsSaved() {
        BookmarkManager.getInstance().checkIsBookmarked(currentNews.getId(), isBookmarked -> {
            if (isFinishing() || isDestroyed()) return;
            isSaved = isBookmarked;
            updateSaveIcon();
        });
    }

    private void toggleSaveNews() {
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để lưu tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiệu ứng nảy nhẹ (Scale Animation) cho nút bấm sướng tay
        binding.fabSave.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
            binding.fabSave.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
        });

        binding.fabSave.setEnabled(false); // Chống spam click

        BookmarkManager.getInstance().toggleBookmark(currentNews, new BookmarkManager.BookmarkCallback() {
            @Override
            public void onSuccess(boolean savedState) {
                if (isFinishing()) return;
                isSaved = savedState;
                updateSaveIcon();
                binding.fabSave.setEnabled(true);

                // --- DÙNG SNACKBAR THAY TOAST (CHUYÊN NGHIỆP HƠN) ---
                if (isSaved) {
                    // Nếu vừa Lưu xong -> Thông báo "Đã lưu"
                    com.google.android.material.snackbar.Snackbar.make(binding.getRoot(), "Đã lưu vào bộ sưu tập", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                            .setAction("XEM NGAY", v -> {
                                // Bấm Xem ngay -> Mở màn hình Tin đã lưu (SavedFragment)
                                // Vì SavedFragment nằm trong MainActivity, ta cần xử lý khéo léo hoặc mở Activity riêng
                                // Đơn giản nhất ở đây là chỉ thông báo thôi.
                            })
                            .show();
                } else {
                    // Nếu vừa Bỏ lưu -> Thông báo "Đã bỏ lưu" kèm nút Hoàn tác
                    com.google.android.material.snackbar.Snackbar.make(binding.getRoot(), "Đã bỏ lưu bài viết", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                            .setAction("HOÀN TÁC", v -> toggleSaveNews()) // Bấm hoàn tác thì lưu lại
                            .show();
                }
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing()) return;
                Toast.makeText(DetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                binding.fabSave.setEnabled(true);
            }
        });
    }

    private void updateSaveIcon() {
        if (isSaved) {
            // Đã lưu -> Hiện icon ĐẶC, màu CAM hoặc XANH (tùy bạn)
            binding.fabSave.setImageResource(R.drawable.ic_bookmark_filled);
            binding.fabSave.setColorFilter(getResources().getColor(R.color.purple_500)); // Hoặc màu chủ đạo của App
        } else {
            // Chưa lưu -> Hiện icon RỖNG, màu XÁM
            binding.fabSave.setImageResource(R.drawable.ic_bookmark_outline);
            binding.fabSave.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        }
    }


    private void checkIsLiked() {
        if (currentUser == null) return;
        db.collection("news").document(currentNews.getId())
                .collection("likes").document(currentUser.getUid())
                .get().addOnSuccessListener(doc -> {
                    isLiked = doc.exists();
                    updateLikeIcon();
                });
    }

    private void toggleLikeNews() {
        if (currentUser == null) {
            Toast.makeText(this, "Đăng nhập để thả tim!", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnLike.setEnabled(false); // Khóa tạm thời

        // --- 1. CẬP NHẬT GIAO DIỆN NGAY LẬP TỨC (OPTIMISTIC UPDATE) ---
        boolean newState = !isLiked; // Đảo ngược trạng thái hiện tại
        isLiked = newState;          // Cập nhật biến cờ luôn

        updateLikeIcon();            // Đổi màu tim ngay

        // Cập nhật số lượng ngay
        int change = isLiked ? 1 : -1;
        updateViewLikeCount(change);

        // Chạy hiệu ứng nảy (Bounce)
        binding.imgLike.animate().scaleX(0.7f).scaleY(0.7f).setDuration(100).withEndAction(() -> {
            binding.imgLike.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100).withEndAction(() -> {
                binding.imgLike.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
            });
        });

        // --- 2. GỬI YÊU CẦU LÊN SERVER (CHẠY NGẦM) ---
        if (isLiked) {
            // LOGIC THẢ TIM
            Map<String, Object> data = new HashMap<>();
            data.put("timestamp", Timestamp.now());

            db.collection("news").document(currentNews.getId())
                    .collection("likes").document(currentUser.getUid()).set(data)
                    .addOnSuccessListener(a -> {
                        binding.btnLike.setEnabled(true); // Thành công -> Mở lại nút
                    })
                    .addOnFailureListener(e -> {
                        revertLikeState(); // Lỗi -> Hoàn tác
                    });
        } else {
            // LOGIC BỎ TIM
            db.collection("news").document(currentNews.getId())
                    .collection("likes").document(currentUser.getUid()).delete()
                    .addOnSuccessListener(a -> {
                        binding.btnLike.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        revertLikeState(); // Lỗi -> Hoàn tác
                    });
        }
        DocumentReference newsRef = db.collection("news").document(currentNews.getId());

        if (isLiked) {
            // === LOGIC THẢ TIM ===

            // A. Lưu thông tin người like (để giữ trái tim màu đỏ)
            Map<String, Object> data = new HashMap<>();
            data.put("timestamp", Timestamp.now());
            newsRef.collection("likes").document(currentUser.getUid()).set(data);

            // B. [QUAN TRỌNG] Tăng biến đếm 'likes' trong bài viết lên 1
            newsRef.update("likes", FieldValue.increment(1));

        } else {
            // === LOGIC BỎ TIM ===

            // A. Xóa thông tin người like
            newsRef.collection("likes").document(currentUser.getUid()).delete();

            // B. [QUAN TRỌNG] Giảm biến đếm 'likes' đi 1
            newsRef.update("likes", FieldValue.increment(-1));
        }
    }
    private void revertLikeState() {
        isLiked = !isLiked; // Đảo lại trạng thái cũ
        updateLikeIcon();
        updateViewLikeCount(isLiked ? 1 : -1);
        binding.btnLike.setEnabled(true);
        Toast.makeText(this, "Lỗi kết nối, vui lòng thử lại", Toast.LENGTH_SHORT).show();
    }

    private void updateViewLikeCount(int change) {
        try {
            long currentCount = Long.parseLong(binding.tvLikeCount.getText().toString());
            long newCount = currentCount + change;
            if (newCount < 0) {
                newCount = 0;
            }
            binding.tvLikeCount.setText(String.valueOf(currentCount + change));
            binding.tvLikeCount.setText(String.valueOf(newCount));
        } catch (Exception e) {}
    }

    private void updateLikeIcon() {
        if (isLiked) {
            // Đã like -> Tim đặc, màu Hồng/Đỏ
            binding.imgLike.setImageResource(R.drawable.ic_heart_filled);
            // Lưu ý: Vì trong vector filled mình đã set tint="#E91E63" nên có thể bỏ dòng setColorFilter này
            binding.imgLike.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            // Chưa like -> Tim rỗng, màu Xám
            binding.imgLike.setImageResource(R.drawable.ic_heart_outline);
            binding.imgLike.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        }
    }

    // --- PHẦN 4: CÀI ĐẶT & MENU ---

    private void loadDisplaySettings() {
        SharedPreferences prefs = getSharedPreferences("SuperNewsSettings", MODE_PRIVATE);
        currentTextSize = prefs.getFloat("content_font_size", 16f);
        binding.tvDetailContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentTextSize);
    }

    private void showDisplaySettingsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.layout_display_settings);

        SeekBar seekbar = dialog.findViewById(R.id.seekbarFontSize);
        androidx.appcompat.widget.SwitchCompat switchDarkMode = dialog.findViewById(R.id.switchDarkModeDetail);

        int progress = (int) ((currentTextSize - 12) / 2);
        if (seekbar != null) {
            seekbar.setProgress(progress);
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    float newSize = 12 + (i * 2);
                    binding.tvDetailContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize);
                    currentTextSize = newSize;
                    SharedPreferences prefs = getSharedPreferences("SuperNewsSettings", MODE_PRIVATE);
                    prefs.edit().putFloat("content_font_size", newSize).apply();
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        SharedPreferences prefs = getSharedPreferences("SuperNewsSettings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        if (switchDarkMode != null) {
            switchDarkMode.setChecked(isDark);
            switchDarkMode.setOnCheckedChangeListener((view, isChecked) -> {
                prefs.edit().putBoolean("dark_mode", isChecked).apply();
                if (isChecked) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                dialog.dismiss();
            });
        }
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        MenuItem editItem = menu.findItem(R.id.action_edit);

        // Kiểm tra quyền Admin thông qua UserManager
        boolean isAdmin = UserManager.getInstance().isAdmin();

        if (deleteItem != null) deleteItem.setVisible(isAdmin);
        if (editItem != null) editItem.setVisible(isAdmin);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            supportFinishAfterTransition();
            return true;
        } else if (id == R.id.action_display_settings) {
            showDisplaySettingsDialog();
            return true;
        } else if (id == R.id.action_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Tin nóng: " + currentNews.getTitle());
            intent.putExtra(Intent.EXTRA_TEXT, currentNews.getTitle() + "\n\n" + currentNews.getImageUrl());
            startActivity(Intent.createChooser(intent, "Chia sẻ qua"));
            return true;
        } else if (id == R.id.action_delete) {
            // 🔥 GỌI HÀM XÓA NÂNG CAO TẠI ĐÂY
            new AlertDialog.Builder(this)
                    .setTitle("Xóa bài").setMessage("Chắc chắn xóa bài viết này và toàn bộ thông báo liên quan?")
                    .setPositiveButton("Xóa", (d, w) -> deleteNewsAndNotifications())
                    .setNegativeButton("Hủy", null).show();
            return true;
        } else if (id == R.id.action_edit) {
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra("news_to_edit", currentNews);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void deleteNewsAndNotifications() {
        if (binding.progressBarDetail != null) binding.progressBarDetail.setVisibility(View.VISIBLE);

        // BƯỚC 1: Dọn dẹp ảnh bìa trên Storage (nếu có)
        String imageUrl = currentNews.getImageUrl();
        if (imageUrl != null && imageUrl.contains("firebasestorage.googleapis.com")) {
            try {
                // Gọi lệnh xóa ảnh (Fire & Forget - Không cần chờ kết quả để user đỡ phải đợi lâu)
                com.google.firebase.storage.FirebaseStorage.getInstance()
                        .getReferenceFromUrl(imageUrl)
                        .delete()
                        .addOnFailureListener(e -> android.util.Log.e("DELETE_IMG", "Lỗi xóa ảnh cũ: " + e.getMessage()));
            } catch (Exception e) {
                // Bỏ qua nếu link ảnh không đúng định dạng
            }
        }

        // BƯỚC 2: Xóa dữ liệu trong Firestore (Logic cũ giữ nguyên)
        db.collection("system_notifications")
                .whereEqualTo("newsId", currentNews.getId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    WriteBatch batch = db.batch();

                    // A. Xóa các thông báo liên quan
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                    }

                    // B. Xóa bài viết gốc
                    batch.delete(db.collection("news").document(currentNews.getId()));

                    // C. Thực thi lệnh xóa
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                // Ghi log hành động
                                com.example.supernews.data.manager.LogManager.getInstance()
                                        .log("DELETE", currentNews.getId(), currentNews.getTitle(), "Xóa bài viết vĩnh viễn");

                                Toast.makeText(this, "Đã xóa bài viết và ảnh đi kèm!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                if (binding.progressBarDetail != null) binding.progressBarDetail.setVisibility(View.GONE);
                                Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    if (binding.progressBarDetail != null) binding.progressBarDetail.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tìm dữ liệu liên quan", Toast.LENGTH_SHORT).show();
                });
    }
}