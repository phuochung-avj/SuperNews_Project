package com.example.supernews.ui.view.view.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.supernews.databinding.FragmentNewsBinding;
import com.example.supernews.ui.adapter.CategoryAdapter;
import com.example.supernews.ui.adapter.NewsAdapter;
import com.example.supernews.ui.view.detail.DetailActivity;
import com.example.supernews.ui.viewmodel.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class NewsFragment extends Fragment {

    private FragmentNewsBinding binding;
    private HomeViewModel viewModel;

    // Adapter cho danh sách tin
    private NewsAdapter newsAdapter;
    // Adapter cho thanh chuyên mục ngang
    private CategoryAdapter categoryAdapter;

    // Biến lưu trạng thái lọc
    private String category = "Mới nhất"; // Mặc định
    private String scope = null;          // "domestic" hoặc "international"

    // Hàm khởi tạo Fragment với tham số scope (Trong nước/Quốc tế)
    public static NewsFragment newInstance(String category, String scope) {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString("category_name", category);
        args.putString("scope_type", scope); // Lưu scope vào bundle
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNewsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Lấy dữ liệu từ Bundle (nếu có)
        if (getArguments() != null) {
            if (getArguments().containsKey("category_name")) {
                category = getArguments().getString("category_name");
            }
            if (getArguments().containsKey("scope_type")) {
                scope = getArguments().getString("scope_type");
            }
        }

        // 2. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // 3. Setup giao diện (Thanh chuyên mục + Danh sách tin)
        setupCategoryRecycler();
        setupNewsRecycler();

        // 4. Setup SwipeRefresh (Kéo để làm mới)
        binding.swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadNews(category, scope));

        // 5. Lắng nghe dữ liệu từ ViewModel
        observeViewModel();

        // 6. Gọi tải tin tức lần đầu
        viewModel.loadNews(category, scope);
    }

    // --- SETUP THANH CHUYÊN MỤC NGANG ---
    private void setupCategoryRecycler() {
        // Danh sách chuyên mục
        List<String> categories = new ArrayList<>();
        categories.add("Mới nhất");
        categories.add("THỂ THAO");
        categories.add("KINH TẾ");
        categories.add("CÔNG NGHỆ");
        categories.add("THỜI SỰ");
        categories.add("GIẢI TRÍ");
        categories.add("SỨC KHỎE");
        categories.add("XE");

        categoryAdapter = new CategoryAdapter(requireContext(), categories, selectedCategory -> {
            this.category = selectedCategory;
            viewModel.loadNews(selectedCategory, scope);
            binding.rvNews.scrollToPosition(0);
        });

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);

        // --- THÊM ĐOẠN CODE NÀY ĐỂ CHỐNG TRÔI TAB ---
        binding.rvCategories.addOnItemTouchListener(new androidx.recyclerview.widget.RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull androidx.recyclerview.widget.RecyclerView rv, @NonNull android.view.MotionEvent e) {
                int action = e.getAction();
                switch (action) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        // Khi ngón tay chạm vào list chuyên mục -> Cấm cha (ViewPager) can thiệp
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        // Khi thả tay ra -> Cho phép cha hoạt động lại bình thường
                        rv.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }

            @Override public void onTouchEvent(@NonNull androidx.recyclerview.widget.RecyclerView rv, @NonNull android.view.MotionEvent e) {}
            @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);
    }

    // --- SETUP DANH SÁCH TIN ---
    private void setupNewsRecycler() {
        // Khởi tạo Adapter với logic chuyển cảnh (Shared Element Transition)
        newsAdapter = new NewsAdapter(requireContext(), new ArrayList<>(), (news, imageView) -> {
            Intent intent = new Intent(requireContext(), DetailActivity.class);
            intent.putExtra("object_news", news);

            // Tạo hiệu ứng phóng to ảnh mượt mà
            android.app.ActivityOptions options = android.app.ActivityOptions
                    .makeSceneTransitionAnimation(requireActivity(), imageView, "news_image_transition");

            startActivity(intent, options.toBundle());
        });

        // Cấu hình RecyclerView dọc
        binding.rvNews.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNews.setAdapter(newsAdapter);
    }

    private void observeViewModel() {
        // Lắng nghe danh sách tin
        viewModel.getNewsList().observe(getViewLifecycleOwner(), news -> {
            if (news != null) {
                newsAdapter.setNewsList(news);
            }
        });

        // Lắng nghe trạng thái Loading để bật/tắt Shimmer
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                // Nếu đang kéo refresh thì không hiện shimmer đè lên
                if (!binding.swipeRefresh.isRefreshing()) {
                    showShimmer();
                }
            } else {
                hideShimmer();
                binding.swipeRefresh.setRefreshing(false);
            }
        });

        // Lắng nghe báo lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                // Bỏ qua lỗi Index của Firebase để tránh spam Toast (vì ta đã xử lý tạo index rồi)
                if (!message.contains("FAILED_PRECONDITION")) {
                    Toast.makeText(requireContext(), "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Hàm hiện hiệu ứng xương
    private void showShimmer() {
        binding.shimmerViewContainer.setVisibility(View.VISIBLE);
        binding.shimmerViewContainer.startShimmer();
        binding.rvNews.setVisibility(View.GONE);
    }

    // Hàm tắt hiệu ứng xương
    private void hideShimmer() {
        binding.shimmerViewContainer.stopShimmer();
        binding.shimmerViewContainer.setVisibility(View.GONE);
        binding.rvNews.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        binding.shimmerViewContainer.stopShimmer();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh rò rỉ bộ nhớ
    }

    @Override
    public void onResume() {
        super.onResume();
        // Kiểm tra: Nếu danh sách đang trống thì tự load lại
        if (newsAdapter.getItemCount() == 0) {
            viewModel.loadNews(category, scope);
        }
    }
}