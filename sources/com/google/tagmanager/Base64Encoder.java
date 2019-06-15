package com.google.tagmanager;

import android.os.Build.VERSION;
import android.util.Base64;
import com.google.android.gms.common.util.VisibleForTesting;

class Base64Encoder {
    public static final int DEFAULT = 0;
    public static final int NO_PADDING = 1;
    public static final int URL_SAFE = 2;

    Base64Encoder() {
    }

    public static String encodeToString(byte[] input, int flags) {
        if (getSdkVersion() >= 8) {
            int newFlags = 2;
            if ((flags & 1) != 0) {
                newFlags = 2 | 1;
            }
            if ((flags & 2) != 0) {
                newFlags |= 8;
            }
            return Base64.encodeToString(input, newFlags);
        }
        boolean websafeDesired = false;
        boolean paddingDesired = (flags & 1) == 0;
        if ((flags & 2) != 0) {
            websafeDesired = true;
        }
        if (websafeDesired) {
            return Base64.encodeWebSafe(input, paddingDesired);
        }
        return Base64.encode(input, paddingDesired);
    }

    public static byte[] decode(String s, int flags) {
        int newFlags;
        if (getSdkVersion() >= 8) {
            newFlags = 2;
            if ((flags & 1) != 0) {
                newFlags = 2 | 1;
            }
            if ((flags & 2) != 0) {
                newFlags |= 8;
            }
            return Base64.decode(s, newFlags);
        }
        boolean websafeDesired = false;
        if ((flags & 1) == 0) {
            newFlags = 1;
        } else {
            boolean paddingDesired = false;
        }
        if ((flags & 2) != 0) {
            websafeDesired = true;
        }
        if (websafeDesired) {
            return Base64.decodeWebSafe(s);
        }
        return Base64.decode(s);
    }

    @VisibleForTesting
    static int getSdkVersion() {
        return VERSION.SDK_INT;
    }
}
