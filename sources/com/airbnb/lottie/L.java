package com.airbnb.lottie;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.os.TraceCompat;

@RestrictTo({Scope.LIBRARY})
public class L {
    public static final boolean DBG = false;
    private static final int MAX_DEPTH = 20;
    public static final String TAG = "LOTTIE";
    private static int depthPastMaxDepth = 0;
    private static String[] sections;
    private static long[] startTimeNs;
    private static int traceDepth = 0;
    private static boolean traceEnabled = false;

    public static void setTraceEnabled(boolean enabled) {
        if (traceEnabled != enabled) {
            traceEnabled = enabled;
            if (traceEnabled) {
                sections = new String[20];
                startTimeNs = new long[20];
            }
        }
    }

    public static void beginSection(String section) {
        if (!traceEnabled) {
            return;
        }
        if (traceDepth == 20) {
            depthPastMaxDepth++;
            return;
        }
        sections[traceDepth] = section;
        startTimeNs[traceDepth] = System.nanoTime();
        TraceCompat.beginSection(section);
        traceDepth++;
    }

    public static float endSection(String section) {
        if (depthPastMaxDepth > 0) {
            depthPastMaxDepth--;
            return 0.0f;
        } else if (!traceEnabled) {
            return 0.0f;
        } else {
            traceDepth--;
            if (traceDepth == -1) {
                throw new IllegalStateException("Can't end trace section. There are none.");
            } else if (section.equals(sections[traceDepth])) {
                TraceCompat.endSection();
                return ((float) (System.nanoTime() - startTimeNs[traceDepth])) / 1000000.0f;
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unbalanced trace call ");
                stringBuilder.append(section);
                stringBuilder.append(". Expected ");
                stringBuilder.append(sections[traceDepth]);
                stringBuilder.append(".");
                throw new IllegalStateException(stringBuilder.toString());
            }
        }
    }
}
