package com.yxy.demo.application;

import android.annotation.SuppressLint;
import android.app.Application;
import com.yxy.demo.model.DownloadItem;
import com.yxy.demo.task.DownloadTask;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Nuclear on 2020/8/15
 */
@SuppressLint("Registered")
public class MyApplication extends Application {
    //下载条目id
    private static AtomicInteger downloadId = new AtomicInteger(1);
    //线程安全的下载条目List
    private static List<DownloadItem> downloadProgressList = new CopyOnWriteArrayList<>();
    //线程安全的下载条目Map
    private static Map<Integer, DownloadItem> downloadItemMap = new ConcurrentHashMap<>();
    //通知ID
    private static AtomicInteger notificationId = new AtomicInteger(1);

    public static Integer getDownloadId() {
        return downloadId.getAndIncrement();
    }

    public static Integer getNotificationId() {
        return notificationId.getAndIncrement();
    }

    public static List<DownloadItem> getDownloadList() {
        //调用前先排序
        downloadProgressList.sort(Comparator.comparing(DownloadItem::getId));
        return downloadProgressList;
    }

    public static Map<Integer, DownloadItem> getDownloadItemMap() {
        return downloadItemMap;
    }
}
