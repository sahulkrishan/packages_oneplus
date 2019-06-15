package com.android.settings.network;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.AirplaneModeEnabler;
import com.android.settings.AirplaneModeEnabler.OnAirplaneModeChangedListener;
import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class AirplaneModePreferenceController extends TogglePreferenceController implements LifecycleObserver, OnResume, OnPause, OnAirplaneModeChangedListener {
    private static final String EXIT_ECM_RESULT = "exit_ecm_result";
    public static final int REQUEST_CODE_EXIT_ECM = 1;
    private AirplaneModeEnabler mAirplaneModeEnabler = new AirplaneModeEnabler(this.mContext, this.mMetricsFeatureProvider, this);
    private SwitchPreference mAirplaneModePreference;
    private Fragment mFragment;
    private final MetricsFeatureProvider mMetricsFeatureProvider;

    public AirplaneModePreferenceController(Context context, String key) {
        super(context, key);
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
    }

    public void setFragment(Fragment hostFragment) {
        this.mFragment = hostFragment;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!"airplane_mode".equals(preference.getKey()) || !Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
            return false;
        }
        if (this.mFragment != null) {
            this.mFragment.startActivityForResult(new Intent("com.android.internal.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS", null), 1);
        }
        return true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mAirplaneModePreference = (SwitchPreference) screen.findPreference(getPreferenceKey());
        }
    }

    public static boolean isAvailable(Context context) {
        return context.getResources().getBoolean(R.bool.config_show_toggle_airplane) && !context.getPackageManager().hasSystemFeature("android.software.leanback");
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "toggle_airplane");
    }

    public int getAvailabilityStatus() {
        return isAvailable(this.mContext) ? 0 : 2;
    }

    public void onResume() {
        if (isAvailable()) {
            this.mAirplaneModeEnabler.resume();
        }
    }

    public void onPause() {
        if (isAvailable()) {
            this.mAirplaneModeEnabler.pause();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            this.mAirplaneModeEnabler.setAirplaneModeInECM(Boolean.valueOf(data.getBooleanExtra(EXIT_ECM_RESULT, false)).booleanValue(), this.mAirplaneModePreference.isChecked());
        }
    }

    public boolean isChecked() {
        return this.mAirplaneModeEnabler.isAirplaneModeOn();
    }

    public boolean setChecked(boolean isChecked) {
        if (isChecked() == isChecked) {
            return false;
        }
        this.mAirplaneModeEnabler.setAirplaneMode(isChecked);
        return true;
    }

    public void onAirplaneModeChanged(boolean isAirplaneModeOn) {
        if (this.mAirplaneModePreference != null) {
            this.mAirplaneModePreference.setChecked(isAirplaneModeOn);
        }
    }
}
