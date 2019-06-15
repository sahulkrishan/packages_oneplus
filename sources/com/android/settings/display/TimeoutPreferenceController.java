package com.android.settings.display;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.TimeoutListPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.core.AbstractPreferenceController;

public class TimeoutPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    public static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;
    private static final String TAG = "TimeoutPrefContr";
    private final String mScreenTimeoutKey;

    public TimeoutPreferenceController(Context context, String key) {
        super(context);
        this.mScreenTimeoutKey = key;
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return this.mScreenTimeoutKey;
    }

    public void updateState(Preference preference) {
        TimeoutListPreference timeoutListPreference = (TimeoutListPreference) preference;
        long currentTimeout = System.getLong(this.mContext.getContentResolver(), "screen_off_timeout", 30000);
        timeoutListPreference.setValue(String.valueOf(currentTimeout));
        DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        if (dpm != null) {
            timeoutListPreference.removeUnusableTimeouts(dpm.getMaximumTimeToLock(0, UserHandle.myUserId()), RestrictedLockUtils.checkIfMaximumTimeToLockIsSet(this.mContext));
        }
        updateTimeoutPreferenceDescription(timeoutListPreference, currentTimeout);
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_config_screen_timeout", UserHandle.myUserId());
        if (admin != null) {
            timeoutListPreference.removeUnusableTimeouts(0, admin);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            int value = Integer.parseInt((String) newValue);
            System.putInt(this.mContext.getContentResolver(), "screen_off_timeout", value);
            updateTimeoutPreferenceDescription((TimeoutListPreference) preference, (long) value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist screen timeout setting", e);
        }
        return true;
    }

    public static CharSequence getTimeoutDescription(long currentTimeout, CharSequence[] entries, CharSequence[] values) {
        if (currentTimeout < 0 || entries == null || values == null || values.length != entries.length) {
            return null;
        }
        for (int i = 0; i < values.length; i++) {
            if (currentTimeout == Long.parseLong(values[i].toString())) {
                return entries[i];
            }
        }
        return null;
    }

    private void updateTimeoutPreferenceDescription(TimeoutListPreference preference, long currentTimeout) {
        String summary;
        CharSequence[] entries = preference.getEntries();
        CharSequence[] values = preference.getEntryValues();
        if (preference.isDisabledByAdmin()) {
            summary = this.mContext.getString(R.string.disabled_by_policy_title);
        } else {
            String str;
            if (getTimeoutDescription(currentTimeout, entries, values) == null) {
                str = "";
            } else {
                str = this.mContext.getString(R.string.screen_timeout_summary, new Object[]{timeoutDescription});
            }
            summary = str;
        }
        preference.setSummary(summary);
    }
}
