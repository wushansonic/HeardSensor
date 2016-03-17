package com.aiqing.niuniuheardsensor;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.aiqing.niuniuheardsensor.Utils.AbstractSharedPreference;
import com.aiqing.niuniuheardsensor.Utils.SPAppInner;
import com.aiqing.niuniuheardsensor.receivers.HSTimeTickReceiver;

/**
 * Created by blue on 16/3/14.
 */
public class HSApplication extends Application{

    private static Context context;


    public static AbstractSharedPreference asp;

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;

        asp = AbstractSharedPreference.SharedPreferenceFactory.getSharedPreference(context, SPAppInner.class);

        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        HSTimeTickReceiver receiver = new HSTimeTickReceiver();
        registerReceiver(receiver, filter);
    }


    public static Context getContext() {
        return context;
    }
}
