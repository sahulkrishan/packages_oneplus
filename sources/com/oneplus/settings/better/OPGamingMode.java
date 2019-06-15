package com.oneplus.settings.better;

import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.SearchIndexableResource;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.widget.MasterSwitchPreference;
import com.android.settingslib.utils.ThreadUtils;
import com.oneplus.settings.apploader.OPApplicationLoader;
import com.oneplus.settings.ui.OPTextViewButtonPreference;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OPGamingMode extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener, Indexable {
    private static final String BATTERY_SAVER_CLOSE_VALUE = "0_0";
    private static final String BATTERY_SAVER_HIGH_VALUE = "56_30";
    private static final String BATTERY_SAVER_LIGHT_VALUE = "56_0";
    private static final String GAME_MODE_AD_ENABLE = "op_game_mode_ad_enable";
    public static final String GAME_MODE_ANSWER_NO_INCALLUI = "game_mode_answer_no_incallui";
    public static final String GAME_MODE_BATTERY_SAVER = "game_mode_battery_saver";
    public static final String GAME_MODE_BLOCK_NOTIFICATION = "game_mode_block_notification";
    public static final String GAME_MODE_CLOSE_AUTOMATIC_BRIGHTNESS = "game_mode_close_automatic_brightness";
    public static final String GAME_MODE_LOCK_BUTTONS = "game_mode_lock_buttons";
    private static final String GAME_MODE_NETWORK_ACCELERATION = "game_mode_network_acceleration";
    private static final String GAME_MODE_NOTIFICATIONS_3RD_CALLS = "game_mode_notifications_3rd_calls";
    public static final String GAME_MODE_STATUS = "game_mode_status";
    public static final String GAME_MODE_STATUS_AUTO = "game_mode_status_auto";
    public static final String GAME_MODE_STATUS_MANUAL = "game_mode_status_manual";
    private static final String KEY_AUTO_TURN_ON_APPS = "auto_turn_on_apps";
    private static final String KEY_BATTERY_SAVER = "battery_saver";
    private static final String KEY_BLOCK_NOTIFICATIONS = "block_notifications";
    private static final String KEY_CLOSE_AUTOMATIC_BRIGHTNESS = "close_automatic_brightness";
    private static final String KEY_DO_NOT_DISTURB_ANSWER_CALL_BY_SPEAKER = "do_not_disturb_answer_call_by_speaker";
    private static final String KEY_DO_NOT_DISTURB_SETTINGS = "do_not_disturb_settings";
    private static final String KEY_GAME_MODE_AD_ENABLE = "op_game_mode_ad_enable";
    private static final String KEY_GAMING_MODE_ADD_APPS = "gaming_mode_add_apps";
    private static final String KEY_HAPTIC_FEEDBACK = "op_haptic_feedback";
    private static final String KEY_LOCK_BUTTONS = "lock_buttons";
    private static final String KEY_NETWORK_ACCELERATION = "network_acceleration";
    private static final String KEY_NOTIFICATIONS_3RD_CALLS = "notifications_3rd_calls";
    private static final String KEY_NOTIFICATION_WAYS = "notification_ways";
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_gaming_mode;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> result = new ArrayList();
            if (!OPUtils.isSupportGameModePowerSaver()) {
                result.add(OPGamingMode.KEY_BATTERY_SAVER);
            }
            if (!OPUtils.isSupportGameAdMode()) {
                result.add("op_game_mode_ad_enable");
            }
            if (OPUtils.isSupportXVibrate()) {
                result.add(OPGamingMode.KEY_HAPTIC_FEEDBACK);
            } else {
                result.add(OPGamingMode.KEY_HAPTIC_FEEDBACK);
            }
            return result;
        }
    };
    private static final int SHIELDING_NOTIFICATION_VALUE = 1;
    private static final int SUSPENSION_NOTICE_VALUE = 0;
    private static final String TAG = "OPGamingMode";
    private static final int WEAK_TEXT_REMINDING_VALUE = 2;
    private static Toast mToast;
    private SwitchPreference mAdEnable;
    private SwitchPreference mAnswerCallBySpeakerPreference;
    private List<OPAppModel> mAppList = new ArrayList();
    private AppOpsManager mAppOpsManager;
    private PreferenceCategory mAutoTurnOnAppList;
    private Preference mBatterySaverPreference;
    private SwitchPreference mBlockNotificationsPreference;
    private SwitchPreference mCloseAutomaticBrightness;
    private Context mContext;
    private PreferenceCategory mDoNotDisturbSettings;
    private Preference mGamingModeAddAppsPreference;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            OPGamingMode.this.mAutoTurnOnAppList.removeAll();
            OPGamingMode.this.mAppList.clear();
            OPGamingMode.this.mAppList.addAll(OPGamingMode.this.mOPApplicationLoader.getAppListByType(msg.what));
            int size = OPGamingMode.this.mAppList.size();
            StringBuilder pkgNames = new StringBuilder();
            for (int i = 0; i < size; i++) {
                final OPAppModel model = (OPAppModel) OPGamingMode.this.mAppList.get(i);
                final OPTextViewButtonPreference pref = new OPTextViewButtonPreference(OPGamingMode.this.mContext);
                pref.setIcon(model.getAppIcon());
                pref.setTitle(model.getLabel());
                pref.setButtonEnable(true);
                pref.setButtonString(OPGamingMode.this.mContext.getString(R.string.suggestion_remove));
                pref.setOnButtonClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        pref.setButtonEnable(false);
                        OPGamingMode.this.mAutoTurnOnAppList.removePreference(pref);
                        OPGamingMode.this.mAppOpsManager.setMode(68, model.getUid(), model.getPkgName(), 1);
                        StringBuilder gameModeAppList = new StringBuilder(OPUtils.getGameModeAppListString(OPGamingMode.this.mContext));
                        if (!OPUtils.isInRemovedGameAppListString(OPGamingMode.this.mContext, model)) {
                            gameModeAppList.append(OPUtils.getGameModeAppString(model));
                            OPUtils.saveGameModeRemovedAppLisStrings(OPGamingMode.this.mContext, gameModeAppList.toString());
                            OPUtils.sendAppTrackerForGameModeRemovedApps();
                        }
                    }
                });
                OPGamingMode.this.mAutoTurnOnAppList.addPreference(pref);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(model.getPkgName());
                stringBuilder.append(";");
                pkgNames.append(stringBuilder.toString());
            }
            System.putString(OPGamingMode.this.getContentResolver(), OPConstants.GAME_MODE_APPS, pkgNames.toString());
            OPUtils.sendAppTrackerForGameModeApps(pkgNames.toString());
        }
    };
    private MasterSwitchPreference mHapticFeedbackPreference;
    private SwitchPreference mLockButtonsPreference;
    private SwitchPreference mNetworkAcceleration;
    private Preference mNotificationWaysPreference;
    private SwitchPreference mNotificationsCalls;
    private OPApplicationLoader mOPApplicationLoader;
    private PackageManager mPackageManager;
    private final SettingsObserver mSettingsObserver = new SettingsObserver();

    private final class SettingsObserver extends ContentObserver {
        private final Uri ESPORTSMODE_URI = System.getUriFor("esport_mode_enabled");

        public SettingsObserver() {
            super(OPGamingMode.this.mHandler);
        }

        public void register(boolean register) {
            ContentResolver cr = OPGamingMode.this.getContentResolver();
            if (register) {
                cr.registerContentObserver(this.ESPORTSMODE_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.ESPORTSMODE_URI.equals(uri)) {
                ThreadUtils.postOnMainThread(new -$$Lambda$OPGamingMode$SettingsObserver$sNzw_XCuNF8n2q9Km_hXz4FJIOA(this));
            }
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_gaming_mode);
        this.mContext = getActivity();
        this.mAppOpsManager = (AppOpsManager) getSystemService("appops");
        this.mPackageManager = getPackageManager();
        this.mOPApplicationLoader = new OPApplicationLoader(this.mContext, this.mAppOpsManager, this.mPackageManager);
        this.mAnswerCallBySpeakerPreference = (SwitchPreference) findPreference(KEY_DO_NOT_DISTURB_ANSWER_CALL_BY_SPEAKER);
        this.mBlockNotificationsPreference = (SwitchPreference) findPreference(KEY_BLOCK_NOTIFICATIONS);
        this.mLockButtonsPreference = (SwitchPreference) findPreference(KEY_LOCK_BUTTONS);
        if (this.mAnswerCallBySpeakerPreference != null) {
            this.mAnswerCallBySpeakerPreference.setOnPreferenceChangeListener(this);
        }
        if (this.mBlockNotificationsPreference != null) {
            this.mBlockNotificationsPreference.setOnPreferenceChangeListener(this);
        }
        if (this.mLockButtonsPreference != null) {
            this.mLockButtonsPreference.setOnPreferenceChangeListener(this);
        }
        this.mAutoTurnOnAppList = (PreferenceCategory) findPreference(KEY_AUTO_TURN_ON_APPS);
        this.mGamingModeAddAppsPreference = findPreference(KEY_GAMING_MODE_ADD_APPS);
        if (this.mGamingModeAddAppsPreference != null) {
            this.mGamingModeAddAppsPreference.setOnPreferenceClickListener(this);
        }
        this.mDoNotDisturbSettings = (PreferenceCategory) findPreference(KEY_DO_NOT_DISTURB_SETTINGS);
        if (OPUtils.isSurportBackFingerprint(this.mContext) && this.mLockButtonsPreference != null) {
            this.mDoNotDisturbSettings.removePreference(this.mLockButtonsPreference);
        }
        this.mBatterySaverPreference = findPreference(KEY_BATTERY_SAVER);
        if (!OPUtils.isSupportGameModePowerSaver()) {
            this.mBatterySaverPreference.setVisible(false);
        }
        if (this.mBatterySaverPreference != null) {
            updateBatterySaverData();
        }
        this.mCloseAutomaticBrightness = (SwitchPreference) findPreference(KEY_CLOSE_AUTOMATIC_BRIGHTNESS);
        if (this.mCloseAutomaticBrightness != null) {
            this.mCloseAutomaticBrightness.setOnPreferenceChangeListener(this);
        }
        this.mNetworkAcceleration = (SwitchPreference) findPreference(KEY_NETWORK_ACCELERATION);
        if (this.mNetworkAcceleration != null) {
            this.mNetworkAcceleration.setOnPreferenceChangeListener(this);
        }
        if (!OPUtils.isSupportGameModeNetBoost()) {
            this.mDoNotDisturbSettings.removePreference(this.mNetworkAcceleration);
        }
        this.mNotificationWaysPreference = findPreference(KEY_NOTIFICATION_WAYS);
        this.mNotificationsCalls = (SwitchPreference) findPreference(KEY_NOTIFICATIONS_3RD_CALLS);
        if (this.mNotificationsCalls != null) {
            this.mNotificationsCalls.setOnPreferenceChangeListener(this);
        }
        this.mAdEnable = (SwitchPreference) findPreference("op_game_mode_ad_enable");
        if (this.mAdEnable != null) {
            this.mAdEnable.setOnPreferenceChangeListener(this);
        }
        if (!(OPUtils.isSupportGameAdMode() || this.mAdEnable == null)) {
            this.mAdEnable.setVisible(false);
        }
        this.mHapticFeedbackPreference = (MasterSwitchPreference) findPreference(KEY_HAPTIC_FEEDBACK);
        if (OPUtils.isSupportXVibrate()) {
            this.mHapticFeedbackPreference.setOnPreferenceChangeListener(this);
            this.mHapticFeedbackPreference.setChecked(OPHapticFeedback.getHapticFeedbackState(this.mContext));
            return;
        }
        this.mDoNotDisturbSettings.removePreference(this.mHapticFeedbackPreference);
    }

    private void updateNotificationWaysSummary() {
        int value = System.getIntForUser(getContentResolver(), GAME_MODE_BLOCK_NOTIFICATION, 0, -2);
        if (value == 0) {
            this.mNotificationWaysPreference.setSummary((int) R.string.oneplus_suspension_notice);
        } else if (2 == value) {
            this.mNotificationWaysPreference.setSummary((int) R.string.oneplus_weak_text_reminding);
        } else if (1 == value) {
            this.mNotificationWaysPreference.setSummary((int) R.string.oneplus_shielding_notification);
        }
    }

    private void updateBatterySaverData() {
        String value = System.getStringForUser(getContentResolver(), "game_mode_battery_saver", -2);
        if (!BATTERY_SAVER_CLOSE_VALUE.equalsIgnoreCase(value) && !TextUtils.isEmpty(value) && !BATTERY_SAVER_LIGHT_VALUE.equalsIgnoreCase(value)) {
            BATTERY_SAVER_HIGH_VALUE.equalsIgnoreCase(value);
        }
    }

    private void updateListData() {
        if (!this.mOPApplicationLoader.isLoading()) {
            this.mOPApplicationLoader.loadSelectedGameOrReadAppMap(68);
            this.mOPApplicationLoader.initData(1, this.mHandler);
        }
    }

    public void onResume() {
        super.onResume();
        boolean z = true;
        this.mSettingsObserver.register(true);
        updateListData();
        int value = System.getIntForUser(getContentResolver(), GAME_MODE_ANSWER_NO_INCALLUI, 0, -2);
        if (this.mAnswerCallBySpeakerPreference != null) {
            this.mAnswerCallBySpeakerPreference.setChecked(value != 0);
        }
        value = System.getIntForUser(getContentResolver(), GAME_MODE_BLOCK_NOTIFICATION, 0, -2);
        this.mDoNotDisturbSettings = (PreferenceCategory) findPreference(KEY_DO_NOT_DISTURB_SETTINGS);
        if (this.mBlockNotificationsPreference != null) {
            this.mBlockNotificationsPreference.setChecked(value != 0);
            this.mDoNotDisturbSettings.removePreference(this.mBlockNotificationsPreference);
        }
        value = System.getIntForUser(getContentResolver(), GAME_MODE_LOCK_BUTTONS, 0, -2);
        if (this.mLockButtonsPreference != null) {
            this.mLockButtonsPreference.setChecked(value != 0);
        }
        value = System.getIntForUser(getContentResolver(), GAME_MODE_CLOSE_AUTOMATIC_BRIGHTNESS, 0, -2);
        if (this.mCloseAutomaticBrightness != null) {
            this.mCloseAutomaticBrightness.setChecked(value != 0);
        }
        if (this.mBatterySaverPreference != null) {
            updateBatterySaverData();
        }
        value = System.getIntForUser(getContentResolver(), GAME_MODE_NETWORK_ACCELERATION, 0, -2);
        if (this.mNetworkAcceleration != null) {
            this.mNetworkAcceleration.setChecked(value != 0);
        }
        if (this.mNotificationWaysPreference != null) {
            updateNotificationWaysSummary();
        }
        value = System.getIntForUser(getContentResolver(), GAME_MODE_NOTIFICATIONS_3RD_CALLS, 1, -2);
        if (this.mNotificationsCalls != null) {
            this.mNotificationsCalls.setChecked(value != 0);
        }
        disableOptionsInEsportsMode();
        value = System.getIntForUser(getContentResolver(), "op_game_mode_ad_enable", 0, -2);
        if (this.mAdEnable != null) {
            SwitchPreference switchPreference = this.mAdEnable;
            if (value == 0) {
                z = false;
            }
            switchPreference.setChecked(z);
        }
        if (OPUtils.isSupportXVibrate() && this.mHapticFeedbackPreference != null) {
            this.mHapticFeedbackPreference.setChecked(OPHapticFeedback.getHapticFeedbackState(this.mContext));
        }
    }

    public void onPause() {
        super.onPause();
        this.mSettingsObserver.register(false);
    }

    private void showToast() {
        if (mToast != null) {
            mToast.cancel();
            mToast = Toast.makeText(getPrefContext(), R.string.oneplus_gaming_toast_title, 1);
        } else {
            mToast = Toast.makeText(getPrefContext(), R.string.oneplus_gaming_toast_title, 1);
        }
        mToast.show();
    }

    private void disableOptionsInEsportsMode() {
        boolean disableOptionsInEsportsMode = isEsportsMode() ^ 1;
        if (this.mAnswerCallBySpeakerPreference != null) {
            this.mAnswerCallBySpeakerPreference.setEnabled(disableOptionsInEsportsMode);
        }
        if (this.mNotificationWaysPreference != null) {
            this.mNotificationWaysPreference.setEnabled(disableOptionsInEsportsMode);
        }
        if (this.mNotificationsCalls != null) {
            this.mNotificationsCalls.setEnabled(disableOptionsInEsportsMode);
        }
    }

    private boolean isEsportsMode() {
        return "1".equals(System.getStringForUser(getContentResolver(), "esport_mode_enabled", -2));
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        String key = preference.getKey();
        if (KEY_BLOCK_NOTIFICATIONS.equals(key)) {
            Log.d(TAG, "KEY_BLOCK_NOTIFICATIONS");
            System.putIntForUser(getContentResolver(), GAME_MODE_BLOCK_NOTIFICATION, ((Boolean) objValue).booleanValue(), -2);
        } else if (KEY_LOCK_BUTTONS.equals(key)) {
            Log.d(TAG, "KEY_LOCK_BUTTONS");
            System.putIntForUser(getContentResolver(), GAME_MODE_LOCK_BUTTONS, ((Boolean) objValue).booleanValue(), -2);
        } else if (KEY_DO_NOT_DISTURB_ANSWER_CALL_BY_SPEAKER.equals(key)) {
            Log.d(TAG, "KEY_LOCK_BUTTONS");
            System.putIntForUser(getContentResolver(), GAME_MODE_ANSWER_NO_INCALLUI, ((Boolean) objValue).booleanValue(), -2);
            OPUtils.sendAppTrackerForGameModeSpeakerAnswer();
        } else if (KEY_CLOSE_AUTOMATIC_BRIGHTNESS.equals(key)) {
            System.putIntForUser(getContentResolver(), GAME_MODE_CLOSE_AUTOMATIC_BRIGHTNESS, ((Boolean) objValue).booleanValue(), -2);
            OPUtils.sendAppTrackerForGameModeBrightness();
        } else if (KEY_NETWORK_ACCELERATION.equals(key)) {
            System.putIntForUser(getContentResolver(), GAME_MODE_NETWORK_ACCELERATION, ((Boolean) objValue).booleanValue(), -2);
            OPUtils.sendAppTrackerForGameModeNetWorkBoost();
        } else if (KEY_NOTIFICATIONS_3RD_CALLS.equals(key)) {
            System.putIntForUser(getContentResolver(), GAME_MODE_NOTIFICATIONS_3RD_CALLS, ((Boolean) objValue).booleanValue(), -2);
            OPUtils.sendAppTrackerForGameMode3drPartyCalls();
        } else if ("op_game_mode_ad_enable".equals(key)) {
            System.putIntForUser(getContentResolver(), "op_game_mode_ad_enable", ((Boolean) objValue).booleanValue(), -2);
            OPUtils.sendAppTrackerForGameModeAdEnable();
        } else if (KEY_HAPTIC_FEEDBACK.equals(key)) {
            OPHapticFeedback.setHapticFeedbackState(this.mContext, ((Boolean) objValue).booleanValue());
        }
        return true;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (!preference.getKey().equals(KEY_GAMING_MODE_ADD_APPS)) {
            return false;
        }
        Log.d(TAG, "KEY_GAMING_MODE_ADD_APPS");
        Intent intent = new Intent("oneplus.intent.action.ONEPLUS_GAME_READ_APP_LIST_ACTION");
        intent.setFlags(268435456);
        intent.putExtra(OPConstants.OP_LOAD_APP_TYEP, 68);
        this.mContext.startActivity(intent);
        return true;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
