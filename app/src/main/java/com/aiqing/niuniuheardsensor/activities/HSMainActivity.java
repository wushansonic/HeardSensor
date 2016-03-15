package com.aiqing.niuniuheardsensor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.aiqing.niuniuheardsensor.R;
import com.aiqing.niuniuheardsensor.Utils.HSRecordsUploadHelper;
import com.aiqing.niuniuheardsensor.Utils.api.HSApiHelper;
import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.aiqing.niuniuheardsensor.services.HSService;

import java.util.List;


public class HSMainActivity extends HSBaseActivity {
    private TextView phone_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hsmain);

        initViews();

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
            finish();
        }
    }

    private void initViews() {

        phone_status = (TextView) findViewById(R.id.phone_status);
    }

}
