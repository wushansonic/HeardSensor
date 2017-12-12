package com.aiqing.niuniuheardsensor.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aiqing.niuniuheardsensor.HSApplication;
import com.aiqing.niuniuheardsensor.R;
import com.aiqing.niuniuheardsensor.Utils.HSQiniuUploadHelper;
import com.aiqing.niuniuheardsensor.Utils.HSRecordsUploadHelper;
import com.aiqing.niuniuheardsensor.Utils.InputUtil;
import com.aiqing.niuniuheardsensor.Utils.SPAppInner;
import com.aiqing.niuniuheardsensor.Utils.api.HSApiHelper;
import com.aiqing.niuniuheardsensor.Utils.api.HSHttpClient;
import com.aiqing.niuniuheardsensor.Utils.api.HSRequestParams;
import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.aiqing.niuniuheardsensor.Utils.db.dao.HSRecordsDaos;
import com.aiqing.niuniuheardsensor.Utils.record.HSRecordHelper;
import com.aiqing.niuniuheardsensor.adapters.HSRecordsAdapter;
import com.aiqing.niuniuheardsensor.services.HSService;
import com.aiqing.niuniuheardsensor.widgets.SweetAlertDialog;
import com.igexin.sdk.PushManager;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HSMainActivity extends HSBaseActivity implements View.OnClickListener {
    private TextView phone_status;
    private TextView change_mobile;
    private ListView record_list;
    private TextView edit;
    private LinearLayout edit_layout;
    private TextView select_all, delete;

    private HSRecordsAdapter recordsAdapter;

    private List<HSRecord> records = new ArrayList<HSRecord>();

    private boolean isEditState = false;
    private boolean isSelectAll = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 1) {
                checkAndUploadRecords();
            }
        }
    };
    private boolean goOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("HS U", "onCreate");


        HSRecordHelper.isPlaying = false;
        setContentView(R.layout.activity_hsmain);

        initViews();


        String myMobile = HSApplication.asp.read(SPAppInner.MOBILE, "");
        if (TextUtils.isEmpty(myMobile))
            showMoblieInputDialog();
        else {
            phone_status.setText("我的号码:" + myMobile);
            HSApiHelper.myMobile = myMobile;
//            checkAndUploadRecords();
            startService(new Intent(this, HSService.class));
            startCheckThread();
        }

        //TODO 删除
        // requestReleaseRecord(this);


        PushManager.getInstance().initialize(this.getApplicationContext());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("HS U", "onNewIntent");

        String myMobile = HSApplication.asp.read(SPAppInner.MOBILE, "");
        if (TextUtils.isEmpty(myMobile))
            showMoblieInputDialog();
        else {
            phone_status.setText("我的号码：" + myMobile);
            HSApiHelper.myMobile = myMobile;
            checkAndUploadRecords();
        }

        List<HSRecord> recordsDB = HSRecordsDaos.getInstance(this).getAllRecords();
        if (recordsDB != null && recordsDB.size() > 0) {
            recordsDB.remove(0);
            records.clear();
            records.addAll(recordsDB);
        }
        recordsAdapter.notifyDataSetChanged();
    }

    private void checkAndUploadRecords() {
//        startService(new Intent(this, HSService.class));

        final List<HSRecord> records_need_upload = HSRecordsUploadHelper.checkNeedUpload(this);

        Log.i("HS U", "need upload records count:" + (records_need_upload == null ? 0 : records_need_upload.size()));

        if (records_need_upload != null && records_need_upload.size() > 0) {

            for (int i = records_need_upload.size() - 1; i >= 0; i--) {
                records.add(records_need_upload.get(i));
            }


            recordsAdapter.notifyDataSetChanged();


            HSApiHelper.requestReleaseRecord(records_need_upload, this, new HSApiHelper.CallBack() {
                @Override
                public void onSuccess(JSONObject response) {
                    //finish();

                    int status = response.optInt("status");

                    if (status != 200) {

                        if (records_need_upload != null && records_need_upload.size() > 0) {


                            for (int i = 0; i < records_need_upload.size(); i++) {
                                HSRecord record = records.get(records.size() - 1 - i);
                                record.setStatus(HSRecord.RECORD_NEED_REUPLOAD);
                                HSRecordsDaos.getInstance(HSMainActivity.this).addOneRecord(record);
                            }

                            recordsAdapter.notifyDataSetChanged();
                        }
                    } else {
                        for (int i = 0; i < records_need_upload.size(); i++) {
                            HSRecord record = records.get(records.size() - 1 - i);
                            record.setStatus(HSRecord.RECORD_UPLOAD_COMPLETE);
                            HSRecordsDaos.getInstance(HSMainActivity.this).addOneRecord(record);
                        }
                        recordsAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure() {
                    //finish();
                    if (records_need_upload != null && records_need_upload.size() > 0) {
                        for (int i = 0; i < records_need_upload.size(); i++) {
                            HSRecord record = records.get(records.size() - 1 - i);
                            record.setStatus(HSRecord.RECORD_NEED_REUPLOAD);
                            HSRecordsDaos.getInstance(HSMainActivity.this).addOneRecord(record);
                        }

                        recordsAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFileUploadSuccess(HSRecord record) {
                    record.setStatus(HSRecord.RECORD_FILE_UPLOAD_COMPLETE);
                    HSRecordsDaos.getInstance(HSMainActivity.this).addOneRecord(record);

                    if (records.size() > 0) {
                        for (int i = records.size() - 1; i >= 0; i--) {
                            HSRecord record1 = records.get(i);
                            if (record1.getId() == record.getId()) {
                                record1.setStatus(HSRecord.RECORD_FILE_UPLOAD_COMPLETE);
                                recordsAdapter.notifyDataSetChanged();
                                break;
                            }
                        }

                    }
                }
            });
        } else {
            //   finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i("HS U", "onResume");

        List<HSRecord> recordsDB = HSRecordsDaos.getInstance(this).getAllRecords();


        if (recordsDB != null && recordsDB.size() > 0) {
            recordsDB.remove(0);
            records.clear();
            records.addAll(recordsDB);
        }
        recordsAdapter.notifyDataSetChanged();

    }


    static public void requestReleaseRecord(Context context) {

        HSRequestParams params = new HSRequestParams();

        List<Map<String, String>> maps = new ArrayList<Map<String, String>>();

        Map<String, String> map = new HashMap<String, String>();

        final List<String> keys = new ArrayList<>();
        final Map<String, HSRecord> keysMap = new HashMap<>();

        map.put("call_type", "out");

        map.put("mobile", "15618863308");

        map.put("duration", "68");

        map.put("started_at", "2016-04-13 10:12:30");

        String key = "" + System.currentTimeMillis();

        map.put("app_key", key);
        map.put("file_type", "mp3");


        keys.add(key);
//        keysMap.put(key, record);

        maps.add(map);

        params.put("issue_phones", maps);


        final String filePath = "/sdcard/heardsensor/1.mp3";

//        try {
//            params.put("audio_record", new File(filePath));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }


        params.put("customer_service_mobile", "15618863308");


        HSHttpClient.instance().post(HSHttpClient.API_ISSUE_PHONES, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);

                        Log.i("HS UPLOAD", response.toString());


                        JSONObject data = response.optJSONObject("data");

                        for (String key : keys) {
                            JSONObject fileObject = data.optJSONObject(key);


                            if (fileObject != null) {
                                String token = fileObject.optString("token");
                                String key_file = fileObject.optString("key");
                                String id = fileObject.optString("id");

                                if (!TextUtils.isEmpty(filePath)) {
                                    HSQiniuUploadHelper.upload(new File(filePath), key_file, token, null, null);
                                }
                            }
                        }


                    }


                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable
                            throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        Log.i("HS UPLOAD", responseString);

                    }
                }

        );


    }


    private boolean isRecording = false;

    private void initViews() {

        phone_status = (TextView) findViewById(R.id.phone_status);
        change_mobile = (TextView) findViewById(R.id.change_mobile);
        change_mobile.setOnClickListener(this);

        record_list = (ListView) findViewById(R.id.record_list);
        recordsAdapter = new HSRecordsAdapter(records, this);
        record_list.setAdapter(recordsAdapter);
        record_list.setDividerHeight(1);

        record_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new SweetAlertDialog(HSMainActivity.this).setHint("请输入管理员密码").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {

                        String mobile = sweetAlertDialog.getMobile();
                        if (mobile.equals("266723")) {
                            if (records.size() > 0 && position < records.size()) {
                                records.get(records.size() - 1 - position).setStatus(HSRecord.RECORD_NEED_REUPLOAD);
                                recordsAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(HSMainActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                        }

                        sweetAlertDialog.dismiss();
                    }
                }).show();
                return false;
            }
        });

        edit = (TextView) findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditState) {
                    isEditState = false;
                    recordsAdapter.setShowSelect(false);
                    recordsAdapter.notifyDataSetChanged();
                    edit_layout.setVisibility(View.GONE);
                    edit.setText("编辑记录");
                } else {
                    isEditState = true;
                    edit_layout.setVisibility(View.VISIBLE);
                    recordsAdapter.setShowSelect(true);
                    for (HSRecord record : records) {
                        record.select = false;
                    }
                    recordsAdapter.notifyDataSetChanged();
                    edit.setText("取消编辑");
                }
            }
        });
        edit_layout = (LinearLayout) findViewById(R.id.edit_layout);

        select_all = (TextView) findViewById(R.id.select_all);
        select_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (records == null || records.size() <= 0)
                    return;

                if (!isSelectAll) {
                    for (HSRecord record : records) {
                        record.select = true;
                    }
                    isSelectAll = true;
                    select_all.setText("取消");
                } else {
                    for (HSRecord record : records) {
                        record.select = false;
                    }
                    isSelectAll = false;
                    select_all.setText("全选");
                }
                recordsAdapter.notifyDataSetChanged();
            }
        });

        delete = (TextView) findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (records == null || records.size() <= 0)
                    return;


                new SweetAlertDialog(HSMainActivity.this).showCancelButton(true).hideEdit(false).setTitleText("确定删除记录吗?").setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                }).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        List<Integer> ids = new ArrayList<Integer>();

                        for (int i = 0; i < records.size(); i++) {
                            HSRecord record = records.get(i);

                            if (record.select) {
                                ids.add(record.getId());
                                if (!TextUtils.isEmpty(record.getFile_path()))
                                    HSRecordHelper.deleteRecordFile(record.getFile_path());
                                records.remove(i--);
                            }
                        }

                        if (ids.size() > 0) {
                            recordsAdapter.notifyDataSetChanged();
                            HSRecordsDaos.getInstance(HSMainActivity.this).deleteRecordsByIds(ids);
                        }
                    }
                }).show();


            }
        });
    }

    private void showMoblieInputDialog() {

        new SweetAlertDialog(this).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {

                String mobile = sweetAlertDialog.getMobile();
                if (checkMobile(mobile, sweetAlertDialog.getDialog_mobile_edit())) {
                    HSApplication.asp.write(SPAppInner.MOBILE, mobile);
                    HSApiHelper.myMobile = mobile;
                    sweetAlertDialog.dismiss();

                    phone_status.setText("我的号码：" + mobile);

                    startCheckThread();
                }
            }
        }).show();
    }


    private boolean checkMobile(String mMobile, EditText editText) {
        if (TextUtils.isEmpty(mMobile)) {
            editText.setError("请输入手机号");
            return false;
        } else if (!InputUtil.isMobileValid(mMobile)) {
            editText.setError("手机号码不正确");
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_mobile:
                showMoblieInputDialog();
                break;
        }
    }

    private void startCheckThread() {


        checkAndUploadRecords();
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//
//
//                while (goOn) {
//                    handler.sendEmptyMessage(1);
//
//                    try {
//                        Thread.sleep(1000 * 5);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        goOn = false;
        HSRecordHelper.isPlaying = false;
    }
}
