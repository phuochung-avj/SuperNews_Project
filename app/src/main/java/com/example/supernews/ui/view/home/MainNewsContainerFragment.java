package com.example.supernews.ui.view.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.supernews.databinding.FragmentMainNewsContainerBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainNewsContainerFragment extends Fragment {

    private FragmentMainNewsContainerBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainNewsContainerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Setup Adapter
        MainNewsPagerAdapter adapter = new MainNewsPagerAdapter(this);
        binding.viewPagerMain.setAdapter(adapter);

        // 2. Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(binding.tabLayoutMain, binding.viewPagerMain, (tab, position) -> {
            if (position == 0) {
                tab.setText("Trong nước");
            } else {
                tab.setText("Quốc tế");
            }
        }).attach();
    }

    // --- ADAPTER RIÊNG CHO CONTAINER NÀY ---
    private static class MainNewsPagerAdapter extends FragmentStateAdapter {

        public MainNewsPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // position 0 -> Tab Trong nước
            // position 1 -> Tab Quốc tế

            String scope = (position == 0) ? "domestic" : "international";

            // Sử dụng NewsFragment với category "Mới nhất" (lấy tất cả) và scope tương ứng
            return NewsFragment.newInstance("Mới nhất", scope);
        }

        @Override
        public int getItemCount() {
            return 2; // Chỉ có 2 tab
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}