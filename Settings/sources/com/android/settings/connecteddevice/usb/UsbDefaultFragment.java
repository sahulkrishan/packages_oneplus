package com.android.settings.connecteddevice.usb;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.widget.RadioButtonPickerFragment;
import com.android.settingslib.widget.CandidateInfo;
import com.android.settingslib.widget.FooterPreferenceMixin;
import com.google.android.collect.Lists;
import java.util.List;

public class UsbDefaultFragment extends RadioButtonPickerFragment {
    @VisibleForTesting
    UsbBackend mUsbBackend;

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mUsbBackend = new UsbBackend(context);
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        new FooterPreferenceMixin(this, getLifecycle()).createFooterPreference().setTitle((int) R.string.usb_default_info);
    }

    public int getMetricsCategory() {
        return 1312;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.usb_default_fragment;
    }

    /* Access modifiers changed, original: protected */
    public List<? extends CandidateInfo> getCandidates() {
        List<CandidateInfo> ret = Lists.newArrayList();
        for (Long option : UsbDetailsFunctionsController.FUNCTIONS_MAP.keySet()) {
            long option2 = option.longValue();
            final String title = getContext().getString(((Integer) UsbDetailsFunctionsController.FUNCTIONS_MAP.get(Long.valueOf(option2))).intValue());
            final String key = UsbBackend.usbFunctionsToString(option2);
            if (this.mUsbBackend.areFunctionsSupported(option2)) {
                ret.add(new CandidateInfo(true) {
                    public CharSequence loadLabel() {
                        return title;
                    }

                    public Drawable loadIcon() {
                        return null;
                    }

                    public String getKey() {
                        return key;
                    }
                });
            }
        }
        return ret;
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        return UsbBackend.usbFunctionsToString(this.mUsbBackend.getDefaultUsbFunctions());
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String key) {
        long functions = UsbBackend.usbFunctionsFromString(key);
        if (!Utils.isMonkeyRunning()) {
            this.mUsbBackend.setDefaultUsbFunctions(functions);
        }
        return true;
    }
}
