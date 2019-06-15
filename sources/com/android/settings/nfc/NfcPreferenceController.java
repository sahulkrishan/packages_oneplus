package com.android.settings.nfc;

import android.content.Context;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class NfcPreferenceController extends TogglePreferenceController implements LifecycleObserver, OnResume, OnPause {
    public static final String KEY_TOGGLE_NFC = "toggle_nfc";
    private NfcAirplaneModeObserver mAirplaneModeObserver;
    private final NfcAdapter mNfcAdapter;
    private NfcEnabler mNfcEnabler;

    public NfcPreferenceController(Context context, String key) {
        super(context, key);
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            SwitchPreference switchPreference = (SwitchPreference) screen.findPreference(getPreferenceKey());
            this.mNfcEnabler = new NfcEnabler(this.mContext, switchPreference);
            if (!isToggleableInAirplaneMode(this.mContext)) {
                this.mAirplaneModeObserver = new NfcAirplaneModeObserver(this.mContext, this.mNfcAdapter, switchPreference);
            }
            return;
        }
        this.mNfcEnabler = null;
    }

    public boolean isChecked() {
        return this.mNfcAdapter.isEnabled();
    }

    public boolean setChecked(boolean isChecked) {
        if (isChecked) {
            this.mNfcAdapter.enable();
        } else {
            this.mNfcAdapter.disable();
        }
        return true;
    }

    public int getAvailabilityStatus() {
        if (this.mNfcAdapter != null) {
            return 0;
        }
        return 2;
    }

    public IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.nfc.action.ADAPTER_STATE_CHANGED");
        filter.addAction("android.nfc.extra.ADAPTER_STATE");
        return filter;
    }

    public boolean hasAsyncUpdate() {
        return true;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), KEY_TOGGLE_NFC);
    }

    public void onResume() {
        if (this.mAirplaneModeObserver != null) {
            this.mAirplaneModeObserver.register();
        }
        if (this.mNfcEnabler != null) {
            this.mNfcEnabler.resume();
        }
    }

    public void onPause() {
        if (this.mAirplaneModeObserver != null) {
            this.mAirplaneModeObserver.unregister();
        }
        if (this.mNfcEnabler != null) {
            this.mNfcEnabler.pause();
        }
    }

    public static boolean isToggleableInAirplaneMode(Context context) {
        String toggleable = Global.getString(context.getContentResolver(), "airplane_mode_toggleable_radios");
        return toggleable != null && toggleable.contains("nfc");
    }
}
