package com.example.supernews.ui.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.supernews.MainActivity;
import com.example.supernews.data.model.User;
import com.example.supernews.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Xử lý sự kiện
        binding.btnRegister.setOnClickListener(v -> registerUser());
        binding.tvLogin.setOnClickListener(v -> finish()); // Quay lại màn login
    }

    private void registerUser() {
        String name = binding.edtName.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String password = binding.edtPassword.getText().toString().trim();
        String confirmPass = binding.edtConfirmPassword.getText().toString().trim();

        // --- VALIDATE DỮ LIỆU (BÁO LỖI ĐỎ) ---
        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        boolean isValid = true;

        if (name.isEmpty()) {
            binding.tilName.setError("Vui lòng nhập họ tên");
            isValid = false;
        }

        if (email.isEmpty()) {
            binding.tilEmail.setError("Vui lòng nhập Email");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Email không hợp lệ");
            isValid = false;
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError("Vui lòng nhập mật khẩu");
            isValid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("Mật khẩu quá ngắn (tối thiểu 6 ký tự)");
            isValid = false;
        }

        if (confirmPass.isEmpty()) {
            binding.tilConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            isValid = false;
        } else if (!password.equals(confirmPass)) {
            binding.tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            isValid = false;
        }

        if (!isValid) return; // Dừng lại nếu lỗi

        // --- TIẾN HÀNH ĐĂNG KÝ ---
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        saveUserToFirestore(firebaseUser, name);
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnRegister.setEnabled(true);

                        String errorMsg = "Đăng ký thất bại.";
                        try {
                            throw task.getException();
                        } catch (com.google.firebase.auth.FirebaseAuthUserCollisionException e) {
                            errorMsg = "Email này đã được đăng ký rồi.";
                            binding.tilEmail.setError(errorMsg);
                        } catch (Exception e) {
                            errorMsg = "Lỗi: " + e.getMessage();
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser, String name) {
        if (firebaseUser == null) return;

        User user = new User();
        user.setId(firebaseUser.getUid());
        user.setName(name);
        user.setEmail(firebaseUser.getEmail());
        user.setRole("user"); // Mặc định là user thường

        FirebaseFirestore.getInstance().collection("users")
                .document(firebaseUser.getUid())
                .set(user)
                .addOnSuccessListener(v -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển thẳng vào App, xóa lịch sử để không back lại màn đăng ký
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnRegister.setEnabled(true);
                    Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}