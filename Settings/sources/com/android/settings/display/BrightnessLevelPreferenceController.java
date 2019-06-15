package com.android.settings.display;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.System;
import android.service.vr.IVrManager.Stub;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.settings.DisplaySettings;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.android.settingslib.display.BrightnessUtils;
import java.text.NumberFormat;

public class BrightnessLevelPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnStart, OnStop {
    private static final Uri BRIGHTNESS_ADJ_URI = System.getUriFor(DisplaySettings.SCREEN_AUTO_BRIGHTNESS_ADJ);
    private static final Uri BRIGHTNESS_FOR_VR_URI = System.getUriFor("screen_brightness_for_vr");
    private static final Uri BRIGHTNESS_URI = System.getUriFor("screen_brightness");
    private static final String KEY_BRIGHTNESS = "brightness";
    private static final String TAG = "BrightnessPrefCtrl";
    private ContentObserver mBrightnessObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        public void onChange(boolean selfChange) {
            BrightnessLevelPreferenceController.this.updatedSummary(BrightnessLevelPreferenceController.this.mPreference);
        }
    };
    private final ContentResolver mContentResolver;
    private final int mMaxBrightness;
    private final int mMaxVrBrightness;
    private final int mMinBrightness;
    private final int mMinVrBrightness;
    private Preference mPreference;

    public BrightnessLevelPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        this.mMinBrightness = powerManager.getMinimumScreenBrightnessSetting();
        this.mMaxBrightness = powerManager.getMaximumScreenBrightnessSetting();
        this.mMinVrBrightness = powerManager.getMinimumScreenBrightnessForVrSetting();
        this.mMaxVrBrightness = powerManager.getMaximumScreenBrightnessForVrSetting();
        this.mContentResolver = this.mContext.getContentResolver();
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_BRIGHTNESS;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(KEY_BRIGHTNESS);
    }

    public void updateState(Preference preference) {
        updatedSummary(preference);
    }

    public void onStart() {
        this.mContentResolver.registerContentObserver(BRIGHTNESS_URI, false, this.mBrightnessObserver);
        this.mContentResolver.registerContentObserver(BRIGHTNESS_FOR_VR_URI, false, this.mBrightnessObserver);
        this.mContentResolver.registerContentObserver(BRIGHTNESS_ADJ_URI, false, this.mBrightnessObserver);
    }

    public void onStop() {
        this.mContentResolver.unregisterContentObserver(this.mBrightnessObserver);
    }

    private void updatedSummary(Preference preference) {
        if (preference != null) {
            preference.setSummary(NumberFormat.getPercentInstance().format(getCurrentBrightness()));
        }
    }

    private double getCurrentBrightness() {
        int value;
        if (isInVrMode()) {
            value = BrightnessUtils.convertLinearToGamma(System.getInt(this.mContentResolver, "screen_brightness_for_vr", this.mMaxBrightness), this.mMinVrBrightness, this.mMaxVrBrightness);
        } else {
            value = BrightnessUtils.convertLinearToGamma(System.getInt(this.mContentResolver, "screen_brightness", this.mMinBrightness), this.mMinBrightness, this.mMaxBrightness);
        }
        return getPercentage((double) value, 0, BrightnessUtils.GAMMA_SPACE_MAX);
    }

    private double getPercentage(double value, int min, int max) {
        if (value > ((double) max)) {
            return 1.0d;
        }
        if (value < ((double) min)) {
            return 0.0d;
        }
        return (value - ((double) min)) / ((double) (max - min));
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isInVrMode() {
        try {
            return Stub.asInterface(ServiceManager.getService("vrmanager")).getVrModeState();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to check vr mode!", e);
            return false;
        }
    }
}
