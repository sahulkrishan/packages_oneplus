package com.oneplus.lib.app.appcompat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.Window;
import android.view.Window.Callback;
import com.oneplus.lib.app.appcompat.SupportActionModeWrapper.CallbackWrapper;

class AppCompatDelegateImplV14 extends AppCompatDelegateImplV11 {
    private static final boolean FLUSH_RESOURCE_CACHES_ON_NIGHT_CHANGE = true;
    private static final String KEY_LOCAL_NIGHT_MODE = "appcompat:local_night_mode";
    private boolean mApplyDayNightCalled;
    private AutoNightModeManager mAutoNightModeManager;
    private boolean mHandleNativeActionModes = true;
    private int mLocalNightMode = -100;

    @VisibleForTesting
    final class AutoNightModeManager {
        private BroadcastReceiver mAutoTimeChangeReceiver;
        private IntentFilter mAutoTimeChangeReceiverFilter;
        private boolean mIsNight;
        private TwilightManager mTwilightManager;

        AutoNightModeManager(@NonNull TwilightManager twilightManager) {
            this.mTwilightManager = twilightManager;
            this.mIsNight = twilightManager.isNight();
        }

        /* Access modifiers changed, original: final */
        public final int getApplyableNightMode() {
            return this.mIsNight ? 2 : 1;
        }

        /* Access modifiers changed, original: final */
        public final void dispatchTimeChanged() {
            boolean isNight = this.mTwilightManager.isNight();
            if (isNight != this.mIsNight) {
                this.mIsNight = isNight;
                AppCompatDelegateImplV14.this.applyDayNight();
            }
        }

        /* Access modifiers changed, original: final */
        public final void setup() {
            cleanup();
            if (this.mAutoTimeChangeReceiver == null) {
                this.mAutoTimeChangeReceiver = new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        AutoNightModeManager.this.dispatchTimeChanged();
                    }
                };
            }
            if (this.mAutoTimeChangeReceiverFilter == null) {
                this.mAutoTimeChangeReceiverFilter = new IntentFilter();
                this.mAutoTimeChangeReceiverFilter.addAction("android.intent.action.TIME_SET");
                this.mAutoTimeChangeReceiverFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
                this.mAutoTimeChangeReceiverFilter.addAction("android.intent.action.TIME_TICK");
            }
            AppCompatDelegateImplV14.this.mContext.registerReceiver(this.mAutoTimeChangeReceiver, this.mAutoTimeChangeReceiverFilter);
        }

        /* Access modifiers changed, original: final */
        public final void cleanup() {
            if (this.mAutoTimeChangeReceiver != null) {
                AppCompatDelegateImplV14.this.mContext.unregisterReceiver(this.mAutoTimeChangeReceiver);
                this.mAutoTimeChangeReceiver = null;
            }
        }
    }

    class AppCompatWindowCallbackV14 extends AppCompatWindowCallbackBase {
        AppCompatWindowCallbackV14(Callback callback) {
            super(callback);
        }

        public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
            if (AppCompatDelegateImplV14.this.isHandleNativeActionModesEnabled()) {
                return startAsSupportActionMode(callback);
            }
            return super.onWindowStartingActionMode(callback);
        }

        /* Access modifiers changed, original: final */
        public final ActionMode startAsSupportActionMode(ActionMode.Callback callback) {
            CallbackWrapper callbackWrapper = new CallbackWrapper(AppCompatDelegateImplV14.this.mContext, callback);
            ActionMode supportActionMode = AppCompatDelegateImplV14.this.startSupportActionMode(callbackWrapper);
            if (supportActionMode != null) {
                return callbackWrapper.getActionModeWrapper(supportActionMode);
            }
            return null;
        }
    }

    AppCompatDelegateImplV14(Context context, Window window, AppCompatCallback callback) {
        super(context, window, callback);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && this.mLocalNightMode == -100) {
            this.mLocalNightMode = savedInstanceState.getInt(KEY_LOCAL_NIGHT_MODE, -100);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public Callback wrapWindowCallback(Callback callback) {
        return new AppCompatWindowCallbackV14(callback);
    }

    public void setHandleNativeActionModesEnabled(boolean enabled) {
        this.mHandleNativeActionModes = enabled;
    }

    public boolean isHandleNativeActionModesEnabled() {
        return this.mHandleNativeActionModes;
    }

    public boolean applyDayNight() {
        boolean applied = false;
        int nightMode = getNightMode();
        int modeToApply = mapNightMode(nightMode);
        if (modeToApply != -1) {
            applied = updateForNightMode(modeToApply);
        }
        if (nightMode == 0) {
            ensureAutoNightModeManager();
            this.mAutoNightModeManager.setup();
        }
        this.mApplyDayNightCalled = true;
        return applied;
    }

    public void onStart() {
        super.onStart();
        applyDayNight();
    }

    public void onStop() {
        super.onStop();
        if (this.mAutoNightModeManager != null) {
            this.mAutoNightModeManager.cleanup();
        }
    }

    public void setLocalNightMode(int mode) {
        switch (mode) {
            case -1:
            case 0:
            case 1:
            case 2:
                if (this.mLocalNightMode != mode) {
                    this.mLocalNightMode = mode;
                    if (this.mApplyDayNightCalled) {
                        applyDayNight();
                        return;
                    }
                    return;
                }
                return;
            default:
                Log.i("AppCompatDelegate", "setLocalNightMode() called with an unknown mode");
                return;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public int mapNightMode(int mode) {
        if (mode == -100) {
            return -1;
        }
        if (mode != 0) {
            return mode;
        }
        ensureAutoNightModeManager();
        return this.mAutoNightModeManager.getApplyableNightMode();
    }

    private int getNightMode() {
        return this.mLocalNightMode != -100 ? this.mLocalNightMode : AppCompatDelegate.getDefaultNightMode();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mLocalNightMode != -100) {
            outState.putInt(KEY_LOCAL_NIGHT_MODE, this.mLocalNightMode);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mAutoNightModeManager != null) {
            this.mAutoNightModeManager.cleanup();
        }
    }

    private boolean updateForNightMode(int mode) {
        int newNightMode;
        Resources res = this.mContext.getResources();
        Configuration conf = res.getConfiguration();
        int currentNightMode = conf.uiMode & 48;
        if (mode == 2) {
            newNightMode = 32;
        } else {
            newNightMode = 16;
        }
        if (currentNightMode == newNightMode) {
            return false;
        }
        if (shouldRecreateOnNightModeChange()) {
            this.mContext.recreate();
        } else {
            Configuration config = new Configuration(conf);
            DisplayMetrics metrics = res.getDisplayMetrics();
            float originalFontScale = config.fontScale;
            config.uiMode = (config.uiMode & -49) | newNightMode;
            config.fontScale = 2.0f * originalFontScale;
            res.updateConfiguration(config, metrics);
            config.fontScale = originalFontScale;
            res.updateConfiguration(config, metrics);
        }
        return true;
    }

    private void ensureAutoNightModeManager() {
        if (this.mAutoNightModeManager == null) {
            this.mAutoNightModeManager = new AutoNightModeManager(TwilightManager.getInstance(this.mContext));
        }
    }

    /* Access modifiers changed, original: final */
    @VisibleForTesting
    public final AutoNightModeManager getAutoNightModeManager() {
        ensureAutoNightModeManager();
        return this.mAutoNightModeManager;
    }

    private boolean shouldRecreateOnNightModeChange() {
        boolean e = false;
        if (!this.mApplyDayNightCalled || !(this.mContext instanceof Activity)) {
            return false;
        }
        try {
            if ((this.mContext.getPackageManager().getActivityInfo(new ComponentName(this.mContext, this.mContext.getClass()), 0).configChanges & 512) == 0) {
                e = true;
            }
            return e;
        } catch (NameNotFoundException e2) {
            Log.d("AppCompatDelegate", "Exception while getting ActivityInfo", e2);
            return true;
        }
    }
}
