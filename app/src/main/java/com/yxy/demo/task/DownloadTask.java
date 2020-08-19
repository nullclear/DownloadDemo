package com.yxy.demo.task;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.yxy.demo.global.DownloadStatus;
import com.yxy.demo.utils.GenericUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 *Created by Nuclear on 2020/8/19
 */
@SuppressWarnings("FieldCanBeLocal")
public class DownloadTask implements Runnable {
    private static final String TAG = "###DownloadTask";

    private String url;
    private String cookie;
    private Handler handler;

    private String fileName;
    private File downloadDirectory;

    private Timer timer = new Timer();
    private long total = 0L;
    private long sum = 0L;

    public DownloadTask(String url, String cookie, Handler handler) {
        this.url = url;
        this.cookie = cookie;
        this.handler = handler;
    }

    @Override
    public void run() {
        //url错误
        if (TextUtils.isEmpty(url)) {
            send(DownloadStatus.URL_ERROR.getCode(), fileName);
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

        //下载文件
        HttpURLConnection conn;
        try {
            URL downloadUrl = new URL(url);
            conn = (HttpURLConnection) downloadUrl.openConnection();
            //携带cookie请求，要写在请求开始之前
            conn.addRequestProperty("Cookie", cookie);
            conn.setDoInput(true);
            conn.connect();
            //文件长度(单位是Byte)
            total = conn.getContentLengthLong();
            //io流初始化
            try (InputStream in = conn.getInputStream();
                 BufferedInputStream bis = new BufferedInputStream(in);
                 FileOutputStream out = new FileOutputStream(downloadDirectory.getAbsolutePath() + "/" + fileName);
                 BufferedOutputStream bos = new BufferedOutputStream(out, 64 * 1024);) {

                //下载进度提示
                Progress progress = new Progress();
                timer.schedule(progress, 0, 200);
                //设置8K的buffer
                byte[] buffer = new byte[8 * 1024];
                int count;
                while ((count = bis.read(buffer)) != -1) {
                    //累计已经下载的大小
                    sum += count;
                    bos.write(buffer, 0, count);
                }
                bos.flush();//强制写入外存
            } catch (IOException e) {
                Log.e(TAG, "run: 写入文件异常", e);
                //下载失败
                send(DownloadStatus.DOWNLOAD_FAILED.getCode(), fileName);
                return;
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, "run: 网络连接错误", e);
        }
        //优雅的取消下载任务
        timer.cancel();
        //下载完成提示
        send(DownloadStatus.DOWNLOAD_SUCCESS.getCode(), fileName);
    }

    //发送信息到主线程
    private void send(int what, Object obj) {
        Message message = Message.obtain();
        message.what = what;
        message.obj = obj;
        handler.sendMessage(message);
    }

    //进度任务
    private class Progress extends TimerTask {
        @Override
        public void run() {
            DecimalFormat df = new DecimalFormat("0.00");
            String rate = df.format((sum / (double) total) * 100) + "%";
            List<String> list = new ArrayList<>();
            list.add(fileName);
            list.add(rate);
            send(DownloadStatus.DOWNLOADING.getCode(), list);
        }
    }
}
