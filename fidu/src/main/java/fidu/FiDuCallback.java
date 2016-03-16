package fidu;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * 文件上传/下载结果回调
 * <p/>
 * Created by fengshzh on 16/3/11.
 */
public interface FiDuCallback {
    /**
     * 成功
     */
    void onResponse(Response response);

    /**
     * 失败
     */
    void onFailure(Request request, Exception e);

    /**
     * 传输进度
     *
     * @param progress 进度百分数(0-100)
     */
    void onProgress(int progress);
}
