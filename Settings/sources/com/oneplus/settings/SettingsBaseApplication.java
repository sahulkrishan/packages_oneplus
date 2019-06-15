package com.oneplus.settings;

import android.app.Application;
import android.content.res.Configuration;
import android.os.SystemProperties;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.google.analytics.tracking.android.Tracker;
import com.oneplus.settings.utils.OPUtils;

public class SettingsBaseApplication extends Application {
    public static final boolean ONEPLUS_DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static Application mApplication;
    private boolean mIsBeta;
    private Tracker mTracker;

    public void onCreate() {
        super.onCreate();
        mApplication = this;
        this.mIsBeta = OPUtils.isBetaRom();
        OPOnlineConfigManager.getInstence(mApplication).init();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public boolean isBetaRom() {
        return this.mIsBeta;
    }

    public Tracker getDefaultTracker() {
        if (this.mTracker == null) {
            synchronized (SettingsBaseApplication.class) {
                if (this.mTracker == null) {
                    GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
                    analytics.getLogger().setLogLevel(LogLevel.VERBOSE);
                    this.mTracker = analytics.getTracker("UA-92966593-3");
                }
            }
        }
        return this.mTracker;
    }
}
