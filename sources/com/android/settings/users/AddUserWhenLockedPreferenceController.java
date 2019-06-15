package com.android.settings.users;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class AddUserWhenLockedPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener, LifecycleObserver, OnPause, OnResume {
    private final String mPrefKey;
    private boolean mShouldUpdateUserList;
    private final UserCapabilities mUserCaps;

    public AddUserWhenLockedPreferenceController(Context context, String key, Lifecycle lifecycle) {
        super(context);
        this.mPrefKey = key;
        this.mUserCaps = UserCapabilities.create(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void updateState(Preference preference) {
        RestrictedSwitchPreference restrictedSwitchPreference = (RestrictedSwitchPreference) preference;
        boolean z = true;
        if (Global.getInt(this.mContext.getContentResolver(), "add_users_when_locked", 0) != 1) {
            z = false;
        }
        restrictedSwitchPreference.setChecked(z);
        restrictedSwitchPreference.setDisabledByAdmin(this.mUserCaps.disallowAddUser() ? this.mUserCaps.getEnforcedAdmin() : null);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i;
        Boolean value = (Boolean) newValue;
        ContentResolver contentResolver = this.mContext.getContentResolver();
        String str = "add_users_when_locked";
        if (value == null || !value.booleanValue()) {
            i = 0;
        } else {
            i = 1;
        }
        Global.putInt(contentResolver, str, i);
        return true;
    }

    public void onPause() {
        this.mShouldUpdateUserList = true;
    }

    public void onResume() {
        if (this.mShouldUpdateUserList) {
            this.mUserCaps.updateAddUserCapabilities(this.mContext);
        }
    }

    public boolean isAvailable() {
        return this.mUserCaps.isAdmin() && (!this.mUserCaps.disallowAddUser() || this.mUserCaps.disallowAddUserSetByAdmin());
    }

    public String getPreferenceKey() {
        return this.mPrefKey;
    }
}
