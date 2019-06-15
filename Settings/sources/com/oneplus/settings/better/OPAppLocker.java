package com.oneplus.settings.better;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.oneplus.settings.apploader.OPApplicationLoader;
import com.oneplus.settings.ui.OPTextViewButtonPreference;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPAppLocker extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String KEY_APP_LOCKER_ADD_APPS = "app_locker_add_apps";
    private static final String KEY_APP_LOCKER_OPEN_APPS = "app_locker_open_apps";
    private static final String KEY_APP_LOCKER_SWITCH = "app_locker_switch";
    private static final String TAG = "OPAppLocker";
    private List<OPAppModel> mAppList = new ArrayList();
    private Preference mAppLockerAddAppsPreference;
    private SwitchPreference mAppLockerSwitch;
    private AppOpsManager mAppOpsManager;
    private Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            OPAppLocker.this.mOpenAppsList.removeAll();
            OPAppLocker.this.mAppList.clear();
            OPAppLocker.this.mAppList.addAll(OPAppLocker.this.mOPApplicationLoader.getAppListByType(msg.what));
            int size = OPAppLocker.this.mAppList.size();
            for (int i = 0; i < size; i++) {
                final OPAppModel model = (OPAppModel) OPAppLocker.this.mAppList.get(i);
                final OPTextViewButtonPreference pref = new OPTextViewButtonPreference(OPAppLocker.this.mContext);
                pref.setIcon(model.getAppIcon());
                pref.setTitle(model.getLabel());
                pref.setButtonEnable(true);
                pref.setButtonString(OPAppLocker.this.mContext.getString(R.string.suggestion_remove));
                pref.setOnButtonClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        pref.setButtonEnable(false);
                        OPAppLocker.this.mOpenAppsList.removePreference(pref);
                        OPAppLocker.this.mAppOpsManager.setMode(63, model.getUid(), model.getPkgName(), 1);
                    }
                });
                OPAppLocker.this.mOpenAppsList.addPreference(pref);
            }
        }
    };
    private OPApplicationLoader mOPApplicationLoader;
    private PreferenceCategory mOpenAppsList;
    private PackageManager mPackageManager;

    private void updateListData() {
        if (!this.mOPApplicationLoader.isLoading()) {
            this.mOPApplicationLoader.loadSelectedGameOrReadAppMap(63);
            this.mOPApplicationLoader.initData(1, this.mHandler);
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_app_locker);
        this.mContext = getActivity();
        this.mAppOpsManager = (AppOpsManager) getSystemService("appops");
        this.mPackageManager = getPackageManager();
        this.mOPApplicationLoader = new OPApplicationLoader(this.mContext, this.mAppOpsManager, this.mPackageManager);
        this.mOPApplicationLoader.setAppType(63);
        this.mOpenAppsList = (PreferenceCategory) findPreference(KEY_APP_LOCKER_OPEN_APPS);
        this.mAppLockerAddAppsPreference = findPreference(KEY_APP_LOCKER_ADD_APPS);
        if (this.mAppLockerAddAppsPreference != null) {
            this.mAppLockerAddAppsPreference.setOnPreferenceClickListener(this);
        }
        this.mAppLockerSwitch = (SwitchPreference) findPreference(KEY_APP_LOCKER_SWITCH);
        if (this.mAppLockerSwitch != null) {
            this.mAppLockerSwitch.setOnPreferenceChangeListener(this);
        }
    }

    public void onResume() {
        super.onResume();
        updateListData();
        if (this.mAppLockerSwitch != null) {
            boolean z = false;
            int value = System.getIntForUser(getContentResolver(), KEY_APP_LOCKER_SWITCH, 0, -2);
            SwitchPreference switchPreference = this.mAppLockerSwitch;
            if (value != 0) {
                z = true;
            }
            switchPreference.setChecked(z);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        Boolean value = (Boolean) objValue;
        if (KEY_APP_LOCKER_SWITCH.equals(preference.getKey())) {
            Log.d(TAG, "KEY_APP_LOCKER_SWITCH");
            System.putIntForUser(getContentResolver(), KEY_APP_LOCKER_SWITCH, value.booleanValue(), -2);
        }
        return true;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (!preference.getKey().equals(KEY_APP_LOCKER_ADD_APPS)) {
            return false;
        }
        Log.d(TAG, "KEY_APP_LOCKER_ADD_APPS");
        Intent intent = new Intent("oneplus.intent.action.ONEPLUS_GAME_READ_APP_LIST_ACTION");
        intent.putExtra(OPConstants.OP_LOAD_APP_TYEP, 63);
        this.mContext.startActivity(intent);
        return true;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
