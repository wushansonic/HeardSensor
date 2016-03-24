package com.aiqing.niuniuheardsensor.Utils.db.dao;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.CallLog;

import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by blue on 16/3/14.
 */
public class HSRecordsDaos extends OrmLiteSqliteOpenHelper {
    private static String TABLE_NAME = "ns_tele_record.db";
    private static final int VSERSION = 4;
    private Dao<HSRecord, Integer> recordsDao;
    private static Context context;


    private HSRecordsDaos(Context context) {
        super(context, TABLE_NAME, null, VSERSION);
    }


    public static void clearInstance() {
        instance = null;
    }

    private static HSRecordsDaos instance;

    public static synchronized HSRecordsDaos getInstance(Context _context) {
        context = _context;
        if (instance == null) {
            synchronized (HSRecordsDaos.class) {
                if (instance == null) {
                    instance = new HSRecordsDaos(context);
                }
            }
        }
        try {
            TableUtils.createTableIfNotExists(instance.getConnectionSource(), HSRecord.class);


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, HSRecord.class);
            initRecordsDB(context);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
//            updateColumn(sqLiteDatabase, "hs_records", "reupload_id", "char(20)", "");
            TableUtils.dropTable(connectionSource, HSRecord.class, true);
            onCreate(sqLiteDatabase, connectionSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Dao<HSRecord, Integer> getRecordsDao() throws SQLException {
        if (recordsDao == null) {
            recordsDao = getDao(HSRecord.class);
        }
        return recordsDao;
    }

    public List<HSRecord> getAllRecords() {
        List<HSRecord> records = null;
        try {
            records = getRecordsDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return records;
    }

    public void addOneRecord(HSRecord record) {
        try {
            getRecordsDao().createOrUpdate(record);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearAllRecords() {

        try {
            TableUtils.clearTable(getConnectionSource(), HSRecord.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放资源
     */
    @Override
    public void close() {
        super.close();
        recordsDao = null;
    }

    public synchronized void updateColumn(SQLiteDatabase db, String tableName,
                                          String columnName, String columnType, Object defaultField) {
        try {
            if (db != null) {
                Cursor c = db.rawQuery("SELECT * from " + tableName
                        + " limit 1 ", null);
                boolean flag = false;

                if (c != null) {
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        if (columnName.equalsIgnoreCase(c.getColumnName(i))) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        String sql = "alter table " + tableName + " add "
                                + columnName + " " + columnType + " default "
                                + defaultField;
                        db.execSQL(sql);
                    }
                    c.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initRecordsDB(Context context) {
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                new String[]{CallLog.Calls.DURATION, CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.NUMBER},
                null,
                null,
                CallLog.Calls.DEFAULT_SORT_ORDER);
        ((Activity) context).startManagingCursor(cursor);
        boolean hasRecord = cursor.moveToFirst();
        long incoming = 0L;
        long outgoing = 0L;
        while (hasRecord) {
            int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
            long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
            long number = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.NUMBER));

            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))));
            String time = sfd.format(date);

            addOneRecord(new HSRecord(date, type, number, duration, "", false, ""));

            break;
        }
    }

}
