package com.example.supernews.ui.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.supernews.R;
import com.example.supernews.data.model.AdminLog;
import com.example.supernews.ui.adapter.AdminLogAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminLogActivity extends AppCompatActivity {

    private RecyclerView rvLogs;
    private AdminLogAdapter adapter;
    private List<AdminLog> logList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    // Các nút lọc
    private Chip chipAll, chipCreate, chipUpdate, chipDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_log);

        // Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarLog);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Ánh xạ View
        rvLogs = findViewById(R.id.rvLogs);
        progressBar = findViewById(R.id.progressBarLog);
        chipAll = findViewById(R.id.chipAll);
        chipCreate = findViewById(R.id.chipCreate);
        chipUpdate = findViewById(R.id.chipUpdate);
        chipDelete = findViewById(R.id.chipDelete);

        db = FirebaseFirestore.getInstance();
        logList = new ArrayList<>();

        // Setup Adapter
        adapter = new AdminLogAdapter(logList, this::showDetailDialog);
        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        rvLogs.setAdapter(adapter);

        // Mặc định load tất cả
        loadLogs(null);
        setupFilterListeners();
    }

    private void setupFilterListeners() {
        // Logic: Bấm nút nào thì load lại dữ liệu theo nút đó
        chipAll.setOnClickListener(v -> loadLogs(null));
        chipCreate.setOnClickListener(v -> loadLogs("CREATE"));
        chipUpdate.setOnClickListener(v -> loadLogs("UPDATE"));
        chipDelete.setOnClickListener(v -> loadLogs("DELETE"));
    }

    private void loadLogs(String actionFilter) {
        progressBar.setVisibility(View.VISIBLE);
        logList.clear();
        adapter.notifyDataSetChanged();

        Query query = db.collection("admin_logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        // Nếu có bộ lọc thì thêm điều kiện where
        if (actionFilter != null) {
            query = query.whereEqualTo("action", actionFilter);
        }
        query.addSnapshotListener((value, error) -> {
            // Kiểm tra Activity còn sống không để tránh lỗi crash
            if (isDestroyed() || isFinishing()) return;
            progressBar.setVisibility(View.GONE);
            if (error != null) {
                // Nếu chưa sửa Rules, nó sẽ báo lỗi ở đây
                Toast.makeText(this, "Lỗi tải Log: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            if (value != null) {
                logList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    AdminLog log = doc.toObject(AdminLog.class);
                    log.setId(doc.getId());
                    logList.add(log);
                }
                adapter.notifyDataSetChanged();
                if(logList.isEmpty()) {
                    Toast.makeText(this, "Không tìm thấy hoạt động nào", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //  TÍNH NĂNG CHI TIẾT: HIỆN BOTTOM SHEET
    private void showDetailDialog(AdminLog log) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.layout_log_detail_popup);

        TextView tvTitle = dialog.findViewById(R.id.tvPopupTitle);
        TextView tvDetail = dialog.findViewById(R.id.tvPopupDetail);

        if (tvTitle != null && tvDetail != null) {
            tvTitle.setText(log.getAction() + ": " + log.getTargetTitle());

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
            String time = log.getTimestamp() != null ? sdf.format(log.getTimestamp().toDate()) : "N/A";

            String info = "🕒 Thời gian: " + time + "\n\n" +
                    "👤 Người thực hiện:\n" +
                    "- Tên: " + log.getAdminName() + "\n" +
                    "📄 Đối tượng tác động:\n" +
                    "- Bài viết: " + log.getTargetTitle() + "\n" +
                    "- News ID: " + log.getTargetId() + "\n\n" +
                    "📝 Chi tiết hành động:\n" + log.getDetails() + "\n\n" +
                    "🆔 Log ID: " + log.getId();

            tvDetail.setText(info);
        }

        dialog.show();
    }
}