package com.android.settings.bluetooth;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.System;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Pair;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

public class BluetoothDeviceRenamePreferenceController extends BluetoothDeviceNamePreferenceController {
    private Fragment mFragment;
    private MetricsFeatureProvider mMetricsFeatureProvider;

    public BluetoothDeviceRenamePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
    }

    @VisibleForTesting
    BluetoothDeviceRenamePreferenceController(Context context, LocalBluetoothAdapter localAdapter, String preferenceKey) {
        super(context, localAdapter, preferenceKey);
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
    }

    @VisibleForTesting
    public void setFragment(Fragment fragment) {
        this.mFragment = fragment;
    }

    /* Access modifiers changed, original: protected */
    public void updatePreferenceState(Preference preference) {
        preference.setSummary(getSummary());
        boolean z = this.mLocalAdapter != null && this.mLocalAdapter.isEnabled();
        preference.setVisible(z);
    }

    public CharSequence getSummary() {
        return System.getString(this.mContext.getContentResolver(), "oem_oneplus_devicename");
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(getPreferenceKey(), preference.getKey()) || this.mFragment == null) {
            return false;
        }
        this.mMetricsFeatureProvider.action(this.mContext, 161, new Pair[0]);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$OPDeviceNameActivity"));
        this.mContext.startActivity(intent);
        return true;
    }
}
