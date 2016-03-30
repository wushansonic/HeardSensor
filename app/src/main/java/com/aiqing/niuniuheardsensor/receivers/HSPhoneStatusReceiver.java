package com.aiqing.niuniuheardsensor.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aiqing.niuniuheardsensor.Utils.HSRecordsUploadHelper;
import com.aiqing.niuniuheardsensor.Utils.api.HSApiHelper;
import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.aiqing.niuniuheardsensor.Utils.db.dao.HSRecordsDaos;
import com.aiqing.niuniuheardsensor.Utils.record.HSRecordHelper;
import com.aiqing.niuniuheardsensor.activities.HSMainActivity;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by blue on 16/3/11.
 */
public class HSPhoneStatusReceiver extends BroadcastReceiver {

    private static final String TAG = "message";
    private static boolean mIncomingFlag = false;
    private static String mIncomingNumber = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        String phoneState = intent.getAction();
        if (phoneState.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            String phoneNum = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);//拨出号码
            Log.d(TAG, "设置为去电状态");
            Log.d(TAG, "去电状态 呼叫：" + phoneNum);
        }

        if (phoneState.equals(ForeGroundCallState.DIALING)) {
            Log.d(TAG, "正在拨号...");
        }

        if (phoneState.equals(ForeGroundCallState.ALERTING)) {
            Log.d(TAG, "正在呼叫...");
        }

        if (phoneState.equals(ForeGroundCallState.ACTIVE)) {
            HSRecordHelper.startRecord_3();
        }

        if (phoneState.equals(ForeGroundCallState.DISCONNECTED)) {
            HSRecordHelper.stopRecord_3();
            checkAndUploadRecords(context);
            openHS(context);
        }
    }


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
//            Intent activityIntent = new Intent(context, HSMainActivity.class);
//            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            // 启动Activity
//            context.startActivity(activityIntent);
//        }

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


    public static final class ForeGroundCallState {
        public static final String DIALING =
                "com.sdvdxl.phonerecorder.FORE_GROUND_DIALING";
        public static final String ALERTING =
                "com.sdvdxl.phonerecorder.FORE_GROUND_ALERTING";
        public static final String ACTIVE =
                "com.sdvdxl.phonerecorder.FORE_GROUND_ACTIVE";
        public static final String IDLE =
                "com.sdvdxl.phonerecorder.FORE_GROUND_IDLE";
        public static final String DISCONNECTED =
                "com.sdvdxl.phonerecorder.FORE_GROUND_DISCONNECTED";
    }


}
