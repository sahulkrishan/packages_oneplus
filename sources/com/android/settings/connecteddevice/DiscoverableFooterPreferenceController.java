package com.android.settings.connecteddevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings.System;
import android.support.v7.preference.PreferenceScreen;
import android.text.BidiFormatter;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.bluetooth.AlwaysDiscoverable;
import com.android.settings.bluetooth.Utils;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.widget.FooterPreference;
import com.android.settingslib.widget.FooterPreferenceMixin;

public class DiscoverableFooterPreferenceController extends BasePreferenceController implements LifecycleObserver, OnResume, OnPause {
    private static final String KEY = "discoverable_footer_preference";
    private AlwaysDiscoverable mAlwaysDiscoverable;
    @VisibleForTesting
    BroadcastReceiver mBluetoothChangedReceiver;
    private FooterPreferenceMixin mFooterPreferenceMixin;
    private LocalBluetoothAdapter mLocalAdapter;
    private LocalBluetoothManager mLocalManager;
    private FooterPreference mPreference;

    public DiscoverableFooterPreferenceController(Context context) {
        super(context, KEY);
        this.mLocalManager = Utils.getLocalBtManager(context);
        if (this.mLocalManager != null) {
            this.mLocalAdapter = this.mLocalManager.getBluetoothAdapter();
            this.mAlwaysDiscoverable = new AlwaysDiscoverable(context, this.mLocalAdapter);
            initReceiver();
        }
    }

    private void initReceiver() {
        this.mBluetoothChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                    DiscoverableFooterPreferenceController.this.updateFooterPreferenceTitle(intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE));
                }
            }
        };
    }

    public void init(DashboardFragment fragment) {
        this.mFooterPreferenceMixin = new FooterPreferenceMixin(fragment, fragment.getLifecycle());
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void init(FooterPreferenceMixin footerPreferenceMixin, FooterPreference preference, AlwaysDiscoverable alwaysDiscoverable) {
        this.mFooterPreferenceMixin = footerPreferenceMixin;
        this.mPreference = preference;
        this.mAlwaysDiscoverable = alwaysDiscoverable;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        addFooterPreference(screen);
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
            return 0;
        }
        return 2;
    }

    private void addFooterPreference(PreferenceScreen screen) {
        this.mPreference = this.mFooterPreferenceMixin.createFooterPreference();
        this.mPreference.setKey(KEY);
        screen.addPreference(this.mPreference);
    }

    public void onResume() {
        this.mContext.registerReceiver(this.mBluetoothChangedReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
        this.mAlwaysDiscoverable.start();
        updateFooterPreferenceTitle(this.mLocalAdapter.getState());
    }

    public void onPause() {
        this.mContext.unregisterReceiver(this.mBluetoothChangedReceiver);
        this.mAlwaysDiscoverable.stop();
    }

    private void updateFooterPreferenceTitle(int bluetoothState) {
        if (bluetoothState == 12) {
            this.mPreference.setTitle(getPreferenceTitle());
        } else {
            this.mPreference.setTitle((int) R.string.bluetooth_off_footer);
        }
    }

    private CharSequence getPreferenceTitle() {
        if (TextUtils.isEmpty(System.getString(this.mContext.getContentResolver(), "oem_oneplus_devicename"))) {
            return null;
        }
        return TextUtils.expandTemplate(this.mContext.getText(R.string.bluetooth_device_name_summary), new CharSequence[]{BidiFormatter.getInstance().unicodeWrap(deviceName)});
    }
}
