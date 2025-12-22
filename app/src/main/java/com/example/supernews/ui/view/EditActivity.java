package com.example.supernews.ui.view;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.supernews.data.model.News;
import com.example.supernews.databinding.ActivityEditBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditActivity extends AppCompatActivity {

    private ActivityEditBinding binding;
    private News currentNews;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri imageUri; // Ảnh mới được chọn

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.imgPreview.setImageURI(uri);
                    binding.tvSelectImage.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nếu không phải Admin -> Đuổi ra ngay!
        if (!com.example.supernews.data.manager.UserManager.getInstance().isAdmin()) {
            Toast.makeText(this, "⛔ Bạn không có quyền chỉnh sửa!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- NẾU LÀ ADMIN THÌ MỚI CHẠY TIẾP ĐOẠN DƯỚI ---
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        currentNews = (News) getIntent().getSerializableExtra("news_to_edit");
        if (currentNews == null) { finish(); return; }

        setupSpinner();
        fillOldData();

        binding.cardViewImage.setOnClickListener(v -> pickImage.launch("image/*"));
        binding.btnUpdate.setOnClickListener(v -> processUpdate());
        binding.toolbarEdit.setNavigationOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        String[] categories = {"THỂ THAO", "KINH TẾ", "CÔNG NGHỆ", "THỜI SỰ", "GIẢI TRÍ", "SỨC KHỎE", "XE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void fillOldData() {
        binding.edtUploadTitle.setText(currentNews.getTitle());
        binding.edtUploadSummary.setText(currentNews.getSummary());
        binding.edtUploadContent.setText(currentNews.getContent());
        if (currentNews.getAuthor() != null) binding.edtAuthor.setText(currentNews.getAuthor());
        if (currentNews.getImageSource() != null) binding.edtImageSource.setText(currentNews.getImageSource());

        if ("international".equals(currentNews.getScope())) {
            binding.rbInternational.setChecked(true);
        } else {
            binding.rbDomestic.setChecked(true);
        }

        String category = currentNews.getSource();
        if (category != null) {
            ArrayAdapter adapter = (ArrayAdapter) binding.spinnerCategory.getAdapter();
            int position = adapter.getPosition(category);
            if (position >= 0) binding.spinnerCategory.setSelection(position);
        }

        if (currentNews.getImageUrl() != null && !currentNews.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentNews.getImageUrl())
                    .into(binding.imgPreview);
            binding.tvSelectImage.setVisibility(View.GONE);
        }
    }

    private void processUpdate() {
        binding.progressBarUpload.setVisibility(View.VISIBLE);
        binding.btnUpdate.setEnabled(false);

        if (imageUri != null) {
            // TRƯỜNG HỢP 1: NGƯỜI DÙNG CÓ CHỌN ẢNH MỚI
            String fileName = "news_images/" + UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storage.getReference().child(fileName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // A. Lấy được link ảnh MỚI
                            String newImageUrl = uri.toString();
                            // B. Xóa ảnh CŨ đi (Dọn rác) -> QUAN TRỌNG
                            deleteOldImage(currentNews.getImageUrl());
                            // C. Cập nhật Database với link mới
                            updateFirestore(newImageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBarUpload.setVisibility(View.GONE);
                        binding.btnUpdate.setEnabled(true);
                        Toast.makeText(this, "Lỗi upload ảnh mới: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // TRƯỜNG HỢP 2: KHÔNG ĐỔI ẢNH
            // -> Giữ nguyên, chỉ cập nhật nội dung chữ
            updateFirestore(null);
        }
    }
    // --- HÀM MỚI: XÓA ẢNH CŨ KHỎI STORAGE ---
    private void deleteOldImage(String oldImageUrl) {
        if (oldImageUrl == null || oldImageUrl.isEmpty()) return;
        // Chỉ xóa nếu ảnh đó nằm trên Firebase Storage của mình
        if (oldImageUrl.contains("firebasestorage.googleapis.com")) {
            try {
                // Tạo tham chiếu từ URL và xóa
                storage.getReferenceFromUrl(oldImageUrl).delete()
                        .addOnSuccessListener(aVoid -> android.util.Log.d("CLEANUP", "Đã dọn dẹp ảnh cũ thành công"))
                        .addOnFailureListener(e -> android.util.Log.e("CLEANUP", "Lỗi dọn ảnh cũ: " + e.getMessage()));
            } catch (Exception e) {
                android.util.Log.e("CLEANUP", "URL ảnh cũ không hợp lệ: " + e.getMessage());
            }
        }
    }

    private void updateFirestore(String newImageUrl) {
        String title = binding.edtUploadTitle.getText().toString().trim();
        String summary = binding.edtUploadSummary.getText().toString().trim();
        String content = binding.edtUploadContent.getText().toString().trim();
        String category = binding.spinnerCategory.getSelectedItem().toString();
        String author = binding.edtAuthor.getText().toString().trim();
        String imgSource = binding.edtImageSource.getText().toString().trim();
        String scope = binding.rbInternational.isChecked() ? "international" : "domestic";

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("summary", summary);
        updates.put("content", content);
        updates.put("source", category);
        updates.put("scope", scope);
        updates.put("author", author);
        updates.put("imageSource", imgSource);

        if (newImageUrl != null) {
            updates.put("imageUrl", newImageUrl);
        }

        db.collection("news").document(currentNews.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // 🔥 GHI LOG (Gộp vào đây)
                    com.example.supernews.data.manager.LogManager.getInstance()
                            .log("UPDATE", currentNews.getId(), title, "Chỉnh sửa nội dung bài viết");

                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBarUpload.setVisibility(View.GONE);
                    binding.btnUpdate.setEnabled(true);
                    Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}