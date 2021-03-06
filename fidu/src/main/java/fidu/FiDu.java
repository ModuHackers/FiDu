package fidu;

import android.content.Context;
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

import fidu.db.FiDuDbManager;
import fidu.db.Segment;

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

    private static final int SEGMENT_SIZE = 1024 * 1024; // 下载分片大小,1M

    public static void init(Context context) {
        FiDuDbManager.init(context.getApplicationContext());
    }


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
                .post(ProgressRequestBody.create(MediaType.parse(contentType), localFile,
                        mDelivery, callback))
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
    public void download(@NonNull String url, @NonNull String file, @NonNull final FiDuCallback
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
                        fos.flush();
                        mDelivery.postProgress(100, callback);
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
    }

    @Override
    public void downloadBySegments(@NonNull final String url, @NonNull final String file,
                                   @NonNull final FiDuCallback callback) {
        final File localFile = new File(file);
        localFile.getParentFile().mkdirs();
        localFile.delete();
        FiDuDbManager.cancelSegments(url);

        final Call headCall;
        Request request = new Request.Builder()
                .url(url)
                .method("HEAD", null)
                .tag(url)
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

                    int totalSegments = (int) (bodyLength / SEGMENT_SIZE + (bodyLength %
                            SEGMENT_SIZE == 0 ? 0 : 1));
                    Segment[] segments = new Segment[totalSegments];
                    long start = 0;
                    for (int seg = 0; seg < totalSegments; seg++) {
                        long segSize = Math.min(SEGMENT_SIZE, bodyLength - start);
                        long end = start + segSize - 1;
                        segments[seg] = new Segment();
                        segments[seg].file = file;
                        segments[seg].url = url;
                        segments[seg].totalSegments = totalSegments;
                        segments[seg].segmentNum = seg;
                        segments[seg].start = start;
                        segments[seg].end = end;
                        segments[seg].complete = 0;
                        start += segSize;
                    }

                    FiDuDbManager.startSegments(url, file, segments);
                    for (int seg = 0; seg < totalSegments; seg++) {
                        // TODO 使用父请求callback不合适
                        downloadSegment(segments[seg], callback);
                    }
                } else {
                    mDelivery.postResponse(headCall, response, callback);
                }
            }
        });
    }

    private Call downloadSegment(@NonNull final Segment segment, @NonNull final FiDuCallback
            callback) {
        final String file = segment.file;
        final int segmentNum = segment.segmentNum;
        final long start = segment.start;
        final long end = segment.end;
        final String url = segment.url;

        final File segmentFile = new File(file + "_" + segmentNum);
        segmentFile.getParentFile().mkdirs();
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
                .tag(url)
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
                        fos.flush();
                        FiDuDbManager.completeSegment(segment);
                        mDelivery.postProgress(100, callback);
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
    public void pauseDownloadBySegments(@NonNull String url) {
        mHttpClient.cancel(url);
    }

    @Override
    public void resumeDownloadBySegments(@NonNull String url, @NonNull FiDuCallback callback) {
        Segment[] segments = FiDuDbManager.querySegmentsUnCompleted(url);
        for (Segment segment : segments) {
            downloadSegment(segment, callback);
        }
    }

    @Override
    public void cancelDownloadBySegments(@NonNull String url) {
        FiDuUtil.deleteSegments(url, FiDuDbManager.querySegments(url));
        FiDuDbManager.cancelSegments(url);
    }
}
