package com.zc.RecordDemo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 7/1/14  11:00 AM
 * Created by JustinZhang.
 */
public class MyAudioRecorder {

    private static final String TAG = "MyAudioRecorder";
    private AudioRecord mRecorder = null;
    //private MediaPlayer mPlayer = null;

    public static final int SAMPLE_RATE = 44100;

    private Mp3Conveter mConveter;
    private short[] mBuffer;
    private boolean mIsRecording = false;
    private File mRawFile;
    private File mEncodedFile;

    public static String AUDIO_MP3_FILENAME = "FinalAudio.mp3";
    private final static String AUDIO_RAW_FILENAME = "RawAudio.raw";

    private static String path = "/sdcard/heardsensor";

    public static void setFileName_mp3(String fileName) {
        AUDIO_MP3_FILENAME = fileName;
    }

    public void prepare() {
//        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT);
//        mBuffer = new short[bufferSize];
//        mRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_CALL, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        mRecorder = findAudioRecord();
        mConveter = new Mp3Conveter();
        mConveter.SAMPLE_RATE = rate_cur;
    }


    private static int[] mSampleRates = new int[]{44100, 22050, 16000, 11025, 8000};

    private static int rate_cur = 44100;

    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[]{AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT}) {
                for (short channelConfig : new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO}) {
                    try {

                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success


                            mBuffer = new short[bufferSize];
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                                rate_cur = rate;
                                return recorder;
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        return null;
    }


    /**
     * 开始录音
     */
    public void startRecording() {

        if (mIsRecording) {
            return;
        }

        Log.e(TAG, "startRcording");
        if (mRecorder == null) {
            Log.e(TAG, "mRocorder is nul this should not happen");
            return;
        }
        mIsRecording = true;
        mRecorder.startRecording();
        mRawFile = new File(path + "/" + AUDIO_RAW_FILENAME);
        startBufferedWrite(mRawFile);
    }

    private void startBufferedWrite(final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataOutputStream output = null;
                try {
                    output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
                    while (mIsRecording) {

                        if (mIsPause) {
                            continue;
                        }

                        int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);
                        for (int i = 0; i < readSize; i++) {
                            output.writeShort(mBuffer[i]);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (output != null) {
                        try {
                            output.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                output.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private boolean mIsPause = false;

    public void pauseRecording() {
        mIsPause = true;
    }

    public void restartRecording() {
        mIsPause = false;
    }


    public void stopRecording() {
        Log.e(TAG, "stopRecording");
        if (mRecorder == null) {
            return;
        }
        if (!mIsRecording) {
            return;
        }
        mRecorder.stop();
        mIsPause = false;
        mIsRecording = false;
        mEncodedFile = new File(path + "/" + AUDIO_MP3_FILENAME);
        mConveter.encodeFile(mRawFile.getAbsolutePath(), mEncodedFile.getAbsolutePath());
    }


    public void release() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mIsPause = false;
            mIsRecording = false;
        }

        if (mConveter != null)
            mConveter.destroyEncoder();
    }


    public static String getAudioMp3Filename() {
        return path + "/" + AUDIO_MP3_FILENAME;
    }
}
