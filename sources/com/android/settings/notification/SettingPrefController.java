package com.android.settings.notification;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public abstract class SettingPrefController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnResume, OnPause {
    protected static final int DEFAULT_ON = 1;
    private SettingsPreferenceFragment mParent;
    protected SettingPref mPreference;
    protected SettingsObserver mSettingsObserver;

    @VisibleForTesting
    final class SettingsObserver extends ContentObserver {
        public SettingsObserver() {
            super(new Handler());
        }

        public void register(boolean register) {
            ContentResolver cr = SettingPrefController.this.mContext.getContentResolver();
            if (register) {
                cr.registerContentObserver(SettingPrefController.this.mPreference.getUri(), false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (SettingPrefController.this.mPreference.getUri().equals(uri)) {
                SettingPrefController.this.mPreference.update(SettingPrefController.this.mContext);
            }
        }
    }

    public SettingPrefController(Context context, SettingsPreferenceFragment parent, Lifecycle lifecycle) {
        super(context);
        this.mParent = parent;
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        this.mPreference.init(this.mParent);
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mSettingsObserver = new SettingsObserver();
        }
    }

    public String getPreferenceKey() {
        return this.mPreference.getKey();
    }

    public boolean isAvailable() {
        return this.mPreference.isApplicable(this.mContext);
    }

    public void updateState(Preference preference) {
        this.mPreference.update(this.mContext);
    }

    public void onResume() {
        if (this.mSettingsObserver != null) {
            this.mSettingsObserver.register(true);
        }
    }

    public void onPause() {
        if (this.mSettingsObserver != null) {
            this.mSettingsObserver.register(false);
        }
    }
}
