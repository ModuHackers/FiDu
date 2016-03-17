package fidu.db;

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
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_FILE = "file";
        public static final String COLUMN_TOTAL_SEGMENTS = "total_segments";
        // 主文件与segment通过COLUMN_SEGMENT_NUM判断,-1为主文件,>=0为segment文件
        public static final String COLUMN_SEGMENT_NUM = "segment_num";
        public static final String COLUMN_START = "start";
        public static final String COLUMN_END = "end";
        public static final String COLUMN_COMPLETE = "complete";
    }
}
