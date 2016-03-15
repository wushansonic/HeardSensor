package com.aiqing.niuniuheardsensor.listeners;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.aiqing.niuniuheardsensor.Utils.HSRecordsUploadHelper;
import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;

import java.util.List;

/**
 * Created by blue on 16/3/11.
 */
public class HSPhoneLisener extends PhoneStateListener {
    private final Context context;
    //获取本次通话的时间(单位:秒)
    int time = 0;
    //判断是否正在通话
    boolean isCalling;
    //控制循环是否结束
    boolean isFinish;
//    private ExecutorService service;

    private CallBack callBack;
    private boolean needCheck;


    public HSPhoneLisener(Context context, CallBack callBack, boolean needCheck) {
        this.context = context;
        this.callBack = callBack;
        this.needCheck = needCheck;
//        service = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {

        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                if (isCalling) {
                    isCalling = false;
                    isFinish = true;
//                    service.shutdown();
                    time = 0;
                    List<HSRecord> records = null;
                    if (needCheck) {
                        records = HSRecordsUploadHelper.checkNeedUpload(context);
                    }

                    if (callBack != null) {
                        callBack.onIDLE(records);
                    }
                }
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                isCalling = true;
//                service.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        while (!isFinish) {
//                            try {
//                                Thread.sleep(1000);
//                                time++;
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                });
                if (callBack != null) {
                    callBack.onOffHook();
                }
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                isFinish = false;
                isCalling = true;
//                if (service.isShutdown()) {
//                    service = Executors.newSingleThreadExecutor();
//                }
                if (callBack != null) {
                    callBack.onRinging(incomingNumber);
                }
                break;
        }
    }


    static public interface CallBack {
        public void onRinging(String incomingNumber);

        public void onOffHook();

        public void onIDLE(List<HSRecord> records);
    }


}
