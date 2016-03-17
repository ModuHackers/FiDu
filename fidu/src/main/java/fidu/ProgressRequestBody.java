package fidu;

import android.support.annotation.NonNull;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.internal.Util;

import java.io.File;
import java.io.IOException;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * 文件上传RequestBody,支持上传进度
 * <p/>
 * Created by fengshzh on 16/3/11.
 */
class ProgressRequestBody {

    /**
     * The size of all totalSegments in bytes.
     */
    static final int SIZE = 2048;

    /**
     * Returns a new request body that transmits the content of {@code file}.
     */
    public static RequestBody create(@NonNull final MediaType contentType, @NonNull final File
            file, @NonNull final ResponseDelivery mDelivery, @NonNull final FiDuCallback callback) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return file.length();
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(file);
                    long contentLength = contentLength();
                    long totalBytesRead = 0;
                    while (totalBytesRead < contentLength) {
                        long segmentSize = Math.min(contentLength - totalBytesRead, SIZE);
                        sink.write(source, segmentSize);
                        totalBytesRead += segmentSize;
                        mDelivery.postProgress((int) (totalBytesRead * 100 / contentLength),
                                callback);
                    }
                } finally {
                    Util.closeQuietly(source);
                }
            }
        };
    }
}
