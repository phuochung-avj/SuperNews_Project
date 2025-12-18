package com.example.supernews.data.model;

import java.io.Serializable;

// Serializable giúp truyền object này qua lại giữa các màn hình (Home -> Detail) bằng Intent
public class News implements Serializable {

    // --- 1. KHAI BÁO BIẾN (FIELDS) ---
    private String id;
    private String title;       // Tiêu đề
    private String summary;     // Tóm tắt
    private String content;     // Nội dung chi tiết
    private String imageUrl;    // Link ảnh (hoặc chuỗi Base64)
    private String source;      // Nguồn/Chuyên mục (VD: Thể thao, Kinh tế)
    private String publishedAt; // Ngày đăng

    // Các trường bổ sung (Mới thêm)
    private String author;      // Tác giả
    private String imageSource; // Nguồn ảnh hoặc chú thích ảnh (VD: Ảnh: TTXVN)
    private String scope; // Giá trị sẽ là: "domestic" (Trong nước) hoặc "international" (Quốc tế)

    // Các trường số liệu
    private long views;         // Lượt xem
    private long likes;         // Lượt thích

    // --- 2. CONSTRUCTOR (HÀM KHỞI TẠO) ---

    // Constructor rỗng: BẮT BUỘC PHẢI CÓ để Firebase Firestore map dữ liệu về
    public News() {
    }

    // Constructor đầy đủ (Option, có thể dùng hoặc không)
    public News(String id, String title, String summary, String content, String imageUrl, String source, String publishedAt, String author, String imageSource) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.imageUrl = imageUrl;
        this.source = source;
        this.publishedAt = publishedAt;
        this.author = author;
        this.imageSource = imageSource;
    }

    // --- 3. GETTER & SETTER ---
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getImageSource() { return imageSource; }
    public void setImageSource(String imageSource) { this.imageSource = imageSource; }

    public long getViews() { return views; }
    public void setViews(long views) { this.views = views; }

    public long getLikes() { return likes; }
    public void setLikes(long likes) { this.likes = likes; }
}