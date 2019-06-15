package com.android.settings.wifi;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.NetworkScoreManager;
import android.net.NetworkScorerAppData;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class UseOpenWifiPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener, LifecycleObserver, OnResume, OnPause {
    public static final String KEY_USE_OPEN_WIFI_AUTOMATICALLY = "use_open_wifi_automatically";
    public static final int REQUEST_CODE_OPEN_WIFI_AUTOMATICALLY = 400;
    private final ContentResolver mContentResolver;
    private boolean mDoFeatureSupportedScorersExist;
    private ComponentName mEnableUseWifiComponentName;
    private final Fragment mFragment;
    private final NetworkScoreManager mNetworkScoreManager;
    private Preference mPreference;
    private final SettingObserver mSettingObserver = new SettingObserver();

    class SettingObserver extends ContentObserver {
        private final Uri NETWORK_RECOMMENDATIONS_ENABLED_URI = Global.getUriFor("network_recommendations_enabled");

        public SettingObserver() {
            super(new Handler(Looper.getMainLooper()));
        }

        public void register(ContentResolver cr) {
            cr.registerContentObserver(this.NETWORK_RECOMMENDATIONS_ENABLED_URI, false, this);
            onChange(true, this.NETWORK_RECOMMENDATIONS_ENABLED_URI);
        }

        public void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.NETWORK_RECOMMENDATIONS_ENABLED_URI.equals(uri)) {
                UseOpenWifiPreferenceController.this.updateEnableUseWifiComponentName();
                UseOpenWifiPreferenceController.this.updateState(UseOpenWifiPreferenceController.this.mPreference);
            }
        }
    }

    public UseOpenWifiPreferenceController(Context context, Fragment fragment, Lifecycle lifecycle) {
        super(context);
        this.mContentResolver = context.getContentResolver();
        this.mFragment = fragment;
        this.mNetworkScoreManager = (NetworkScoreManager) context.getSystemService("network_score");
        updateEnableUseWifiComponentName();
        checkForFeatureSupportedScorers();
        lifecycle.addObserver(this);
    }

    private void updateEnableUseWifiComponentName() {
        NetworkScorerAppData appData = this.mNetworkScoreManager.getActiveScorer();
        this.mEnableUseWifiComponentName = appData == null ? null : appData.getEnableUseOpenWifiActivity();
    }

    private void checkForFeatureSupportedScorers() {
        if (this.mEnableUseWifiComponentName != null) {
            this.mDoFeatureSupportedScorersExist = true;
            return;
        }
        for (NetworkScorerAppData scorer : this.mNetworkScoreManager.getAllValidScorers()) {
            if (scorer.getEnableUseOpenWifiActivity() != null) {
                this.mDoFeatureSupportedScorersExist = true;
                return;
            }
        }
        this.mDoFeatureSupportedScorersExist = false;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(KEY_USE_OPEN_WIFI_AUTOMATICALLY);
    }

    public void onResume() {
        this.mSettingObserver.register(this.mContentResolver);
    }

    public void onPause() {
        this.mSettingObserver.unregister(this.mContentResolver);
    }

    public boolean isAvailable() {
        return this.mDoFeatureSupportedScorersExist;
    }

    public String getPreferenceKey() {
        return KEY_USE_OPEN_WIFI_AUTOMATICALLY;
    }

    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            SwitchPreference useOpenWifiPreference = (SwitchPreference) preference;
            boolean z = false;
            boolean isScorerSet = this.mNetworkScoreManager.getActiveScorerPackage() != null;
            boolean doesActiveScorerSupportFeature = this.mEnableUseWifiComponentName != null;
            useOpenWifiPreference.setChecked(isSettingEnabled());
            useOpenWifiPreference.setVisible(isAvailable());
            if (isScorerSet && doesActiveScorerSupportFeature) {
                z = true;
            }
            useOpenWifiPreference.setEnabled(z);
            if (!isScorerSet) {
                useOpenWifiPreference.setSummary((int) R.string.use_open_wifi_automatically_summary_scoring_disabled);
            } else if (doesActiveScorerSupportFeature) {
                useOpenWifiPreference.setSummary((int) R.string.use_open_wifi_automatically_summary);
            } else {
                useOpenWifiPreference.setSummary((int) R.string.use_open_wifi_automatically_summary_scorer_unsupported_disabled);
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!TextUtils.equals(preference.getKey(), KEY_USE_OPEN_WIFI_AUTOMATICALLY) || !isAvailable()) {
            return false;
        }
        if (isSettingEnabled()) {
            Global.putString(this.mContentResolver, "use_open_wifi_package", "");
            return true;
        }
        Intent intent = new Intent("android.net.scoring.CUSTOM_ENABLE");
        intent.setComponent(this.mEnableUseWifiComponentName);
        this.mFragment.startActivityForResult(intent, 400);
        return false;
    }

    private boolean isSettingEnabled() {
        return TextUtils.equals(Global.getString(this.mContentResolver, "use_open_wifi_package"), this.mEnableUseWifiComponentName == null ? null : this.mEnableUseWifiComponentName.getPackageName());
    }

    public boolean onActivityResult(int requestCode, int resultCode) {
        if (requestCode != 400) {
            return false;
        }
        if (resultCode == -1) {
            Global.putString(this.mContentResolver, "use_open_wifi_package", this.mEnableUseWifiComponentName.getPackageName());
        }
        return true;
    }
}
