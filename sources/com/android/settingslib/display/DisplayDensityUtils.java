package com.android.settingslib.display;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManagerGlobal;
import com.android.settingslib.R;
import java.util.Arrays;

public class DisplayDensityUtils {
    private static final int DEFINDED_DP = 420;
    private static final String LOG_TAG = "DisplayDensityUtils";
    private static final float MAX_SCALE = 1.5f;
    private static final int MIN_DIMENSION_DP = 320;
    private static final float MIN_SCALE = 0.85f;
    private static final float MIN_SCALE_INTERVAL = 0.09f;
    private static final int[] SUMMARIES_LARGER = new int[]{R.string.screen_zoom_summary_large, R.string.screen_zoom_summary_very_large, R.string.screen_zoom_summary_extremely_large};
    private static final int[] SUMMARIES_SMALLER = new int[]{R.string.screen_zoom_summary_small};
    private static final int SUMMARY_CUSTOM = R.string.screen_zoom_summary_custom;
    public static final int SUMMARY_DEFAULT = R.string.screen_zoom_summary_default;
    private final int mCurrentIndex;
    private final int mDefaultDensity;
    private final String[] mEntries;
    private final int[] mValues;

    public DisplayDensityUtils(Context context) {
        int defaultDensity = getDefaultDisplayDensity(0);
        if (defaultDensity <= 0) {
            this.mEntries = null;
            this.mValues = null;
            this.mDefaultDensity = 0;
            this.mCurrentIndex = -1;
            return;
        }
        int i;
        int density;
        Resources res = context.getResources();
        DisplayMetrics metrics = new DisplayMetrics();
        context.getDisplay().getRealMetrics(metrics);
        int currentDensity = metrics.densityDpi;
        int currentDensityIndex = -1;
        float maxScale = Math.min(1.5f, ((float) ((160 * Math.min(metrics.widthPixels, metrics.heightPixels)) / MIN_DIMENSION_DP)) / ((float) defaultDensity));
        int numLarger = SUMMARIES_LARGER.length;
        int numSmaller = SUMMARIES_SMALLER.length;
        String[] entries = new String[((1 + numSmaller) + numLarger)];
        int[] values = new int[entries.length];
        int curIndex = 0;
        if (numSmaller > 0) {
            float interval = 0.14999998f / ((float) numSmaller);
            i = 0;
            while (i < numSmaller) {
                density = 420 - (40 * (numSmaller - i));
                if (currentDensity == density) {
                    currentDensityIndex = curIndex;
                }
                DisplayMetrics metrics2 = metrics;
                entries[curIndex] = res.getString(SUMMARIES_SMALLER[i]);
                values[curIndex] = density;
                curIndex++;
                i++;
                metrics = metrics2;
            }
        }
        if (currentDensity == defaultDensity) {
            currentDensityIndex = curIndex;
        }
        values[curIndex] = defaultDensity;
        entries[curIndex] = res.getString(SUMMARY_DEFAULT);
        curIndex++;
        if (numLarger > 0) {
            float interval2 = (maxScale - 1.0f) / ((float) numLarger);
            int i2 = 0;
            while (i2 < numLarger) {
                float interval3;
                density = 0;
                if (i2 == 0) {
                    density = 480;
                    interval3 = interval2;
                } else {
                    interval3 = interval2;
                    if (i2 == 1) {
                        density = 500;
                    } else if (i2 == 2) {
                        density = 540;
                    }
                }
                if (currentDensity == density) {
                    currentDensityIndex = curIndex;
                }
                values[curIndex] = density;
                entries[curIndex] = res.getString(SUMMARIES_LARGER[i2]);
                curIndex++;
                i2++;
                interval2 = interval3;
            }
        }
        if (currentDensityIndex >= 0) {
            i = currentDensityIndex;
        } else {
            i = values.length + 1;
            values = Arrays.copyOf(values, i);
            values[curIndex] = currentDensity;
            entries = (String[]) Arrays.copyOf(entries, i);
            entries[curIndex] = res.getString(SUMMARY_CUSTOM, new Object[]{Integer.valueOf(currentDensity)});
            i = curIndex;
        }
        this.mDefaultDensity = defaultDensity;
        this.mCurrentIndex = i;
        this.mEntries = entries;
        this.mValues = values;
    }

    public String[] getEntries() {
        return this.mEntries;
    }

    public int[] getValues() {
        return this.mValues;
    }

    public int getCurrentIndex() {
        return this.mCurrentIndex;
    }

    public int getDefaultDensity() {
        return this.mDefaultDensity;
    }

    private static int getDefaultDisplayDensity(int displayId) {
        try {
            return WindowManagerGlobal.getWindowManagerService().getInitialDisplayDensity(displayId);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static void clearForcedDisplayDensity(int displayId) {
        AsyncTask.execute(new -$$Lambda$DisplayDensityUtils$FjSo_v2dJihYeklLmCubVRPf_nw(displayId, UserHandle.myUserId()));
    }

    static /* synthetic */ void lambda$clearForcedDisplayDensity$0(int displayId, int userId) {
        try {
            WindowManagerGlobal.getWindowManagerService().clearForcedDisplayDensityForUser(displayId, userId);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "Unable to clear forced display density setting");
        }
    }

    public static void setForcedDisplayDensity(int displayId, int density) {
        AsyncTask.execute(new -$$Lambda$DisplayDensityUtils$jbnNZEy3zYf8rJTNV5wQSa3Z5eQ(displayId, density, UserHandle.myUserId()));
    }

    static /* synthetic */ void lambda$setForcedDisplayDensity$1(int displayId, int density, int userId) {
        try {
            WindowManagerGlobal.getWindowManagerService().setForcedDisplayDensityForUser(displayId, density, userId);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "Unable to save forced display density setting");
        }
    }
}
