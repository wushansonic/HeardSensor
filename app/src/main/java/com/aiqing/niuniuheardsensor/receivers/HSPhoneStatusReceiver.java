package com.aiqing.niuniuheardsensor.receivers;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.aiqing.niuniuheardsensor.activities.HSMainActivity;
import com.aiqing.niuniuheardsensor.services.HSService;

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
        // 如果是拨打电话
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            mIncomingFlag = false;
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.i(TAG, "HS call OUT:" + phoneNumber);

            context.startService(new Intent(context, HSService.class));

            openHS(context);


        } else {
            // 如果是来电
            TelephonyManager tManager = (TelephonyManager) context
                    .getSystemService(Service.TELEPHONY_SERVICE);
            switch (tManager.getCallState()) {

                case TelephonyManager.CALL_STATE_RINGING:
                    mIncomingNumber = intent.getStringExtra("incoming_number");
                    mIncomingFlag = true;
                    Log.i(TAG, "HS RINGING :" + mIncomingNumber);
                    openHS(context);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.i(TAG, "HS incoming ACCEPT :" + mIncomingNumber);
                    openHS(context);
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.i(TAG, "CALL_STATE_IDLE");
                    openHS(context);
                    break;
            }
        }
    }


    private void openHS(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        boolean isAppRunning = false;
        String MY_PKG_NAME = "com.aiqing.niuniuheardsensor";
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(MY_PKG_NAME) || info.baseActivity.getPackageName().equals(MY_PKG_NAME)) {
                isAppRunning = true;
                break;
            }
        }

        if (!isAppRunning) {
        Intent activityIntent = new Intent(context, HSMainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 启动Activity
        context.startActivity(activityIntent);
        }
    }


}
