package com.aiqing.niuniuheardsensor.Utils;

import android.util.Log;

import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by blue on 16/4/13.
 */
public class HSQiniuUploadHelper {


    public static void upload(File f, String key, String token, final Callback callback, final HSRecord record) {

        UploadManager uploadManager = new UploadManager();
        uploadManager.put(f, key, token,
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info, JSONObject res) {

                        if (callback != null)
                            callback.onComplete(record);

                        //  res 包含hash、key等信息，具体字段取决于上传策略的设置。
                        try {
                            Log.i("HS UPLOAD", "key:" + key + ",\r\n " + "info:" + info + ",\r\n " + "res:" + res);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, null);

    }


    static public interface Callback {
        public void onComplete(HSRecord record);
    }
}
