package com.aiqing.niuniuheardsensor.Utils.api;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.aiqing.niuniuheardsensor.HSApplication;
import com.aiqing.niuniuheardsensor.Utils.ToastUtil;
import com.aiqing.niuniuheardsensor.Utils.models.Result;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

/**
 * Created by blue on 16/3/15.
 */
public abstract  class HSHttpResponseHandler<T> extends TextHttpResponseHandler {
    public static final int HTTP_ERROR = 0;
    public static final int NNQC_ERROR = 1;
    public static final int UNEXPECT__ERROR = 2;
    public static final int ERROR_404 = 404;
    public static final String TYPE_DATAS = "datas";
    public static final String TYPE_DATA = "data";
    private static final String TAG = HSHttpResponseHandler.class.getSimpleName();
    protected TypeReference<Result<T>> mTypeReference;
    protected String mDataType = "";
    private Callback callback;

    public HSHttpResponseHandler(TypeReference<Result<T>> typeReference, String... dataType) {
        this.mTypeReference = typeReference;
        if (dataType.length != 0) {
            this.mDataType = dataType[0];
        } else {
            this.mDataType = TYPE_DATA;
        }
    }

    public HSHttpResponseHandler(Callback callback, TypeReference<Result<T>> typeReference, String... dataType) {
        this.callback = callback;
        this.mTypeReference = typeReference;
        if (dataType.length != 0) {
            this.mDataType = dataType[0];
        } else {
            this.mDataType = TYPE_DATA;
        }
    }


    @Override
    public void onFailure(final int statusCode, final Header[] headers, final String responseString, Throwable throwable) {
        if (responseString != null) {
            Runnable parser = new Runnable() {
                @Override
                public void run() {
                    try {
                        postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                if (statusCode == 404 || statusCode == 500 || statusCode == 502) {
                                    onFailure(statusCode, headers, null, ERROR_404, null);
                                } else {
                                    try {
                                        final Result<T> result = (Result<T>) JSON.parseObject(responseString, mTypeReference);
                                        final int status = result.status;
                                        final String notice = result.notice;
                                        onFailure(statusCode, headers, null, status, notice);
                                    } catch (Exception e) {
                                        onFailure(statusCode, headers, null, UNEXPECT__ERROR, null);
                                    }
                                }
                            }
                        });
                    } catch (final Exception ex) {
                        postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                onFailure(statusCode, headers, ex, UNEXPECT__ERROR, null);
                            }
                        });

                    }
                }
            };
            if (!getUseSynchronousMode() && !getUsePoolThread()) {
                new Thread(parser).start();
            } else {
                // In synchronous mode everything should be run on one thread
                parser.run();
            }
        } else {
            Log.v(TAG, "response body is null, calling onFailure(Throwable, JSONObject)");
            onFailure(statusCode, headers, null, HTTP_ERROR, null);
        }
    }



    @Override
    public void onSuccess(final int statusCode, final Header[] headers, final String responseString) {
        Runnable parser = new Runnable() {
            @Override
            public void run() {
                try {
                    final Result<T> result = (Result<T>) JSON.parseObject(responseString, mTypeReference);

                    postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            if (!TextUtils.isEmpty(result.forbidden) && "true".equals(result.forbidden)) {
//                                NNGeeTestHelper.GeeTest(context);
                                if (callback != null)
                                    callback.onNeedCodeCheck();
                                return;
                            }

                            if (result.status == 200) {
                                if (mDataType.equals(TYPE_DATAS)) {
                                    onSuccess(statusCode, headers, (T) result.datas);
                                } else {
                                    onSuccess(statusCode, headers, (T) result.data);
                                }
                                if (!TextUtils.isEmpty(result.message)) {
                                    ToastUtil.showPositions(HSApplication.getContext(), result.message, Toast.LENGTH_SHORT);
                                }
                            } else {
                                if (!TextUtils.isEmpty(result.message)) {
                                    ToastUtil.showPositions(HSApplication.getContext(), result.message, Toast.LENGTH_SHORT);
                                }
                                onFailure(statusCode, headers, null, result.status, result.message);
                            }
                        }
                    });
                } catch (final Exception ex) {
                    postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            onFailure(statusCode, headers, ex, UNEXPECT__ERROR, null);
                        }
                    });
                }
            }
        };
        if (!getUseSynchronousMode() && !getUsePoolThread()) {
            new Thread(parser).start();
        } else {
            // In synchronous mode everything should be run on one thread
            parser.run();
        }
    }

    public void onSuccess(final int statusCode, final Header[] headers, T response) {
        Log.w(TAG, "onSuccess(int, Header[], JSONObject) was not overriden, but callback was received");
    }

    public void onFailure(final int statusCode, final Header[] headers, Throwable throwable, int status, String message) {
        Log.w(TAG, "onSuccess(int, Header[], Throwable, int, String) was not overriden, but callback was received");
    }


    public static interface Callback {
        public void onNeedCodeCheck();
    }
}
