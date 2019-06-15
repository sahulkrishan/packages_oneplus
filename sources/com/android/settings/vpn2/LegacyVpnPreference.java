package com.android.settings.vpn2;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.view.View;
import com.android.internal.net.VpnProfile;
import com.android.settings.R;

public class LegacyVpnPreference extends ManageablePreference {
    private VpnProfile mProfile;

    LegacyVpnPreference(Context context) {
        super(context, null);
        setIcon((int) R.drawable.ic_vpn_key);
        setIconSize(2);
    }

    public VpnProfile getProfile() {
        return this.mProfile;
    }

    public void setProfile(VpnProfile profile) {
        String newLabel = null;
        String oldLabel = this.mProfile != null ? this.mProfile.name : null;
        if (profile != null) {
            newLabel = profile.name;
        }
        if (!TextUtils.equals(oldLabel, newLabel)) {
            setTitle((CharSequence) newLabel);
            notifyHierarchyChanged();
        }
        this.mProfile = profile;
    }

    public int compareTo(Preference preference) {
        if (preference instanceof LegacyVpnPreference) {
            LegacyVpnPreference another = (LegacyVpnPreference) preference;
            int i = another.mState - this.mState;
            int result = i;
            if (i == 0) {
                i = this.mProfile.name.compareToIgnoreCase(another.mProfile.name);
                result = i;
                if (i == 0) {
                    i = this.mProfile.type - another.mProfile.type;
                    result = i;
                    if (i == 0) {
                        result = this.mProfile.key.compareTo(another.mProfile.key);
                    }
                }
            }
            return result;
        } else if (!(preference instanceof AppPreference)) {
            return super.compareTo(preference);
        } else {
            AppPreference another2 = (AppPreference) preference;
            if (this.mState == 3 || another2.getState() != 3) {
                return -1;
            }
            return 1;
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.settings_button && isDisabledByAdmin()) {
            performClick();
        } else {
            super.onClick(v);
        }
    }
}
