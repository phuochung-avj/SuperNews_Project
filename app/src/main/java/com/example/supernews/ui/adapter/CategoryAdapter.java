package com.example.supernews.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.supernews.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private Context context;
    private List<String> categories;
    private OnCategoryClick listener;

    // Biến quan trọng: Lưu vị trí nút đang được chọn (Mặc định là 0 - Mới nhất)
    private int selectedPosition = 0;

    public interface OnCategoryClick {
        void onCategoryClick(String category);
    }

    public CategoryAdapter(Context context, List<String> categories, OnCategoryClick listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);
        holder.tvCategory.setText(category);

        // --- LOGIC ĐỔI MÀU ---
        if (selectedPosition == position) {
            // TRẠNG THÁI ĐƯỢC CHỌN:
            // Nền: Màu chủ đạo (Xanh)
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.purple_500));
            // Chữ: Màu Trắng
            holder.tvCategory.setTextColor(Color.WHITE);
            // Stroke (Viền): Không có
            holder.cardView.setStrokeWidth(0);
        } else {
            // TRẠNG THÁI BÌNH THƯỜNG:
            // Nền: Theo giao diện Sáng/Tối (content_background)
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.content_background));
            // Chữ: Theo giao diện Sáng/Tối (text_primary)
            holder.tvCategory.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            // Stroke (Viền): Mỏng màu xám cho đẹp
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.divider));            holder.cardView.setStrokeWidth(2); // 2px
        }

        // --- SỰ KIỆN CLICK ---
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Cập nhật giao diện: Chỉ vẽ lại nút cũ và nút mới bấm (Tối ưu hiệu năng)
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            // Gửi tên chuyên mục ra ngoài Fragment để lọc tin
            listener.onCategoryClick(category);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategoryName);
            cardView = (MaterialCardView) itemView;
        }
    }
}