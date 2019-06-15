package com.oneplus.settings.quicklaunch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.view.View;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.fingerprint.FingerprintEnrollIntroduction;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.better.OPAppModel;
import com.oneplus.settings.quickpay.QuickPayLottieAnimPreference.OnPreferenceViewClickListener;
import com.oneplus.settings.ui.OPViewPagerGuideCategory;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OPQuickLaunchSettings extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener, OnPreferenceViewClickListener, Indexable {
    public static final int CODE_REQUEST_FINGERPRINT = 1;
    private static final int MY_USER_ID = UserHandle.myUserId();
    public static final int OP_HOME_LONG_ACTION_QUICKPAY = 11;
    public static final String OP_QUICKPAY_DEFAULT_WAY = "op_quickpay_default_way";
    public static final String OP_QUICKPAY_ENABLE = "op_quickpay_enable";
    public static final String OP_QUICKPAY_SHOW = "op_quickpay_show";
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_quicklaunch_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            return new ArrayList();
        }
    };
    private final String KEY_ENABLE_QUICK_LAUNCH = "key_enable_quick_launch";
    private final String KEY_FINGERPRINT_LONGPRESS_ACTION_FOR_QUICKPAY = "op_fingerprint_longpress_action_for_quickpay";
    private final String KEY_OP_QUICK_LAUNCHER_SETTINGS = "op_quick_launcher_settings";
    private final String KEY_QUICKLAUNCH_INSTRUCTIONS = "key_quick_launch_instructions";
    private final String OP_FINGERPRINT_LONG_PRESS_ACTION = "op_fingerprint_long_press_action";
    private SettingsActivity mActivity;
    private List<OPAppModel> mDefaultQuickLaunchAppList = new ArrayList();
    private SwitchPreference mEnableQuickLaunch;
    private FingerprintManager mFingerprintManager;
    private boolean mHasFingerprint;
    private PackageManager mPackageManager;
    private String[] mPayWaysName = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_quickpay_ways_name);
    private OPViewPagerGuideCategory mQuickLaunchGuide;
    private Preference mQuickLaunchPreferece;

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mActivity = (SettingsActivity) getActivity();
        this.mPackageManager = this.mActivity.getPackageManager();
        this.mFingerprintManager = (FingerprintManager) getActivity().getSystemService("fingerprint");
        initPreference();
        initDefaultData();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mActivity = (SettingsActivity) getActivity();
    }

    public void onResume() {
        super.onResume();
        checkFingerPrint();
        updatePreferenceState();
        if (this.mQuickLaunchGuide != null) {
            this.mQuickLaunchGuide.startAnim();
        }
    }

    private void checkFingerPrint() {
        if (this.mFingerprintManager.getEnrolledFingerprints(MY_USER_ID).size() > 0) {
            this.mHasFingerprint = true;
        } else {
            this.mHasFingerprint = false;
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mQuickLaunchGuide != null) {
            this.mQuickLaunchGuide.stopAnim();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mQuickLaunchGuide != null) {
            this.mQuickLaunchGuide.releaseAnim();
        }
    }

    private void initPreference() {
        addPreferencesFromResource(R.xml.op_quicklaunch_settings);
        this.mQuickLaunchPreferece = findPreference("op_quick_launcher_settings");
        if (!OPUtils.isSupportQuickLaunch()) {
            this.mQuickLaunchPreferece.setVisible(false);
        }
        this.mEnableQuickLaunch = (SwitchPreference) findPreference("key_enable_quick_launch");
        this.mEnableQuickLaunch.setOnPreferenceChangeListener(this);
        this.mQuickLaunchGuide = (OPViewPagerGuideCategory) findPreference("key_quick_launch_instructions");
        this.mQuickLaunchGuide.setAnimationWhiteResources(new String[]{"op_quick_launch_guide_active_white.json", "op_quick_launch_guide_exit_white.json"});
        this.mQuickLaunchGuide.setAnimationDarkResources(new String[]{"op_quick_launch_guide_active_dark.json", "op_quick_launch_guide_exit_dark.json"});
        this.mQuickLaunchGuide.setTitleResources(new int[]{R.string.oneplus_quick_launch_how_to_use_title, R.string.oneplus_quick_launch_how_to_exit_title});
        this.mQuickLaunchGuide.setDescriptionIdResources(new int[]{R.string.oneplus_quick_launch_how_to_use_summary, R.string.oneplus_quick_launch_how_to_exit_summary});
    }

    private void updatePreferenceState() {
        if (Secure.getInt(getContentResolver(), "op_quickpay_enable", 0) != 1) {
            this.mEnableQuickLaunch.setChecked(false);
        } else if (this.mHasFingerprint) {
            this.mEnableQuickLaunch.setChecked(true);
        } else {
            Secure.putInt(getContentResolver(), "op_quickpay_enable", 0);
            this.mEnableQuickLaunch.setChecked(false);
        }
    }

    private void initDefaultData() {
        boolean z = false;
        if (System.getInt(getContentResolver(), OPConstants.HAVE_EDIT_QUICK_LAUNCH_LIST, 0) == 1) {
            z = true;
        }
        if (!z) {
            createDefaultData();
            System.putInt(getContentResolver(), OPConstants.HAVE_EDIT_QUICK_LAUNCH_LIST, 1);
        }
    }

    private void createDefaultData() {
        this.mDefaultQuickLaunchAppList.clear();
        OPAppModel oPAppModel;
        StringBuilder quickLauncherhortcut;
        OPAppModel model;
        if (OPUtils.isO2()) {
            ResolveInfo reInfo;
            if (OPUtils.isAppExist(this.mActivity, OPConstants.PACKAGE_PAYTM)) {
                oPAppModel = new OPAppModel(OPConstants.PACKAGE_PAYTM, this.mPayWaysName[4], String.valueOf(4), 0, false);
                oPAppModel.setType(2);
                oPAppModel.setAppIcon(OPUtils.getAppIcon(this.mActivity, OPConstants.PACKAGE_PAYTM));
                this.mDefaultQuickLaunchAppList.add(oPAppModel);
            }
            if (OPUtils.isAppExist(this.mActivity, "com.google.android.googlequicksearchbox")) {
                reInfo = OPUtils.getResolveInfoByPackageName(this.mActivity, "com.google.android.googlequicksearchbox");
                if (reInfo != null) {
                    this.mDefaultQuickLaunchAppList.add(OPUtils.loadShortcutByPackageNameAndShortcutId(this.mActivity, "com.google.android.googlequicksearchbox", "voice_shortcut", reInfo.activityInfo.applicationInfo.uid));
                }
            }
            if (OPUtils.isAppExist(this.mActivity, OPConstants.PACKAGE_ONEPLUS_NOTE)) {
                reInfo = OPUtils.getResolveInfoByPackageName(this.mActivity, OPConstants.PACKAGE_ONEPLUS_NOTE);
                if (reInfo != null) {
                    this.mDefaultQuickLaunchAppList.add(OPUtils.loadShortcutByPackageNameAndShortcutId(this.mActivity, OPConstants.PACKAGE_ONEPLUS_NOTE, "new_note", reInfo.activityInfo.applicationInfo.uid));
                }
            }
            if (OPUtils.isAppExist(this.mActivity, OPConstants.PACKAGE_GOOGLE_MUSIC)) {
                reInfo = OPUtils.getResolveInfoByPackageName(this.mActivity, OPConstants.PACKAGE_GOOGLE_MUSIC);
                if (reInfo != null) {
                    this.mDefaultQuickLaunchAppList.add(OPUtils.loadShortcutByPackageNameAndShortcutId(this.mActivity, OPConstants.PACKAGE_GOOGLE_MUSIC, "music-mylibrary", reInfo.activityInfo.applicationInfo.uid));
                }
            }
            if (OPUtils.isAppExist(this.mActivity, "com.google.android.calendar")) {
                reInfo = OPUtils.getResolveInfoByPackageName(this.mActivity, "com.google.android.calendar");
                if (reInfo != null) {
                    this.mDefaultQuickLaunchAppList.add(OPUtils.loadShortcutByPackageNameAndShortcutId(this.mActivity, "com.google.android.calendar", "launcher_shortcuts_shortcut_new_event", reInfo.activityInfo.applicationInfo.uid));
                }
            }
            quickLauncherhortcut = new StringBuilder();
            for (OPAppModel model2 : this.mDefaultQuickLaunchAppList) {
                if (model2 != null) {
                    String quickShortcut = OPUtils.getQuickPayAppString(model2);
                    if (model2.getType() == 0) {
                        quickShortcut = OPUtils.getQuickLaunchAppString(model2);
                    } else if (model2.getType() == 1) {
                        quickShortcut = OPUtils.getQuickLaunchShortcutsString(model2);
                    } else if (model2.getType() == 2) {
                        quickShortcut = OPUtils.getQuickPayAppString(model2);
                    }
                    quickLauncherhortcut.append(quickShortcut);
                    OPUtils.saveQuickLaunchStrings(this.mActivity, quickLauncherhortcut.toString());
                }
            }
        } else {
            if (OPUtils.isAppExist(this.mActivity, OPConstants.PACKAGE_WECHAT)) {
                oPAppModel = new OPAppModel(OPConstants.PACKAGE_WECHAT, this.mPayWaysName[0], String.valueOf(0), 0, false);
                oPAppModel.setType(2);
                oPAppModel.setAppIcon(OPUtils.getQuickPayIconByType(this.mActivity, 0));
                model2 = new OPAppModel(OPConstants.PACKAGE_WECHAT, this.mPayWaysName[1], String.valueOf(1), 0, false);
                model2.setType(2);
                model2.setAppIcon(OPUtils.getQuickPayIconByType(this.mActivity, 1));
                this.mDefaultQuickLaunchAppList.add(oPAppModel);
                this.mDefaultQuickLaunchAppList.add(model2);
            }
            if (OPUtils.isAppExist(this.mActivity, OPConstants.PACKAGE_ALIPAY)) {
                oPAppModel = new OPAppModel(OPConstants.PACKAGE_ALIPAY, this.mPayWaysName[2], String.valueOf(2), 0, false);
                oPAppModel.setType(2);
                oPAppModel.setAppIcon(OPUtils.getQuickPayIconByType(this.mActivity, 2));
                oPAppModel = new OPAppModel(OPConstants.PACKAGE_ALIPAY, this.mPayWaysName[3], String.valueOf(3), 0, false);
                oPAppModel.setType(2);
                oPAppModel.setAppIcon(OPUtils.getQuickPayIconByType(this.mActivity, 3));
                this.mDefaultQuickLaunchAppList.add(oPAppModel);
                this.mDefaultQuickLaunchAppList.add(oPAppModel);
            }
            quickLauncherhortcut = new StringBuilder();
            for (OPAppModel model3 : this.mDefaultQuickLaunchAppList) {
                quickLauncherhortcut.append(OPUtils.getQuickPayAppString(model3));
                OPUtils.saveQuickLaunchStrings(this.mActivity, quickLauncherhortcut.toString());
            }
        }
        OPUtils.sendAppTrackerForQuickLaunch();
    }

    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        if (preference != this.mEnableQuickLaunch) {
            return false;
        }
        boolean state = ((Boolean) obj).booleanValue();
        if (!state || this.mHasFingerprint) {
            boolean ret = Secure.putInt(getContentResolver(), "op_quickpay_enable", state);
            return true;
        }
        gotoFingerprintEnrollIntroduction(1);
        return false;
    }

    public void gotoFingerprintEnrollIntroduction(int requstCode) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", FingerprintEnrollIntroduction.class.getName());
        startActivityForResult(intent, requstCode);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 1) {
            checkFingerPrint();
            if (this.mHasFingerprint) {
                Secure.putInt(getContentResolver(), "op_quickpay_enable", 1);
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    public void onPreferenceViewClick(View view) {
    }
}
