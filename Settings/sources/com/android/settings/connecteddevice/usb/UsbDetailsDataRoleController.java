package com.android.settings.connecteddevice.usb;

import android.content.Context;
import android.os.Handler;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.widget.RadioButtonPreference;
import com.android.settings.widget.RadioButtonPreference.OnClickListener;

public class UsbDetailsDataRoleController extends UsbDetailsController implements OnClickListener {
    private RadioButtonPreference mDevicePref;
    private final Runnable mFailureCallback = new -$$Lambda$UsbDetailsDataRoleController$cU-Vca-1LUjTmehDhPZv_qMdSP8(this);
    private RadioButtonPreference mHostPref;
    private RadioButtonPreference mNextRolePref;
    private PreferenceCategory mPreferenceCategory;

    public static /* synthetic */ void lambda$new$0(UsbDetailsDataRoleController usbDetailsDataRoleController) {
        if (usbDetailsDataRoleController.mNextRolePref != null) {
            usbDetailsDataRoleController.mNextRolePref.setSummary((int) R.string.usb_switching_failed);
            usbDetailsDataRoleController.mNextRolePref = null;
        }
    }

    public UsbDetailsDataRoleController(Context context, UsbDetailsFragment fragment, UsbBackend backend) {
        super(context, fragment, backend);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreferenceCategory = (PreferenceCategory) screen.findPreference(getPreferenceKey());
        this.mHostPref = makeRadioPreference(UsbBackend.dataRoleToString(1), R.string.usb_control_host);
        this.mDevicePref = makeRadioPreference(UsbBackend.dataRoleToString(2), R.string.usb_control_device);
    }

    /* Access modifiers changed, original: protected */
    public void refresh(boolean connected, long functions, int powerRole, int dataRole) {
        if (dataRole == 2) {
            this.mDevicePref.setChecked(true);
            this.mHostPref.setChecked(false);
            this.mPreferenceCategory.setEnabled(true);
        } else if (dataRole == 1) {
            this.mDevicePref.setChecked(false);
            this.mHostPref.setChecked(true);
            this.mPreferenceCategory.setEnabled(true);
        } else if (!connected) {
            this.mPreferenceCategory.setEnabled(false);
            if (this.mNextRolePref == null) {
                this.mHostPref.setSummary((CharSequence) "");
                this.mDevicePref.setSummary((CharSequence) "");
            }
        }
        if (this.mNextRolePref != null && dataRole != 0) {
            if (UsbBackend.dataRoleFromString(this.mNextRolePref.getKey()) == dataRole) {
                this.mNextRolePref.setSummary((CharSequence) "");
            } else {
                this.mNextRolePref.setSummary((int) R.string.usb_switching_failed);
            }
            this.mNextRolePref = null;
            this.mHandler.removeCallbacks(this.mFailureCallback);
        }
    }

    public void onRadioButtonClicked(RadioButtonPreference preference) {
        int role = UsbBackend.dataRoleFromString(preference.getKey());
        if (role != this.mUsbBackend.getDataRole() && this.mNextRolePref == null && !Utils.isMonkeyRunning()) {
            long j;
            this.mUsbBackend.setDataRole(role);
            this.mNextRolePref = preference;
            preference.setSummary((int) R.string.usb_switching);
            Handler handler = this.mHandler;
            Runnable runnable = this.mFailureCallback;
            if (this.mUsbBackend.areAllRolesSupported()) {
                j = 3000;
            } else {
                j = 15000;
            }
            handler.postDelayed(runnable, j);
        }
    }

    public boolean isAvailable() {
        return false;
    }

    public String getPreferenceKey() {
        return "usb_details_data_role";
    }

    private RadioButtonPreference makeRadioPreference(String key, int titleId) {
        RadioButtonPreference pref = new RadioButtonPreference(this.mPreferenceCategory.getContext());
        pref.setKey(key);
        pref.setTitle(titleId);
        pref.setOnClickListener(this);
        this.mPreferenceCategory.addPreference(pref);
        return pref;
    }
}
