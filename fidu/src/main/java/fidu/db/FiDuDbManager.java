package fidu.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import fidu.FiDuUtil;

import static fidu.db.SegmentDownload.SegmentEntry.COLUMN_COMPLETE;
import static fidu.db.SegmentDownload.SegmentEntry.COLUMN_FILE;
import static fidu.db.SegmentDownload.SegmentEntry.COLUMN_SEGMENT_NUM;
import static fidu.db.SegmentDownload.SegmentEntry.COLUMN_TOTAL_SEGMENTS;
import static fidu.db.SegmentDownload.SegmentEntry.COLUMN_URL;
import static fidu.db.SegmentDownload.SegmentEntry.TABLE_NAME;
import static fidu.db.SegmentDownload.SegmentEntry.COLUMN_START;
import static fidu.db.SegmentDownload.SegmentEntry.COLUMN_END;

/**
 * 断点下载数据库管理
 * <p/>
 * Created by fengshzh on 16/3/16.
 */
public class FiDuDbManager {
    private static SegmentDownloadDbHelper mDbHelper;

    public static void init(Context context) {
        mDbHelper = new SegmentDownloadDbHelper(context);
    }

    public static void startSegments(String file, String url, Segment[] segments) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FILE, file);
        values.put(COLUMN_URL, url);
        values.put(COLUMN_TOTAL_SEGMENTS, segments.length);
        db.insert(TABLE_NAME, null, values);

        for (int i = 0; i < segments.length; i++) {
            values = new ContentValues();
            values.put(COLUMN_FILE, segments[i].file);
            values.put(COLUMN_URL, segments[i].url);
            values.put(COLUMN_TOTAL_SEGMENTS, segments[i].totalSegments);
            values.put(COLUMN_SEGMENT_NUM, segments[i].segmentNum);
            values.put(COLUMN_START, segments[i].start);
            values.put(COLUMN_END, segments[i].end);
            values.put(COLUMN_COMPLETE, segments[i].complete);
            db.insert(TABLE_NAME, null, values);
        }
        db.close();
    }

    public static void completeSegment(Segment segment) {
        String file = segment.file;
        int segmentNum = segment.segmentNum;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPLETE, 1);
        String selection = COLUMN_FILE + " is ? and " + COLUMN_SEGMENT_NUM + "=?";
        String[] selectionArgs = {file, String.valueOf(segmentNum)};
        db.update(
                TABLE_NAME,
                values,
                selection,
                selectionArgs);
        db.close();

        segment.complete = 1;

        int totalSegments = segment.totalSegments;
        int completed = querySegmentsCompleted(file);
        if (totalSegments == completed) {
            FiDuUtil.assembleSegments(file, totalSegments);
            cancelSegments(file);
        }
    }

    public static void cancelSegments(String file) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String selection = COLUMN_FILE + " is ?";
        String[] selectionArgs = {file};
        db.delete(TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    public static int querySegments(String file) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {COLUMN_TOTAL_SEGMENTS};
        Cursor cursor = db.query(
                TABLE_NAME,
                projection,
                COLUMN_FILE + " is ? AND " + COLUMN_TOTAL_SEGMENTS + ">0",
                new String[]{file},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        int total = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_SEGMENTS));
        return total;
    }

    public static int querySegmentsCompleted(String file) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {COLUMN_TOTAL_SEGMENTS};
        Cursor c = db.query(
                TABLE_NAME,
                projection,
                COLUMN_FILE + " is ? AND " + COLUMN_COMPLETE + "=1",
                new String[]{file},
                null,
                null,
                null
        );
        int completed = c.getCount();
        db.close();
        return completed;
    }

    public static Segment[] querySegmentsUnCompleted(String url) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {COLUMN_FILE, COLUMN_URL, COLUMN_TOTAL_SEGMENTS, COLUMN_SEGMENT_NUM,
                COLUMN_START, COLUMN_END};
        Cursor cursor = db.query(
                TABLE_NAME,
                projection,
                COLUMN_URL + " is ? AND " + COLUMN_COMPLETE + "=0",
                new String[]{url},
                null,
                null,
                null
        );
        int unComplete = cursor.getCount();
        Segment[] segments = new Segment[unComplete];
        int seg = 0;
        while (cursor.moveToNext()) {
            segments[seg].file = cursor.getString(cursor.getColumnIndexOrThrow
                    (COLUMN_TOTAL_SEGMENTS));
            segments[seg].url = cursor.getString(cursor.getColumnIndexOrThrow
                    (COLUMN_TOTAL_SEGMENTS));
            segments[seg].segmentNum = cursor.getInt(cursor.getColumnIndexOrThrow
                    (COLUMN_TOTAL_SEGMENTS));
            segments[seg].start = cursor.getLong(cursor.getColumnIndexOrThrow
                    (COLUMN_TOTAL_SEGMENTS));
            segments[seg].end = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_SEGMENTS));
            seg++;
        }
        db.close();
        return segments;
    }
}
