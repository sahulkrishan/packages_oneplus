package com.oneplus.settings.better;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.fuelgauge.WallOfTextPreference;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.apploader.OPApplicationLoader;
import com.oneplus.settings.ui.OPTextViewButtonPreference;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPReadingMode extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String KEY_AUTO_TURN_ON_APPS = "auto_turn_on_apps";
    private static final String KEY_BLOCK_PEEK_NOTIFICATIONS = "block_peek_notifications";
    private static final String KEY_READING_MODE_ADD_APPS = "reading_mode_add_apps";
    private static final String KEY_READING_MODE_SUMMARY = "reading_mode_summary";
    private static final String KEY_READING_MODE_TURN_ON = "reading_mode_turn_on";
    public static final String READING_MODE_STATUS = "reading_mode_status";
    public static final String READING_MODE_STATUS_AUTO = "reading_mode_status_auto";
    public static final String READING_MODE_STATUS_MANUAL = "reading_mode_status_manual";
    private static final String TAG = "OPReadingMode";
    private List<OPAppModel> mAppList = new ArrayList();
    private AppOpsManager mAppOpsManager;
    private PreferenceCategory mAutoTurnOnAppList;
    private SwitchPreference mBlockPeekNotificationsPreference;
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            boolean z = false;
            int value = System.getIntForUser(OPReadingMode.this.getContentResolver(), OPReadingMode.READING_MODE_STATUS, 0, -2);
            if (OPReadingMode.this.mReadingModeTurnOnPreference != null) {
                SwitchPreference access$700 = OPReadingMode.this.mReadingModeTurnOnPreference;
                if (value != 0) {
                    z = true;
                }
                access$700.setChecked(z);
            }
        }
    };
    private Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            OPReadingMode.this.mAutoTurnOnAppList.removeAll();
            OPReadingMode.this.mAppList.clear();
            OPReadingMode.this.mAppList.addAll(OPReadingMode.this.mOPApplicationLoader.getAppListByType(msg.what));
            int size = OPReadingMode.this.mAppList.size();
            StringBuilder pkgNames = new StringBuilder();
            for (int i = 0; i < size; i++) {
                final OPAppModel model = (OPAppModel) OPReadingMode.this.mAppList.get(i);
                final OPTextViewButtonPreference pref = new OPTextViewButtonPreference(OPReadingMode.this.mContext);
                pref.setIcon(model.getAppIcon());
                pref.setTitle(model.getLabel());
                pref.setButtonEnable(true);
                pref.setButtonString(OPReadingMode.this.mContext.getString(R.string.suggestion_remove));
                pref.setOnButtonClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        pref.setButtonEnable(false);
                        OPReadingMode.this.mAutoTurnOnAppList.removePreference(pref);
                        OPReadingMode.this.mAppOpsManager.setMode(67, model.getUid(), model.getPkgName(), 1);
                    }
                });
                OPReadingMode.this.mAutoTurnOnAppList.addPreference(pref);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(model.getPkgName());
                stringBuilder.append(";");
                pkgNames.append(stringBuilder.toString());
            }
            System.putString(OPReadingMode.this.getContentResolver(), OPConstants.READ_MODE_APPS, pkgNames.toString());
            OPUtils.sendAppTrackerForReadingModeApps(pkgNames.toString());
        }
    };
    private OPApplicationLoader mOPApplicationLoader;
    private PackageManager mPackageManager;
    private WallOfTextPreference mReadingModSummary;
    private Preference mReadingModeAddAppsPreference;
    private SwitchPreference mReadingModeTurnOnPreference;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_reading_mode);
        this.mContext = getActivity();
        this.mAppOpsManager = (AppOpsManager) getSystemService("appops");
        this.mPackageManager = getPackageManager();
        this.mOPApplicationLoader = new OPApplicationLoader(this.mContext, this.mAppOpsManager, this.mPackageManager);
        this.mReadingModeTurnOnPreference = (SwitchPreference) findPreference(KEY_READING_MODE_TURN_ON);
        if (this.mReadingModeTurnOnPreference != null) {
            this.mReadingModeTurnOnPreference.setOnPreferenceChangeListener(this);
        }
        this.mAutoTurnOnAppList = (PreferenceCategory) findPreference(KEY_AUTO_TURN_ON_APPS);
        this.mReadingModeAddAppsPreference = findPreference(KEY_READING_MODE_ADD_APPS);
        if (this.mReadingModeAddAppsPreference != null) {
            this.mReadingModeAddAppsPreference.setOnPreferenceClickListener(this);
        }
        this.mBlockPeekNotificationsPreference = (SwitchPreference) findPreference(KEY_BLOCK_PEEK_NOTIFICATIONS);
        if (this.mBlockPeekNotificationsPreference != null) {
            this.mBlockPeekNotificationsPreference.setOnPreferenceChangeListener(this);
        }
        this.mReadingModSummary = (WallOfTextPreference) findPreference(KEY_READING_MODE_SUMMARY);
        if ("1".equals(SystemProperties.get("ro.sensor.not_support_rbg", "0"))) {
            this.mReadingModSummary.setSummary(SettingsBaseApplication.mApplication.getText(R.string.oneplus_reading_mode_summary_no_sensor));
        }
    }

    private void updateListData() {
        if (!this.mOPApplicationLoader.isLoading()) {
            this.mOPApplicationLoader.loadSelectedGameOrReadAppMap(67);
            this.mOPApplicationLoader.initData(1, this.mHandler);
        }
    }

    public void onResume() {
        super.onResume();
        updateListData();
        boolean z = false;
        int value = System.getIntForUser(getContentResolver(), READING_MODE_STATUS, 0, -2);
        if (this.mReadingModeTurnOnPreference != null) {
            this.mReadingModeTurnOnPreference.setChecked(value != 0);
        }
        value = System.getIntForUser(getContentResolver(), "reading_mode_block_notification", 0, -2);
        if (this.mBlockPeekNotificationsPreference != null) {
            SwitchPreference switchPreference = this.mBlockPeekNotificationsPreference;
            if (value != 0) {
                z = true;
            }
            switchPreference.setChecked(z);
        }
        getContentResolver().registerContentObserver(System.getUriFor(READING_MODE_STATUS), true, this.mContentObserver, -2);
    }

    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(this.mContentObserver);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        String key = preference.getKey();
        if (KEY_READING_MODE_TURN_ON.equals(key)) {
            if (((Boolean) objValue).booleanValue()) {
                System.putStringForUser(getContentResolver(), READING_MODE_STATUS_MANUAL, "force-on", -2);
            } else {
                System.putStringForUser(getContentResolver(), READING_MODE_STATUS_MANUAL, "force-off", -2);
            }
            OPUtils.sendAppTrackerForReadingMode();
        } else if (KEY_BLOCK_PEEK_NOTIFICATIONS.equals(key)) {
            System.putIntForUser(getContentResolver(), "reading_mode_block_notification", ((Boolean) objValue).booleanValue(), -2);
            OPUtils.sendAppTrackerForReadingModeNotification();
        }
        return true;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (!preference.getKey().equals(KEY_READING_MODE_ADD_APPS)) {
            return false;
        }
        Log.d(TAG, "KEY_READING_MODE_ADD_APPS");
        Intent intent = new Intent("oneplus.intent.action.ONEPLUS_GAME_READ_APP_LIST_ACTION");
        intent.putExtra(OPConstants.OP_LOAD_APP_TYEP, 67);
        this.mContext.startActivity(intent);
        return true;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
