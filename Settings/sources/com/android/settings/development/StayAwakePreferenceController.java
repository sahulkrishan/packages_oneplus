package com.android.settings.development;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class StayAwakePreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, LifecycleObserver, OnResume, OnPause, PreferenceControllerMixin {
    private static final String PREFERENCE_KEY = "keep_screen_on";
    @VisibleForTesting
    static final int SETTING_VALUE_OFF = 0;
    @VisibleForTesting
    static final int SETTING_VALUE_ON = 7;
    private static final String TAG = "StayAwakeCtrl";
    private RestrictedSwitchPreference mPreference;
    @VisibleForTesting
    SettingsObserver mSettingsObserver;

    @VisibleForTesting
    class SettingsObserver extends ContentObserver {
        private final Uri mStayAwakeUri = Global.getUriFor("stay_on_while_plugged_in");

        public SettingsObserver() {
            super(new Handler());
        }

        public void register(boolean register) {
            ContentResolver cr = StayAwakePreferenceController.this.mContext.getContentResolver();
            if (register) {
                cr.registerContentObserver(this.mStayAwakeUri, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.mStayAwakeUri.equals(uri)) {
                StayAwakePreferenceController.this.updateState(StayAwakePreferenceController.this.mPreference);
            }
        }
    }

    public StayAwakePreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public String getPreferenceKey() {
        return PREFERENCE_KEY;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (RestrictedSwitchPreference) screen.findPreference(getPreferenceKey());
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Global.putInt(this.mContext.getContentResolver(), "stay_on_while_plugged_in", ((Boolean) newValue).booleanValue() ? 7 : 0);
        return true;
    }

    public void updateState(Preference preference) {
        EnforcedAdmin admin = checkIfMaximumTimeToLockSetByAdmin();
        if (admin != null) {
            this.mPreference.setDisabledByAdmin(admin);
            return;
        }
        boolean z = false;
        int stayAwakeMode = Global.getInt(this.mContext.getContentResolver(), "stay_on_while_plugged_in", 0);
        RestrictedSwitchPreference restrictedSwitchPreference = this.mPreference;
        if (stayAwakeMode != 0) {
            z = true;
        }
        restrictedSwitchPreference.setChecked(z);
    }

    public void onResume() {
        if (this.mPreference != null) {
            if (this.mSettingsObserver == null) {
                this.mSettingsObserver = new SettingsObserver();
            }
            this.mSettingsObserver.register(true);
        }
    }

    public void onPause() {
        if (this.mPreference != null && this.mSettingsObserver != null) {
            this.mSettingsObserver.register(false);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Global.putInt(this.mContext.getContentResolver(), "stay_on_while_plugged_in", 0);
        this.mPreference.setChecked(false);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public EnforcedAdmin checkIfMaximumTimeToLockSetByAdmin() {
        return RestrictedLockUtils.checkIfMaximumTimeToLockIsSet(this.mContext);
    }
}
