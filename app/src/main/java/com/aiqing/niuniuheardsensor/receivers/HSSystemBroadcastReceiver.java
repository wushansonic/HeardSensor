package com.aiqing.niuniuheardsensor.receivers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aiqing.niuniuheardsensor.services.HSService;

/**
 * Created by blue on 16/3/29.
 */
public class HSSystemBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("HSBroadcastReceiver", intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Log.i("HSBroadcastReceiver", "ACTION_USER_PRESENT");
            openHS(context);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.i("HSBroadcastReceiver", "ACTION_SCREEN_ON");
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.i("HSBroadcastReceiver", "ACTION_SCREEN_OFF");
        }

    }

    private void openHS(Context context) {
        boolean isServiceRunning = false;

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);


        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.aiqing.niuniuheardsensor.services.HSService".equals(service.service.getClassName())) {
                isServiceRunning = true;
            }
        }
        if (!isServiceRunning) {
            Log.i("HSBroadcastReceiver", "startService");
            Intent i = new Intent(context, HSService.class);
            context.startService(i);

        } else {
            Log.i("HSBroadcastReceiver", "do not startService");
        }
    }
}
