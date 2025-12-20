package com.example.supernews.ui.adapter;

import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.supernews.R;
import com.example.supernews.data.model.News;
import java.util.List;

public class RelatedAdapter extends RecyclerView.Adapter<RelatedAdapter.ViewHolder> {

    private List<News> list;
    private Context context;
    private NewsAdapter.OnNewsClickListener listener; // Tái sử dụng interface cũ

    public RelatedAdapter(Context context, List<News> list, NewsAdapter.OnNewsClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_related, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        News news = list.get(position);
        holder.tvTitle.setText(news.getTitle());

        // Xử lý ảnh (Link hoặc Base64)
        if (news.getImageUrl() != null) {
            if (news.getImageUrl().startsWith("http")) {
                Glide.with(context).load(news.getImageUrl()).centerCrop().into(holder.imgThumb);
            } else {
                try {
                    byte[] imageBytes = Base64.decode(news.getImageUrl(), Base64.DEFAULT);
                    Glide.with(context).load(imageBytes).centerCrop().into(holder.imgThumb);
                } catch (Exception e) {
                    holder.imgThumb.setImageResource(R.drawable.ic_launcher_background);
                }
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onNewsClick(news, holder.imgThumb));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.imgRelatedThumb);
            tvTitle = itemView.findViewById(R.id.tvRelatedTitle);
        }
    }
}