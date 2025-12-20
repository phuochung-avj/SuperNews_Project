package com.example.supernews.ui.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.supernews.R;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchDarkMode;
    private SeekBar seekBarFontSize;
    private TextView tvPreviewFont;
    private Button btnChangePassword;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Setup Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Ánh xạ
        switchDarkMode = findViewById(R.id.switchDarkMode);
        seekBarFontSize = findViewById(R.id.seekBarFontSize);
        tvPreviewFont = findViewById(R.id.tvPreviewFont);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        prefs = getSharedPreferences("SuperNewsSettings", MODE_PRIVATE);

        setupDarkMode();
        setupFontSize();

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    private void setupDarkMode() {
        // 1. Đọc trạng thái cũ
        boolean isDark = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDark);

        // 2. Lắng nghe thay đổi
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private void setupFontSize() {
        // Mặc định là 16sp (tương ứng progress = 2 trong công thức 12 + i*2)
        float currentSize = prefs.getFloat("content_font_size", 16f);
        int progress = (int) ((currentSize - 12) / 2);

        seekBarFontSize.setProgress(progress);
        tvPreviewFont.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentSize);

        seekBarFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                // Công thức: Min 12sp, mỗi nấc tăng 2sp
                float newSize = 12 + (i * 2);
                tvPreviewFont.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize);

                // Lưu vào bộ nhớ
                prefs.edit().putFloat("content_font_size", newSize).apply();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}