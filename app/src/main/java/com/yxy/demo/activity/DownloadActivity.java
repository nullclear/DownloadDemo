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
            Log.d(TAG, "onServiceConnected: 执行 ");
            downloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: 执行");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        //初始化
        init();
    }

    private void init() {
        ButterKnife.bind(this);
        isBind = bindService(new Intent(this, DownloadService.class), connection, Service.BIND_AUTO_CREATE);
        if (isBind) {
            GenericUtils.show(this, "绑定服务成功");
        } else {
            GenericUtils.show(this, "绑定服务失败");
        }
    }

    @OnClick({R.id.start_download, R.id.pause_download, R.id.cancel_download})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.start_download:
                startDownload("http://192.168.2.55:8080/data/datafilepage/%E6%B5%8B%E8%AF%95%E7%94%A8%E5%A4%A7%E6%96%87%E4%BB%B6.zip?dir=/&did=1000");
                break;
            case R.id.pause_download:
                break;
            case R.id.cancel_download:
                downloadBinder.cancelDownload(1);
                break;
        }
    }

    private void startDownload(String url) {
        if (downloadBinder != null && GenericUtils.isServiceRunning(this, DownloadService.class) && isBind) {
            downloadBinder.startDownload(url, "SESSION=852a9bea-f2d6-4186-883d-6aad57551637");
        } else {
            GenericUtils.showGravity(this, "服务未开启");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //判断服务是否已经绑定 多次解绑会报错
        if (isBind && GenericUtils.isServiceRunning(this, DownloadService.class)) {
            unbindService(connection);
            isBind = false;
        }
    }
}
