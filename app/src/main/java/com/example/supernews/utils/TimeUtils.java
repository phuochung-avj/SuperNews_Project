package com.example.supernews.utils;

import android.text.format.DateUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
    // Hàm chuyển đổi chuỗi ngày (dd/MM/yyyy HH:mm) thành "x phút trước"
    public static String getTimeAgo(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date date = sdf.parse(dateString);
            if (date != null) {
                long time = date.getTime();
                long now = System.currentTimeMillis();

                // Sử dụng thư viện có sẵn của Android để tính toán
                CharSequence ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
                return ago.toString();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString; // Nếu lỗi thì trả về ngày tháng gốc
    }
}