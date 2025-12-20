package com.example.supernews.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.supernews.R;
import com.example.supernews.data.model.User;

import java.util.List;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.UserViewHolder> {

    private Context context;
    private List<User> list;
    private OnRoleToggleListener listener;

    public interface OnRoleToggleListener {
        void onToggleRole(User user);
    }

    public UserManagementAdapter(Context context, List<User> list, OnRoleToggleListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_manage, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = list.get(position);

        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());

        // Load Avatar
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            Glide.with(context).load(user.getAvatar()).circleCrop().into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.mipmap.ic_launcher_round);
        }

        // --- LOGIC HIỂN THỊ QUYỀN ---
        boolean isAdmin = User.ROLE_ADMIN.equals(user.getRole());

        if (isAdmin) {
            // Nếu đang là Admin
            holder.tvLabelAdmin.setVisibility(View.VISIBLE);
            holder.btnToggle.setText("Hủy quyền");
            holder.btnToggle.setBackgroundColor(Color.parseColor("#F44336")); // Màu Đỏ
        } else {
            // Nếu là Member
            holder.tvLabelAdmin.setVisibility(View.GONE);
            holder.btnToggle.setText("Thăng chức");
            holder.btnToggle.setBackgroundColor(Color.parseColor("#4CAF50")); // Màu Xanh lá
        }

        holder.btnToggle.setOnClickListener(v -> listener.onToggleRole(user));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvEmail, tvLabelAdmin;
        Button btnToggle;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgUserAvatar);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvLabelAdmin = itemView.findViewById(R.id.tvLabelAdmin);
            btnToggle = itemView.findViewById(R.id.btnToggleRole);
        }
    }
}