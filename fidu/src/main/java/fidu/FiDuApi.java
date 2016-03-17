package fidu;

import android.support.annotation.NonNull;

import com.squareup.okhttp.Call;

/**
 * 文件上传/下载接口定义
 * <p/>
 * Created by fengshzh on 16/3/8.
 */
public interface FiDuApi {
    /**
     * 上传文件
     *
     * @param url         上传地址
     * @param file        本地文件
     * @param contentType 文件MIME,如"image/jpeg"
     * @param callback    回调
     */
    Call upload(@NonNull String url, @NonNull String file, @NonNull String contentType,
                @NonNull FiDuCallback callback);

    /**
     * 下载文件
     *
     * @param url      文件地址
     * @param file     本地存储位置
     * @param callback 回调
     */
    void download(@NonNull String url, @NonNull String file, @NonNull FiDuCallback callback);

    /**
     * 使用分片方式下载文件
     *
     * @param url      文件地址
     * @param file     本地存储位置
     * @param callback 回调
     */
    void downloadBySegments(@NonNull String url, @NonNull String file, @NonNull FiDuCallback
            callback);

    /**
     * 暂停分片下载
     *
     * @param url 下载地址
     */
    void pauseDownloadBySegments(@NonNull String url);

    /**
     * 重试分片下载
     *
     * @param url      下载地址
     * @param callback 回调
     */
    void resumeDownloadBySegments(@NonNull String url, @NonNull FiDuCallback callback);

    /**
     * 取消分片下载
     * @param url 下载w
     */
    void cancelDownloadBySegments(@NonNull String url);
}

