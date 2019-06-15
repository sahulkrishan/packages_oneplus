package com.oneplus.custom.utils;

import android.util.Log;

public class MyLog {
    protected static final boolean DBG = "true".equals(SystemProperties.get("persist.sys.assert.panic"));

    protected static void v(String tag, String log) {
        if (DBG) {
            Log.v(tag, log);
        }
    }

    protected static void d(String tag, String log) {
        if (DBG) {
            Log.d(tag, log);
        }
    }

    protected static void i(String tag, String log) {
        if (DBG) {
            Log.i(tag, log);
        }
    }

    protected static void w(String tag, String log) {
        if (DBG) {
            Log.w(tag, log);
        }
    }

    protected static void e(String tag, String log) {
        if (DBG) {
            Log.e(tag, log);
        }
    }
}
