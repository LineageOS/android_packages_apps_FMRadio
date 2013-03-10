package com.cyanogenmod.fmradio.utils;

import android.util.Log;

/**
 * User: Pedro Veloso
 */
public final class Utils {

    /**
     * @param message Message to display
     * @param type    [Log.Error, Log.Warn, ...] . see @Log for more
     */
    public static void debugFunc(String message, int type) {
        // errors must always be sent to logcat
        if (type == Log.ERROR) {
            Log.e(Constants.LOG_TAG, message);
        } else if (Constants.DEBUG_ACTIVE) {
            switch (type) {
                case Log.DEBUG:
                    Log.d(Constants.LOG_TAG, message);
                    break;
                case Log.INFO:
                    // use this only to print development time info
                    if (Constants.DEBUG_REALLY_VERBOSE) Log.i(Constants.LOG_TAG, message);
                    break;
                case Log.VERBOSE:
                    Log.v(Constants.LOG_TAG, message);
                    break;
                case Log.WARN:
                    Log.w(Constants.LOG_TAG, message);
                    break;
                default:
                    Log.v(Constants.LOG_TAG, message);
                    break;
            }
        }
    }

}
