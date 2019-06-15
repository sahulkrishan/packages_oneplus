package com.android.settings.connecteddevice;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.FeatureFlagUtils;
import com.android.settings.core.FeatureFlags;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.TogglePreferenceController;

public class BluetoothOnWhileDrivingPreferenceController extends TogglePreferenceController implements PreferenceControllerMixin {
    static final String KEY_BLUETOOTH_ON_DRIVING = "bluetooth_on_while_driving";

    public BluetoothOnWhileDrivingPreferenceController(Context context) {
        super(context, KEY_BLUETOOTH_ON_DRIVING);
    }

    public int getAvailabilityStatus() {
        if (FeatureFlagUtils.isEnabled(this.mContext, FeatureFlags.BLUETOOTH_WHILE_DRIVING)) {
            return 0;
        }
        return 1;
    }

    public boolean isChecked() {
        return Secure.getInt(this.mContext.getContentResolver(), KEY_BLUETOOTH_ON_DRIVING, 0) != 0;
    }

    public boolean setChecked(boolean isChecked) {
        return Secure.putInt(this.mContext.getContentResolver(), KEY_BLUETOOTH_ON_DRIVING, isChecked);
    }
}
