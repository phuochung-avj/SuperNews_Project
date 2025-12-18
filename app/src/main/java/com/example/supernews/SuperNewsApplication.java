package com.example.supernews;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class SuperNewsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Kiểm tra cài đặt Dark Mode ngay khi App khởi động
        SharedPreferences prefs = getSharedPreferences("SuperNewsSettings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);

        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}