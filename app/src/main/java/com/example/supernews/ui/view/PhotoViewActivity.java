package com.example.supernews.ui.view;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.supernews.R;
import com.github.chrisbanes.photoview.PhotoView;

public class PhotoViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        String imageUrl = getIntent().getStringExtra("image_url");
        PhotoView photoView = findViewById(R.id.photo_view);

        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(photoView);
        }
        findViewById(R.id.btnClosePhoto).setOnClickListener(v -> finish());
    }
}