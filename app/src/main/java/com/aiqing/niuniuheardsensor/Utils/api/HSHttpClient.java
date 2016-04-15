package com.aiqing.niuniuheardsensor.Utils.api;

import com.aiqing.niuniuheardsensor.HSApplication;
import com.aiqing.niuniuheardsensor.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.SyncHttpClient;

/**
 * Created by blue on 16/3/15.
 */
public class HSHttpClient {

    private static AsyncHttpClient sInstance = null;
    private final static String V = "v4";

    public static synchronized AsyncHttpClient instance() {
        if (sInstance == null) {
            sInstance = new AsyncHttpClient(API_HTTP_PORT);
            sInstance.setUserAgent("");
        }
        return sInstance;
    }

    public static synchronized SyncHttpClient instanceForSync() {
        return new SyncHttpClient(API_HTTP_PORT);
    }


    private static final String API_HOST = HSApplication.getContext().getResources().getString(R.string.API_HOST);
    private static final int API_HTTP_PORT = HSApplication.getContext().getResources().getInteger(R.integer.API_HTTP_PORT);

    public static final String API_ISSUE_PHONES = API_HOST + "/api/" + V + "/issue_phones/create_issue_phone";
}
