package com.oneplus.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.Indexable;
import com.android.settings.ui.RadioButtonPreference;
import com.android.settings.ui.RadioButtonPreference.OnClickListener;
import com.android.settingslib.utils.ThreadUtils;
import com.oneplus.settings.ui.OPGamingModeNotificationWayCategory;
import com.oneplus.settings.utils.OPUtils;

public class OPGamingModeNotificationWaySettings extends SettingsPreferenceFragment implements OnClickListener, OnPreferenceChangeListener, Indexable {
    private static final String GAME_MODE_BLOCK_NOTIFICATION = "game_mode_block_notification";
    private static final String KEY_ONEPLUS_INSTRUCITONS = "oneplus_instrucitons";
    private static final String KEY_SHIELDING_NOTIFICATION = "shielding_notification";
    private static final String KEY_SUSPENSION_NOTICE = "suspension_notice";
    private static final String KEY_WEAK_TEXT_REMINDING = "weak_text_reminding";
    private static final int SHIELDING_NOTIFICATION_VALUE = 1;
    private static final int SUSPENSION_NOTICE_VALUE = 0;
    private static final int WEAK_TEXT_REMINDING_VALUE = 2;
    private Context mContext;
    private Handler mHandler = new Handler();
    private OPGamingModeNotificationWayCategory mOPGamingModeNotificationWayCategory;
    private final SettingsObserver mSettingsObserver = new SettingsObserver();
    private RadioButtonPreference mShieldingNotification;
    private RadioButtonPreference mSuspensionNotice;
    private RadioButtonPreference mWeakTextReminding;

    private final class SettingsObserver extends ContentObserver {
        private final Uri ESPORTSMODE_URI = System.getUriFor("esport_mode_enabled");

        public SettingsObserver() {
            super(OPGamingModeNotificationWaySettings.this.mHandler);
        }

        public void register(boolean register) {
            ContentResolver cr = OPGamingModeNotificationWaySettings.this.getContentResolver();
            if (register) {
                cr.registerContentObserver(this.ESPORTSMODE_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.ESPORTSMODE_URI.equals(uri)) {
                ThreadUtils.postOnMainThread(new -$$Lambda$OPGamingModeNotificationWaySettings$SettingsObserver$AcDQ8_nycE-AymWaebuoz_lQERE(this));
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.op_gamingmode_notification_way_settings);
        this.mContext = SettingsBaseApplication.mApplication;
        this.mSuspensionNotice = (RadioButtonPreference) findPreference(KEY_SUSPENSION_NOTICE);
        this.mWeakTextReminding = (RadioButtonPreference) findPreference(KEY_WEAK_TEXT_REMINDING);
        this.mShieldingNotification = (RadioButtonPreference) findPreference(KEY_SHIELDING_NOTIFICATION);
        this.mSuspensionNotice.setOnClickListener(this);
        this.mWeakTextReminding.setOnClickListener(this);
        this.mShieldingNotification.setOnClickListener(this);
        this.mOPGamingModeNotificationWayCategory = (OPGamingModeNotificationWayCategory) findPreference(KEY_ONEPLUS_INSTRUCITONS);
    }

    private void setGamingModeNotificationWayValue(int value) {
        System.putIntForUser(getContentResolver(), "game_mode_block_notification", value, -2);
        this.mOPGamingModeNotificationWayCategory.setAnimTypes(value);
    }

    public void onRadioButtonClicked(RadioButtonPreference pref) {
        if (pref == this.mSuspensionNotice) {
            this.mSuspensionNotice.setChecked(true);
            this.mWeakTextReminding.setChecked(false);
            this.mShieldingNotification.setChecked(false);
            setGamingModeNotificationWayValue(0);
        } else if (pref == this.mWeakTextReminding) {
            this.mSuspensionNotice.setChecked(false);
            this.mWeakTextReminding.setChecked(true);
            this.mShieldingNotification.setChecked(false);
            setGamingModeNotificationWayValue(2);
        } else if (pref == this.mShieldingNotification) {
            this.mSuspensionNotice.setChecked(false);
            this.mWeakTextReminding.setChecked(false);
            this.mShieldingNotification.setChecked(true);
            setGamingModeNotificationWayValue(1);
        }
        OPUtils.sendAppTrackerForGameModeNotificationShow();
    }

    public void onResume() {
        updateUI();
        super.onResume();
        disableOptionsInEsportsMode();
        this.mSettingsObserver.register(true);
        if (this.mOPGamingModeNotificationWayCategory != null) {
            this.mOPGamingModeNotificationWayCategory.startAnim();
        }
    }

    public void onPause() {
        super.onPause();
        this.mSettingsObserver.register(false);
        if (this.mOPGamingModeNotificationWayCategory != null) {
            this.mOPGamingModeNotificationWayCategory.stopAnim();
        }
    }

    private void disableOptionsInEsportsMode() {
        boolean disableOptionsInEsportsMode = isEsportsMode() ^ 1;
        if (this.mSuspensionNotice != null) {
            this.mSuspensionNotice.setEnabled(disableOptionsInEsportsMode);
        }
        if (this.mWeakTextReminding != null) {
            this.mWeakTextReminding.setEnabled(disableOptionsInEsportsMode);
        }
        if (this.mShieldingNotification != null) {
            this.mShieldingNotification.setEnabled(disableOptionsInEsportsMode);
        }
    }

    private boolean isEsportsMode() {
        return "1".equals(System.getStringForUser(getContentResolver(), "esport_mode_enabled", -2));
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mOPGamingModeNotificationWayCategory != null) {
            this.mOPGamingModeNotificationWayCategory.releaseAnim();
        }
    }

    private void updateUI() {
        boolean z = false;
        int value = System.getIntForUser(getContentResolver(), "game_mode_block_notification", 0, -2);
        this.mSuspensionNotice.setChecked(value == 0);
        this.mWeakTextReminding.setChecked(value == 2);
        RadioButtonPreference radioButtonPreference = this.mShieldingNotification;
        if (value == 1) {
            z = true;
        }
        radioButtonPreference.setChecked(z);
        this.mSuspensionNotice.setEnabled(true);
        this.mWeakTextReminding.setEnabled(true);
        this.mShieldingNotification.setEnabled(true);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
