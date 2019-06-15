package com.android.settings.system;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class OPOTGPreferenceController extends AbstractPreferenceController implements LifecycleObserver, OnResume, OnPause {
    private static final String KEY_OTG_READ_ENABLE = "otg_read_enable";
    private SettingObserver mSettingObserver;

    class SettingObserver extends ContentObserver {
        private final Uri OTG_AUTO_ENABLED_URI = Global.getUriFor("oneplus_otg_auto_disable");
        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            this.mPreference = preference;
        }

        public void register(ContentResolver cr, boolean register) {
            if (register) {
                cr.registerContentObserver(this.OTG_AUTO_ENABLED_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.OTG_AUTO_ENABLED_URI.equals(uri)) {
                OPOTGPreferenceController.this.updateState(this.mPreference);
            }
        }
    }

    public OPOTGPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        lifecycle.addObserver(this);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSettingObserver = new SettingObserver(screen.findPreference(KEY_OTG_READ_ENABLE));
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
        if (!TextUtils.equals(preference.getKey(), KEY_OTG_READ_ENABLE) || !(preference instanceof SwitchPreference)) {
            return false;
        }
        SystemProperties.set("persist.sys.oem.otg_support", ((SwitchPreference) preference).isChecked() ? "true" : "false");
        Global.putInt(this.mContext.getContentResolver(), "oneplus_otg_auto_disable", ((SwitchPreference) preference).isChecked());
        return true;
    }

    public String getPreferenceKey() {
        return KEY_OTG_READ_ENABLE;
    }

    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            SwitchPreference enableSwitchNetwork = (SwitchPreference) preference;
            boolean z = true;
            if (Global.getInt(this.mContext.getContentResolver(), "oneplus_otg_auto_disable", 0) != 1) {
                z = false;
            }
            enableSwitchNetwork.setChecked(z);
        }
    }
}
