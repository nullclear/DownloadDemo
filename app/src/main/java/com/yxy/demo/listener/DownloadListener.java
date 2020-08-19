package com.yxy.demo.listener;

/**
 *Created by Nuclear on 2020/8/19
 */
public interface DownloadListener {
    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    void onPaused();

    void onCanceled();
}
