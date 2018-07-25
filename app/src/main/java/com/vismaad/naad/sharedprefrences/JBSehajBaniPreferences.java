package com.vismaad.naad.sharedprefrences;

import android.content.SharedPreferences;

public class JBSehajBaniPreferences {

    public static void setJwtToken(SharedPreferences preferences, String email) {
        preferences.edit().putString(SehajBaniPreferences.JWT_TOKEN, email).commit();
    }

    public static String getJwtToken(SharedPreferences preferences) {
        return preferences.getString(SehajBaniPreferences.JWT_TOKEN, SehajBaniPreferences.JWT_TOKEN_VALUE);
    }

    public static void setLoginId(SharedPreferences preferences, String email) {
        preferences.edit().putString(SehajBaniPreferences.LOGIN_ID, email).commit();
    }

    public static String getLoginId(SharedPreferences preferences) {
        return preferences.getString(SehajBaniPreferences.LOGIN_ID, SehajBaniPreferences.LOGIN_ID_VALUE);
    }

    public static void setRaggiId(SharedPreferences preferences, String raggi) {
        preferences.edit().putString(SehajBaniPreferences.RAGGI_ID, raggi).commit();
    }

    public static String getRaggiId(SharedPreferences preferences) {
        return preferences.getString(SehajBaniPreferences.RAGGI_ID, SehajBaniPreferences.RAGGI_ID_VALUE);
    }
}