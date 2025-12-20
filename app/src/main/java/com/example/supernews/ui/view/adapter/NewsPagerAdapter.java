package com.example.supernews.ui.view.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.supernews.ui.view.home.NewsFragment;

public class NewsPagerAdapter extends FragmentStateAdapter {

    // 1. KHAI BÁO DANH SÁCH CHUYÊN MỤC (Hardcode hoặc lấy từ API)
    // Lưu ý: Tên ở đây phải TRÙNG KHỚP 100% với trường 'source' bạn lưu trên Firebase
    private final String[] titles = new String[]{
            "Mới nhất",
            "THỂ THAO",
            "KINH TẾ",
            "CÔNG NGHỆ",
            "THỜI SỰ",
            "GIẢI TRÍ",
            "SỨC KHỎE",
            "GIÁO DỤC",
            "XE"
    };

    public NewsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 2. TẠO FRAGMENT VÀ GẮN "THẺ TÊN" CHO NÓ
        // Khi tạo NewsFragment, ta gói theo cái tên chuyên mục (Bundle)
        // Để Fragment biết nó phải load tin gì.

        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString("category_name", titles[position]); // Gửi tên chuyên mục vào
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getItemCount() {
        return titles.length; // Tổng số tab
    }

    // Hàm lấy tên Tab để hiển thị lên TabLayout
    public String getTitle(int position) {
        return titles[position];
    }
}