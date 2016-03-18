package com.aiqing.niuniuheardsensor.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aiqing.niuniuheardsensor.Utils.record.HSRecordHelper;
import com.aiqing.niuniuheardsensor.activities.HSMainActivity;

/**
 * Created by blue on 16/3/11.
 */
public class HSPhoneStatusReceiver extends BroadcastReceiver {

    private static final String TAG = "message";
    private static boolean mIncomingFlag = false;
    private static String mIncomingNumber = null;

    @Override
    public void onReceive(Context context, Intent intent) {
//        // 如果是拨打电话
//        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
//            mIncomingFlag = false;
//            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
//            Log.i(TAG, "HS call OUT:" + phoneNumber);
//
//
//            openHS(context);
//
//
//        } else {
//            // 如果是来电
//            TelephonyManager tManager = (TelephonyManager) context
//                    .getSystemService(Service.TELEPHONY_SERVICE);
//            switch (tManager.getCallState()) {
//
//                case TelephonyManager.CALL_STATE_RINGING:
//                    mIncomingNumber = intent.getStringExtra("incoming_number");
//                    mIncomingFlag = true;
//                    Log.i(TAG, "HS RINGING :" + mIncomingNumber);
//                    startHSService(context,0);
//                    openHS(context);
//                    break;
//                case TelephonyManager.CALL_STATE_OFFHOOK:
//                    Log.i(TAG, "HS incoming ACCEPT :" + mIncomingNumber);
//                    openHS(context);
//                    break;
//                case TelephonyManager.CALL_STATE_IDLE:
//                    Log.i(TAG, "CALL_STATE_IDLE");
//                    startHSService(context, 1);
//                    openHS(context);
//                    break;
//            }
//        }


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
            HSRecordHelper.startRecord_2();
        }

        if (phoneState.equals(ForeGroundCallState.DISCONNECTED)) {
            HSRecordHelper.stopRecord_2();

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
