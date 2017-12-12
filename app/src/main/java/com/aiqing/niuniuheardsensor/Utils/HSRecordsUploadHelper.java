package com.aiqing.niuniuheardsensor.Utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

import com.aiqing.niuniuheardsensor.HSApplication;
import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.aiqing.niuniuheardsensor.Utils.db.dao.HSRecordsDaos;
import com.zc.RecordDemo.MyAudioRecorder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        List<HSRecord> resultRecords = new ArrayList<HSRecord>();


        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                new String[]{CallLog.Calls.DURATION, CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.NUMBER},
                null,
                null,
                CallLog.Calls.DEFAULT_SORT_ORDER);
        ((Activity) context).startManagingCursor(cursor);
        boolean hasRecord = cursor.moveToFirst();
//        List<HSRecord> records = HSRecordsDaos.getInstance(context).getAllRecords();
//        HSRecord latestRecord = (records != null && records.size() > 0) ? records.get(0) : null;


        HSRecord latestRecord = HSRecordsDaos.getInstance(context).getLatestRecord();

        System.out.println("record date: " + "=====================");

//        while (hasRecord) {
//            Date date = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))));
//            System.out.println("record date: " + date);
//            hasRecord = cursor.moveToNext();
//        }


        while (hasRecord) {
            int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
            long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));

            if (type == 3)
                duration = 0;
            String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));

            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))));
            System.out.println("record date: " + date);


            if (type == 1) {

                long lastFileTime = HSApplication.asp.read(SPAppInner.LastFileRecordDATE, 0l);
                if (lastFileTime > 0) {
                    Date lastFileDate = new Date(lastFileTime);
                    if (lastFileDate.before(date)) {
                        duration = 0;
                        type = 3;
                    }
                }
            }

            String time = sfd.format(date);
            hasRecord = cursor.moveToNext();


            if (latestRecord == null) {
                HSRecord record_tmp = new HSRecord(date, type, number, duration, "", false, "", HSRecord.RECORD_UPLOADING);
                HSRecordsDaos.getInstance(context).addOneRecord(record_tmp);
                break;
            } else {
                if (date.after(latestRecord.getDate())) {
                    Log.i(TAG, "type:" + type + " duration:" + duration + " number:" + number + " time:" + time);
                    String filePath = "";
//                    filePath = MyAudioRecorder.getAudioMp3Filename();

                    if (duration > 0) {
                        File recFile = getRecFile(date, duration);
                        if (recFile != null) {
                            filePath = recFile.getPath();
                        }
                    }

                    HSRecord record = new HSRecord(date, type, number, duration, filePath, false, "", HSRecord.RECORD_UPLOADING);

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

//            latestRecord.setDate(resultRecords.get(0).getDate());
//            HSRecordsDaos.getInstance(context).addOneRecord(latestRecord);
        }

        return resultRecords;
    }


    private static File getRecFile(Date date, long duration) {
        List<File> recFiles = getRecFiles(new File("mnt/sdcard/record"), date, duration);
        Collections.sort(recFiles, new FileComparator());

        if (recFiles.size() > 0) {
            return recFiles.get(0);
        } else {
            return null;
        }
    }

    private static List<File> getRecFiles(File file, Date date, long duration) {
        List<File> recFiles = new ArrayList<>();
        File[] fileArray = file.listFiles();
        for (File f : fileArray) {
            if (f.isFile() && f.getName().endsWith(".amr") && date.getTime() < f.lastModified() && f.lastModified() - date.getTime() - duration * 1000 < 1 * 60 * 1000) {
                recFiles.add(f);
            }
        }
        return recFiles;
    }

    static class FileComparator implements Comparator<File> {

        @Override
        public int compare(File file1, File file2) {
            if (file1.lastModified() < file2.lastModified()) {
                return -1;// 最后修改的文件在前
            } else {
                return 1;
            }
        }
    }
}
