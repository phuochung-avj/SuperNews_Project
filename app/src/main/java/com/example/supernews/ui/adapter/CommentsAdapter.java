package com.example.supernews.ui.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.supernews.R;
import com.example.supernews.data.model.Comment;
import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<Comment> list = new ArrayList<>();

    public void setComments(List<Comment> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = list.get(position);

        // 1. Tên người dùng
        holder.tvUser.setText(comment.getUserName() != null ? comment.getUserName() : "Người dùng ẩn danh");

        // 2. Nội dung
        holder.tvContent.setText(comment.getContent());

        // 3. Thời gian
        if (comment.getTimestamp() != null) {
            long time = comment.getTimestamp().toDate().getTime();
            long now = System.currentTimeMillis();
            CharSequence ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            holder.tvTime.setText(ago);
        } else {
            holder.tvTime.setText("Vừa xong");
        }

        // 4. 🔥 LOGIC MỚI: Hiển thị Avatar
        String avatarUrl = comment.getAvatarUrl(); // Cần đảm bảo Model Comment có getter này

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .circleCrop() // Bo tròn ảnh
                    .placeholder(R.drawable.ic_launcher_background) // Ảnh chờ (nếu chưa tải xong)
                    .error(R.drawable.ic_launcher_background)       // Ảnh lỗi (nếu link hỏng)
                    .into(holder.imgAvatar);
        } else {
            // Nếu không có link ảnh -> Hiện ảnh mặc định
            holder.imgAvatar.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvContent, tvTime;
        ImageView imgAvatar;

        public CommentViewHolder(View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvCommentUser);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            tvTime = itemView.findViewById(R.id.tvCommentTime);
            imgAvatar = itemView.findViewById(R.id.imgCommentAvatar);
        }
    }
}