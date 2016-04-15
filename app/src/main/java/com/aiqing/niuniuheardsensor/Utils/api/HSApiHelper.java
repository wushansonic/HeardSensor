package com.aiqing.niuniuheardsensor.Utils.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.aiqing.niuniuheardsensor.Utils.HSQiniuUploadHelper;
import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
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


        List<Map<String, String>> maps = new ArrayList<Map<String, String>>();

        final List<String> keys = new ArrayList<>();
        final Map<String, HSRecord> keysMap = new HashMap<>();

        for (HSRecord record : records) {
            Map<String, String> map = new HashMap<String, String>();
            if (record.getType() == 1 || record.getType() == 3) {
                map.put("call_type", "in");
            } else if (record.getType() == 2) {
                map.put("call_type", "out");
            }

            map.put("mobile", record.getNumber() + "");

            if (!TextUtils.isEmpty(record.getReupload_id()) && !record.getReupload_id().equals("-1")) {
                map.put("id", record.getReupload_id());
            }

            map.put("duration", String.valueOf(record.getDuration()));

            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = sfd.format(record.getDate());
            map.put("started_at", time);

            String filePath = record.getFile_path();
            if (!TextUtils.isEmpty(filePath) && record.getDuration() > 0) {
                String key = "" + System.currentTimeMillis();

                map.put("app_key", key);
                map.put("file_type", "mp3");

                keys.add(key);
                keysMap.put(key, record);
            }

            maps.add(map);
        }

        params.put("issue_phones", maps);

        params.put("customer_service_mobile", myMobile);


        Log.i(TAG, "issue_phones:" + maps + " customer_service_mobile:" + myMobile);


        HSHttpClient.instance().post(HSHttpClient.API_ISSUE_PHONES, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                Log.i(TAG, response.toString());

                if (callBack != null)
                    callBack.onSuccess(response);


                JSONObject data = response.optJSONObject("data");

                if (data == null)
                    return;

                for (String key : keys) {
                    JSONObject fileObject = data.optJSONObject(key);

                    if (fileObject != null) {
                        String token = fileObject.optString("token");
                        String key_file = fileObject.optString("key");
                        int id = fileObject.optInt("id");
                        HSRecord record = keysMap.get(key);
                        record.setReupload_id(String.valueOf(id));
                        String filePath = null;
                        if (record != null) {
                            filePath = record.getFile_path();
                        }
                        if (!TextUtils.isEmpty(filePath)) {
                            HSQiniuUploadHelper.upload(new File(filePath), key_file, token, new HSQiniuUploadHelper.Callback() {
                                @Override
                                public void onComplete(HSRecord record) {
                                    callBack.onFileUploadSuccess(record);
                                }
                            }, record);
                        }
                    }
                }

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

        public void onFileUploadSuccess(HSRecord record);
    }

}
