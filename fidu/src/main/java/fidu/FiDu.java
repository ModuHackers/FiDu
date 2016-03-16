package fidu;

import android.os.Handler;
import android.os.Looper;
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
    private static ResponseDelivery mDelivery = new ExecutorDelivery(new Handler(Looper
            .getMainLooper()));

    public static FiDu getInstance() {
        return InstanceHolder.mInstance;
    }

    private static class InstanceHolder {
        static final FiDu mInstance = new FiDu();
    }

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

        final Request request = new Request.Builder()
                .url(url)
                .post(ProgressRequestBody.create(MediaType.parse(contentType), localFile, mDelivery, callback))
                .build();
        final Call call = mHttpClient.newCall(request);
        FiDuLog.d(TAG, "Call ready");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                FiDuLog.d(TAG, "onFailure");
                mDelivery.postFailure(call, request, e, callback);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                FiDuLog.d(TAG, "onResponse");
                mDelivery.postResponse(call, response, callback);
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
        final Call call;
        final Request request = new Request.Builder()
                .url(url)
                .build();
        call = mHttpClient.newCall(request);
        FiDuLog.d(TAG, "Call ready");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                FiDuLog.d(TAG, "onFailure");
                mDelivery.postFailure(call, request, e, callback);
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
                            mDelivery.postProgress((int) (got * 100 / total), callback);
                        }
                        mDelivery.postProgress(100, callback);
                        fos.flush();
                        mDelivery.postResponse(call, response, callback);
                    } catch (IOException e) {
                        FiDuLog.d(TAG, e.toString());
                        mDelivery.postFailure(call, request, e, callback);
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
    public Call downloadByRange(@NonNull final String url, @NonNull final String file,
                                @NonNull final FiDuCallback callback) {
        final File localFile = new File(file);
        localFile.getParentFile().mkdirs();
        // TODO
        localFile.delete();
        try {
            localFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Call headCall;
        Request request = new Request.Builder()
                .url(url)
                .method("HEAD", null) // TODO HEAD方法不读取body
                .build();

        headCall = mHttpClient.newCall(request);
        headCall.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                mDelivery.postFailure(headCall, request, e, callback);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    long bodyLength = Long.parseLong(response.header("Content-Length"));
                    FiDuLog.d(TAG, "bodyLength: " + bodyLength);
                    // TODO 分片
                    downloadRange(url, 0, 0, bodyLength - 1, file, callback);
                } else {
                    mDelivery.postResponse(headCall, response, callback);
                }
            }
        });

        return headCall;
    }

    public Call downloadRange(@NonNull final String url, int segmentNum, long start, long
            end, @NonNull final String file, @NonNull final FiDuCallback callback) {
        final File segmentFile = new File(file + "_" + segmentNum);
        segmentFile.getParentFile().mkdirs();
        // TODO
        segmentFile.delete();
        try {
            segmentFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        segmentFile.getParentFile().mkdirs();
        final Call call;
        final Request request = new Request.Builder()
                .url(url)
                .header("Range", "bytes=" + start + "-" + end)
                .build();
        call = mHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                mDelivery.postFailure(call, request, e, callback);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream in = null;
                    FileOutputStream fos = null;
                    try {
                        in = response.body().byteStream();
                        fos = new FileOutputStream(segmentFile);
                        long total = response.body().contentLength();
                        long got = 0;
                        int len;
                        byte[] buf = new byte[2048];

                        while ((len = in.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                            got += len;
                            mDelivery.postProgress((int) (got * 100 / total), callback);
                        }
                        mDelivery.postProgress(100, callback);
                        fos.flush();
                        mDelivery.postResponse(call, response, callback);
                    } catch (IOException e) {
                        mDelivery.postFailure(call, request, e, callback);
                    } finally {
                        try {
                            if (in != null) in.close();
                            if (fos != null) fos.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        });

        return call;
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
