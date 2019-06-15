package com.android.settings.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.util.Pair;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

public class BluetoothFilesPreferenceController extends BasePreferenceController implements PreferenceControllerMixin {
    @VisibleForTesting
    static final String ACTION_OPEN_FILES = "com.android.bluetooth.action.TransferHistory";
    @VisibleForTesting
    static final String EXTRA_DIRECTION = "direction";
    @VisibleForTesting
    static final String EXTRA_SHOW_ALL_FILES = "android.btopp.intent.extra.SHOW_ALL";
    public static final String KEY_RECEIVED_FILES = "bt_received_files";
    private static final String TAG = "BluetoothFilesPrefCtrl";
    private MetricsFeatureProvider mMetricsFeatureProvider;

    public BluetoothFilesPreferenceController(Context context) {
        super(context, KEY_RECEIVED_FILES);
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
            return 0;
        }
        return 2;
    }

    public String getPreferenceKey() {
        return KEY_RECEIVED_FILES;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_RECEIVED_FILES.equals(preference.getKey())) {
            return false;
        }
        this.mMetricsFeatureProvider.action(this.mContext, 162, new Pair[0]);
        Intent intent = new Intent(ACTION_OPEN_FILES);
        intent.setFlags(335544320);
        intent.putExtra(EXTRA_DIRECTION, 1);
        intent.putExtra(EXTRA_SHOW_ALL_FILES, true);
        this.mContext.startActivity(intent);
        return true;
    }
}
