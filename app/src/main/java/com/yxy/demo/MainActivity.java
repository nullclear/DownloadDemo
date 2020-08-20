package com.yxy.demo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.yxy.demo.activity.DownloadActivity;
import com.yxy.demo.global.RequestCode;
import com.yxy.demo.service.DownloadService;
import com.yxy.demo.utils.GenericUtils;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "###MainActivity";

    @BindView(R.id.go_download)
    Button goDownload;
    @BindView(R.id.go_download_list)
    Button goDownloadList;
    @BindView(R.id.go_download_directory)
    Button goDownloadDirectory;
    @BindView(R.id.go_document_directory)
    Button goDocumentDirectory;

    Intent serviceIntent;
    private AlertDialog alert_permission;
    private AlertDialog alert_waring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();//初始化
    }

    //初始化
    private void init() {
        ButterKnife.bind(this);
        //开启服务
        serviceIntent = new Intent(this, DownloadService.class);
        ComponentName service = startService(serviceIntent);
        if (DownloadService.class.getName().equals(service.getClassName())) {
            Log.i(TAG, "开启下载服务成功");
        } else {
            Log.e(TAG, "开启下载服务失败");
            GenericUtils.showTop(this, "开启下载服务失败");
        }

        //权限提示
        alert_permission = new AlertDialog.Builder(this).setTitle("系统提示")
                .setMessage("不授予权限无法使用数据上传下载功能!")
                .setPositiveButton("我要授予权限", (dialog, which) -> getStoragePermission())
                .setNeutralButton("拒绝授予权限", (dialog, which) -> System.exit(0)).create();

        //警告
        alert_waring = new AlertDialog.Builder(MainActivity.this).setTitle("警告")
                .setMessage("您选择了拒绝并且不在询问权限是否授予\n如果您想继续使用此应用的上传下载功能\n请前往系统设置里手动更改此应用的权限")
                .setPositiveButton("我要授予权限", (dialog, which) ->
                        {
                            startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
                            this.finish();
                        }
                ).setNeutralButton("拒绝授予权限", (dialog, which) -> System.exit(0)).create();

        //获取存储权限
        getStoragePermission();
    }

    @OnClick({R.id.go_download, R.id.go_download_list, R.id.go_download_directory, R.id.go_document_directory})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.go_download:
                startActivity(new Intent(this, DownloadActivity.class));
                break;
            case R.id.go_download_list:
                break;
            case R.id.go_download_directory:
                break;
            case R.id.go_document_directory:
                break;
        }
    }

    //请求外部存储权限
    public void getStoragePermission() {
        int permissionCheckRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheckWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheckRead == PackageManager.PERMISSION_DENIED || permissionCheckWrite == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestCode.EXTERNAL_STORAGE);
        }
    }

    //请求权限回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestCode.EXTERNAL_STORAGE) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.i(TAG, "getStoragePermission 授权成功!");
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    alert_permission.show();
                } else {
                    alert_waring.show();
                }
            }
        } else {
            Log.e(TAG, "getStoragePermission 未知权限!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (GenericUtils.isServiceRunning(this, DownloadService.class)) {
            //多次调用不会报错
            if (stopService(serviceIntent)) {
                Log.i(TAG, "关闭下载服务成功");
            }
        }
    }
}
