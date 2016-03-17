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

    private static final int MAIN_FILE_SEGMENT_NUM = -1;

    public static void init(Context context) {
        mDbHelper = new SegmentDownloadDbHelper(context);
    }

    public static void startSegments(String url, String file, Segment[] segments) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FILE, file);
        values.put(COLUMN_URL, url);
        values.put(COLUMN_TOTAL_SEGMENTS, segments.length);
        values.put(COLUMN_SEGMENT_NUM, MAIN_FILE_SEGMENT_NUM);
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
        String url = segment.url;
        int segmentNum = segment.segmentNum;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPLETE, 1);
        String selection = COLUMN_URL + " is ? and " + COLUMN_SEGMENT_NUM + "=?";
        String[] selectionArgs = {url, String.valueOf(segmentNum)};
        db.update(
                TABLE_NAME,
                values,
                selection,
                selectionArgs);
        db.close();

        segment.complete = 1;

        int totalSegments = segment.totalSegments;
        int completed = querySegmentsCompleted(url);
        if (totalSegments == completed) {
            FiDuUtil.assembleSegments(segment.file, totalSegments);
            cancelSegments(url);
        }
    }

    public static void cancelSegments(String url) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String selection = COLUMN_URL + " is ?";
        String[] selectionArgs = {url};
        db.delete(TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    public static int querySegments(String url) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {COLUMN_TOTAL_SEGMENTS};
        Cursor cursor = db.query(
                TABLE_NAME,
                projection,
                COLUMN_URL + " is ? and " + COLUMN_TOTAL_SEGMENTS + ">0",
                new String[]{url},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        int total = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_SEGMENTS));
        return total;
    }

    public static int querySegmentsCompleted(String url) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {COLUMN_TOTAL_SEGMENTS};
        Cursor c = db.query(
                TABLE_NAME,
                projection,
                COLUMN_URL + " is ? AND " + COLUMN_COMPLETE + "=1",
                new String[]{url},
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
        String[] projection = {COLUMN_URL, COLUMN_FILE, COLUMN_TOTAL_SEGMENTS, COLUMN_SEGMENT_NUM,
                COLUMN_START, COLUMN_END, COLUMN_COMPLETE};
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
            segments[seg] = new Segment();
            segments[seg].url = cursor.getString(cursor.getColumnIndexOrThrow
                    (COLUMN_URL));
            segments[seg].file = cursor.getString(cursor.getColumnIndexOrThrow
                    (COLUMN_FILE));
            segments[seg].totalSegments = cursor.getInt(cursor.getColumnIndexOrThrow
                    (COLUMN_TOTAL_SEGMENTS));
            segments[seg].segmentNum = cursor.getInt(cursor.getColumnIndexOrThrow
                    (COLUMN_SEGMENT_NUM));
            segments[seg].start = cursor.getLong(cursor.getColumnIndexOrThrow
                    (COLUMN_START));
            segments[seg].end = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_END));
            segments[seg].complete = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETE));
            seg++;
        }
        db.close();
        return segments;
    }
}
