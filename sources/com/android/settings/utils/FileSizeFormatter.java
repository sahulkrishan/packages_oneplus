package com.android.settings.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.BidiFormatter;
import android.text.format.Formatter.BytesResult;

public final class FileSizeFormatter {
    public static final long GIGABYTE_IN_BYTES = 1000000000;
    public static final long KILOBYTE_IN_BYTES = 1000;
    public static final long MEGABYTE_IN_BYTES = 1000000;

    public static String formatFileSize(Context context, long sizeBytes, int suffix, long mult) {
        if (context == null) {
            return "";
        }
        BytesResult res = formatBytes(context.getResources(), sizeBytes, suffix, mult);
        return BidiFormatter.getInstance().unicodeWrap(context.getString(getFileSizeSuffix(context), new Object[]{res.value, res.units}));
    }

    private static int getFileSizeSuffix(Context context) {
        return context.getResources().getIdentifier("fileSizeSuffix", "string", "android");
    }

    private static BytesResult formatBytes(Resources res, long sizeBytes, int suffix, long mult) {
        int roundFactor;
        String roundFormat;
        boolean isNegative = sizeBytes < 0;
        float result = (isNegative ? (float) (-sizeBytes) : (float) sizeBytes) / ((float) mult);
        if (mult == 1) {
            roundFactor = 1;
            roundFormat = "%.0f";
        } else if (result < 1.0f) {
            roundFactor = 100;
            roundFormat = "%.2f";
        } else if (result < 10.0f) {
            roundFactor = 10;
            roundFormat = "%.1f";
        } else {
            roundFactor = 1;
            roundFormat = "%.0f";
        }
        if (isNegative) {
            result = -result;
        }
        return new BytesResult(String.format(roundFormat, new Object[]{Float.valueOf(result)}), res.getString(suffix), (((long) Math.round(((float) roundFactor) * result)) * mult) / ((long) roundFactor));
    }
}
