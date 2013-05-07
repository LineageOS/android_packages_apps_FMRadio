package com.cyanogenmod.fmradio.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Android Shared preferences helper methods
 */
public final class Prefs {

    /**
     * Get Preferred FM Band
     */
    public static int getPreferredBand(Context ctx) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        return settings.getInt(Constants.PREFS_SELECTED_BAND, 1);
    }


    public static void setPreferredBand(Context ctx, final int band) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.PREFS_SELECTED_BAND, band);
        editor.commit();
    }

    /**
     * @param ctx App Context
     * @return Print Debug Information to Logcat
     */
        public static boolean getPrintDebugInfo(Context ctx) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        return settings.getBoolean(Constants.PREFS_VERBOSE_LOGGING, false);
    }

    public static void setPrintDebugInfo(Context ctx, boolean newValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.PREFS_VERBOSE_LOGGING, newValue);
        editor.commit();
    }


    /**
     * @param ctx App Context
     * @return Use notification bar controls
     */
    public static boolean getUseNotifications(Context ctx) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        return settings.getBoolean(Constants.PREFS_NOTIFICATION_CONTROLS, true);
    }

    public static void setUseNotifications(Context ctx, boolean newValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.PREFS_NOTIFICATION_CONTROLS, newValue);
        editor.commit();
    }


    /**
     * @return True if this the first time the application executes, else otherwise
     */
    public static boolean getIsFirstTime(Context ctx) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
            return settings.getBoolean(Constants.PREFS_IS_FIRST_TIME, true);
        }

        public static void setIsFirstTime(Context ctx, boolean newValue) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.PREFS_IS_FIRST_TIME, newValue);
            editor.commit();
        }

}
