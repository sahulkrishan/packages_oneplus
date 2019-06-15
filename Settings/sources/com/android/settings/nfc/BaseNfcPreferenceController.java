package com.android.settings.nfc;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import java.util.List;

public abstract class BaseNfcPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnResume, OnPause {
    private int mAirplaneMode;
    private AirplaneModeObserver mAirplaneModeObserver;
    private NfcAdapter mNfcAdapter;
    protected BaseNfcEnabler mNfcEnabler;
    protected Preference mPreference;

    private final class AirplaneModeObserver extends ContentObserver {
        private final Uri AIRPLANE_MODE_URI;

        private AirplaneModeObserver() {
            super(new Handler());
            this.AIRPLANE_MODE_URI = Global.getUriFor("airplane_mode_on");
        }

        public void register() {
            BaseNfcPreferenceController.this.mContext.getContentResolver().registerContentObserver(this.AIRPLANE_MODE_URI, false, this);
        }

        public void unregister() {
            BaseNfcPreferenceController.this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            BaseNfcPreferenceController.this.updateNfcPreference();
        }
    }

    public abstract String getPreferenceKey();

    public BaseNfcPreferenceController(Context context) {
        super(context);
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mPreference = screen.findPreference(getPreferenceKey());
            if (!isToggleableInAirplaneMode(this.mContext)) {
                this.mAirplaneModeObserver = new AirplaneModeObserver();
                updateNfcPreference();
            }
            return;
        }
        this.mNfcEnabler = null;
    }

    public void updateNonIndexableKeys(List<String> keys) {
        if (!isAvailable()) {
            keys.add(getPreferenceKey());
        }
    }

    public boolean isAvailable() {
        return this.mNfcAdapter != null;
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

    private void updateNfcPreference() {
        int airplaneMode = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", this.mAirplaneMode);
        if (airplaneMode != this.mAirplaneMode) {
            this.mAirplaneMode = airplaneMode;
            boolean z = true;
            if (this.mAirplaneMode == 1) {
                z = false;
            }
            boolean toggleable = z;
            if (toggleable) {
                this.mNfcAdapter.enable();
            } else {
                this.mNfcAdapter.disable();
            }
            this.mPreference.setEnabled(toggleable);
        }
    }

    public static boolean isToggleableInAirplaneMode(Context context) {
        String toggleable = Global.getString(context.getContentResolver(), "airplane_mode_toggleable_radios");
        return toggleable != null && toggleable.contains("nfc");
    }
}
