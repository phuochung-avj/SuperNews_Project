package com.example.supernews.ui.view;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.supernews.data.model.News;
import com.example.supernews.databinding.ActivityUploadBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {

    private ActivityUploadBinding binding;
    private Uri imageUri;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // Bộ chọn ảnh
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
        // Kiểm tra ngay khi vừa mở màn hình. Nếu không phải Admin -> Đóng ngay lập tức.
        if (!com.example.supernews.data.manager.UserManager.getInstance().isAdmin()) {
            Toast.makeText(this, "⛔ Bạn không có quyền đăng bài!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- NẾU LÀ ADMIN THÌ MỚI CHẠY TIẾP ĐOẠN DƯỚI ---
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        setupSpinner();

        // Tự điền tác giả
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null) {
            binding.edtAuthor.setText(user.getDisplayName());
        } else {
            binding.edtAuthor.setText("Ban biên tập SuperNews");
        }

        binding.cardViewImage.setOnClickListener(v -> pickImage.launch("image/*"));
        binding.btnUpload.setOnClickListener(v -> startUploadProcess());
    }

    private void setupSpinner() {
        String[] categories = {"THỂ THAO", "KINH TẾ", "CÔNG NGHỆ", "THỜI SỰ", "GIẢI TRÍ", "SỨC KHỎE", "XE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void startUploadProcess() {
        String title = binding.edtUploadTitle.getText().toString().trim();
        String content = binding.edtUploadContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề và nội dung!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh bìa!", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBarUpload.setVisibility(View.VISIBLE);
        binding.btnUpload.setEnabled(false);

        // 1. Upload ảnh lên Storage
        String fileName = "news_images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storage.getReference().child(fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // 2. Lấy link ảnh
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveNewsToFirestore(imageUrl); // Có link rồi mới lưu tin
                    });
                })
                .addOnFailureListener(e -> {
                    binding.progressBarUpload.setVisibility(View.GONE);
                    binding.btnUpload.setEnabled(true);
                    Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveNewsToFirestore(String imageUrl) {
        String title = binding.edtUploadTitle.getText().toString().trim();
        String summary = binding.edtUploadSummary.getText().toString().trim();
        String content = binding.edtUploadContent.getText().toString().trim();
        String category = binding.spinnerCategory.getSelectedItem().toString();
        String author = binding.edtAuthor.getText().toString().trim();
        String imgSource = binding.edtImageSource.getText().toString().trim();

        String scope = "domestic";
        if (binding.rbInternational.isChecked()) scope = "international";

        if (summary.isEmpty()) summary = content.length() > 100 ? content.substring(0, 100) + "..." : content;
        if (imgSource.isEmpty()) imgSource = "Nguồn: Internet";
        if (author.isEmpty()) author = "Ban biên tập";

        News news = new News();
        news.setTitle(title);
        news.setSummary(summary);
        news.setContent(content);
        news.setSource(category);
        news.setScope(scope);
        news.setImageUrl(imageUrl);
        news.setViews(0);
        news.setLikes(0);
        news.setAuthor(author);
        news.setImageSource(imgSource);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        news.setPublishedAt(sdf.format(new Date()));

        db.collection("news").add(news)
                .addOnSuccessListener(documentReference -> {
                    String newsId = documentReference.getId();
                    db.collection("news").document(newsId).update("id", newsId);

                    // 🔥 GHI LOG (Thêm vào đây)
                    com.example.supernews.data.manager.LogManager.getInstance()
                            .log("CREATE", newsId, news.getTitle(), "Đăng bài viết mới");

                    Toast.makeText(this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Chỉ gọi finish() 1 lần ở đây
                })
                .addOnFailureListener(e -> {
                    binding.progressBarUpload.setVisibility(View.GONE);
                    binding.btnUpload.setEnabled(true);
                    Toast.makeText(this, "Lỗi lưu tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }
}