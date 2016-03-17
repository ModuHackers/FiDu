package fidu.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 断点下载数据库
 * <p/>
 * Created by fengshzh on 16/3/16.
 */
public class SegmentDownloadDbHelper extends SQLiteOpenHelper {

    // TODO 文件和segment是否要分表

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "fidu.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SegmentDownload.SegmentEntry.TABLE_NAME + " (" +
                    SegmentDownload.SegmentEntry._ID + " INTEGER PRIMARY KEY," +
                    SegmentDownload.SegmentEntry.COLUMN_FILE + TEXT_TYPE + COMMA_SEP +
                    SegmentDownload.SegmentEntry.COLUMN_URL + TEXT_TYPE + COMMA_SEP +
                    SegmentDownload.SegmentEntry.COLUMN_TOTAL_SEGMENTS + TEXT_TYPE + COMMA_SEP +
                    SegmentDownload.SegmentEntry.COLUMN_SEGMENT_NUM + TEXT_TYPE + COMMA_SEP +
                    SegmentDownload.SegmentEntry.COLUMN_START + TEXT_TYPE + COMMA_SEP +
                    SegmentDownload.SegmentEntry.COLUMN_END + TEXT_TYPE + COMMA_SEP +
                    SegmentDownload.SegmentEntry.COLUMN_COMPLETE + TEXT_TYPE
                    + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SegmentDownload.SegmentEntry.TABLE_NAME;

    public SegmentDownloadDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
