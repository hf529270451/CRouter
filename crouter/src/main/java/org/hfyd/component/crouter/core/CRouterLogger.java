package org.hfyd.component.crouter.core;

import android.util.Log;

public class CRouterLogger {

    private static boolean sIsDebug;
    private static final String TAG = "CRouter";

    public static void debugger(boolean isDebug) {
        sIsDebug = isDebug;
    }

    public static void info(String info) {
        if (sIsDebug) {
            Log.i(TAG, info);
        }
    }

    public static void error(String error) {
        if (sIsDebug) {
            Log.e(TAG, error);
        }
    }
}
