package com.aiqing.niuniuheardsensor.Utils.record;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by blue on 16/3/15.
 */
public class HSRecordHelper {
    private static final String TAG = "AudioRecordTest";
    //语音操作对象
    private static MediaRecorder mRecorder = null;
    private static String path = "/sdcard/niuniuqiche/records/";

    private static boolean haveStarted = false;

    public static String currentFilePath = "";


    public static void startRecord() {
        if (!haveStarted) {

            haveStarted = true;

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);


            File pp = new File(path);
            if (!pp.isDirectory() && !pp.exists()) {
                pp.mkdir();
            }


            currentFilePath = path
                    + new SimpleDateFormat(
                    "yyyyMMddHHmmss").format(System
                    .currentTimeMillis())
                    + ".mp3";
            File saveFilePath = new File(currentFilePath);


            mRecorder.setOutputFile(saveFilePath
                    .getAbsolutePath());

            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e(TAG, "prepare() failed");
            }
            mRecorder.start();
        }
    }

    public static void stopRecord() {
        haveStarted = false;
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }


    //type2 test

    public static void startRecord_2() {
        AudiorecordFunc.getInstance().startRecordAndFile();
    }

    public static void stopRecord_2() {
        AudiorecordFunc.getInstance().stopRecordAndFile();
    }
}
