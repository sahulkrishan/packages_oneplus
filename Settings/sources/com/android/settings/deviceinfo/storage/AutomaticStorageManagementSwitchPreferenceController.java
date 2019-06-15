package com.android.settings.deviceinfo.storage;

import android.app.ActivityManager;
import android.app.FragmentManager;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.deletionhelper.ActivationWarningFragment;
import com.android.settings.widget.MasterSwitchController;
import com.android.settings.widget.MasterSwitchPreference;
import com.android.settings.widget.SwitchWidgetController.OnSwitchChangeListener;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class AutomaticStorageManagementSwitchPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnResume, OnSwitchChangeListener {
    private static final String KEY_TOGGLE_ASM = "toggle_asm";
    @VisibleForTesting
    static final String STORAGE_MANAGER_ENABLED_BY_DEFAULT_PROPERTY = "ro.storage_manager.enabled";
    private final FragmentManager mFragmentManager;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private MasterSwitchPreference mSwitch;
    private MasterSwitchController mSwitchController;

    public AutomaticStorageManagementSwitchPreferenceController(Context context, MetricsFeatureProvider metricsFeatureProvider, FragmentManager fragmentManager) {
        super(context);
        this.mMetricsFeatureProvider = metricsFeatureProvider;
        this.mFragmentManager = fragmentManager;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSwitch = (MasterSwitchPreference) screen.findPreference(KEY_TOGGLE_ASM);
    }

    public boolean isAvailable() {
        return ActivityManager.isLowRamDeviceStatic() ^ 1;
    }

    public String getPreferenceKey() {
        return KEY_TOGGLE_ASM;
    }

    public void onResume() {
        if (isAvailable()) {
            this.mSwitch.setChecked(Utils.isStorageManagerEnabled(this.mContext));
            if (this.mSwitch != null) {
                this.mSwitchController = new MasterSwitchController(this.mSwitch);
                this.mSwitchController.setListener(this);
                this.mSwitchController.startListening();
            }
        }
    }

    public boolean onSwitchToggled(boolean isChecked) {
        this.mMetricsFeatureProvider.action(this.mContext, 489, isChecked);
        Secure.putInt(this.mContext.getContentResolver(), "automatic_storage_manager_enabled", isChecked);
        boolean storageManagerDisabledByPolicy = false;
        boolean storageManagerEnabledByDefault = SystemProperties.getBoolean(STORAGE_MANAGER_ENABLED_BY_DEFAULT_PROPERTY, false);
        if (Secure.getInt(this.mContext.getContentResolver(), "automatic_storage_manager_turned_off_by_policy", 0) != 0) {
            storageManagerDisabledByPolicy = true;
        }
        if (isChecked && (!storageManagerEnabledByDefault || storageManagerDisabledByPolicy)) {
            ActivationWarningFragment.newInstance().show(this.mFragmentManager, ActivationWarningFragment.TAG);
        }
        return true;
    }
}
