package com.android.settings.bluetooth;

import android.text.InputFilter;
import android.text.Spanned;

public class Utf8ByteLengthFilter implements InputFilter {
    private final int mMaxBytes;

    Utf8ByteLengthFilter(int maxBytes) {
        this.mMaxBytes = maxBytes;
    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        int i;
        Spanned spanned;
        CharSequence charSequence = source;
        int i2 = end;
        int srcByteCount = 0;
        int i3 = start;
        while (true) {
            int i4 = 1;
            if (i3 >= i2) {
                break;
            }
            char c = charSequence.charAt(i3);
            if (c >= 128) {
                i4 = c < 2048 ? 2 : 3;
            }
            srcByteCount += i4;
            i3++;
        }
        i3 = dest.length();
        int destByteCount = 0;
        for (int i5 = 0; i5 < i3; i5++) {
            if (i5 < dstart) {
                i = dend;
            } else if (i5 < dend) {
                spanned = dest;
            }
            char c2 = dest.charAt(i5);
            int i6 = c2 < 128 ? 1 : c2 < 2048 ? 2 : 3;
            destByteCount += i6;
        }
        spanned = dest;
        int i7 = dstart;
        i = dend;
        int keepBytes = this.mMaxBytes - destByteCount;
        if (keepBytes <= 0) {
            return "";
        }
        if (keepBytes >= srcByteCount) {
            return null;
        }
        int keepBytes2 = keepBytes;
        for (keepBytes = start; keepBytes < i2; keepBytes++) {
            char c3 = charSequence.charAt(keepBytes);
            int i8 = c3 < 128 ? 1 : c3 < 2048 ? 2 : 3;
            keepBytes2 -= i8;
            if (keepBytes2 < 0) {
                return charSequence.subSequence(start, keepBytes);
            }
        }
        return null;
    }
}
