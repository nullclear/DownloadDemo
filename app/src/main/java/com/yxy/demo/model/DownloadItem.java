package com.yxy.demo.model;

import com.yxy.demo.global.DownloadStatus;
import com.yxy.demo.task.DownloadTask;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Nuclear on 2020/8/15
 */
public class DownloadItem {
    private Integer id;
    private String fileName;
    private String fileType;
    private String parentPath;
    private Long totalLength = 0L;
    private Long savedLength = 0L;
    private String progress = "0.00%";
    private String startTime;
    private String endTime;
    private DownloadStatus downloadStatus;
    private DownloadTask downloadTask;

    public DownloadItem() {
    }

    private DownloadItem(Builder builder) {
        setId(builder.id);
        setFileName(builder.fileName);
        setFileType(builder.fileType);
        setParentPath(builder.parentPath);
        setTotalLength(builder.totalLength);
        setSavedLength(builder.savedLength);
        setProgress(builder.progress);
        setStartTime(builder.startTime);
        setEndTime(builder.endTime);
        setDownloadStatus(builder.downloadStatus);
        setDownloadTask(builder.downloadTask);
    }

    public static Builder Builder() {
        return new Builder();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public Long getTotalLength() {
        return totalLength;
    }

    public int getMax() {
        return Long.valueOf(totalLength / 1024).intValue();
    }

    public void setTotalLength(Long totalLength) {
        this.totalLength = totalLength;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = Integer.valueOf(totalLength).longValue();
    }

    public Long getSavedLength() {
        return savedLength;
    }

    public int getNow() {
        return Long.valueOf(savedLength / 1024).intValue();
    }

    public void setSavedLength(Long savedLength) {
        this.savedLength = savedLength;
    }

    public void setSavedLength(int savedLength) {
        this.savedLength = Integer.valueOf(savedLength).longValue();
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public DownloadTask getDownloadTask() {
        return downloadTask;
    }

    public void setDownloadTask(DownloadTask downloadTask) {
        this.downloadTask = downloadTask;
    }

    @NotNull
    @Override
    public String toString() {
        return "{\"DownloadItem\":{"
                + "\"id\":"
                + id
                + ",\"fileName\":\""
                + fileName + '\"'
                + ",\"fileType\":\""
                + fileType + '\"'
                + ",\"parentPath\":\""
                + parentPath + '\"'
                + ",\"totalLength\":"
                + totalLength
                + ",\"savedLength\":"
                + savedLength
                + ",\"progress\":\""
                + progress + '\"'
                + ",\"startTime\":\""
                + startTime + '\"'
                + ",\"endTime\":\""
                + endTime + '\"'
                + ",\"downloadStatus\":"
                + downloadStatus.getMessage()
                + "}}\n";
    }

    public static final class Builder {
        private Integer id;
        private String fileName;
        private String fileType;
        private String parentPath;
        private Long totalLength = 0L;
        private Long savedLength = 0L;
        private String progress = "0.00%";
        private String startTime;
        private String endTime;
        private DownloadStatus downloadStatus;
        private DownloadTask downloadTask;

        private Builder() {
        }

        public Builder id(Integer val) {
            id = val;
            return this;
        }

        public Builder fileName(String val) {
            fileName = val;
            return this;
        }

        public Builder fileType(String val) {
            fileType = val;
            return this;
        }

        public Builder parentPath(String val) {
            parentPath = val;
            return this;
        }

        public Builder totalLength(Long val) {
            totalLength = val;
            return this;
        }

        public Builder totalLength(int val) {
            totalLength = Integer.valueOf(val).longValue();
            return this;
        }

        public Builder savedLength(Long val) {
            savedLength = val;
            return this;
        }

        public Builder savedLength(int val) {
            savedLength = Integer.valueOf(val).longValue();
            return this;
        }

        public Builder progress(String val) {
            progress = val;
            return this;
        }

        public Builder startTime(String val) {
            startTime = val;
            return this;
        }

        public Builder endTime(String val) {
            endTime = val;
            return this;
        }

        public Builder downloadStatus(DownloadStatus val) {
            downloadStatus = val;
            return this;
        }

        public Builder downloadTask(DownloadTask val) {
            downloadTask = val;
            return this;
        }

        public DownloadItem build() {
            return new DownloadItem(this);
        }
    }
}
