package com.android.settings.connecteddevice.usb;

import android.content.Context;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.widget.RadioButtonPreference;
import com.android.settings.widget.RadioButtonPreference.OnClickListener;
import java.util.LinkedHashMap;
import java.util.Map;

public class UsbDetailsFunctionsController extends UsbDetailsController implements OnClickListener {
    static final Map<Long, Integer> FUNCTIONS_MAP = new LinkedHashMap();
    private PreferenceCategory mProfilesContainer;

    static {
        FUNCTIONS_MAP.put(Long.valueOf(4), Integer.valueOf(R.string.usb_use_file_transfers));
        FUNCTIONS_MAP.put(Long.valueOf(32), Integer.valueOf(R.string.usb_use_tethering));
        FUNCTIONS_MAP.put(Long.valueOf(8), Integer.valueOf(R.string.usb_use_MIDI));
        FUNCTIONS_MAP.put(Long.valueOf(16), Integer.valueOf(R.string.usb_use_photo_transfers));
        FUNCTIONS_MAP.put(Long.valueOf(0), Integer.valueOf(R.string.usb_use_charging_only));
    }

    public UsbDetailsFunctionsController(Context context, UsbDetailsFragment fragment, UsbBackend backend) {
        super(context, fragment, backend);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mProfilesContainer = (PreferenceCategory) screen.findPreference(getPreferenceKey());
    }

    private RadioButtonPreference getProfilePreference(String key, int titleId) {
        RadioButtonPreference pref = (RadioButtonPreference) this.mProfilesContainer.findPreference(key);
        if (pref != null) {
            return pref;
        }
        pref = new RadioButtonPreference(this.mProfilesContainer.getContext());
        pref.setKey(key);
        pref.setTitle(titleId);
        pref.setOnClickListener(this);
        this.mProfilesContainer.addPreference(pref);
        return pref;
    }

    /* Access modifiers changed, original: protected */
    public void refresh(boolean connected, long functions, int powerRole, int dataRole) {
        if (connected) {
            this.mProfilesContainer.setEnabled(true);
        } else {
            this.mProfilesContainer.setEnabled(false);
        }
        for (Long option : FUNCTIONS_MAP.keySet()) {
            long option2 = option.longValue();
            RadioButtonPreference pref = getProfilePreference(UsbBackend.usbFunctionsToString(option2), ((Integer) FUNCTIONS_MAP.get(Long.valueOf(option2))).intValue());
            if (this.mUsbBackend.areFunctionsSupported(option2)) {
                pref.setChecked(functions == option2);
            } else {
                this.mProfilesContainer.removePreference(pref);
            }
        }
    }

    public void onRadioButtonClicked(RadioButtonPreference preference) {
        long function = UsbBackend.usbFunctionsFromString(preference.getKey());
        if (function != this.mUsbBackend.getCurrentFunctions() && !Utils.isMonkeyRunning()) {
            this.mUsbBackend.setCurrentFunctions(function);
        }
    }

    public boolean isAvailable() {
        return Utils.isMonkeyRunning() ^ 1;
    }

    public String getPreferenceKey() {
        return "usb_details_functions";
    }
}
