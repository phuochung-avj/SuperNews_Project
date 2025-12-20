package com.example.supernews.ui.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.supernews.databinding.ActivityChangePasswordBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = FirebaseAuth.getInstance().getCurrentUser();

        setupToolbar();
        checkProvider(); // Kiểm tra loại tài khoản

        binding.btnConfirmChange.setOnClickListener(v -> performChangePassword());
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarChangePass);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbarChangePass.setNavigationOnClickListener(v -> finish());
    }

    private void checkProvider() {
        // Kiểm tra xem user có đăng nhập bằng Google không
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                if (profile.getProviderId().equals("google.com")) {
                    // Nếu là Google -> Khóa hết chức năng
                    binding.edtOldPass.setEnabled(false);
                    binding.edtNewPass.setEnabled(false);
                    binding.edtConfirmPass.setEnabled(false);
                    binding.btnConfirmChange.setEnabled(false);
                    binding.btnConfirmChange.setText("Tài khoản Google không thể đổi mật khẩu");
                    Toast.makeText(this, "Bạn đang dùng Google, hãy đổi mật khẩu tại cài đặt Google", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }

    private void performChangePassword() {
        String oldPass = binding.edtOldPass.getText().toString().trim();
        String newPass = binding.edtNewPass.getText().toString().trim();
        String confirmPass = binding.edtConfirmPass.getText().toString().trim();

        // 1. Validate dữ liệu
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải từ 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBarChangePass.setVisibility(View.VISIBLE);
        binding.btnConfirmChange.setEnabled(false);

        // 2. Xác thực lại (Re-authenticate) - BẮT BUỘC
        // Tạo credential từ email và mật khẩu CŨ
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 3. Nếu mật khẩu cũ đúng -> Tiến hành đổi mật khẩu mới
                user.updatePassword(newPass).addOnCompleteListener(taskUpdate -> {
                    binding.progressBarChangePass.setVisibility(View.GONE);
                    binding.btnConfirmChange.setEnabled(true);

                    if (taskUpdate.isSuccessful()) {
                        Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng màn hình
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Lỗi cập nhật: " + taskUpdate.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Mật khẩu cũ sai
                binding.progressBarChangePass.setVisibility(View.GONE);
                binding.btnConfirmChange.setEnabled(true);
                Toast.makeText(ChangePasswordActivity.this, "Mật khẩu hiện tại không đúng!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}