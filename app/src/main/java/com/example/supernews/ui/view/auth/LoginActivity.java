package com.example.supernews.ui.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.supernews.MainActivity;
import com.example.supernews.R;
import com.example.supernews.data.model.User;
import com.example.supernews.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // 1. CẤU HÌNH GOOGLE (Hardcode ID)
        String myClientId = "264453310144-mogsn8mu6d69u2k1gngg4ai0tn7km3sh.apps.googleusercontent.com";

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(myClientId)
                .requestEmail()
                .build();
        mGoogleClient = GoogleSignIn.getClient(this, gso);

        // 2. SETUP SỰ KIỆN
        binding.btnLogin.setOnClickListener(v -> loginWithEmail());
        binding.btnGoogleSignIn.setOnClickListener(v -> googleSignIn());
        binding.tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        binding.tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));

        // 3. TIỆN ÍCH: Bấm Enter ở ô mật khẩu -> Đăng nhập luôn
        binding.edtPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginWithEmail();
                return true;
            }
            return false;
        });
        TextView tvSkip = findViewById(R.id.tvSkipLogin);

        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Tạo Intent để chuyển sang màn hình chính
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                // 2. QUAN TRỌNG: Gửi kèm "Cờ hiệu" (Flag) để báo đây là Khách
                // Key là "IS_GUEST", Value là true
                intent.putExtra("IS_GUEST", true);

                // 3. Khởi chạy
                startActivity(intent);

                // 4. Đóng màn hình Login lại để người dùng không back về được
                finish();
            }
        });
    }

    // --- GOOGLE SIGN IN ---
    private void googleSignIn() {
        Intent intent = mGoogleClient.getSignInIntent();
        googleLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> googleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(this, "Lỗi Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void firebaseAuthWithGoogle(String idToken) {
        showLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkAndSaveUser(mAuth.getCurrentUser());
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Xác thực thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- EMAIL SIGN IN (VALIDATION CHUYÊN NGHIỆP) ---
    private void loginWithEmail() {
        String email = binding.edtEmail.getText().toString().trim();
        String password = binding.edtPassword.getText().toString().trim();

        // Xóa lỗi cũ
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        boolean isValid = true;
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
        }

        if (!isValid) return;

        showLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        goToMain();
                    } else {
                        showLoading(false);
                        handleError(task.getException());
                    }
                });
    }

    // Dịch lỗi sang tiếng Việt
    private void handleError(Exception e) {
        String msg = "Đăng nhập thất bại.";
        try {
            throw e;
        } catch (com.google.firebase.auth.FirebaseAuthInvalidUserException ex) {
            msg = "Tài khoản không tồn tại.";
            binding.tilEmail.setError(msg);
        } catch (com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ex) {
            msg = "Sai mật khẩu.";
            binding.tilPassword.setError(msg);
        } catch (Exception ex) {
            msg = "Lỗi: " + ex.getMessage();
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // --- LƯU USER & CHUYỂN MÀN HÌNH ---
    private void checkAndSaveUser(FirebaseUser firebaseUser) {
        if (firebaseUser == null) { goToMain(); return; }

        FirebaseFirestore.getInstance().collection("users").document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        User newUser = new User();
                        newUser.setId(firebaseUser.getUid());
                        newUser.setName(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "No Name");
                        newUser.setEmail(firebaseUser.getEmail());
                        newUser.setRole("user");
                        if (firebaseUser.getPhotoUrl() != null) newUser.setAvatar(firebaseUser.getPhotoUrl().toString());

                        FirebaseFirestore.getInstance().collection("users").document(firebaseUser.getUid()).set(newUser);
                    }
                    goToMain();
                })
                .addOnFailureListener(e -> goToMain());
    }

    private void showLoading(boolean isShow) {
        binding.progressBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isShow);
        binding.btnGoogleSignIn.setEnabled(!isShow);
    }

    private void goToMain() {
        showLoading(false);
        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}