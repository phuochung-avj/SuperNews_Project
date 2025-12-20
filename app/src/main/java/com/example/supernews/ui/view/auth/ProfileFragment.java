package com.example.supernews.ui.view.auth;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.supernews.R;
import com.example.supernews.data.model.User; // 🔥 Class User chứa hằng số ROLE_ADMIN
import com.example.supernews.databinding.FragmentProfileBinding;
import com.example.supernews.ui.view.AdminLogActivity;
import com.example.supernews.ui.view.SettingsActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final String TAG = "DEBUG_PROFILE";
    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Glide.with(this).load(imageUri).circleCrop().into(binding.imgAvatar);
                    showSaveButton();
                    Log.d(TAG, "Đã chọn ảnh mới: " + imageUri.toString());
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        loadUserProfile();
        setupListeners();
    }

    private void setupListeners() {
        binding.layoutAvatar.setOnClickListener(v -> openImagePicker());

        binding.edtName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { showSaveButton(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.btnSaveProfile.setOnClickListener(v -> processUpdateProfile());

        // Nút Lịch sử hoạt động
        binding.btnAdminLog.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AdminLogActivity.class));
        });

        // 🔥 NÚT QUẢN LÝ TÀI KHOẢN (USER MANAGEMENT)
        binding.btnUserManage.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), com.example.supernews.ui.view.UserManagementActivity.class));
        });

        binding.btnSettings.setOnClickListener(v -> startActivity(new Intent(requireContext(), SettingsActivity.class)));
        binding.btnLogout.setOnClickListener(v -> performLogout());
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageLauncher.launch(Intent.createChooser(intent, "Chọn ảnh đại diện"));
    }

    private void showSaveButton() {
        if (binding.btnSaveProfile.getVisibility() != View.VISIBLE) {
            binding.btnSaveProfile.setVisibility(View.VISIBLE);
        }
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || getContext() == null) return;

                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            binding.edtName.setText(user.getName());
                            binding.tvEmail.setText(user.getEmail());

                            // 🔥 LOGIC PHÂN QUYỀN: ẨN/HIỆN NÚT 🔥
                            if (User.ROLE_ADMIN.equals(user.getRole())) {
                                binding.tvRole.setText("QUẢN TRỊ VIÊN");
                                binding.tvRole.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

                                // Hiện các nút dành cho Admin
                                binding.btnAdminLog.setVisibility(View.VISIBLE);
                                binding.btnUserManage.setVisibility(View.VISIBLE); // <--- Đã thêm dòng này
                            } else {
                                binding.tvRole.setText("Thành viên");

                                // Ẩn các nút dành cho Admin
                                binding.btnAdminLog.setVisibility(View.GONE);
                                binding.btnUserManage.setVisibility(View.GONE); // <--- Đã thêm dòng này
                            }

                            // Load ảnh
                            String avatarUrl = user.getAvatar();
                            if (avatarUrl == null || avatarUrl.isEmpty()) {
                                if (currentUser.getPhotoUrl() != null) {
                                    avatarUrl = currentUser.getPhotoUrl().toString();
                                }
                            }

                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Glide.with(this).load(avatarUrl).placeholder(R.drawable.ic_launcher_background).circleCrop().into(binding.imgAvatar);
                            } else {
                                binding.imgAvatar.setImageResource(R.drawable.ic_launcher_background);
                            }

                            binding.btnSaveProfile.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void processUpdateProfile() {
        binding.progressBarProfile.setVisibility(View.VISIBLE);
        binding.btnSaveProfile.setEnabled(false);

        if (imageUri != null) {
            uploadImageToStorage();
        } else {
            updateFirestoreAndAuth(null);
        }
    }

    private void uploadImageToStorage() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String fileName = "profile_images/" + currentUser.getUid() + ".jpg";
        StorageReference profileRef = storage.getReference().child(fileName);

        profileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateFirestoreAndAuth(uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    binding.progressBarProfile.setVisibility(View.GONE);
                    binding.btnSaveProfile.setEnabled(true);
                    Toast.makeText(requireContext(), "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateFirestoreAndAuth(@Nullable String newAvatarUrl) {
        String newName = binding.edtName.getText().toString().trim();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        if (newAvatarUrl != null) {
            updates.put("avatar", newAvatarUrl);
        }

        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(newName);
                    if (newAvatarUrl != null) {
                        profileUpdates.setPhotoUri(Uri.parse(newAvatarUrl));
                    }

                    currentUser.updateProfile(profileUpdates.build())
                            .addOnCompleteListener(task -> {
                                binding.progressBarProfile.setVisibility(View.GONE);
                                binding.btnSaveProfile.setEnabled(true);
                                binding.btnSaveProfile.setVisibility(View.GONE);
                                Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    binding.progressBarProfile.setVisibility(View.GONE);
                    binding.btnSaveProfile.setEnabled(true);
                    Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void performLogout() {
        mAuth.signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        googleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            Intent intent = new Intent(requireContext(), com.example.supernews.ui.view.auth.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}