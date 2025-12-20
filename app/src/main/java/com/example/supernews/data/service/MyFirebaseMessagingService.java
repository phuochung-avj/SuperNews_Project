package com.example.supernews.data.service; // Hoặc package của bạn (lưu ý sửa cho đúng)

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.supernews.MainActivity;
import com.example.supernews.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // 1. Lấy dữ liệu hiển thị (Notification)
        String title = "Tin mới từ SuperNews";
        String body = "Bấm để xem chi tiết";

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        // 2. Lấy dữ liệu ẩn (Data Payload - Chứa ID bài viết)
        Map<String, String> data = remoteMessage.getData();
        String newsId = null;
        String type = null;

        if (data.size() > 0) {
            newsId = data.get("newsId");
            type = data.get("type");
        }

        // 3. Gọi hàm tạo thông báo kèm dữ liệu
        sendNotification(title, body, newsId, type);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("FCM", "Refreshed token: " + token);
    }

    private void sendNotification(String title, String messageBody, String newsId, String type) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // 🔥 QUAN TRỌNG: Nhét ID bài viết vào Intent để MainActivity bắt được
        if (newsId != null) {
            intent.putExtra("newsId", newsId);
            intent.putExtra("type", type);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = "SuperNews_Channel_ID";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher_round) // Đảm bảo icon này tồn tại
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo Channel cho Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Tin tức SuperNews",
                    NotificationManager.IMPORTANCE_HIGH); // Đổi thành HIGH để dễ thấy
            notificationManager.createNotificationChannel(channel);
        }

        // Dùng ID ngẫu nhiên (System.currentTimeMillis) để thông báo không bị đè lên nhau
        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }
}