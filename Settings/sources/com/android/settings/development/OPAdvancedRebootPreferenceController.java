package com.android.settings.development;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class OPAdvancedRebootPreferenceController extends DeveloperOptionsPreferenceController implements LifecycleObserver, OnResume, OnPause {
    private static final String KEY_ADVANCED_REBOOT = "advanced_reboot";
    private SettingObserver mSettingObserver;

    class SettingObserver extends ContentObserver {
        private final Uri ADVANCED_REBOOT_ENABLED_URI = Secure.getUriFor("advanced_reboot");
        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            this.mPreference = preference;
        }

        public void register(ContentResolver cr, boolean register) {
            if (register) {
                cr.registerContentObserver(this.ADVANCED_REBOOT_ENABLED_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.ADVANCED_REBOOT_ENABLED_URI.equals(uri)) {
                OPAdvancedRebootPreferenceController.this.updateState(this.mPreference);
            }
        }
    }

    public OPAdvancedRebootPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        lifecycle.addObserver(this);
    }

    public String getPreferenceKey() {
        return "advanced_reboot";
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (!(this.mPreference == null || isAdminUser())) {
            this.mPreference.setEnabled(false);
        }
        this.mSettingObserver = new SettingObserver(screen.findPreference("advanced_reboot"));
    }

    public void onResume() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.register(this.mContext.getContentResolver(), true);
        }
    }

    public void onPause() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.register(this.mContext.getContentResolver(), false);
        }
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), "advanced_reboot") || !(preference instanceof SwitchPreference)) {
            return false;
        }
        Secure.putInt(this.mContext.getContentResolver(), "advanced_reboot", ((SwitchPreference) preference).isChecked());
        return true;
    }

    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            SwitchPreference enableSwitchNetwork = (SwitchPreference) preference;
            boolean z = false;
            if (Secure.getInt(this.mContext.getContentResolver(), "advanced_reboot", 0) != 0) {
                z = true;
            }
            enableSwitchNetwork.setChecked(z);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isAdminUser() {
        return ((UserManager) this.mContext.getSystemService("user")).isAdminUser();
    }
}
