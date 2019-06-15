package com.oneplus.settings.navigationbargestures;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.ui.RadioButtonPreference;
import com.android.settings.ui.RadioButtonPreference.OnClickListener;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.ui.OPPhoneControlWayCategory;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OPNavigationBarGesturesSettings extends SettingsPreferenceFragment implements OnClickListener, OnPreferenceChangeListener, Indexable {
    private static final String KEY_ALWAYS_SHOW_NAVIGATION_BAR = "always_show_navigation_bar";
    private static final String KEY_BACK_HOME = "back_home";
    private static final String KEY_CUSTOMIZATION = "customization";
    private static final String KEY_GESTURE_NAVIGATION_BAR = "gesture_navigation_bar";
    private static final String KEY_OP_NAVIGATION_BAR_TYPE = "op_navigation_bar_type";
    private static final String KEY_PHONE_CONTROL_WAY = "phone_control_way";
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_navigation_bar_gestures_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> result = new ArrayList();
            if (OPNavigationBarGesturesSettings.isSupportHardwareKeys()) {
                result.add(OPNavigationBarGesturesSettings.KEY_CUSTOMIZATION);
                result.add("choose_navigation_bar");
                result.add(OPNavigationBarGesturesSettings.KEY_ALWAYS_SHOW_NAVIGATION_BAR);
                result.add(OPNavigationBarGesturesSettings.KEY_BACK_HOME);
                result.add(OPNavigationBarGesturesSettings.KEY_GESTURE_NAVIGATION_BAR);
                result.add(OPNavigationBarGesturesSettings.KEY_PHONE_CONTROL_WAY);
            }
            return result;
        }
    };
    private static final int TYPE_ALWAYS_SHOW_NAVIGATION_BAR = 1;
    private static final int TYPE_BACK_HOME = 2;
    private static final int TYPE_GESTURE_NAVIGATION_BAR = 3;
    private RadioButtonPreference mAlwaysShowNavigationBar;
    private ActivityManager mAm;
    private IActivityManager mAms;
    private RadioButtonPreference mBackHome;
    private Context mContext;
    private Preference mCustomization;
    private RadioButtonPreference mGestureNavigationBar;
    private Handler mHandler = new Handler();
    private OPPhoneControlWayCategory mOPPhoneControlWayCategory;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_navigation_bar_gestures_settings);
        this.mAm = (ActivityManager) getActivity().getSystemService("activity");
        this.mAms = ActivityManager.getService();
        this.mContext = SettingsBaseApplication.mApplication;
        this.mCustomization = findPreference(KEY_CUSTOMIZATION);
        this.mAlwaysShowNavigationBar = (RadioButtonPreference) findPreference(KEY_ALWAYS_SHOW_NAVIGATION_BAR);
        this.mBackHome = (RadioButtonPreference) findPreference(KEY_BACK_HOME);
        this.mGestureNavigationBar = (RadioButtonPreference) findPreference(KEY_GESTURE_NAVIGATION_BAR);
        this.mOPPhoneControlWayCategory = (OPPhoneControlWayCategory) findPreference(KEY_PHONE_CONTROL_WAY);
        this.mAlwaysShowNavigationBar.setOnClickListener(this);
        this.mBackHome.setOnClickListener(this);
        this.mGestureNavigationBar.setOnClickListener(this);
        if (OPUtils.isO2()) {
            this.mAlwaysShowNavigationBar.setTitle((CharSequence) getString(R.string.oneplus_fixed_navigation_bar_o2));
            this.mBackHome.setTitle((CharSequence) getString(R.string.oneplus_back_home_o2));
        }
    }

    public void onResume() {
        super.onResume();
        updateUI();
        if (this.mOPPhoneControlWayCategory != null) {
            this.mOPPhoneControlWayCategory.startAnim();
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mOPPhoneControlWayCategory != null) {
            this.mOPPhoneControlWayCategory.stopAnim();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mOPPhoneControlWayCategory != null) {
            this.mOPPhoneControlWayCategory.releaseAnim();
        }
    }

    private void updateUI() {
        boolean z = false;
        if (getBackHomeEnabled()) {
            this.mAlwaysShowNavigationBar.setChecked(false);
            this.mAlwaysShowNavigationBar.setEnabled(true);
            this.mBackHome.setChecked(true);
            this.mBackHome.setEnabled(true);
            this.mGestureNavigationBar.setChecked(false);
            this.mGestureNavigationBar.setEnabled(true);
            this.mCustomization.setEnabled(true);
            return;
        }
        int value = System.getInt(this.mContext.getContentResolver(), KEY_OP_NAVIGATION_BAR_TYPE, 1);
        this.mAlwaysShowNavigationBar.setChecked(value == 1);
        this.mBackHome.setChecked(false);
        this.mGestureNavigationBar.setChecked(value == 3);
        this.mAlwaysShowNavigationBar.setEnabled(true);
        this.mBackHome.setEnabled(true);
        this.mGestureNavigationBar.setEnabled(true);
        Preference preference = this.mCustomization;
        if (value != 3) {
            z = true;
        }
        preference.setEnabled(z);
    }

    private void delayHideNavkey() {
        this.mAlwaysShowNavigationBar.setEnabled(false);
        this.mBackHome.setEnabled(false);
        this.mGestureNavigationBar.setEnabled(false);
        this.mCustomization.setEnabled(false);
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                OPNavigationBarGesturesSettings.this.mAlwaysShowNavigationBar.setEnabled(true);
                OPNavigationBarGesturesSettings.this.mBackHome.setEnabled(true);
                OPNavigationBarGesturesSettings.this.mGestureNavigationBar.setEnabled(true);
                OPNavigationBarGesturesSettings.this.updateUI();
            }
        }, 1000);
    }

    private void setNavigationType(int type) {
        System.putInt(this.mContext.getContentResolver(), KEY_OP_NAVIGATION_BAR_TYPE, type);
        this.mOPPhoneControlWayCategory.setViewType(type);
        if (type == 1) {
            if (System.getInt(this.mContext.getContentResolver(), OPConstants.OP_GESTURE_BUTTON_ENABLED, 0) == 1) {
                type = 4;
            }
        } else if (type == 3) {
            OPUtils.sendAppTracker(OPConstants.SWAP_BUTTON, 0);
        }
        OPUtils.sendAppTracker(OPConstants.NAV_GESTURES_SETTINGS, type);
    }

    private void setBackHomeEnabled(int enabled) {
        Secure.putInt(this.mContext.getContentResolver(), "swipe_up_to_switch_apps_enabled", enabled);
    }

    private boolean getBackHomeEnabled() {
        return Secure.getInt(this.mContext.getContentResolver(), "swipe_up_to_switch_apps_enabled", this.mContext.getResources().getBoolean(17957053)) != 0;
    }

    public static boolean isCustomSettingsEnable(Context context) {
        if (System.getInt(context.getContentResolver(), KEY_OP_NAVIGATION_BAR_TYPE, 1) != 3) {
            return true;
        }
        return false;
    }

    public void onRadioButtonClicked(RadioButtonPreference pref) {
        if (pref == this.mAlwaysShowNavigationBar) {
            this.mAlwaysShowNavigationBar.setChecked(true);
            this.mBackHome.setChecked(false);
            this.mGestureNavigationBar.setChecked(false);
            setBackHomeEnabled(0);
            delayHideNavkey();
            setNavigationType(1);
        } else if (pref == this.mBackHome) {
            this.mAlwaysShowNavigationBar.setChecked(false);
            this.mBackHome.setChecked(true);
            this.mGestureNavigationBar.setChecked(false);
            System.putInt(this.mContext.getContentResolver(), KEY_OP_NAVIGATION_BAR_TYPE, 1);
            this.mOPPhoneControlWayCategory.setViewType(2);
            setBackHomeEnabled(1);
            delayHideNavkey();
            OPUtils.sendAppTracker(OPConstants.NAV_GESTURES_SETTINGS, 2);
        } else if (pref == this.mGestureNavigationBar) {
            this.mAlwaysShowNavigationBar.setChecked(false);
            this.mBackHome.setChecked(false);
            this.mGestureNavigationBar.setChecked(true);
            setBackHomeEnabled(0);
            System.putInt(this.mContext.getContentResolver(), "lock_to_app_enabled", 0);
            Secure.putInt(this.mContext.getContentResolver(), "lock_to_app_exit_locked", 0);
            try {
                ActivityManager.getService().stopSystemLockTaskMode();
            } catch (RemoteException e) {
                Log.w("OPNotchDisplayGuideActivity", "Failed to rstopSystemLockTaskMode");
            }
            delayHideNavkey();
            setNavigationType(3);
            removeRunningTask();
            killSomeProcess();
        }
    }

    private void killSomeProcess() {
        if (this.mAm != null) {
            this.mAm.killBackgroundProcesses(OPConstants.PACKAGENAME_DIALER);
            this.mAm.killBackgroundProcesses(OPConstants.PACKAGENAME_CONTACTS);
            this.mAm.killBackgroundProcesses(OPConstants.PACKAGENAME_MMS);
        }
    }

    private void removeRunningTask() {
        List<RecentTaskInfo> recentTaskInfos = null;
        try {
            recentTaskInfos = ActivityManager.getService().getRecentTasks(Integer.MAX_VALUE, 2, -2).getList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (recentTaskInfos != null) {
            boolean skipSettings = false;
            for (RecentTaskInfo recentTaskInfo : recentTaskInfos) {
                if (!skipSettings) {
                    ComponentName topActivity = recentTaskInfo != null ? recentTaskInfo.topActivity : null;
                    if (topActivity != null && "com.android.settings".equals(topActivity.getPackageName())) {
                        skipSettings = true;
                    }
                }
                if (recentTaskInfo != null) {
                    try {
                        ActivityManager.getService().removeTask(recentTaskInfo.persistentId);
                    } catch (RemoteException e2) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Failed to remove task=");
                        stringBuilder.append(recentTaskInfo.persistentId);
                        Log.w("OPNotchDisplayGuideActivity", stringBuilder.toString(), e2);
                    }
                }
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    private static boolean isSupportHardwareKeys() {
        return SettingsBaseApplication.mApplication.getResources().getBoolean(17957024) ^ 1;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
