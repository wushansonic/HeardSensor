package com.aiqing.niuniuheardsensor.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import com.aiqing.niuniuheardsensor.R;
import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.aiqing.niuniuheardsensor.Utils.record.HSRecordHelper;
import com.aiqing.niuniuheardsensor.activities.HSMainActivity;
import com.aiqing.niuniuheardsensor.listeners.HSPhoneLisener;
import com.aiqing.niuniuheardsensor.receivers.HSPhoneStatusReceiver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by blue on 16/3/14.
 */
public class HSService extends Service {
    private static String TAG = "HS";

    private static final int NOTIFICATION_ID = 2;
    private static final Class<?>[] mSetForegroundSignature = new Class[]{
            boolean.class};
    private static final Class<?>[] mStartForegroundSignature = new Class[]{
            int.class, Notification.class};
    private static final Class<?>[] mStopForegroundSignature = new Class[]{
            boolean.class};
    private NotificationManager mNM;
    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        //来电监听
//        registerPhoneStatusLisener();
//
//        //去电监听
//        IntentFilter outgoingCallFilter = new IntentFilter();
//        outgoingCallFilter.addAction(HSPhoneStatusReceiver.ForeGroundCallState.IDLE);
//        outgoingCallFilter.addAction(HSPhoneStatusReceiver.ForeGroundCallState.DIALING);
//        outgoingCallFilter.addAction(HSPhoneStatusReceiver.ForeGroundCallState.ALERTING);
//        outgoingCallFilter.addAction(HSPhoneStatusReceiver.ForeGroundCallState.ACTIVE);
//        outgoingCallFilter.addAction(HSPhoneStatusReceiver.ForeGroundCallState.DISCONNECTED);
//        outgoingCallFilter.addAction("android.intent.action.PHONE_STATE");
//        outgoingCallFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
//        registerReceiver(new HSPhoneStatusReceiver(), outgoingCallFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundCompat(2);


        //来电监听
        registerPhoneStatusLisener();

        //去电监听
        IntentFilter outgoingCallFilter = new IntentFilter();
        outgoingCallFilter.addAction(HSPhoneStatusReceiver.ForeGroundCallState.IDLE);
        outgoingCallFilter.addAction(HSPhoneStatusReceiver.ForeGroundCallState.DIALING);
        outgoingCallFilter.addAction(HSPhoneStatusReceiver.ForeGroundCallState.ALERTING);
        outgoingCallFilter.addAction(HSPhoneStatusReceiver.ForeGroundCallState.ACTIVE);
        outgoingCallFilter.addAction(HSPhoneStatusReceiver.ForeGroundCallState.DISCONNECTED);
        outgoingCallFilter.addAction("android.intent.action.PHONE_STATE");
        outgoingCallFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        registerReceiver(new HSPhoneStatusReceiver(), outgoingCallFilter);
        return START_STICKY;
    }

    private void registerPhoneStatusLisener() {
        TelephonyManager phoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneManager.listen(new HSPhoneLisener(this, new HSPhoneLisener.CallBack() {
                    @Override
                    public void onRinging(String incomingNumber) {
//                        HSRecordHelper.startRecord_2();
                    }

                    @Override
                    public void onOffHook() {
                        HSRecordHelper.startRecord_3();
                    }

                    @Override
                    public void onIDLE(List<HSRecord> records) {
                        HSRecordHelper.stopRecord_3();
                        openHS(HSService.this);
                    }
                }, false),
                HSPhoneLisener.LISTEN_CALL_STATE);
    }

    private void openHS(Context context) {
        Intent activityIntent = new Intent(context, HSMainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 启动Activity
        context.startActivity(activityIntent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void startForegroundCompat(int id) {


        NotificationManager mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            mStartForeground = HSService.class.getMethod("startForeground", mStartForegroundSignature);
            mStopForeground = HSService.class.getMethod("stopForeground", mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            mStartForeground = mStopForeground = null;
        }

        try {
            mSetForeground = getClass().getMethod("setForeground",
                    mSetForegroundSignature);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "OS doesn't have Service.startForeground OR Service.setForeground!");
        }

        Notification.Builder builder = new Notification.Builder(this);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, HSMainActivity.class), 0);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setTicker("牛牛客服已开启!");
        builder.setContentTitle("牛牛客服");
        builder.setContentText("点击可快速进入应用");
        Notification notification = builder.build();


        if (Build.VERSION.SDK_INT >= 5) {
            startForeground(id, notification);
        } else {
            // Fall back on the old API.
            mSetForegroundArgs[0] = Boolean.TRUE;
            invokeMethod(mSetForeground, mSetForegroundArgs);
            mNM.notify(id, notification);
        }
    }

    private void stopForegroundCompat(int id) {

        if (Build.VERSION.SDK_INT >= 5) {
            stopForeground(true);
        } else {
            // Fall back on the old API.  Note to cancel BEFORE changing the
            // foreground state, since we could be killed at that point.
            mNM.cancel(id);
            mSetForegroundArgs[0] = Boolean.FALSE;
            invokeMethod(mSetForeground, mSetForegroundArgs);
        }
    }

    private void invokeMethod(Method method, Object[] args) {
        try {
            method.invoke(this, args);
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        }
    }
}
