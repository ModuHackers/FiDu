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
    Call download(@NonNull String url, @NonNull String file, @NonNull FiDuCallback callback);

    Call downloadByRange(@NonNull String url, @NonNull String file, @NonNull FiDuCallback callback);

    Call resumeDownloadByRange(@NonNull String url, @NonNull String file, @NonNull FiDuCallback
            callback);

    void cancelDownloadByRange(@NonNull String localFile);
}

