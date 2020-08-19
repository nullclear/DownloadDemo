package com.yxy.demo.global;

/**
 * Created by Nuclear on 2020/8/4
 */
public enum DownloadStatus {
    START_DOWNLOAD(2, "开始下载"),
    DOWNLOADING(1, "下载中..."),
    DOWNLOAD_SUCCESS(0, "下载完成!"),
    DOWNLOAD_FAILED(-1, "下载失败!"),
    URL_ERROR(-2, "下载链接出错!"),
    FILENAME_ERROR(-3, "文件名错误!"),
    DIRECTORY_CREATE_FAILED(-4, "下载目录创建失败!"),
    DIRECTORY_WRITE_DENY(-5, "下载目录拒绝写入!"),
    FILE_EXIST(-6, "同名文件已存在!"),
    UNKNOWN_ERROR(-999, "未知错误!");

    private int code;
    private String message;

    DownloadStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static String getMessage(int code) {
        for (DownloadStatus status : DownloadStatus.values()) {
            if (status.getCode() == code) {
                return status.getMessage();
            }
        }
        return UNKNOWN_ERROR.getMessage();
    }

    public static DownloadStatus getType(int code) {
        for (DownloadStatus status : DownloadStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return UNKNOWN_ERROR;
    }
}
