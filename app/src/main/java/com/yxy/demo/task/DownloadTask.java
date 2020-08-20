package com.yxy.demo.task;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.yxy.demo.R;
import com.yxy.demo.application.MyApplication;
import com.yxy.demo.global.DownloadStatus;
import com.yxy.demo.model.DownloadItem;
import com.yxy.demo.service.DownloadService;
import com.yxy.demo.utils.GenericUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 *Created by Nuclear on 2020/8/19
 */
public class DownloadTask implements Runnable {
    private static final String TAG = "###DownloadTask";
    //网络信息
    private String url;
    private String cookie;
    private Handler handler;
    //下载信息
    private String fileName;
    private File downloadDirectory;
    private AtomicLong total = new AtomicLong(0L);
    private AtomicLong sum = new AtomicLong(0L);
    private Integer downloadId;
    private DownloadItem item;
    //定时任务
    private Timer timer = new Timer();
    private AtomicBoolean isCancel = new AtomicBoolean(false);
    private AtomicBoolean isFinal = new AtomicBoolean(false);

    public DownloadTask(String url, String cookie, DownloadService service) {
        this.url = url;
        this.cookie = cookie;
        this.handler = service.handler;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + service.getString(R.string.root_directory) + "/" + service.getString(R.string.download_directory);
        downloadDirectory = new File(path); // /storage/emulated/0/Union/download
    }

    @Override
    public void run() {
        //url错误
        if (TextUtils.isEmpty(url)) {
            send(DownloadStatus.URL_ERROR.getCode(), url);
            return;
        }

        //获取文件名
        fileName = GenericUtils.getFileNameFromURL(url);
        //文件名错误
        if (TextUtils.isEmpty(fileName)) {
            send(DownloadStatus.FILENAME_ERROR.getCode(), fileName);
            return;
        }

        if (downloadDirectory.exists()) {
            //拒绝写入
            if (!downloadDirectory.canWrite()) {
                send(DownloadStatus.DIRECTORY_WRITE_DENY.getCode(), downloadDirectory.getName());
                return;
            }
        } else {
            boolean flag = downloadDirectory.mkdirs();
            //初始化失败
            if (!flag) {
                send(DownloadStatus.DIRECTORY_CREATE_FAILED.getCode(), downloadDirectory.getName());
                return;
            }
        }

        File file = new File(downloadDirectory, fileName);
        //文件已经存在
        if (file.exists()) {
            send(DownloadStatus.FILE_EXIST.getCode(), fileName);
            return;
        }

        //初始化下载信息
        initializeDownloadInformation();

        //开启网络连接
        HttpURLConnection conn;
        try {
            URL downloadUrl = new URL(url);
            conn = (HttpURLConnection) downloadUrl.openConnection();
            //携带cookie请求，要写在请求开始之前
            conn.addRequestProperty("Cookie", cookie);
            conn.setConnectTimeout(3000);
            conn.setDoInput(true);
            conn.connect();

            Log.d(TAG, "Content-Disposition [ " + conn.getHeaderField("Content-Disposition") + " ]");
            //文件长度(单位是Byte)
            total.set(conn.getContentLengthLong());
            item.setTotalLength(total.get());
            //io流初始化
            try (InputStream in = conn.getInputStream();
                 BufferedInputStream bis = new BufferedInputStream(in);
                 FileOutputStream out = new FileOutputStream(file);
                 BufferedOutputStream bos = new BufferedOutputStream(out, 64 * 1024);) {

                //自动记录下载进度
                AutoRecord autoRecord = new AutoRecord();
                timer.schedule(autoRecord, 0, 500);

                //设置8K的buffer
                byte[] buffer = new byte[8 * 1024];
                int count;
                while ((count = bis.read(buffer)) != -1) {
                    if (isCancel.get()) {
                        conn.disconnect();
                        break;
                    } else {
                        bos.write(buffer, 0, count);
                        sum.getAndAdd(count);//累计已经下载的大小
                    }
                }
                //强制写入外存
                bos.flush();
                //判断最终状态
                if (isCancel.get()) {
                    if (file.exists()) {
                        if (file.delete()) {
                            setFinalStatus(DownloadStatus.DOWNLOAD_CANCEL_SUCCESS);
                            Log.d(TAG, "DOWNLOAD_CANCEL_SUCCESS");
                        } else {
                            setFinalStatus(DownloadStatus.DOWNLOAD_CANCEL_FAILED);
                            Log.d(TAG, "DOWNLOAD_CANCEL_FAILED");
                        }
                    } else {
                        setFinalStatus(DownloadStatus.UNKNOWN_ERROR);
                    }
                } else {
                    if (sum.get() == total.get()) {
                        setFinalStatus(DownloadStatus.DOWNLOAD_SUCCESS);
                    } else {
                        setFinalStatus(DownloadStatus.DOWNLOAD_FAILED);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException: 文件写入错误", e);
                setFinalStatus(DownloadStatus.WRITE_ERROR);
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: 网络连接错误", e);
            setFinalStatus(DownloadStatus.INTERNET_CONNECTION_ERROR);
        }
    }

    //初始化下载信息
    private void initializeDownloadInformation() {
        downloadId = MyApplication.getDownloadId();
        item = DownloadItem.Builder()
                .id(downloadId)
                .fileName(fileName)
                .fileType(GenericUtils.obtainFileType(fileName))
                .parentPath(downloadDirectory.getAbsolutePath())
                .startTime(GenericUtils.D2S(new Date()))
                .downloadStatus(DownloadStatus.START_DOWNLOAD)
                .downloadTask(this)
                .build();
        Log.d(TAG, "downloadId = = >> " + downloadId);
        MyApplication.getDownloadItemMap().put(downloadId, item);
        MyApplication.getDownloadList().add(item);
        send(DownloadStatus.START_DOWNLOAD.getCode(), downloadId);
    }

    //发送信息到下载服务
    private void send(int what, Object obj) {
        Message message = Message.obtain();
        message.what = what;
        message.obj = obj;
        handler.sendMessage(message);
    }

    //设置最终状态
    private void setFinalStatus(DownloadStatus downloadStatus) {
        isFinal.set(true);
        send(downloadStatus.getCode(), downloadId);
        item.setDownloadStatus(downloadStatus);
        item.setEndTime(GenericUtils.D2S(new Date()));
        item.setSavedLength(sum.get());
        item.setTotalLength(total.get());
        item.setProgress(GenericUtils.N2P(sum.get(), total.get()));
    }

    //自动记录
    private class AutoRecord extends TimerTask {
        @Override
        public void run() {
            if (isFinal.get()) {
                timer.cancel();
            } else {
                item.setDownloadStatus(DownloadStatus.DOWNLOADING);
                item.setSavedLength(sum.get());
                item.setProgress(GenericUtils.N2P(sum.get(), total.get()));
                send(DownloadStatus.DOWNLOADING.getCode(), downloadId);
            }
        }
    }

    //取消下载
    public void cancel() {
        if (isFinal.get()) {
            Log.d(TAG, "cancel: 下载任务已经结束");
        } else {
            isCancel.set(true);
        }
    }
}
