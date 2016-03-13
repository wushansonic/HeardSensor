package com.aiqing.niuniuheardsensor.receivers;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

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

        } else {
            // 如果是来电
            TelephonyManager tManager = (TelephonyManager) context
                    .getSystemService(Service.TELEPHONY_SERVICE);
            switch (tManager.getCallState()) {

                case TelephonyManager.CALL_STATE_RINGING:
                    mIncomingNumber = intent.getStringExtra("incoming_number");
                    Log.i(TAG, "HS RINGING :" + mIncomingNumber);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (mIncomingFlag) {
                        Log.i(TAG, "HS incoming ACCEPT :" + mIncomingNumber);
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (mIncomingFlag) {
                        Log.i(TAG, "HS incoming IDLE");
                    }
                    break;
            }
        }
    }
}
