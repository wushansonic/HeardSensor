package com.aiqing.niuniuheardsensor.Utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.aiqing.niuniuheardsensor.Utils.db.dao.HSRecordsDaos;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by blue on 16/3/14.
 */
public class HSRecordsUploadHelper {
    private static final String TAG = "HS_RECORDS_UPLOAD";
    public static final int INCOMING_TYPE = 1;
    public static final int OUTGOING_TYPE = 2;
    public static final int MISSED_TYPE = 3;


    public static List<HSRecord> checkNeedUpload(Context context) {
        List<HSRecord> resultRecords = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                new String[]{CallLog.Calls.DURATION, CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.NUMBER},
                null,
                null,
                CallLog.Calls.DEFAULT_SORT_ORDER);
        ((Activity) context).startManagingCursor(cursor);
        boolean hasRecord = cursor.moveToFirst();
        if (hasRecord) Log.i(TAG, "=============================");
        while (hasRecord) {
            int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
            long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
            long number = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.NUMBER));

            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))));
            String time = sfd.format(date);
            hasRecord = cursor.moveToNext();

            List<HSRecord> records = HSRecordsDaos.getInstance(context).getAllRecords();

            if (records == null && records.size() <= 0) {
                HSRecordsDaos.getInstance(context).addOneRecord(new HSRecord(date, type, number, duration));
                break;
            } else {
                HSRecord latestRecord = records.get(records.size() - 1);
                if (date.after(latestRecord.getDate())) {
                    Log.i(TAG, "type:" + type + " duration:" + duration + " number:" + number + " time:" + time);
                    HSRecord record = new HSRecord(date, type, number,duration);
                    HSRecordsDaos.getInstance(context).addOneRecord(record);
                    resultRecords.add(record);
                } else {
                    break;
                }
            }



        }

        return resultRecords;
    }
}
