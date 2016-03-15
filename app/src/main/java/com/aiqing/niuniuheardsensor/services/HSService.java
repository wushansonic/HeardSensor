package com.aiqing.niuniuheardsensor.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import com.aiqing.niuniuheardsensor.Utils.HSRecordHelper;
import com.aiqing.niuniuheardsensor.Utils.HSRecordsUploadHelper;
import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.aiqing.niuniuheardsensor.activities.HSMainActivity;
import com.aiqing.niuniuheardsensor.listeners.HSPhoneLisener;
import com.aiqing.niuniuheardsensor.receivers.HSPhoneStatusReceiver;

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
                        HSRecordHelper.startRecord();
                    }

                    @Override
                    public void onOffHook() {
                        HSRecordHelper.startRecord();
                    }

                    @Override
                    public void onIDLE(List<HSRecord> records) {
                        HSRecordHelper.stopRecord();
                        openHS(HSService.this);

                        if (records != null && records.size() > 0) {
                            String str = "";
                            for (HSRecord record : records) {

                                SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                String time = sfd.format(record.getDate());

                                String type = "";
                                switch (record.getType()) {
                                    case HSRecordsUploadHelper.INCOMING_TYPE:
                                        type = "接听";
                                        break;
                                    case HSRecordsUploadHelper.MISSED_TYPE:
                                        type = "未接";
                                        break;
                                    case HSRecordsUploadHelper.OUTGOING_TYPE:
                                        type = "打出";
                                        break;
                                }


                                str += "ID:" + record.getId() + " type:" + type + " time:" + time + " number:" + record.getNumber() + " duration:" + record.getDuration()
                                        + "\n";
                            }

                        }
                    }
                }, false),
                HSPhoneLisener.LISTEN_CALL_STATE);
    }


//    private void requestReleaseRecord(final List<HSRecord> records) {
//        HSRequestParams params = new HSRequestParams();
//
//        List<Map<String, String>> maps = new ArrayList<>();
//        for (HSRecord record : records) {
//            Map<String, String> map = new HashMap<>();
//            if (record.getType() == 1 || record.getType() == 3) {
//                map.put("call_type", "in");
//            } else if (record.getType() == 2) {
//                map.put("call_type", "out");
//            }
//
//            map.put("mobile", record.getNumber() + "");
//
//            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//            String time = sfd.format(record.getDate());
//            map.put("ringed_at", time);
//            map.put("connected_at", time);
//            map.put("ended_at", time);
//
//            maps.add(map);
//        }
//
//        params.put("issue_phones", maps);
//
//        TelephonyManager phoneMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//        params.put("customer_service_mobile", "15802162343");
//
//
//        Log.i("HS", "issue_phones:" + maps + " customer_service_mobile:15802162343");
//
//
//        HSHttpClient.instance().post(HSHttpClient.API_ISSUE_PHONES, params, new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                super.onSuccess(statusCode, headers, response);
//
//                Log.i("HS", response.toString());
//
//            }
//
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                super.onFailure(statusCode, headers, responseString, throwable);
//                Log.i("HS", responseString);
//            }
//        });
//    }


    private void openHS(Context context) {
//        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
//        boolean isAppRunning = false;
//        String MY_PKG_NAME = "com.aiqing.niuniuheardsensor";
//        for (ActivityManager.RunningTaskInfo info : list) {
//            if (info.topActivity.getPackageName().equals(MY_PKG_NAME) || info.baseActivity.getPackageName().equals(MY_PKG_NAME)) {
//                isAppRunning = true;
//                break;
//            }
//        }
//
//        if (!isAppRunning) {
//
//        }

        Intent activityIntent = new Intent(context, HSMainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 启动Activity
        context.startActivity(activityIntent);
    }


}
