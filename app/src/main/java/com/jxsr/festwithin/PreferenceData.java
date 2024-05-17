package com.jxsr.festwithin;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceData{
    static final String PREF_USER_LOGGEDIN_DISPLAYNAME = "logged_in_username";
    static final String PREF_USER_LOGGEDIN_EMAIL = "logged_in_email";
    static final String PREF_USER_LOGGEDIN_STATUS = "logged_in_status";
    static final String PREF_USER_LOGGEDIN_ID = "logged_in_id";
    static final String PREF_USER_SETTING_NEARBY_MAX_RANGE_IN_KM = "max_range_km";
    static final String PREF_USER_SETTING_TIMEZONE = "user_timezone";

    public static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setUserTimezone(Context ctx, String timezone) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_SETTING_TIMEZONE, timezone);
        editor.apply();
    }

    public static String getUserTimezone(Context ctx){
        return getSharedPreferences(ctx).getString(PREF_USER_SETTING_TIMEZONE, "GMT+7");
    }

    public static void setMaxRangeKM(Context ctx, int maxRange) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putInt(PREF_USER_SETTING_NEARBY_MAX_RANGE_IN_KM, maxRange);
        editor.apply();
    }

    public static int getMaxRangeKM(Context ctx){
        return getSharedPreferences(ctx).getInt(PREF_USER_SETTING_NEARBY_MAX_RANGE_IN_KM, 10);
    }

    public static void setLoggedInUserUsername(Context ctx, String username) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_LOGGEDIN_DISPLAYNAME, username);
        editor.apply();
    }

    public static String getLoggedInUserUsername(Context ctx){
        return getSharedPreferences(ctx).getString(PREF_USER_LOGGEDIN_DISPLAYNAME, "");
    }

    public static void setLoggedInUserId(Context ctx, String userId) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_LOGGEDIN_ID, userId);
        editor.apply();
    }

    public static String getLoggedInUserId(Context ctx){
        return getSharedPreferences(ctx).getString(PREF_USER_LOGGEDIN_ID, "");
    }

    public static void setUserLoggedInStatus(Context ctx, boolean status) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(PREF_USER_LOGGEDIN_STATUS, status);
        editor.apply();
    }

    public static boolean getUserLoggedInStatus(Context ctx) {
        return getSharedPreferences(ctx).getBoolean(PREF_USER_LOGGEDIN_STATUS, false);
    }

    public static void setUserLoggedInEmail(Context ctx, String email) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_LOGGEDIN_EMAIL, email);
        editor.apply();
    }

    public static String getUserLoggedInEmail(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_USER_LOGGEDIN_EMAIL, "");
    }

    public static void clearLoggedInUser(Context ctx) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(PREF_USER_LOGGEDIN_DISPLAYNAME);
        editor.remove(PREF_USER_LOGGEDIN_EMAIL);
        editor.remove(PREF_USER_LOGGEDIN_STATUS);
        editor.remove(PREF_USER_LOGGEDIN_ID);
        editor.apply();
    }

}
