package fidu;

import android.util.Log;

/**
 * 文件上传/下载Log
 * <p/>
 * Created by fengshzh on 16/3/11.
 */
public class FiDuLog {
    private static boolean mDebug = true;

    private FiDuLog() {
    }

    public static void debug(boolean debug) {
        mDebug = debug;
    }

    public static void d(String tag, String msg) {
        if (mDebug)
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (mDebug)
            Log.e(tag, msg);
    }
}
