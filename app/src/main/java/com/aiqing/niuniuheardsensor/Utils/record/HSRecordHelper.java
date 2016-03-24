package com.aiqing.niuniuheardsensor.Utils.record;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import com.zc.RecordDemo.MyAudioRecorder;

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

    private static String path = "/sdcard/heardsensor/";

    private static boolean haveStarted = false;

    public static String currentFilePath = "";


    private static MediaPlayer mediaPlayer;
    public static boolean isPlaying = false;

    private static MyAudioRecorder recorder;


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
        File pp = new File(path);
        if (!pp.isDirectory() && !pp.exists()) {
            pp.mkdir();
        }

        String fileName = new SimpleDateFormat(
                "yyyyMMddHHmmss").format(System
                .currentTimeMillis())
                + ".wav";
        AudioFileFunc.setFileName(fileName);
        AudiorecordFunc.getInstance().startRecordAndFile();
    }

    public static void stopRecord_2() {
        AudiorecordFunc.getInstance().stopRecordAndFile();
    }

    public static void startRecord_3() {
        if (recorder == null) {
            recorder = new MyAudioRecorder();
            recorder.prepare();
        }


        File pp = new File(path);
        if (!pp.isDirectory() && !pp.exists()) {
            pp.mkdir();
        }

        String fileName_mp3 = new SimpleDateFormat(
                "yyyyMMddHHmmss").format(System
                .currentTimeMillis())
                + ".mp3";

        recorder.setFileName_mp3(fileName_mp3);
        recorder.startRecording();
    }

    public static void stopRecord_3() {
        recorder.stopRecording();
    }


    public static void play(String filePath) {
        if (isPlaying)
            return;

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopPlay() {
        //position = mediaPlayer.getCurrentPosition();
        mediaPlayer.stop();
        isPlaying = false;
    }


    public static void deleteRecordFile(String filePath) {
        File f = new File(filePath);
        if (f.exists()) {
            f.delete();
        }
    }
}
