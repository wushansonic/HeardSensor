package com.aiqing.niuniuheardsensor.listeners;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by blue on 16/3/11.
 */
public class HSPhoneLisener extends PhoneStateListener {
    private static final String TAG = "HS_PHONE_LISENER";
    private final Context context;
    //获取本次通话的时间(单位:秒)
    int time = 0;
    //判断是否正在通话
    boolean isCalling;
    //控制循环是否结束
    boolean isFinish;
    private ExecutorService service;

    public HSPhoneLisener(Context context) {
        this.context = context;
        service = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {

        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                if (isCalling) {
                    isCalling = false;
                    isFinish = true;
                    service.shutdown();
                    Toast.makeText(context, "本次通话" + time + "秒",
                            Toast.LENGTH_LONG).show();
                    Log.i(TAG,  "本次通话" + time + "秒");
                    time = 0;
                }
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                isCalling = true;
                service.execute(new Runnable() {
                    @Override
                    public void run() {
                        while (!isFinish) {
                            try {
                                Thread.sleep(1000);
                                time++;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                isFinish = false;
                if (service.isShutdown()) {
                    service = Executors.newSingleThreadExecutor();
                }
                break;
        }
    }
}
