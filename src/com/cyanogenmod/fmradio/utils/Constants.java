package com.cyanogenmod.fmradio.utils;

/**
 * User: Pedro Veloso
 */
public final class Constants {

    public static final String LOG_TAG = "CM_FM_RADIO";
    public static final boolean DEBUG_ACTIVE = true;
    public static final boolean DEBUG_REALLY_VERBOSE = true;

    // The 50kHz channel offset
    public static final int CHANNEL_OFFSET_50KHZ = 50;

    public static final String MEDIA_SOURCE = "fmradio://rx";
    public static final String FM_RECEIVER_SERVICE = "fm_receiver";

    // PREFERENCES
    public static final String PREFS_SELECTED_BAND = "SELECTED_BAND";
    public static final String PREFS_VERBOSE_LOGGING = "VERBOSE_LOGGING";
    public static final String PREFS_NOTIFICATION_CONTROLS = "NOTIFICATION_CONTROLS";
    public static final String PREFS_IS_FIRST_TIME = "IS_FIRST_TIME";
}
