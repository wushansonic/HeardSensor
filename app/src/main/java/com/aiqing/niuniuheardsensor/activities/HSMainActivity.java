package com.aiqing.niuniuheardsensor.activities;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.TextView;

import com.aiqing.niuniuheardsensor.R;
import com.aiqing.niuniuheardsensor.Utils.HSRecordsUploadHelper;
import com.aiqing.niuniuheardsensor.Utils.db.beans.HSRecord;
import com.aiqing.niuniuheardsensor.listeners.HSPhoneLisener;

import java.text.SimpleDateFormat;
import java.util.List;


public class HSMainActivity extends HSBaseActivity {
    private TextView phone_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hsmain);

        initViews();


        TelephonyManager phoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneManager.listen(new HSPhoneLisener(this, new HSPhoneLisener.CallBack() {
                    @Override
                    public void onRinging(String incomingNumber) {
                        phone_status.setText(incomingNumber + "打来电话");
                    }

                    @Override
                    public void onOffHook() {

                    }

                    @Override
                    public void onIDLE(List<HSRecord> records) {
                        String str = "";
                        for (HSRecord record : records) {

                            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                            String time = sfd.format(record.getDate());

                            String type = "";
                            switch (record.getType()) {
                                case HSRecordsUploadHelper.INCOMING_TYPE:
                                    type = "接听";
                                    break;
                                case HSRecordsUploadHelper.MISSED_TYPE:
                                    type = "未接";
                                    break;
                                case HSRecordsUploadHelper.OUTGOING_TYPE:
                                    type = "打出";
                                    break;
                            }


                            str += "ID:" + record.getId() + " type:" + type + " time:" + time + " number:" + record.getNumber() + " duration:" + record.getDuration()
                                    + "\n";
                        }

                        phone_status.setText(str);


                    }
                }),
                HSPhoneLisener.LISTEN_CALL_STATE);

        HSRecordsUploadHelper.checkNeedUpload(this);
    }

    private void initViews() {

        phone_status = (TextView) findViewById(R.id.phone_status);
    }


}
