package com.google.analytics.tracking.android;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import com.google.analytics.tracking.android.GAUsage.Field;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.google.android.gms.common.util.VisibleForTesting;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class EasyTracker extends Tracker {
    private static final int DEFAULT_SAMPLE_RATE = 100;
    private static final String EASY_TRACKER_NAME = "easy_tracker";
    static final int NUM_MILLISECONDS_TO_WAIT_FOR_OPEN_ACTIVITY = 1000;
    private static EasyTracker sInstance;
    private static String sResourcePackageName;
    private int mActivitiesActive;
    private final Map<String, String> mActivityNameMap;
    private Clock mClock;
    private Context mContext;
    private final GoogleAnalytics mGoogleAnalytics;
    private boolean mIsAutoActivityTracking;
    private boolean mIsInForeground;
    private boolean mIsReportUncaughtExceptionsEnabled;
    private long mLastOnStopTime;
    private ParameterLoader mParameterFetcher;
    private ServiceManager mServiceManager;
    private long mSessionTimeout;
    private boolean mStartSessionOnNextSend;
    private Timer mTimer;
    private TimerTask mTimerTask;

    private class NotInForegroundTimerTask extends TimerTask {
        private NotInForegroundTimerTask() {
        }

        /* synthetic */ NotInForegroundTimerTask(EasyTracker x0, AnonymousClass1 x1) {
            this();
        }

        public void run() {
            EasyTracker.this.mIsInForeground = false;
        }
    }

    private EasyTracker(Context ctx) {
        this(ctx, new ParameterLoaderImpl(ctx), GoogleAnalytics.getInstance(ctx), GAServiceManager.getInstance(), null);
    }

    private EasyTracker(Context ctx, ParameterLoader parameterLoader, GoogleAnalytics ga, ServiceManager serviceManager, TrackerHandler handler) {
        super(EASY_TRACKER_NAME, null, handler != null ? handler : ga);
        this.mIsAutoActivityTracking = false;
        this.mActivitiesActive = 0;
        this.mActivityNameMap = new HashMap();
        this.mIsInForeground = false;
        this.mStartSessionOnNextSend = false;
        if (sResourcePackageName != null) {
            parameterLoader.setResourcePackageName(sResourcePackageName);
        }
        this.mGoogleAnalytics = ga;
        setContext(ctx, parameterLoader, serviceManager);
        this.mClock = new Clock() {
            public long currentTimeMillis() {
                return System.currentTimeMillis();
            }
        };
    }

    public static EasyTracker getInstance(Context ctx) {
        if (sInstance == null) {
            sInstance = new EasyTracker(ctx);
        }
        return sInstance;
    }

    @VisibleForTesting
    static EasyTracker getNewInstance(Context ctx, ParameterLoader parameterLoader, GoogleAnalytics ga, ServiceManager serviceManager, TrackerHandler handler) {
        sInstance = new EasyTracker(ctx, parameterLoader, ga, serviceManager, handler);
        return sInstance;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean checkForNewSession() {
        return this.mSessionTimeout == 0 || (this.mSessionTimeout > 0 && this.mClock.currentTimeMillis() > this.mLastOnStopTime + this.mSessionTimeout);
    }

    private void loadParameters() {
        StringBuilder stringBuilder;
        Log.v("Starting EasyTracker.");
        String trackingId = this.mParameterFetcher.getString("ga_trackingId");
        if (TextUtils.isEmpty(trackingId)) {
            trackingId = this.mParameterFetcher.getString("ga_api_key");
        }
        set(Fields.TRACKING_ID, trackingId);
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("[EasyTracker] trackingId loaded: ");
        stringBuilder2.append(trackingId);
        Log.v(stringBuilder2.toString());
        String appName = this.mParameterFetcher.getString("ga_appName");
        if (!TextUtils.isEmpty(appName)) {
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("[EasyTracker] app name loaded: ");
            stringBuilder3.append(appName);
            Log.v(stringBuilder3.toString());
            set(Fields.APP_NAME, appName);
        }
        String appVersion = this.mParameterFetcher.getString("ga_appVersion");
        if (appVersion != null) {
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append("[EasyTracker] app version loaded: ");
            stringBuilder4.append(appVersion);
            Log.v(stringBuilder4.toString());
            set(Fields.APP_VERSION, appVersion);
        }
        String logLevelString = this.mParameterFetcher.getString("ga_logLevel");
        if (logLevelString != null) {
            LogLevel logLevel = getLogLevelFromString(logLevelString);
            if (logLevel != null) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("[EasyTracker] log level loaded: ");
                stringBuilder.append(logLevel);
                Log.v(stringBuilder.toString());
                this.mGoogleAnalytics.getLogger().setLogLevel(logLevel);
            }
        }
        Double sampleRate = this.mParameterFetcher.getDoubleFromString("ga_sampleFrequency");
        if (sampleRate == null) {
            sampleRate = new Double((double) this.mParameterFetcher.getInt("ga_sampleRate", 100));
        }
        if (sampleRate.doubleValue() != 100.0d) {
            set(Fields.SAMPLE_RATE, Double.toString(sampleRate.doubleValue()));
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append("[EasyTracker] sample rate loaded: ");
        stringBuilder.append(sampleRate);
        Log.v(stringBuilder.toString());
        int dispatchPeriod = this.mParameterFetcher.getInt("ga_dispatchPeriod", 1800);
        StringBuilder stringBuilder5 = new StringBuilder();
        stringBuilder5.append("[EasyTracker] dispatch period loaded: ");
        stringBuilder5.append(dispatchPeriod);
        Log.v(stringBuilder5.toString());
        this.mServiceManager.setLocalDispatchPeriod(dispatchPeriod);
        this.mSessionTimeout = (long) (this.mParameterFetcher.getInt("ga_sessionTimeout", 30) * 1000);
        stringBuilder5 = new StringBuilder();
        stringBuilder5.append("[EasyTracker] session timeout loaded: ");
        stringBuilder5.append(this.mSessionTimeout);
        Log.v(stringBuilder5.toString());
        boolean z = this.mParameterFetcher.getBoolean("ga_autoActivityTracking") || this.mParameterFetcher.getBoolean("ga_auto_activity_tracking");
        this.mIsAutoActivityTracking = z;
        stringBuilder5 = new StringBuilder();
        stringBuilder5.append("[EasyTracker] auto activity tracking loaded: ");
        stringBuilder5.append(this.mIsAutoActivityTracking);
        Log.v(stringBuilder5.toString());
        z = this.mParameterFetcher.getBoolean("ga_anonymizeIp");
        if (z) {
            set(Fields.ANONYMIZE_IP, "1");
            StringBuilder stringBuilder6 = new StringBuilder();
            stringBuilder6.append("[EasyTracker] anonymize ip loaded: ");
            stringBuilder6.append(z);
            Log.v(stringBuilder6.toString());
        }
        this.mIsReportUncaughtExceptionsEnabled = this.mParameterFetcher.getBoolean("ga_reportUncaughtExceptions");
        if (this.mIsReportUncaughtExceptionsEnabled) {
            Thread.setDefaultUncaughtExceptionHandler(new ExceptionReporter(this, this.mServiceManager, Thread.getDefaultUncaughtExceptionHandler(), this.mContext));
            StringBuilder stringBuilder7 = new StringBuilder();
            stringBuilder7.append("[EasyTracker] report uncaught exceptions loaded: ");
            stringBuilder7.append(this.mIsReportUncaughtExceptionsEnabled);
            Log.v(stringBuilder7.toString());
        }
        this.mGoogleAnalytics.setDryRun(this.mParameterFetcher.getBoolean("ga_dryRun"));
    }

    private LogLevel getLogLevelFromString(String logLevelString) {
        try {
            return LogLevel.valueOf(logLevelString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void overrideUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
        if (this.mIsReportUncaughtExceptionsEnabled) {
            Thread.setDefaultUncaughtExceptionHandler(handler);
        }
    }

    private void setContext(Context ctx, ParameterLoader parameterLoader, ServiceManager serviceManager) {
        if (ctx == null) {
            Log.e("Context cannot be null");
        }
        this.mContext = ctx.getApplicationContext();
        this.mServiceManager = serviceManager;
        this.mParameterFetcher = parameterLoader;
        loadParameters();
    }

    public void activityStart(Activity activity) {
        GAUsage.getInstance().setUsage(Field.EASY_TRACKER_ACTIVITY_START);
        clearExistingTimer();
        if (!this.mIsInForeground && this.mActivitiesActive == 0 && checkForNewSession()) {
            this.mStartSessionOnNextSend = true;
        }
        this.mIsInForeground = true;
        this.mActivitiesActive++;
        if (this.mIsAutoActivityTracking) {
            Map<String, String> params = new HashMap();
            params.put(Fields.HIT_TYPE, HitTypes.APP_VIEW);
            GAUsage.getInstance().setDisableUsage(true);
            set("&cd", getActivityName(activity));
            send(params);
            GAUsage.getInstance().setDisableUsage(false);
        }
    }

    public void activityStop(Activity activity) {
        GAUsage.getInstance().setUsage(Field.EASY_TRACKER_ACTIVITY_STOP);
        this.mActivitiesActive--;
        this.mActivitiesActive = Math.max(0, this.mActivitiesActive);
        this.mLastOnStopTime = this.mClock.currentTimeMillis();
        if (this.mActivitiesActive == 0) {
            clearExistingTimer();
            this.mTimerTask = new NotInForegroundTimerTask(this, null);
            this.mTimer = new Timer("waitForActivityStart");
            this.mTimer.schedule(this.mTimerTask, 1000);
        }
    }

    @Deprecated
    public void dispatchLocalHits() {
        this.mServiceManager.dispatchLocalHits();
    }

    private synchronized void clearExistingTimer() {
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
        }
    }

    private String getActivityName(Activity activity) {
        String canonicalName = activity.getClass().getCanonicalName();
        if (this.mActivityNameMap.containsKey(canonicalName)) {
            return (String) this.mActivityNameMap.get(canonicalName);
        }
        String name = this.mParameterFetcher.getString(canonicalName);
        if (name == null) {
            name = canonicalName;
        }
        this.mActivityNameMap.put(canonicalName, name);
        return name;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setClock(Clock clock) {
        this.mClock = clock;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public int getActivitiesActive() {
        return this.mActivitiesActive;
    }

    public void send(Map<String, String> params) {
        if (this.mStartSessionOnNextSend) {
            params.put(Fields.SESSION_CONTROL, "start");
            this.mStartSessionOnNextSend = false;
        }
        super.send(params);
    }

    public static void setResourcePackageName(String resourcePackageName) {
        sResourcePackageName = resourcePackageName;
    }
}
