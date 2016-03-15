package com.aiqing.niuniuheardsensor.receivers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aiqing.niuniuheardsensor.activities.HSMainActivity;

import java.util.List;

/**
 * Created by blue on 16/3/14.
 */
public class HSTimeTickReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {

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
                openHS(context);
            }

        }
    }


    private void openHS(Context context) {
        Intent activityIntent = new Intent(context, HSMainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 启动Activity
        context.startActivity(activityIntent);
    }

}
