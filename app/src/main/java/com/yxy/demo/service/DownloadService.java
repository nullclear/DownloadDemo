package com.yxy.demo.service;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.yxy.demo.MainActivity;
import com.yxy.demo.R;
import com.yxy.demo.application.MyApplication;
import com.yxy.demo.global.DownloadStatus;
import com.yxy.demo.model.DownloadItem;
import com.yxy.demo.notification.DownloadNotification;
import com.yxy.demo.task.DownloadTask;
import com.yxy.demo.utils.GenericUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadService extends Service {
    private static final String TAG = "###DownloadService";
    private ExecutorService exec = Executors.newFixedThreadPool(5);
    private Handler handler = new ServiceHandler(Looper.getMainLooper());
    private DownloadBinder binder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;//解绑服务不会销毁
    }

    @Override
    public void onDestroy() {
        exec.shutdown();
        super.onDestroy();
        Log.w(TAG, "下载服务销毁");
    }

    public class DownloadBinder extends Binder {
        //开始下载
        public void startDownload(String url, String cookie) {
            Log.i(TAG, "download url = [" + url + "]");
            DownloadTask task = new DownloadTask(url, cookie, DownloadService.this);
            exec.execute(task);
        }

        //取消下载
        public void cancelDownload(int downloadId) {
            DownloadItem item = MyApplication.getDownloadItemMap().get(downloadId);
            if (item == null) {
                Log.e(TAG, "cancelDownload " + DownloadStatus.INVALID_DOWNLOAD_ID.getMessage() + " [" + downloadId + "]");
                GenericUtils.showCenter(getApplicationContext(), DownloadStatus.INVALID_DOWNLOAD_ID.getMessage());
            } else {
                item.getDownloadTask().cancel();
            }
        }
    }

    //Service的Handler
    private class ServiceHandler extends Handler {

        public ServiceHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (DownloadStatus.getType(msg.what)) {
                case START_DOWNLOAD:
                    //initializeNotification((Integer) msg.obj, msg.what);
                    DownloadNotification.initializeNotification(DownloadService.this, (Integer) msg.obj, msg.what);
                    break;
                case DOWNLOADING:
                    //updateNotification((Integer) msg.obj, msg.what);
                    DownloadNotification.updateNotification(DownloadService.this, (Integer) msg.obj, msg.what);
                    break;
                case DOWNLOAD_SUCCESS:
                case DOWNLOAD_FAILED:
                    //String bottom = closeNotification((Integer) msg.obj, msg.what);
                    String bottom = DownloadNotification.finalNotification(DownloadService.this, (Integer) msg.obj, msg.what);
                    GenericUtils.show(getApplicationContext(), bottom);
                    break;
                case DOWNLOAD_CANCEL_SUCCESS:
                case DOWNLOAD_CANCEL_FAILED:
                    //String center = closeNotification((Integer) msg.obj, msg.what);
                    String center = DownloadNotification.finalNotification(DownloadService.this, (Integer) msg.obj, msg.what);
                    GenericUtils.showCenter(getApplicationContext(), center);
                    break;
                case WRITE_ERROR:
                case UNKNOWN_ERROR:
                case INTERNET_CONNECTION_ERROR:
                    //String top = closeNotification((Integer) msg.obj, msg.what);
                    String top = DownloadNotification.finalNotification(DownloadService.this, (Integer) msg.obj, msg.what);
                    GenericUtils.showTop(getApplicationContext(), top);
                    break;
                case URL_ERROR:
                case FILE_EXIST:
                case FILENAME_ERROR:
                case DIRECTORY_WRITE_DENY:
                case DIRECTORY_CREATE_FAILED:
                    String preError = "名称: " + msg.obj.toString() + "\n\n状态: " + DownloadStatus.getMessage(msg.what);
                    GenericUtils.showCenter(getApplicationContext(), preError);
                    break;
            }
        }
    }

    public Handler getHandler() {
        return handler;
    }

    //在服务内部写通知
    private static final String CHANNEL_ID = "com.yxy.demo.service.DownloadService";
    private static final String CHANNEL_NAME = "Download Service";

    //获取通知管理器
    @SuppressLint("ObsoleteSdkInt")
    private NotificationManager getNotificationManager() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
        String title = "[" + item.getFileName() + "]\n" + DownloadStatus.getMessage(code);
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
            Log.e(TAG, "initializeNotification " + DownloadStatus.INVALID_DOWNLOAD_ID.getMessage() + " [" + downloadId + "]");
        } else {
            getNotificationManager();
            startForeground(downloadId, getNotification(item, code));
        }
    }

    //更新通知
    private void updateNotification(Integer downloadId, int code) {
        DownloadItem item = MyApplication.getDownloadItemMap().get(downloadId);
        if (item == null) {
            Log.e(TAG, "updateNotification " + DownloadStatus.INVALID_DOWNLOAD_ID.getMessage() + " [" + downloadId + "]");
        } else {
            getNotificationManager().notify(downloadId, getNotification(item, code));
        }
    }

    //关闭通知
    private String closeNotification(Integer downloadId, int code) {
        String text;
        stopForeground(true);
        getNotificationManager().cancel(downloadId);
        DownloadItem item = MyApplication.getDownloadItemMap().get(downloadId);
        if (item == null) {
            Log.e(TAG, "closeNotification " + DownloadStatus.INVALID_DOWNLOAD_ID.getMessage() + " [" + downloadId + "]");
            text = DownloadStatus.INVALID_DOWNLOAD_ID.getMessage() + " [" + downloadId + "]";
        } else {
            text = "名称: " + item.getFileName() + "\n\n状态: " + DownloadStatus.getMessage(code);
        }
        return text;
    }
}
