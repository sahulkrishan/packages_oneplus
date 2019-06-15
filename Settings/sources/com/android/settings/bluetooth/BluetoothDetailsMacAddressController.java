package com.android.settings.bluetooth;

import android.content.Context;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.widget.FooterPreference;
import com.android.settingslib.widget.FooterPreferenceMixin;

public class BluetoothDetailsMacAddressController extends BluetoothDetailsController {
    FooterPreference mFooterPreference;
    FooterPreferenceMixin mFooterPreferenceMixin;

    public BluetoothDetailsMacAddressController(Context context, PreferenceFragment fragment, CachedBluetoothDevice device, Lifecycle lifecycle) {
        super(context, fragment, device, lifecycle);
        this.mFooterPreferenceMixin = new FooterPreferenceMixin(fragment, lifecycle);
    }

    /* Access modifiers changed, original: protected */
    public void init(PreferenceScreen screen) {
        this.mFooterPreference = this.mFooterPreferenceMixin.createFooterPreference();
        this.mFooterPreference.setTitle((CharSequence) this.mContext.getString(R.string.bluetooth_device_mac_address, new Object[]{this.mCachedDevice.getAddress()}));
    }

    /* Access modifiers changed, original: protected */
    public void refresh() {
    }

    public String getPreferenceKey() {
        if (this.mFooterPreference == null) {
            return null;
        }
        return this.mFooterPreference.getKey();
    }
}
