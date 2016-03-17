package com.aiqing.niuniuheardsensor.Utils;

import android.text.TextUtils;

/**
 * Created by blue on 16/3/17.
 */
public class InputUtil {
    public static boolean isMobileValid(String mobile) {
        return !TextUtils.isEmpty(mobile) && mobile.startsWith("1") && mobile.length() == 11;
    }

    public static boolean isSMSCodeFormatValid(String code) {
        return code.length() == 6;
    }

    public static boolean isEmailValid(String email) {
        return email.contains("@");
    }

    public static boolean isPasswordValid(String password) {
        return !TextUtils.isEmpty(password) && password.length() > 4;
    }

    public static boolean isPasswordEmpty(String password) {
        return !TextUtils.isEmpty(password);
    }
}
