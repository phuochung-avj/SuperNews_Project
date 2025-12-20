package com.example.supernews.ui.view.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.supernews.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Nút Back
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Gửi
        binding.btnReset.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = binding.edtEmail.getText().toString().trim();

        // 1. Kiểm tra Email hợp lệ
        if (email.isEmpty()) {
            binding.tilEmail.setError("Vui lòng nhập Email");
            binding.tilEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Email không đúng định dạng");
            binding.tilEmail.requestFocus();
            return;
        }

        binding.tilEmail.setError(null); // Xóa lỗi cũ
        showLoading(true);

        // 2. Gửi yêu cầu lên Firebase
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đã gửi link khôi phục vào Email của bạn. Hãy kiểm tra hộp thư (kể cả mục Spam).", Toast.LENGTH_LONG).show();
                        finish(); // Đóng màn hình này để quay lại Login
                    } else {
                        String errorMsg = "Lỗi gửi yêu cầu.";
                        try {
                            throw task.getException();
                        } catch (com.google.firebase.auth.FirebaseAuthInvalidUserException e) {
                            errorMsg = "Email này chưa đăng ký tài khoản.";
                        } catch (Exception e) {
                            errorMsg = "Lỗi: " + e.getMessage();
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean isShow) {
        binding.progressBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
        binding.btnReset.setEnabled(!isShow);
    }
}