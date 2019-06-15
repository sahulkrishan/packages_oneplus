package com.android.settings.location;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class LocationForWorkPreferenceController extends LocationBasePreferenceController {
    public static final String KEY_MANAGED_PROFILE_SWITCH = "managed_profile_location_switch";
    private RestrictedSwitchPreference mPreference;

    public LocationForWorkPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, lifecycle);
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_MANAGED_PROFILE_SWITCH.equals(preference.getKey())) {
            return false;
        }
        boolean switchState = this.mPreference.isChecked();
        this.mUserManager.setUserRestriction("no_share_location", switchState ^ 1, Utils.getManagedProfile(this.mUserManager));
        this.mPreference.setSummary(switchState ? R.string.switch_on_text : R.string.switch_off_text);
        return true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (RestrictedSwitchPreference) screen.findPreference(KEY_MANAGED_PROFILE_SWITCH);
    }

    public boolean isAvailable() {
        return Utils.getManagedProfile(this.mUserManager) != null;
    }

    public String getPreferenceKey() {
        return KEY_MANAGED_PROFILE_SWITCH;
    }

    public void onLocationModeChanged(int mode, boolean restricted) {
        if (this.mPreference.isVisible() && isAvailable()) {
            EnforcedAdmin admin = this.mLocationEnabler.getShareLocationEnforcedAdmin(Utils.getManagedProfile(this.mUserManager).getIdentifier());
            boolean isRestrictedByBase = this.mLocationEnabler.isManagedProfileRestrictedByBase();
            if (isRestrictedByBase || admin == null) {
                boolean enabled = this.mLocationEnabler.isEnabled(mode);
                this.mPreference.setEnabled(enabled);
                int summaryResId = R.string.switch_off_text;
                if (enabled) {
                    this.mPreference.setChecked(isRestrictedByBase ^ 1);
                    summaryResId = isRestrictedByBase ? R.string.switch_off_text : R.string.switch_on_text;
                } else {
                    this.mPreference.setChecked(false);
                }
                this.mPreference.setSummary(summaryResId);
            } else {
                this.mPreference.setDisabledByAdmin(admin);
                this.mPreference.setChecked(false);
            }
        }
    }
}
