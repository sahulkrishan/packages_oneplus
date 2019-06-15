package com.android.settings.notification;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Global;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ZenRule;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.notification.EnableZenModeDialog;
import com.oneplus.settings.utils.OPUtils;

public class OPZenModeTurnOnSettings extends AbstractZenModePreferenceController implements LifecycleObserver, OnResume, OnPause {
    private static final String KEY_ZEN_TURN_ON = "zen_turn_on";
    private static final String TAG = "OPZenModeTurnOnSettings";
    private FragmentManager mFragment;
    private SettingObserver mSettingObserver;
    SwitchPreference mSwitchPreference;
    OnDismissListener onDismissListener = new OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            OPZenModeTurnOnSettings.this.updateState(OPZenModeTurnOnSettings.this.mSwitchPreference);
        }
    };

    class SettingObserver extends ContentObserver {
        private final Uri ZEN_MODE_CONFIG_ETAG_URI = Global.getUriFor("zen_mode_config_etag");
        private final Uri ZEN_MODE_DURATION_URI = Global.getUriFor("zen_duration");
        private final Uri ZEN_MODE_URI = Global.getUriFor("zen_mode");
        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            this.mPreference = preference;
        }

        public void register(ContentResolver cr) {
            cr.registerContentObserver(this.ZEN_MODE_URI, false, this, -1);
            cr.registerContentObserver(this.ZEN_MODE_CONFIG_ETAG_URI, false, this, -1);
            cr.registerContentObserver(this.ZEN_MODE_DURATION_URI, false, this, -1);
        }

        public void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri == null || this.ZEN_MODE_URI.equals(uri) || this.ZEN_MODE_CONFIG_ETAG_URI.equals(uri) || this.ZEN_MODE_DURATION_URI.equals(uri)) {
                OPZenModeTurnOnSettings.this.mBackend.updatePolicy();
                OPZenModeTurnOnSettings.this.mBackend.updateZenMode();
                OPZenModeTurnOnSettings.this.updateState(this.mPreference);
            }
        }
    }

    public OPZenModeTurnOnSettings(Context context, Lifecycle lifecycle, FragmentManager fragment) {
        super(context, KEY_ZEN_TURN_ON, lifecycle);
        this.mFragment = fragment;
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public boolean isAvailable() {
        return OPUtils.isSupportSocTriState();
    }

    public String getPreferenceKey() {
        return KEY_ZEN_TURN_ON;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        switch (getZenMode()) {
            case 1:
            case 2:
            case 3:
                this.mSwitchPreference.setChecked(true);
                this.mSwitchPreference.setSummary((CharSequence) getPreferenceSummary());
                return;
            default:
                this.mSwitchPreference.setChecked(false);
                this.mSwitchPreference.setSummary((CharSequence) getPreferenceSummary());
                return;
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSwitchPreference = (SwitchPreference) screen.findPreference(getPreferenceKey());
        Preference pref = screen.findPreference(KEY_ZEN_TURN_ON);
        if (pref != null) {
            this.mSettingObserver = new SettingObserver(pref);
        }
    }

    public void onResume() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.register(this.mContext.getContentResolver());
            this.mSettingObserver.onChange(false, null);
        }
        updateState(this.mSwitchPreference);
    }

    public void onPause() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.unregister(this.mContext.getContentResolver());
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_ZEN_TURN_ON) || !(preference instanceof SwitchPreference)) {
            return false;
        }
        if (((SwitchPreference) preference).isChecked()) {
            Log.d(TAG, "Click true");
            int zenDuration = getZenDuration();
            switch (zenDuration) {
                case -1:
                    Dialog mDialog = new EnableZenModeDialog(this.mContext).createDialog();
                    mDialog.setOnDismissListener(this.onDismissListener);
                    mDialog.show();
                    break;
                case 0:
                    this.mBackend.setZenMode(1);
                    break;
                default:
                    this.mBackend.setZenModeForDuration(zenDuration);
                    break;
            }
        }
        Log.d(TAG, "Click off");
        this.mBackend.setZenMode(0);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public String getPreferenceSummary() {
        ZenModeConfig config = getZenModeConfig();
        String preferenceSummary = "";
        long latestEndTime = -1;
        if (config.manualRule != null) {
            Uri id = config.manualRule.conditionId;
            if (config.manualRule.enabler != null) {
                if (!mZenModeConfigWrapper.getOwnerCaption(config.manualRule.enabler).isEmpty()) {
                    preferenceSummary = this.mContext.getString(R.string.zen_mode_settings_dnd_automatic_rule_app, new Object[]{appOwner});
                }
            } else if (id == null) {
                return this.mContext.getString(R.string.zen_mode_settings_dnd_manual_indefinite);
            } else {
                latestEndTime = mZenModeConfigWrapper.parseManualRuleTime(id);
                if (latestEndTime > 0) {
                    CharSequence formattedTime = mZenModeConfigWrapper.getFormattedTime(latestEndTime, this.mContext.getUserId());
                    preferenceSummary = this.mContext.getString(R.string.zen_mode_settings_dnd_manual_end_time, new Object[]{formattedTime});
                }
            }
        }
        for (ZenRule automaticRule : config.automaticRules.values()) {
            if (automaticRule.isAutomaticActive()) {
                if (mZenModeConfigWrapper.isTimeRule(automaticRule.conditionId)) {
                    long endTime = mZenModeConfigWrapper.parseAutomaticRuleEndTime(automaticRule.conditionId);
                    if (endTime > latestEndTime) {
                        latestEndTime = endTime;
                        preferenceSummary = this.mContext.getString(R.string.zen_mode_settings_dnd_automatic_rule, new Object[]{automaticRule.name});
                    }
                } else {
                    return this.mContext.getString(R.string.zen_mode_settings_dnd_automatic_rule, new Object[]{automaticRule.name});
                }
            }
        }
        return preferenceSummary;
    }
}
