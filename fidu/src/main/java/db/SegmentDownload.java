package db;

import android.provider.BaseColumns;

/**
 * 断点下载表结构定义
 * <p/>
 * Created by fengshzh on 16/3/16.
 */
public final class SegmentDownload {
    public SegmentDownload() {
    }

    public static abstract class SegmentEntry implements BaseColumns {
        public static final String TABLE_NAME = "segment";
        public static final String COLUMN_FILE = "file";
        public static final String COLUMN_SEGMENTS = "segments";
        public static final String COLUMN_SEGMENT = "segment";
        public static final String COLUMN_COMPLETE = "complete";
    }
}
