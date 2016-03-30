package com.aiqing.niuniuheardsensor.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.aiqing.niuniuheardsensor.Utils.HSRecordsUploadHelper;
import com.aiqing.niuniuheardsensor.Utils.api.HSApiHelper;
import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.aiqing.niuniuheardsensor.Utils.db.dao.HSRecordsDaos;
import com.aiqing.niuniuheardsensor.Utils.record.HSRecordHelper;
import com.aiqing.niuniuheardsensor.activities.HSMainActivity;
import com.aiqing.niuniuheardsensor.listeners.HSPhoneLisener;
import com.aiqing.niuniuheardsensor.receivers.HSPhoneStatusReceiver;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by blue on 16/3/14.
 */
public class HSService extends Service {
    private static String TAG = "HS";


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //来电监听
        registerPhoneStatusLisener();

        //去电监听
        IntentFilter outgoingCallFilter = new IntentFilter();
        outgoingCallFilter.addAction(HSPhoneStatusReceiver.ForeGroundCallState.IDLE);
        outgoingCallFilter.addAction(HSPhoneStatusReceiver.ForeGroundCallState.DIALING);
        outgoingCallFilter.addAction(HSPhoneStatusReceiver.ForeGroundCallState.ALERTING);
        outgoingCallFilter.addAction(HSPhoneStatusReceiver.ForeGroundCallState.ACTIVE);
        outgoingCallFilter.addAction(HSPhoneStatusReceiver.ForeGroundCallState.DISCONNECTED);
        outgoingCallFilter.addAction("android.intent.action.PHONE_STATE");
        outgoingCallFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        registerReceiver(new HSPhoneStatusReceiver(), outgoingCallFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerPhoneStatusLisener() {
        TelephonyManager phoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneManager.listen(new HSPhoneLisener(this, new HSPhoneLisener.CallBack() {
                    @Override
                    public void onRinging(String incomingNumber) {
//                        HSRecordHelper.startRecord_2();
                    }

                    @Override
                    public void onOffHook() {
                        HSRecordHelper.startRecord_3();
                    }

                    @Override
                    public void onIDLE(List<HSRecord> records) {
                        HSRecordHelper.stopRecord_3();
                        checkAndUploadRecords(HSService.this);
                        openHS(HSService.this);
                    }
                }, false),
                HSPhoneLisener.LISTEN_CALL_STATE);
    }


    private void openHS(Context context) {
        Intent activityIntent = new Intent(context, HSMainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 启动Activity
        context.startActivity(activityIntent);
    }

    private void checkAndUploadRecords(Context context) {

        final List<HSRecord> records_need_upload = HSRecordsUploadHelper.checkNeedUpload(context);

//        final HSRecord record = (records_need_upload != null && records_need_upload.size() > 0) ? records_need_upload.get(0) : null;

        Log.i("HS U", "need upload records count:" + (records_need_upload == null ? 0 : records_need_upload.size()));

        if (records_need_upload != null && records_need_upload.size() > 0) {

            HSApiHelper.requestReleaseRecord(records_need_upload, context, new HSApiHelper.CallBack() {
                @Override
                public void onSuccess(JSONObject response) {
                    //finish();

                    int status = response.optInt("status");
                    int id = response.optInt("id");

                    if (status != 200) {

                        if (records_need_upload != null && records_need_upload.size() > 0) {

                            if (status == 201)
                                records_need_upload.get(0).setReupload_id(String.valueOf(id));
                            else
                                records_need_upload.get(0).setReupload_id(String.valueOf(-1));

                            HSRecordsDaos.getInstance(context).addOneRecord(records_need_upload.get(0));
                        }
                    }
                }

                @Override
                public void onFailure() {
                    //finish();
                    if (records_need_upload != null && records_need_upload.size() > 0) {
                        records_need_upload.get(0).setReupload_id(String.valueOf(-1));
                        HSRecordsDaos.getInstance(context).addOneRecord(records_need_upload.get(0));
                    }
                }
            });
        } else {
            //   finish();
        }
    }


}
