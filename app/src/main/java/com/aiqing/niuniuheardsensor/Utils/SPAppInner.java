package com.aiqing.niuniuheardsensor.Utils;

import android.content.Context;

/**
 * Created by blue on 16/3/17.
 */
public class SPAppInner extends AbstractSharedPreference {

    public static final String MOBILE = "mobile";


    private static final String STORE_NAME = "hsqc_share_data";
    //不要手动进行new
    public SPAppInner(Context context) {
        super(context, STORE_NAME);
    }


}
