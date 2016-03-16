package fidu;

import android.support.annotation.NonNull;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件上传/下载接口实现,基于OkHttp
 * <p/>
 * Created by fengshzh on 16/3/16.
 */
public class FiDu implements FiDuApi {
    private static final String TAG = "FiDu";

    private static OkHttpClient mHttpClient = new OkHttpClient();

    /**
     * 上传文件
     *
     * @param url         上传地址
     * @param file        本地文件
     * @param contentType 文件MIME,如"image/jpeg"
     * @param callback    回调
     */
    @Override
    public Call upload(@NonNull String url, @NonNull String file, @NonNull String contentType,
                       @NonNull final FiDuCallback callback) {
        File localFile = new File(file);

        Request request = new Request.Builder()
                .url(url)
                .post(ProgressRequestBody.create(MediaType.parse(contentType), localFile, callback))
                .build();
        final Call call = mHttpClient.newCall(request);
        FiDuLog.d(TAG, "Call ready");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                FiDuLog.d(TAG, "onFailure");
                callback.onFailure(request, e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                FiDuLog.d(TAG, "onResponse");
                callback.onResponse(response);
            }
        });

        return call;
    }

    /**
     * 下载文件
     *
     * @param url      文件地址
     * @param file     本地存储位置
     * @param callback 回调
     */
    @Override
    public Call download(@NonNull String url, @NonNull String file, @NonNull final FiDuCallback
            callback) {
        final File localFile = new File(file);
        localFile.getParentFile().mkdirs();
        Call call;
        final Request request = new Request.Builder()
                .url(url)
                .build();
        call = mHttpClient.newCall(request);
        FiDuLog.d(TAG, "Call ready");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                FiDuLog.d(TAG, "onFailure");
                callback.onFailure(request, e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                FiDuLog.d(TAG, "onResponse");
                if (response.isSuccessful()) {
                    InputStream in = null;
                    FileOutputStream fos = null;
                    try {
                        in = response.body().byteStream();
                        fos = new FileOutputStream(localFile);
                        long total = response.body().contentLength();
                        long got = 0;
                        int len;
                        byte[] buf = new byte[2048];

                        while ((len = in.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                            got += len;
                            callback.onProgress((int) (got * 100 / total));
                        }
                        callback.onProgress(100);
                        fos.flush();
                        callback.onResponse(response);
                    } catch (IOException e) {
                        FiDuLog.d(TAG, e.toString());
                        callback.onFailure(request, e);
                    } finally {
                        try {
                            if (in != null) in.close();
                            if (fos != null) fos.close();
                        } catch (IOException e) {
                            FiDuLog.e(TAG, e.toString());
                        }
                    }
                }
            }
        });

        return call;
    }

    @Override
    public Call downloadByRange(@NonNull String url, @NonNull File file,
                                @NonNull FiDuCallback callback) {
        return null;
    }

    @Override
    public Call resumeDownloadByRange(@NonNull String url, @NonNull String file,
                                      @NonNull FiDuCallback callback) {
        return null;
    }

    @Override
    public void cancelDownloadByRange(@NonNull String localFile) {

    }
}
