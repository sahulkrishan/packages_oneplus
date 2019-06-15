package com.oneplus.settings.utils;

import android.content.Context;
import com.oneplus.settings.SettingsBaseApplication;
import java.util.HashMap;
import java.util.Map;
import net.oneplus.odm.insight.tracker.AppTracker;

public class AppTrackerHelper {
    public static final String APPTRACKER_ID = "YLTI9SVG4L";
    private static Context mContext;
    private static Object mLock = new byte[0];
    private static AppTrackerHelper sInstance;
    private AppTracker mAppTracker;

    private AppTrackerHelper() {
        this.mAppTracker = null;
        this.mAppTracker = new AppTracker(SettingsBaseApplication.mApplication.getApplicationContext(), APPTRACKER_ID);
    }

    public static AppTrackerHelper getInstance() {
        if (sInstance == null) {
            synchronized (mLock) {
                if (sInstance == null) {
                    sInstance = new AppTrackerHelper();
                }
            }
        }
        return sInstance;
    }

    public final void putAnalytics(final String tag, final String label, final String value) {
        new Thread(new Runnable() {
            public void run() {
                if (AppTrackerHelper.this.mAppTracker != null) {
                    Map<String, String> mdmData = new HashMap();
                    mdmData.put(label, value);
                    AppTrackerHelper.this.mAppTracker.onEvent(tag, mdmData);
                }
            }
        }).start();
    }

    public final void putAnalytics(final String tag, final Map<String, String> mdmData) {
        if (mdmData != null && mdmData.size() > 0) {
            new Thread(new Runnable() {
                public void run() {
                    if (AppTrackerHelper.this.mAppTracker != null) {
                        AppTrackerHelper.this.mAppTracker.onEvent(tag, mdmData);
                    }
                }
            }).start();
        }
    }
}
