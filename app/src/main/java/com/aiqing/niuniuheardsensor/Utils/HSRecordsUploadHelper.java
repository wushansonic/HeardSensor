package com.aiqing.niuniuheardsensor.Utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.aiqing.niuniuheardsensor.Utils.db.dao.HSRecordsDaos;
import com.zc.RecordDemo.MyAudioRecorder;

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
        List<HSRecord> records = HSRecordsDaos.getInstance(context).getAllRecords();
        HSRecord latestRecord = (records != null && records.size() > 0) ? records.get(0) : null;
        while (hasRecord) {
            int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
            long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
            long number = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.NUMBER));

            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))));
            String time = sfd.format(date);
            hasRecord = cursor.moveToNext();

            if (records == null || records.size() <= 0) {
                HSRecord record_tmp = new HSRecord(date, type, number, duration, "", false, "");
                HSRecordsDaos.getInstance(context).addOneRecord(record_tmp);
                records = HSRecordsDaos.getInstance(context).getAllRecords();
                latestRecord = (records != null && records.size() > 0) ? records.get(0) : null;
                break;
            } else {
                if (date.after(latestRecord.getDate())) {
                    Log.i(TAG, "type:" + type + " duration:" + duration + " number:" + number + " time:" + time);
                    String filePath = "";
//                    if (duration > 0 && type != 3) {
                    filePath = MyAudioRecorder.getAudioMp3Filename();
//                    }
                    HSRecord record = new HSRecord(date, type, number, duration, filePath, false, "");

                    boolean haveSameDate = false;
                    for (HSRecord record1 : resultRecords) {
                        Date date1 = record1.getDate();
                        Date date2 = record.getDate();
                        if (!date1.after(date2) && !date1.before(date2)) {
                            haveSameDate = true;
                            break;
                        }
                    }

                    if (!haveSameDate) {
                        resultRecords.add(record);
                    }
                } else {
                    break;
                }
            }
        }

        if (resultRecords.size() > 0) {
            for (int i = resultRecords.size() - 1; i >= 0; i--) {
                HSRecordsDaos.getInstance(context).addOneRecord(resultRecords.get(i));
            }

            latestRecord.setDate(resultRecords.get(0).getDate());
            HSRecordsDaos.getInstance(context).addOneRecord(latestRecord);
        }

        return resultRecords;
    }
}
