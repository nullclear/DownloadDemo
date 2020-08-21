package com.yxy.demo.activity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.yxy.demo.R;
import com.yxy.demo.service.DownloadService;
import com.yxy.demo.utils.GenericUtils;

@SuppressWarnings("SameParameterValue")
public class DownloadActivity extends AppCompatActivity {
    private final String TAG = "###DownloadActivity";

    @BindView(R.id.start_download)
    Button startDownload;
    @BindView(R.id.pause_download)
    Button pauseDownload;
    @BindView(R.id.cancel_download)
    Button cancelDownload;

    //绑定标识
    private boolean isBind = false;
    //下载服务
    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "ServiceConnected: 执行");
            downloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "ServiceDisconnected: 执行");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        init();//初始化
    }

    //初始化
    private void init() {
        ButterKnife.bind(this);
        //绑定服务
        isBind = bindService(new Intent(this, DownloadService.class), connection, Service.BIND_AUTO_CREATE);
        if (isBind) {
            Log.i(TAG, "绑定下载服务成功");
        } else {
            GenericUtils.showTop(this, "绑定下载服务失败");
            Log.e(TAG, "绑定下载服务失败");
        }
    }

    @OnClick({R.id.start_download, R.id.pause_download, R.id.cancel_download})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.start_download:
                startDownload("http://192.168.2.55:8080/data/datafilepage/%E6%B5%8B%E8%AF%95%E6%96%87%E4%BB%B61%E5%8F%B7.zip?dir=/&did=1000");
                break;
            case R.id.pause_download:
                startDownload("http://192.168.2.55:8080/data/datafilepage/%E6%B5%8B%E8%AF%95%E6%96%87%E4%BB%B62%E5%8F%B7.zip?dir=/&did=1000");
                break;
            case R.id.cancel_download:
                downloadBinder.cancelDownload(1);
                break;
        }
    }

    //开始下载
    private void startDownload(String url) {
        if (downloadBinder != null && GenericUtils.isServiceRunning(this, DownloadService.class) && isBind) {
            downloadBinder.startDownload(url, "SESSION=d0125b56-5e16-4c63-a496-9ded16f4e8f1");
        } else {
            GenericUtils.showTop(this, "下载服务未开启");
            Log.e(TAG, "下载服务未开启");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //判断服务是否已经绑定 多次解绑会报错
        if (isBind && GenericUtils.isServiceRunning(this, DownloadService.class)) {
            unbindService(connection);
            isBind = false;
            downloadBinder = null;
            Log.i(TAG, "解绑下载服务成功");
        }
    }
}
