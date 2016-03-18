package com.aiqing.niuniuheardsensor.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aiqing.niuniuheardsensor.activities.HSMainActivity;

/**
 * Created by blue on 16/3/18.
 */
public class HSBootBroadcastReceiver extends BroadcastReceiver {
    static final String action_boot = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(action_boot)) {
            Log.i("HS UPLOAD", "ACTION_BOOT_COMPLETED");
            Intent ootStartIntent = new Intent(context, HSMainActivity.class);
            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(ootStartIntent);
        }
    }

}
