package com.android.settings.connecteddevice.usb;

import android.content.Context;
import android.os.Handler;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.Utils;

public class UsbDetailsPowerRoleController extends UsbDetailsController implements OnPreferenceClickListener {
    private final Runnable mFailureCallback = new -$$Lambda$UsbDetailsPowerRoleController$jiVF0c0jApWPiJapsUjjyYudYlM(this);
    private int mNextPowerRole = 0;
    private PreferenceCategory mPreferenceCategory;
    private SwitchPreference mSwitchPreference;

    public static /* synthetic */ void lambda$new$0(UsbDetailsPowerRoleController usbDetailsPowerRoleController) {
        if (usbDetailsPowerRoleController.mNextPowerRole != 0) {
            usbDetailsPowerRoleController.mSwitchPreference.setSummary((int) R.string.usb_switching_failed);
            usbDetailsPowerRoleController.mNextPowerRole = 0;
        }
    }

    public UsbDetailsPowerRoleController(Context context, UsbDetailsFragment fragment, UsbBackend backend) {
        super(context, fragment, backend);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreferenceCategory = (PreferenceCategory) screen.findPreference(getPreferenceKey());
        this.mSwitchPreference = new SwitchPreference(this.mPreferenceCategory.getContext());
        this.mSwitchPreference.setTitle((int) R.string.usb_use_power_only);
        this.mSwitchPreference.setOnPreferenceClickListener(this);
        this.mPreferenceCategory.addPreference(this.mSwitchPreference);
    }

    /* Access modifiers changed, original: protected */
    public void refresh(boolean connected, long functions, int powerRole, int dataRole) {
        if (connected && !this.mUsbBackend.areAllRolesSupported()) {
            this.mFragment.getPreferenceScreen().removePreference(this.mPreferenceCategory);
        } else if (connected && this.mUsbBackend.areAllRolesSupported()) {
            this.mFragment.getPreferenceScreen().addPreference(this.mPreferenceCategory);
        }
        if (powerRole == 1) {
            this.mSwitchPreference.setChecked(true);
            this.mPreferenceCategory.setEnabled(true);
        } else if (powerRole == 2) {
            this.mSwitchPreference.setChecked(false);
            this.mPreferenceCategory.setEnabled(true);
        } else if (!connected) {
            this.mPreferenceCategory.setEnabled(false);
            if (this.mNextPowerRole == 0) {
                this.mSwitchPreference.setSummary((CharSequence) "");
            }
        }
        if (this.mNextPowerRole != 0 && powerRole != 0) {
            if (this.mNextPowerRole == powerRole) {
                this.mSwitchPreference.setSummary((CharSequence) "");
            } else {
                this.mSwitchPreference.setSummary((int) R.string.usb_switching_failed);
            }
            this.mNextPowerRole = 0;
            this.mHandler.removeCallbacks(this.mFailureCallback);
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        int newRole = this.mSwitchPreference.isChecked() ? 1 : 2;
        if (!(this.mUsbBackend.getPowerRole() == newRole || this.mNextPowerRole != 0 || Utils.isMonkeyRunning())) {
            long j;
            this.mUsbBackend.setPowerRole(newRole);
            this.mNextPowerRole = newRole;
            this.mSwitchPreference.setSummary((int) R.string.usb_switching);
            Handler handler = this.mHandler;
            Runnable runnable = this.mFailureCallback;
            if (this.mUsbBackend.areAllRolesSupported()) {
                j = 3000;
            } else {
                j = 15000;
            }
            handler.postDelayed(runnable, j);
        }
        this.mSwitchPreference.setChecked(this.mSwitchPreference.isChecked() ^ 1);
        return true;
    }

    public boolean isAvailable() {
        return Utils.isMonkeyRunning() ^ 1;
    }

    public String getPreferenceKey() {
        return "usb_details_power_role";
    }
}
