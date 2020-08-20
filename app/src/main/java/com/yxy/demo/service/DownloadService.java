package com.yxy.demo.service;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.yxy.demo.MainActivity;
import com.yxy.demo.R;
import com.yxy.demo.application.MyApplication;
import com.yxy.demo.global.DownloadStatus;
import com.yxy.demo.model.DownloadItem;
import com.yxy.demo.task.DownloadTask;
import com.yxy.demo.utils.GenericUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadService extends Service {
    private final String TAG = "###DownloadService";
    //Service中也能使用handler
    public Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (DownloadStatus.getType(msg.what)) {
                case START_DOWNLOAD:
                    initializeNotification((Integer) msg.obj, msg.what);
                    break;
                case DOWNLOADING:
                    updateNotification((Integer) msg.obj, msg.what);
                    break;
                case DOWNLOAD_SUCCESS:
                    stopForeground(true);
                    getNotificationManager().cancel((Integer) msg.obj);
                    String text = "文件名称:" + msg.obj + "\n\n下载状态:" + DownloadStatus.getMessage(msg.what);
                    GenericUtils.show(getApplicationContext(), text);
                    break;
            }
        }
    };

    private ExecutorService exec = Executors.newFixedThreadPool(5);
    private DownloadBinder binder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() returned: " + binder);
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {
        exec.shutdown();
        super.onDestroy();
    }

    public class DownloadBinder extends Binder {
        public void startDownload(String url, String cookie) {
            Log.d(TAG, "startDownload() called with: url = [" + url + "]");
            DownloadTask task = new DownloadTask(url, cookie, DownloadService.this);
            exec.execute(task);
        }

        public void cancelDownload(int downloadId) {
            DownloadItem item = MyApplication.getDownloadItemMap().get(downloadId);
            if (item == null) {
                GenericUtils.showGravity(getApplicationContext(), "无效的下载ID", Gravity.TOP);
            } else {
                item.getDownloadTask().cancel();
            }
        }
    }

    public static final String CHANNEL_ID = "com.yxy.demo.service.DownloadService";
    public static final String CHANNEL_NAME = "Download service practice";

    /**
     * 获取通知管理器
     */
    @SuppressLint("ObsoleteSdkInt")
    private NotificationManager getNotificationManager() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = manager.getNotificationChannel(CHANNEL_ID);
            if (notificationChannel == null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
            }
        }
        return manager;
    }

    private Notification getNotification(DownloadItem item, int code) {
        String title = item.getFileName() + "\n" + DownloadStatus.getMessage(code);
        Intent intent = new Intent(this, MainActivity.class);//点击通知后要启动的Activity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        //构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        //builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        //builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        builder.setOnlyAlertOnce(true);
        builder.setAutoCancel(true);
        if (code == DownloadStatus.DOWNLOADING.getCode()) {
            builder.setContentText(item.getProgress());
            builder.setProgress(item.getMax(), item.getNow(), false);
        }
        return builder.build();
    }

    //初始化通知
    private void initializeNotification(Integer downloadId, int code) {
        DownloadItem item = MyApplication.getDownloadItemMap().get(downloadId);
        if (item == null) {
            Log.d(TAG, "initializeNotification: null");
        } else {
            getNotificationManager();
            startForeground(downloadId, getNotification(item, code));
        }
    }

    //更新通知
    private void updateNotification(Integer downloadId, int code) {
        DownloadItem item = MyApplication.getDownloadItemMap().get(downloadId);
        if (item == null) {
            Log.d(TAG, "updateNotification: null");
        } else {
            getNotificationManager().notify(downloadId, getNotification(item, code));
        }
    }
}
