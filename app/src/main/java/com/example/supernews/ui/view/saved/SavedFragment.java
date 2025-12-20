package com.example.supernews.ui.view.saved;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.supernews.data.manager.BookmarkManager;
import com.example.supernews.data.model.News;
import com.example.supernews.databinding.FragmentSavedBinding;
import com.example.supernews.ui.adapter.NewsAdapter;
import com.example.supernews.ui.view.detail.DetailActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment {

    private FragmentSavedBinding binding;
    private NewsAdapter adapter;
    private List<News> savedList;

    // Các biến phục vụ tính năng Vuốt để Xóa
    private News deletedNews = null;
    private final ColorDrawable swipeBackground = new ColorDrawable(Color.parseColor("#D32F2F")); // Nền đỏ
    private Drawable deleteIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSavedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Chuẩn bị icon thùng rác
        deleteIcon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete);
        if (deleteIcon != null) {
            deleteIcon.setTint(Color.WHITE);
        }

        savedList = new ArrayList<>();

        // 2. Cấu hình RecyclerView
        setupRecyclerView();

        // 3. Tải dữ liệu
        loadSavedNews();
    }

    private void setupRecyclerView() {
        adapter = new NewsAdapter(requireContext(), savedList, (news, imageView) -> {
            Intent intent = new Intent(requireContext(), DetailActivity.class);
            intent.putExtra("object_news", news);

            android.app.ActivityOptions options = android.app.ActivityOptions
                    .makeSceneTransitionAnimation(requireActivity(), imageView, "news_image_transition");

            startActivity(intent, options.toBundle());
        });

        binding.rvSavedNews.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSavedNews.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.rvSavedNews);
    }

    // --- LOGIC VUỐT ĐỂ XÓA ---
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder v, @NonNull RecyclerView.ViewHolder t) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            deletedNews = savedList.get(position);
            String newsId = deletedNews.getId();

            savedList.remove(position);
            adapter.notifyItemRemoved(position);
            checkEmptyState();

            BookmarkManager.getInstance().removeBookmark(newsId, new BookmarkManager.BookmarkCallback() {
                @Override
                public void onSuccess(boolean isSaved) {
                    showUndoSnackbar(position);
                }

                @Override
                public void onFailure(String error) {
                    savedList.add(position, deletedNews);
                    adapter.notifyItemInserted(position);
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder v, float dX, float dY, int actionState, boolean isActive) {
            View itemView = v.itemView;
            int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;

            if (dX < 0) {
                swipeBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                swipeBackground.draw(c);

                int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;

                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                deleteIcon.draw(c);
            }
            super.onChildDraw(c, r, v, dX, dY, actionState, isActive);
        }
    };

    private void showUndoSnackbar(int position) {
        if (binding == null) return; // Check binding

        Snackbar.make(binding.rvSavedNews, "Đã xóa tin khỏi danh sách", Snackbar.LENGTH_LONG)
                .setAction("Hoàn tác", v -> {
                    BookmarkManager.getInstance().addBookmark(deletedNews, new BookmarkManager.BookmarkCallback() {
                        @Override
                        public void onSuccess(boolean isSaved) {
                            if (!savedList.contains(deletedNews)) {
                                savedList.add(position, deletedNews);
                                adapter.notifyItemInserted(position);
                                checkEmptyState();
                            }
                        }
                        @Override
                        public void onFailure(String error) {}
                    });
                })
                .setActionTextColor(Color.YELLOW)
                .show();
    }

    private void loadSavedNews() {
        if (binding == null) return;
        binding.progressBarSaved.setVisibility(View.VISIBLE);

        com.google.firebase.firestore.Query query = BookmarkManager.getInstance().getBookmarksQuery();

        if (query == null) {
            binding.progressBarSaved.setVisibility(View.GONE);
            return;
        }

        query.addSnapshotListener((value, error) -> {
            // 🔥 [QUAN TRỌNG] KHIÊN CHẮN CHỐNG CRASH 🔥
            // Nếu Fragment đã bị hủy hoặc binding null -> Dừng ngay, không làm gì cả
            if (binding == null || !isAdded()) {
                return;
            }

            if (error != null) {
                binding.progressBarSaved.setVisibility(View.GONE);
                return;
            }

            if (value != null) {
                savedList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    News news = doc.toObject(News.class);
                    if (news.getId() == null) news.setId(doc.getId());
                    savedList.add(news);
                }
                adapter.notifyDataSetChanged();
                binding.progressBarSaved.setVisibility(View.GONE);
                checkEmptyState();
            }
        });
    }

    private void checkEmptyState() {
        // Thêm check binding để an toàn tuyệt đối
        if (binding == null) return;

        if (savedList.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.rvSavedNews.setVisibility(View.GONE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.rvSavedNews.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Set null để giải phóng bộ nhớ
    }
}