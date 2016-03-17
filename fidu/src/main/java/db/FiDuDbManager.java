package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static db.SegmentDownload.SegmentEntry.COLUMN_COMPLETE;
import static db.SegmentDownload.SegmentEntry.COLUMN_FILE;
import static db.SegmentDownload.SegmentEntry.COLUMN_SEGMENT;
import static db.SegmentDownload.SegmentEntry.COLUMN_SEGMENTS;
import static db.SegmentDownload.SegmentEntry.TABLE_NAME;

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

    public static void startSegments(String file, int segments) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FILE, file);
        values.put(COLUMN_SEGMENTS, segments);
        db.insert(TABLE_NAME, null, values);

        for (int i = 0; i < segments; i++) {
            values = new ContentValues();
            values.put(COLUMN_FILE, file);
            values.put(COLUMN_SEGMENTS, segments);
            values.put(COLUMN_SEGMENT, i);
            db.insert(TABLE_NAME, null, values);
        }
        db.close();
    }

    public static void completeSegment(String file, int segmentNum) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPLETE, 1);
        String selection = COLUMN_FILE + " is ? and " + COLUMN_SEGMENT + "=?";
        String[] selectionArgs = {file, String.valueOf(segmentNum)};
        db.update(
                TABLE_NAME,
                values,
                selection,
                selectionArgs);
        db.close();

        int total = querySegments(file);
        int completed = querySegmentsCompleted(file);
        if (total == completed) {
            assembleSegments(file, total);
            cancelSegments(file);
        }
    }

    public static void assembleSegments(String file, int segments) {
        File completeFile = new File(file);
        completeFile.delete();
        File firstSegment = new File(file + "_" + 0);
        firstSegment.renameTo(completeFile);
        try {
            FileOutputStream out = new FileOutputStream(file, true);
            for (int i = 1; i < segments; i++) {
                File segmentFile = new File(file + "_" + i);
                InputStream in = new FileInputStream(segmentFile);
                byte[] buf = new byte[2048];
                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                out.flush();
                segmentFile.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        String[] projection = {COLUMN_SEGMENTS};
        Cursor cursor = db.query(
                TABLE_NAME,
                projection,
                COLUMN_FILE + " is ? AND " + COLUMN_SEGMENTS + ">0",
                new String[]{file},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        int total = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SEGMENTS));
        return total;
    }

    public static int querySegmentsCompleted(String file) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {COLUMN_SEGMENTS};
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
}
