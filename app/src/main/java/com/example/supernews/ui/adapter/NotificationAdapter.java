package com.example.supernews.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.supernews.R;
import com.example.supernews.data.model.SystemNotification;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotiViewHolder> {

    private Context context;
    private List<SystemNotification> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SystemNotification notification);
    }

    public NotificationAdapter(Context context, List<SystemNotification> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotiViewHolder holder, int position) {
        SystemNotification item = list.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvBody.setText(item.getBody());

        // Format thời gian (Giữ nguyên code cũ của bạn)
        if (item.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());
            holder.tvTime.setText(sdf.format(item.getTimestamp().toDate()));
        }

        // 🔥 PHẦN QUAN TRỌNG NHẤT: PHẢI CÓ CẢ IF VÀ ELSE 🔥
        if (item.isRead()) {
            // TRƯỜNG HỢP ĐÃ ĐỌC:
            // 1. Phải set lại nền màu TRẮNG
            holder.layoutRoot.setBackgroundColor(Color.WHITE);
            // 2. Chữ thường
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
            // 3. Ẩn chấm đỏ
            if (holder.imgDot != null) holder.imgDot.setVisibility(View.GONE);
        } else {
            // TRƯỜNG HỢP CHƯA ĐỌC:
            // 1. Set nền màu XANH NHẠT
            holder.layoutRoot.setBackgroundColor(Color.parseColor("#E3F2FD"));
            // 2. Chữ đậm
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
            // 3. Hiện chấm đỏ
            if (holder.imgDot != null) holder.imgDot.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class NotiViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBody, tvTime;
        LinearLayout layoutRoot;
        ImageView imgDot;

        public NotiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotiTitle);
            tvBody = itemView.findViewById(R.id.tvNotiBody);
            tvTime = itemView.findViewById(R.id.tvNotiTime);
            layoutRoot = itemView.findViewById(R.id.layoutItemNoti);
            imgDot = itemView.findViewById(R.id.imgUnreadDot);
        }
    }
}