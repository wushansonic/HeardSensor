package com.aiqing.niuniuheardsensor;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import com.aiqing.niuniuheardsensor.receivers.HSTimeTickReceiver;

/**
 * Created by blue on 16/3/14.
 */
public class HSApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();


        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        HSTimeTickReceiver receiver = new HSTimeTickReceiver();
        registerReceiver(receiver, filter);
    }
}
