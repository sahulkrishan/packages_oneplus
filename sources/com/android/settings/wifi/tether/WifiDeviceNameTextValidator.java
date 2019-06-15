package com.android.settings.wifi.tether;

import com.android.settings.widget.ValidatedEditTextPreference.Validator;
import com.android.settings.wifi.WifiUtils;

public class WifiDeviceNameTextValidator implements Validator {
    public boolean isTextValid(String value) {
        return (WifiUtils.isSSIDTooLong(value) || WifiUtils.isSSIDTooShort(value)) ? false : true;
    }
}
