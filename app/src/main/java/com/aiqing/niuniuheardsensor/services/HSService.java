package com.aiqing.niuniuheardsensor.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);
    }



}
