package com.example.supernews.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.supernews.R;
import com.example.supernews.data.model.News;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<News> newsList;
    private OnNewsClickListener listener;

    public interface OnNewsClickListener {
        void onNewsClick(News news, ImageView sharedImageView);
    }

    public NewsAdapter(Context context, List<News> newsList, OnNewsClickListener listener) {
        this.context = context;
        this.newsList = newsList;
        this.listener = listener;
    }

    public void setNewsList(List<News> newsList) {
        this.newsList = newsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);

        holder.tvTitle.setText(news.getTitle());
        holder.tvDate.setText(news.getPublishedAt());
        holder.tvViews.setText(news.getViews() + " lượt xem");
        // Kiểm tra và hiển thị nguồn tin
        if (news.getSource() != null) {
            holder.tvSource.setText(news.getSource());
        } else {
            holder.tvSource.setText("TIN TỨC"); // Giá trị dự phòng
        }
        // Load ảnh từ Storage URL
        if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(news.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(holder.imgNews);
        } else {
            holder.imgNews.setImageResource(R.mipmap.ic_launcher);
        }

        holder.itemView.setOnClickListener(v -> listener.onNewsClick(news, holder.imgNews));
    }

    @Override
    public int getItemCount() {
        return newsList != null ? newsList.size() : 0;
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        // Khai báo biến
        ImageView imgNews;
        TextView tvTitle, tvDate, tvViews;
        TextView tvSource;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ đúng ID từ file item_news.xml
            imgNews = itemView.findViewById(R.id.imgThumbnail); // ID trong xml là imgThumbnail
            tvTitle = itemView.findViewById(R.id.tvTitle);      // ID trong xml là tvTitle
            tvDate = itemView.findViewById(R.id.tvDate);        // ID trong xml là tvDate
            tvViews = itemView.findViewById(R.id.tvViews);
            tvSource = itemView.findViewById(R.id.tvSource);
        }
    }
}