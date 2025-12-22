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

        // 1. Tên người dùng (In đậm)
        holder.tvUser.setText(comment.getUserName() != null ? comment.getUserName() : "Người dùng ẩn danh");

        // 2. Nội dung bình luận
        holder.tvContent.setText(comment.getContent());

        // 3. Xử lý thời gian
        if (comment.getTimestamp() != null) {
            long time = comment.getTimestamp().toDate().getTime();
            long now = System.currentTimeMillis();
            // Dùng hàm có sẵn của Android để tính khoảng cách thời gian
            CharSequence ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            holder.tvTime.setText(ago);
        } else {
            holder.tvTime.setText("Vừa xong");
        }

        // 4. Avatar
        holder.imgAvatar.setImageResource(R.mipmap.ic_launcher_round);
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