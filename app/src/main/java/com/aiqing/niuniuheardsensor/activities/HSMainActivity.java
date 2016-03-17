package com.aiqing.niuniuheardsensor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hsmain);

        initViews();


        String myMobile = HSApplication.asp.read(SPAppInner.MOBILE, "");
        if (TextUtils.isEmpty(myMobile))
            showMoblieInputDialog();
        else {
            phone_status.setText("我的号码：" + myMobile);
            HSApiHelper.myMobile = myMobile;
            startSensor();
        }

    }

    private void startSensor() {
        startService(new Intent(this, HSService.class));

        List<HSRecord> records = HSRecordsUploadHelper.checkNeedUpload(this);
        if (records != null && records.size() > 0) {
            HSApiHelper.requestReleaseRecord(records, this, new HSApiHelper.CallBack() {
                @Override
                public void onSuccess() {
                    finish();
                }

                @Override
                public void onFailure() {
                    finish();
                }
            });
        } else {
//            finish();
        }
    }

    private void initViews() {

        phone_status = (TextView) findViewById(R.id.phone_status);
        change_mobile = (TextView) findViewById(R.id.change_mobile);
        change_mobile.setOnClickListener(this);

        List<HSRecord> recordsDB = HSRecordsDaos.getInstance(this).getAllRecords();
        if (recordsDB != null && recordsDB.size() > 0)
            records.addAll(recordsDB);
        record_list = (ListView) findViewById(R.id.record_list);
        recordsAdapter = new HSRecordsAdapter(records, this);
        record_list.setAdapter(recordsAdapter);
        recordsAdapter.notifyDataSetChanged();
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
                    startSensor();
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
}
