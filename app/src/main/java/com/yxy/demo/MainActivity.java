package com.yxy.demo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
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

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public class MainActivity extends AppCompatActivity {

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
        //初始化
        init();
    }

    //初始化
    private void init() {
        ButterKnife.bind(this);

        serviceIntent = new Intent(this, DownloadService.class);
        ComponentName service = startService(serviceIntent);
        if (DownloadService.class.getName().equals(service.getClassName())) {
            GenericUtils.showGravity(this, "开启服务成功");
        } else {
            GenericUtils.showGravity(this, "开启服务失败");
        }

        alert_permission = new AlertDialog.Builder(this).setTitle("系统提示")
                .setMessage("不授予权限无法使用数据上传下载功能!")
                .setPositiveButton("我要授予权限", (dialog, which) -> getStoragePermission())
                .setNeutralButton("拒绝授予权限", (dialog, which) -> System.exit(0)).create();

        alert_waring = new AlertDialog.Builder(MainActivity.this).setTitle("警告")
                .setMessage("您选择了拒绝并且不在询问权限是否授予\n如果您想继续使用此应用的上传下载功能\n请前往系统设置里手动更改此应用的权限")
                .setPositiveButton("我要授予权限", (dialog, which) ->
                        {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                            startActivity(intent);
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
        switch (requestCode) {
            case RequestCode.EXTERNAL_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    GenericUtils.show(this, "授权成功!");
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        alert_permission.show();
                    } else {
                        alert_waring.show();
                    }
                }
                break;
            default:
                GenericUtils.show(this, "未知权限!");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (GenericUtils.isServiceRunning(this, DownloadService.class)) {
            //多次调用不会报错
            stopService(serviceIntent);
        }
    }
}
