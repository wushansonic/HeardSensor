package com.aiqing.niuniuheardsensor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.aiqing.niuniuheardsensor.HSApplication;
import com.aiqing.niuniuheardsensor.R;
import com.aiqing.niuniuheardsensor.Utils.HSRecordsUploadHelper;
import com.aiqing.niuniuheardsensor.Utils.InputUtil;
import com.aiqing.niuniuheardsensor.Utils.SPAppInner;
import com.aiqing.niuniuheardsensor.Utils.api.HSApiHelper;
import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.aiqing.niuniuheardsensor.Utils.db.dao.HSRecordsDaos;
import com.aiqing.niuniuheardsensor.adapters.HSRecordsAdapter;
import com.aiqing.niuniuheardsensor.services.HSService;
import com.aiqing.niuniuheardsensor.widgets.SweetAlertDialog;

import java.util.ArrayList;
import java.util.List;


public class HSMainActivity extends HSBaseActivity implements View.OnClickListener {
    private TextView phone_status;
    private TextView change_mobile;
    private ListView record_list;
    private HSRecordsAdapter recordsAdapter;

    private List<HSRecord> records = new ArrayList<>();

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

        setContentView(R.layout.activity_hsmain);

        initViews();


        String myMobile = HSApplication.asp.read(SPAppInner.MOBILE, "");
        if (TextUtils.isEmpty(myMobile))
            showMoblieInputDialog();
        else {
            phone_status.setText("我的号码：" + myMobile);
            HSApiHelper.myMobile = myMobile;
            checkAndUploadRecords();

            startCheckThread();
        }

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
        startService(new Intent(this, HSService.class));

        List<HSRecord> records_need_upload = HSRecordsUploadHelper.checkNeedUpload(this);

        Log.i("HS U", "need upload records count:" + (records_need_upload == null ? 0 : records_need_upload.size()));

        if (records_need_upload != null && records_need_upload.size() > 0) {

//            List<HSRecord> recordsDB = HSRecordsDaos.getInstance(this).getAllRecords();
//            if (recordsDB != null && recordsDB.size() > 0) {
//                records.clear();
//                records.addAll(recordsDB);
//            }

            records.addAll(records_need_upload);
            recordsAdapter.notifyDataSetChanged();


            HSApiHelper.requestReleaseRecord(records_need_upload, this, new HSApiHelper.CallBack() {
                @Override
                public void onSuccess() {
                    //finish();
                }

                @Override
                public void onFailure() {
                    //finish();
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

    private void initViews() {

        phone_status = (TextView) findViewById(R.id.phone_status);
        change_mobile = (TextView) findViewById(R.id.change_mobile);
        change_mobile.setOnClickListener(this);

        record_list = (ListView) findViewById(R.id.record_list);
        recordsAdapter = new HSRecordsAdapter(records, this);
        record_list.setAdapter(recordsAdapter);
        record_list.setDividerHeight(1);
    }

    private void showMoblieInputDialog() {

        new SweetAlertDialog(this).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {

                String mobile = sweetAlertDialog.getMobile();
                if (checkMobile(mobile, sweetAlertDialog.getDialog_mobile_edit())) {
                    HSApplication.asp.write(SPAppInner.MOBILE, mobile);
                    sweetAlertDialog.dismiss();

                    phone_status.setText("我的号码：" + mobile);
                    checkAndUploadRecords();
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
        new Thread() {
            @Override
            public void run() {
                super.run();


                while (goOn) {
                    handler.sendEmptyMessage(1);

                    try {
                        Thread.sleep(1000 * 5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        goOn = false;
    }
}
