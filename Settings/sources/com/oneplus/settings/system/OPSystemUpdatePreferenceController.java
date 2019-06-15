package com.oneplus.settings.system;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserManager;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreate;
import com.android.settingslib.core.lifecycle.events.OnDestroy;

public class OPSystemUpdatePreferenceController extends BasePreferenceController implements LifecycleObserver, OnCreate, OnDestroy {
    private static final String KEY_OP_SYSTEM_UPDATE_SETTINGS = "oneplus_system_update_settings";
    private static final String TAG = "OPSysUpdatePrefContr";
    private Context mContext;
    private SystemUpdateObserver mSystemUpdateObserver;
    private final UserManager mUm;
    OPSystemUpdatePreference mUpdatePreference;

    private class SystemUpdateObserver extends ContentObserver {
        public static final String HAS_NEW_VERSION_TO_UPDATE = "has_new_version_to_update";
        private final Uri SYSTEM_UPDATE_URI = System.getUriFor("has_new_version_to_update");

        public SystemUpdateObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            OPSystemUpdatePreferenceController.this.updateState(OPSystemUpdatePreferenceController.this.mUpdatePreference);
        }

        public void onChange(boolean selfChange) {
            OPSystemUpdatePreferenceController.this.updateState(OPSystemUpdatePreferenceController.this.mUpdatePreference);
        }

        public void startObserving() {
            ContentResolver cr = OPSystemUpdatePreferenceController.this.mContext.getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(this.SYSTEM_UPDATE_URI, false, this, -1);
        }

        public void stopObserving() {
            OPSystemUpdatePreferenceController.this.mContext.getContentResolver().unregisterContentObserver(this);
        }
    }

    public OPSystemUpdatePreferenceController(Context context, String key) {
        super(context, KEY_OP_SYSTEM_UPDATE_SETTINGS);
        this.mUm = UserManager.get(context);
        this.mContext = context;
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getResources().getBoolean(R.bool.config_use_gota) || !this.mUm.isAdminUser()) {
            return 2;
        }
        return 0;
    }

    public String getPreferenceKey() {
        return KEY_OP_SYSTEM_UPDATE_SETTINGS;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mUpdatePreference = (OPSystemUpdatePreference) screen.findPreference(getPreferenceKey());
        }
    }

    public void updateState(Preference preference) {
        if (isAvailable()) {
            ((OPSystemUpdatePreference) preference).updateView();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        if (isAvailable()) {
            this.mSystemUpdateObserver = new SystemUpdateObserver(new Handler());
            this.mSystemUpdateObserver.startObserving();
        }
    }

    public void onDestroy() {
        if (this.mSystemUpdateObserver != null) {
            this.mSystemUpdateObserver.stopObserving();
            this.mSystemUpdateObserver = null;
        }
    }
}
