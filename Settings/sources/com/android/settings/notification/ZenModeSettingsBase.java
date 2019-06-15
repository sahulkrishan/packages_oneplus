package com.android.settings.notification;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settings.dashboard.RestrictedDashboardFragment;

public abstract class ZenModeSettingsBase extends RestrictedDashboardFragment {
    protected static final boolean DEBUG = Log.isLoggable(TAG, 3);
    protected static final String TAG = "ZenModeSettings";
    protected ZenModeBackend mBackend;
    protected Context mContext;
    private final Handler mHandler = new Handler();
    private final SettingsObserver mSettingsObserver = new SettingsObserver();
    protected int mZenMode;

    private final class SettingsObserver extends ContentObserver {
        private final Uri ZEN_MODE_CONFIG_ETAG_URI;
        private final Uri ZEN_MODE_URI;

        private SettingsObserver() {
            super(ZenModeSettingsBase.this.mHandler);
            this.ZEN_MODE_URI = Global.getUriFor("zen_mode");
            this.ZEN_MODE_CONFIG_ETAG_URI = Global.getUriFor("zen_mode_config_etag");
        }

        public void register() {
            ZenModeSettingsBase.this.getContentResolver().registerContentObserver(this.ZEN_MODE_URI, false, this);
            ZenModeSettingsBase.this.getContentResolver().registerContentObserver(this.ZEN_MODE_CONFIG_ETAG_URI, false, this);
        }

        public void unregister() {
            ZenModeSettingsBase.this.getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.ZEN_MODE_URI.equals(uri)) {
                ZenModeSettingsBase.this.updateZenMode(true);
            }
            if (this.ZEN_MODE_CONFIG_ETAG_URI.equals(uri)) {
                ZenModeSettingsBase.this.mBackend.updatePolicy();
                ZenModeSettingsBase.this.onZenModeConfigChanged();
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onZenModeConfigChanged() {
    }

    public ZenModeSettingsBase() {
        super("no_adjust_volume");
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public void onCreate(Bundle icicle) {
        this.mContext = getActivity();
        this.mBackend = ZenModeBackend.getInstance(this.mContext);
        super.onCreate(icicle);
        updateZenMode(false);
    }

    public void onResume() {
        super.onResume();
        updateZenMode(true);
        this.mSettingsObserver.register();
        if (isUiRestricted()) {
            if (isUiRestrictedByOnlyAdmin()) {
                getPreferenceScreen().removeAll();
                return;
            }
            finish();
        }
        int zen = Global.getInt(this.mContext.getContentResolver(), "zen_mode_car", 0);
        int count = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference pref = getPreferenceScreen().getPreference(i);
            if (pref != null) {
                pref.setEnabled(zen == 0);
            }
        }
    }

    public void onPause() {
        super.onPause();
        this.mSettingsObserver.unregister();
    }

    private void updateZenMode(boolean fireChanged) {
        int zenMode = Global.getInt(getContentResolver(), "zen_mode", this.mZenMode);
        if (zenMode != this.mZenMode) {
            this.mZenMode = zenMode;
            if (DEBUG) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("updateZenMode mZenMode=");
                stringBuilder.append(this.mZenMode);
                stringBuilder.append(" ");
                stringBuilder.append(fireChanged);
                Log.d(str, stringBuilder.toString());
            }
        }
    }
}
