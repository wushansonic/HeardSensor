package com.aiqing.niuniuheardsensor.Utils.api;

import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by blue on 16/3/15.
 */
public class HSRequestParams extends RequestParams {


    public void put(String key, List<Map<String, String>> objectMaps) {
        JSONArray jsonArray = new JSONArray();

        for (Map<String, String> o : objectMaps) {
            JSONObject jsonObject = new JSONObject();

            for (String key1 : o.keySet()) {
                try {
                    jsonObject.put(key1, o.get(key1));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            jsonArray.put(jsonObject);
        }
        put(key, jsonArray.toString());
    }


}
