package com.example.supernews.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.supernews.R;
import com.example.supernews.data.model.AdminLog;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminLogAdapter extends RecyclerView.Adapter<AdminLogAdapter.LogViewHolder> {

    private List<AdminLog> list;
    private OnItemClickListener listener;

    // Interface để gửi sự kiện click ra ngoài Activity
    public interface OnItemClickListener {
        void onItemClick(AdminLog log);
    }

    public AdminLogAdapter(List<AdminLog> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        AdminLog log = list.get(position);

        // 1. Hiển thị thời gian
        if (log.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvTime.setText(sdf.format(log.getTimestamp().toDate()));
        }

        // 2. Hiển thị nội dung tóm tắt
        holder.tvContent.setText(log.getDetails() + ": " + log.getTargetTitle());
        holder.tvAdmin.setText("Bởi: " + log.getAdminName());

        // 3. Tô màu hành động (Logic quan trọng)
        String action = log.getAction();
        holder.tvAction.setText(action);

        if ("CREATE".equals(action)) {
            holder.tvAction.setBackgroundColor(Color.parseColor("#4CAF50")); // Xanh lá
        } else if ("UPDATE".equals(action)) {
            holder.tvAction.setBackgroundColor(Color.parseColor("#FF9800")); // Cam
        } else if ("DELETE".equals(action)) {
            holder.tvAction.setBackgroundColor(Color.parseColor("#F44336")); // Đỏ
        } else {
            holder.tvAction.setBackgroundColor(Color.GRAY);
        }

        // 4. Bắt sự kiện Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(log);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvAction, tvTime, tvContent, tvAdmin;
        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAction = itemView.findViewById(R.id.tvLogAction);
            tvTime = itemView.findViewById(R.id.tvLogTime);
            tvContent = itemView.findViewById(R.id.tvLogContent);
            tvAdmin = itemView.findViewById(R.id.tvLogAdmin);
        }
    }
}