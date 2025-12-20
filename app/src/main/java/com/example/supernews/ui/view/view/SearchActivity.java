package com.example.supernews.ui.view.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.supernews.data.model.News;
import com.example.supernews.databinding.ActivitySearchBinding;
import com.example.supernews.ui.adapter.NewsAdapter;
import com.example.supernews.ui.view.detail.DetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private FirebaseFirestore db;
    private NewsAdapter newsAdapter;

    // Danh sách gốc (chứa tất cả bài viết)
    private List<News> originalList;
    // Danh sách hiển thị (kết quả tìm kiếm)
    private List<News> searchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        originalList = new ArrayList<>();
        searchList = new ArrayList<>();

        setupRecyclerView();

        // Load toàn bộ dữ liệu ngay khi mở màn hình
        loadAllNewsData();

        binding.btnBackSearch.setOnClickListener(v -> finish());

        // Cấu hình ô tìm kiếm
        setupSearchView();
    }

    private void setupRecyclerView() {
        newsAdapter = new NewsAdapter(this, searchList, (news, imageView) -> {
            Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
            intent.putExtra("object_news", news);
            android.app.ActivityOptions options = android.app.ActivityOptions
                    .makeSceneTransitionAnimation(this, imageView, "news_image_transition");
            startActivity(intent, options.toBundle());
        });

        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSearchResults.setAdapter(newsAdapter);
    }

    // --- BƯỚC 1: TẢI TOÀN BỘ DỮ LIỆU VỀ 1 LẦN DUY NHẤT ---
    private void loadAllNewsData() {
        binding.progressBarSearch.setVisibility(View.VISIBLE);

        db.collection("news")
                .orderBy("publishedAt", com.google.firebase.firestore.Query.Direction.DESCENDING) // Lấy tin mới nhất trước
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    originalList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        News news = doc.toObject(News.class);
                        news.setId(doc.getId());
                        originalList.add(news);
                    }
                    binding.progressBarSearch.setVisibility(View.GONE);

                    // Mở bàn phím ngay sau khi load xong (Tùy chọn)
                    binding.searchView.requestFocus();
                    showKeyboard();
                })
                .addOnFailureListener(e -> {
                    binding.progressBarSearch.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterLocal(query);
                hideKeyboard();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Vì lọc tại chỗ rất nhanh, ta không cần Debounce (Handler) nữa
                // Gõ đến đâu lọc đến đó -> Trải nghiệm cực mượt
                filterLocal(newText);
                return true;
            }
        });
    }

    // --- BƯỚC 2: THUẬT TOÁN LỌC THÔNG MINH (CLIENT SIDE) ---
    private void filterLocal(String query) {
        String lowerCaseQuery = query.toLowerCase(Locale.getDefault()).trim();
        searchList.clear();

        if (lowerCaseQuery.isEmpty()) {
            // Nếu ô tìm kiếm rỗng -> Không hiện gì (hoặc hiện gợi ý nếu muốn)
            binding.layoutNoResult.setVisibility(View.GONE);
            binding.rvSearchResults.setVisibility(View.GONE);
        } else {
            for (News news : originalList) {
                // 1. Chuẩn hóa dữ liệu về chữ thường
                String title = news.getTitle() != null ? news.getTitle().toLowerCase() : "";
                String summary = news.getSummary() != null ? news.getSummary().toLowerCase() : "";
                String source = news.getSource() != null ? news.getSource().toLowerCase() : "";

                // 2. Kiểm tra: Tìm trong Tiêu đề HOẶC Tóm tắt HOẶC Chuyên mục
                if (title.contains(lowerCaseQuery) || summary.contains(lowerCaseQuery) || source.contains(lowerCaseQuery)) {
                    searchList.add(news);
                }
            }

            // Cập nhật giao diện
            if (searchList.isEmpty()) {
                binding.layoutNoResult.setVisibility(View.VISIBLE);
                binding.rvSearchResults.setVisibility(View.GONE);
            } else {
                binding.layoutNoResult.setVisibility(View.GONE);
                binding.rvSearchResults.setVisibility(View.VISIBLE);
            }
        }
        newsAdapter.notifyDataSetChanged();
    }

    // Tiện ích: Ẩn/Hiện bàn phím
    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(binding.searchView, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}