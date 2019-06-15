package com.android.settings.notification;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.notification.ZenModeSettings.SummaryBuilder;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class ZenModePreferenceController extends AbstractPreferenceController implements LifecycleObserver, OnResume, OnPause, PreferenceControllerMixin {
    private final String mKey;
    private SettingObserver mSettingObserver;
    private SummaryBuilder mSummaryBuilder;

    class SettingObserver extends ContentObserver {
        private final Uri ZEN_MODE_CONFIG_ETAG_URI = Global.getUriFor("zen_mode_config_etag");
        private final Uri ZEN_MODE_URI = Global.getUriFor("zen_mode");
        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            this.mPreference = preference;
        }

        public void register(ContentResolver cr) {
            cr.registerContentObserver(this.ZEN_MODE_URI, false, this, -1);
            cr.registerContentObserver(this.ZEN_MODE_CONFIG_ETAG_URI, false, this, -1);
        }

        public void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.ZEN_MODE_URI.equals(uri)) {
                ZenModePreferenceController.this.updateState(this.mPreference);
            }
            if (this.ZEN_MODE_CONFIG_ETAG_URI.equals(uri)) {
                ZenModePreferenceController.this.updateState(this.mPreference);
            }
        }
    }

    public ZenModePreferenceController(Context context, Lifecycle lifecycle, String key) {
        super(context);
        this.mSummaryBuilder = new SummaryBuilder(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
        this.mKey = key;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSettingObserver = new SettingObserver(screen.findPreference(this.mKey));
    }

    public void onResume() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.register(this.mContext.getContentResolver());
        }
    }

    public void onPause() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.unregister(this.mContext.getContentResolver());
        }
    }

    public String getPreferenceKey() {
        return this.mKey;
    }

    public boolean isAvailable() {
        return true;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference.isEnabled()) {
            preference.setSummary(this.mSummaryBuilder.getSoundSummary());
        }
    }
}
