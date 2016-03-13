package com.aiqing.niuniuheardsensor.activities;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import com.aiqing.niuniuheardsensor.R;
import com.aiqing.niuniuheardsensor.listeners.HSPhoneLisener;


public class HSMainActivity extends HSBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hsmain);


        TelephonyManager phoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneManager.listen(new HSPhoneLisener(this),
                HSPhoneLisener.LISTEN_CALL_STATE);
    }


}
