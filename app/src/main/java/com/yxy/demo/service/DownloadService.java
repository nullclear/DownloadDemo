package com.yxy.demo.service;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import androidx.annotation.NonNull;
import com.yxy.demo.global.DownloadStatus;
import com.yxy.demo.task.DownloadTask;
import com.yxy.demo.utils.GenericUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadService extends Service {
    private final String TAG = "###DownloadService";
    //Service中也能使用handler
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String text = "文件名称:" + msg.obj + "\n\n下载状态:" + DownloadStatus.getMessage(msg.what);
            GenericUtils.show(getApplicationContext(), text);
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
            DownloadTask task = new DownloadTask(url, cookie, handler);
            exec.execute(task);
        }
    }
}
