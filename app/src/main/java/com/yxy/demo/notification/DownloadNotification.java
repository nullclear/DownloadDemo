package com.yxy.demo.notification;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.yxy.demo.MainActivity;
import com.yxy.demo.R;
import com.yxy.demo.application.MyApplication;
import com.yxy.demo.global.DownloadStatus;
import com.yxy.demo.model.DownloadItem;
import com.yxy.demo.utils.GenericUtils;

/**
 *Created by Nuclear on 2020/8/21
 */
public class DownloadNotification {
    private static final String CHANNEL_ID = "com.yxy.demo.notification.DownloadNotification";
    private static final String CHANNEL_NAME = "Download Service";
    private static final String TAG = "###DownloadNotification";
    private static NotificationManager manager;

    //通知管理器
    @SuppressLint("ObsoleteSdkInt")
    private static NotificationManager getNotificationManager(Context context) {
        if (manager == null) {
            manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        //8.0版本以后需要创建通知频道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = manager.getNotificationChannel(CHANNEL_ID);
            //如果没有该通道就创建该通道
            if (notificationChannel == null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
                //如果该通道被关闭，引导用户去手动打开该通道
            } else if (notificationChannel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID);
                context.startActivity(intent);
                GenericUtils.show(context, "请手动打开通知!");
            }
        }
        return manager;
    }

    //通知
    private static Notification getNotification(Context context, DownloadItem item, int code) {
        String title = "[" + item.getFileName() + "] " + DownloadStatus.getMessage(code);
        Intent intent = new Intent(context, MainActivity.class);//点击通知后要启动的Activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        //构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_cloud_download)
                .setColor(Color.parseColor("#03A9F4"))
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(item.getProgress())
                .setProgress(item.getMax(), item.getNow(), false);
        return builder.build();
    }

    //初始化通知
    public static void initializeNotification(Service service, Integer downloadId, int code) {
        DownloadItem item = MyApplication.getDownloadItemMap().get(downloadId);
        if (item == null) {
            Log.e(TAG, "initializeNotification " + DownloadStatus.INVALID_DOWNLOAD_ID.getMessage() + " [" + downloadId + "]");
        } else {
            getNotificationManager(service);//初始化通知管理器和通知通道
            service.startForeground(downloadId, getNotification(service, item, code));
        }
    }

    //更新通知
    public static void updateNotification(Service service, Integer downloadId, int code) {
        DownloadItem item = MyApplication.getDownloadItemMap().get(downloadId);
        if (item == null) {
            Log.e(TAG, "updateNotification " + DownloadStatus.INVALID_DOWNLOAD_ID.getMessage() + " [" + downloadId + "]");
        } else {
            //更新通知信息
            getNotificationManager(service).notify(downloadId, getNotification(service, item, code));
        }
    }

    //最终通知
    public static String finalNotification(Service service, Integer downloadId, int code) {
        String text;
        service.stopForeground(true);
        DownloadItem item = MyApplication.getDownloadItemMap().get(downloadId);
        if (item == null) {
            Log.e(TAG, "finalNotification " + DownloadStatus.INVALID_DOWNLOAD_ID.getMessage() + " [" + downloadId + "]");
            text = DownloadStatus.INVALID_DOWNLOAD_ID.getMessage() + " [" + downloadId + "]";
        } else {
            text = "名称: " + item.getFileName() + "\n\n状态: " + DownloadStatus.getMessage(code);
            getNotificationManager(service).notify(downloadId, getNotification(service, item, code));
        }
        return text;
    }
}
