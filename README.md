FiDu
---
基于OkHttp的Android文件上传/下载库。

# 使用方法

## 1. 上传
上传使用HTTP POST请求。
```java
String url = "...";
String filePath = "...";
String fileType = "...";
FiDu.getInstance().upload(url, filePath, fileType, new FiDuCallback() {
    @Override
    public void onResponse(Response response) {
        ...
    }
    @Override
    public void onFailure(Request request, Exception e) {
        ...
    }
    @Override
    public void onProgress(int progress) {
        mProgressBar.setProgress(progress);
    }
});
```

## 2. 下载
### 2.1 整文件下载
一个文件通过一个请求下载。
```java
String url = "...";
String localPath = "...";
FiDu.getInstance().download(url, localPath, new FiDuCallback() {
    @Override
    public void onResponse(Response response) {
        ...
    }
    @Override
    public void onFailure(Request request, Exception e) {
        ...
    }
    @Override
    public void onProgress(int progress) {
        mProgressBar.setProgress(progress);
    }
});
```

### 2.2 大文件分片下载（慎用）
一个文件通过分片下载，默认分片大小为1M。分片使用多线程下载，所有分片下载完成后会组装成一个整文件。若下载失败，重试下载时只下载未下载成功的分片。
分片下载基于HTTP协议的请求头部的`Range`字段实现。服务器若未针对优化，分片下载速度较慢，不建议使用。
### 初始化
Application的`onCreate()`里初始化Context：
```java
@Override
public void onCreate() {
    super.onCreate();
    FiDu.init(this);
}
```
### 分片下载
```java
String url = "...";
String localPath = "...";
```

##### - 开始
```java
FiDu.getInstance().downloadBySegments(url, localPath, new FiDuCallback() {
    @Override
    public void onResponse(Response response) {
        ...
    }
    @Override
    public void onFailure(Request request, Exception e) {
        ...
    }
    @Override
    public void onProgress(int progress) {
        mProgressBar.setProgress(progress);
    }
});
```

##### - 暂停
```java
FiDu.getInstance().pauseDownloadBySegments(url);
```

##### - 重试
```java
FiDu.getInstance().resumeDownloadBySegments(url, localPath, new FiDuCallback() {
    @Override
    public void onResponse(Response response) {
        ...
    }
    @Override
    public void onFailure(Request request, Exception e) {
        ...
    }
    @Override
    public void onProgress(int progress) {
        mProgressBar.setProgress(progress);
    }
});
```

##### - 取消
```java
FiDu.getInstance().cancelDownloadBySegments(url);
```
