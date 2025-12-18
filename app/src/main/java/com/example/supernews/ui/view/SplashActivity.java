package com.example.supernews.ui.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.core.splashscreen.SplashScreen;
import androidx.appcompat.app.AppCompatActivity;

import com.example.supernews.MainActivity;
import com.example.supernews.ui.view.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(com.example.supernews.R.layout.activity_splash);

        // Delay 2 giây (2000ms) để hiện logo, sau đó mới kiểm tra
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                // Đã đăng nhập -> Vào thẳng Trang chủ
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // Chưa đăng nhập -> Vào màn hình Đăng nhập
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }

            // Đóng Splash để user không back lại được
            finish();

        }, 2000);
    }
}