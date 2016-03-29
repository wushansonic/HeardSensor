package com.aiqing.niuniuheardsensor.Utils.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.zc.RecordDemo.MyAudioRecorder;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by blue on 16/3/15.
 */
public class HSApiHelper {
    private static final String TAG = "HS UPLOAD";

    public static String myMobile = "";

    static public void requestReleaseRecord(final List<HSRecord> records, Context context, final CallBack callBack) {
        if (records != null && records.size() <= 0)
            return;


        HSRequestParams params = new HSRequestParams();


        List<Map<String, String>> maps = new ArrayList<>();

        for (HSRecord record : records) {
            Map<String, String> map = new HashMap<>();
            if (record.getType() == 1 || record.getType() == 3) {
                map.put("call_type", "in");
            } else if (record.getType() == 2) {
                map.put("call_type", "out");
            }

            map.put("mobile", record.getNumber() + "");

            if (!TextUtils.isEmpty(record.getReupload_id()) && !record.getReupload_id().equals("-1")) {
                map.put("id", record.getReupload_id());
            }

            map.put("duration", record.getType() == 3 ? "0" : String.valueOf(record.getDuration()));

            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = sfd.format(record.getDate());
            map.put("started_at", time);

            maps.add(map);
        }

        params.put("issue_phones", maps);


        long duration = records.get(0).getType() == 3 ? 0 : records.get(0).getDuration();

        if (duration > 0) {
            try {
                String filePath = MyAudioRecorder.getAudioMp3Filename();
                params.put("audio_record", new File(filePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        params.put("customer_service_mobile", myMobile);


        Log.i(TAG, "issue_phones:" + maps + " customer_service_mobile:" + myMobile);


        HSHttpClient.instance().post(HSHttpClient.API_ISSUE_PHONES, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                Log.i(TAG, response.toString());

                if (callBack != null)
                    callBack.onSuccess(response);

            }


            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.i(TAG, responseString);
                if (callBack != null)
                    callBack.onFailure();
            }
        });


    }


    static public interface CallBack {
        public void onSuccess(JSONObject response);

        public void onFailure();
    }

}
