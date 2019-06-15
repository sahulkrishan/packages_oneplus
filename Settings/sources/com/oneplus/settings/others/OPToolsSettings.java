package com.oneplus.settings.others;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.util.OpFeatures;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.password.ChooseLockGeneric;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.quickpay.QuickPaySettings;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OPToolsSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener, Indexable {
    private static final String ANTI_MISOPERATION_SCREEN_TOUCH = "anti_misoperation_of_the_screen_touch_enable";
    private static final int CHOOSE_LOCK_GENERIC_REQUEST = 102;
    private static final int GOTO_APPLOCKER_PAGE_REQUEST = 104;
    private static final String KEY_APP_LOCKER = "oneplus_app_locker";
    private static final String KEY_ONEPLUS_LABORATORY_SETTINGS = "oneplus_laboratory_settings";
    private static final String KEY_ONEPLUS_MULTI_APP = "oneplus_multi_app";
    private static final String KEY_ONEPLUS_QUICK_LAUNCH = "oneplus_quick_launch";
    private static final String KEY_ONEPLUS_QUICK_PAY = "oneplus_quick_pay";
    private static final String KEY_ONEPLUS_QUICK_REPLAY = "oneplus_quick_replay";
    private static final String KEY_OP_MULTITASKING_CLEAN_WAY = "op_multitasking_clean_way";
    private static final String KEY_QUICK_CLIPBOARD = "quick_clipboard";
    private static final String KEY_SWITCH = "switch";
    private static final String PUSH_SWITCH_ACTION = "net.oneplus.push.action.SWITCH_CHANGED";
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_tools_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> result = new ArrayList();
            if (OPUtils.isGuestMode()) {
                result.add(OPToolsSettings.ANTI_MISOPERATION_SCREEN_TOUCH);
                result.add(OPToolsSettings.KEY_APP_LOCKER);
                result.add(OPToolsSettings.KEY_ONEPLUS_LABORATORY_SETTINGS);
            }
            if (!OPUtils.isLaboratoryFeatureExist()) {
                result.add(OPToolsSettings.KEY_ONEPLUS_LABORATORY_SETTINGS);
            }
            if (OPUtils.isSupportQuickLaunch()) {
                result.add(OPToolsSettings.KEY_ONEPLUS_QUICK_PAY);
            } else {
                result.add(OPToolsSettings.KEY_ONEPLUS_QUICK_LAUNCH);
            }
            if (!QuickPaySettings.canShowQuickPay(context) || OPUtils.isGuestMode()) {
                result.add(OPToolsSettings.KEY_ONEPLUS_QUICK_PAY);
            }
            if (OPUtils.isGuestMode()) {
                result.add(OPToolsSettings.KEY_ONEPLUS_QUICK_LAUNCH);
            }
            if (!OPUtils.isAppPakExist(context, "com.oneplus.clipboard")) {
                result.add(OPToolsSettings.KEY_QUICK_CLIPBOARD);
            }
            if (!OPUtils.isAppExist(context, "com.oneplus.backuprestore")) {
                result.add(OPToolsSettings.KEY_SWITCH);
            }
            if (OPUtils.isGuestMode()) {
                result.add(OPToolsSettings.KEY_ONEPLUS_QUICK_LAUNCH);
            }
            if (!OPUtils.isSupportQuickReply() || OPUtils.isGuestMode()) {
                result.add(OPToolsSettings.KEY_ONEPLUS_QUICK_REPLAY);
            }
            return result;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader, null);
        }
    };
    private static final String TAG = "OPOthersSettings";
    private static final String TIMER_SHUTDOWN_STARTUP_KEY = "timer_shutdown_startup_settings";
    private SwitchPreference mAntiMisOperationTouch;
    private Preference mAppLocker;
    private long mChallenge;
    private Context mContext;
    private boolean mGotoAppLockerClick = false;
    private Preference mMultiAppPreference;
    private Preference mOPMultitaskingCleanWayPreference;
    private Preference mOneplusLaboratorySettings;
    private Preference mOneplusQuickReply;
    private SwitchPreference mQuickClipboardSwitchPreference;
    private Preference mQuickLaunchPreference;
    private Preference mQuickPayPreference;
    private Preference mSwitchPreference;
    private Preference mTimerShutdownPreference;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mLoader;

        /* synthetic */ SummaryProvider(Context x0, SummaryLoader x1, AnonymousClass1 x2) {
            this(x0, x1);
        }

        private SummaryProvider(Context context, SummaryLoader loader) {
            this.mContext = context;
            this.mLoader = loader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                updateSummary();
            }
        }

        private void updateSummary() {
            if (OPUtils.isGuestMode()) {
                this.mLoader.setSummary(this, this.mContext.getString(R.string.oneplus_gaming_mode));
            } else if (OPUtils.isSupportQuickLaunch()) {
                this.mLoader.setSummary(this, this.mContext.getString(R.string.oneplus_tools_quicklaunch_summary));
            } else {
                this.mLoader.setSummary(this, this.mContext.getString(R.string.oneplus_tools_summary));
            }
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_tools_settings);
        this.mContext = getActivity();
        updateView();
    }

    public void onResume() {
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void updateView() {
        PreferenceScreen root = getPreferenceScreen();
        this.mMultiAppPreference = findPreference(KEY_ONEPLUS_MULTI_APP);
        this.mMultiAppPreference.setOnPreferenceClickListener(this);
        this.mAppLocker = findPreference(KEY_APP_LOCKER);
        this.mAppLocker.setOnPreferenceClickListener(this);
        this.mQuickPayPreference = findPreference(KEY_ONEPLUS_QUICK_PAY);
        this.mQuickLaunchPreference = findPreference(KEY_ONEPLUS_QUICK_LAUNCH);
        if (OPUtils.isSupportQuickLaunch()) {
            this.mQuickPayPreference.setVisible(false);
        } else {
            this.mQuickLaunchPreference.setVisible(false);
        }
        if (OPUtils.isGuestMode()) {
            this.mQuickLaunchPreference.setVisible(false);
        }
        if (!QuickPaySettings.canShowQuickPay(getContext()) || OPUtils.isGuestMode()) {
            this.mQuickPayPreference.setVisible(false);
        }
        if (OPUtils.isSurportBackFingerprint(SettingsBaseApplication.mApplication)) {
            this.mQuickPayPreference.setSummary((int) R.string.oneplus_fingerprint_longpress_for_quickpay_summary);
        } else {
            this.mQuickPayPreference.setSummary((int) R.string.oneplus_quickpay_entry_summary);
        }
        this.mAntiMisOperationTouch = (SwitchPreference) findPreference(ANTI_MISOPERATION_SCREEN_TOUCH);
        this.mAntiMisOperationTouch.setOnPreferenceChangeListener(this);
        this.mAntiMisOperationTouch.setChecked(System.getInt(getContentResolver(), "oem_acc_anti_misoperation_screen", 0) != 0);
        if (!OpFeatures.isSupport(new int[]{73})) {
            this.mAntiMisOperationTouch.setSummary((int) R.string.oneplus_pocket_mode_summary);
        }
        this.mTimerShutdownPreference = findPreference(TIMER_SHUTDOWN_STARTUP_KEY);
        if (checkIfNeedPasswordToPowerOn()) {
            this.mTimerShutdownPreference.setEnabled(false);
            this.mTimerShutdownPreference.setSummary((int) R.string.oneplus_timer_shutdown_disable_summary);
        } else {
            this.mTimerShutdownPreference.setEnabled(true);
        }
        this.mOneplusLaboratorySettings = findPreference(KEY_ONEPLUS_LABORATORY_SETTINGS);
        this.mOneplusLaboratorySettings.setOnPreferenceClickListener(this);
        this.mOneplusQuickReply = findPreference(KEY_ONEPLUS_QUICK_REPLAY);
        if (!OPUtils.isSupportQuickReply()) {
            root.removePreference(this.mOneplusQuickReply);
        }
        if (OPUtils.isGuestMode()) {
            root.removePreference(this.mTimerShutdownPreference);
            root.removePreference(this.mAntiMisOperationTouch);
            root.removePreference(this.mAppLocker);
            root.removePreference(this.mMultiAppPreference);
            root.removePreference(this.mOneplusLaboratorySettings);
            root.removePreference(this.mOneplusQuickReply);
        }
        if (!OPUtils.isLaboratoryFeatureExist()) {
            root.removePreference(this.mOneplusLaboratorySettings);
        }
        this.mSwitchPreference = findPreference(KEY_SWITCH);
        this.mSwitchPreference.setOnPreferenceClickListener(this);
        if (!OPUtils.isAppExist(getActivity(), "com.oneplus.backuprestore")) {
            getPreferenceScreen().removePreference(this.mSwitchPreference);
        }
    }

    private void launchChooseOrConfirmLock(int requestCode) {
        Intent intent = new Intent();
        if (!new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(requestCode, getString(R.string.op_security_lock_settings_title), null, null, this.mChallenge)) {
            intent.setClassName("com.android.settings", ChooseLockGeneric.class.getName());
            intent.putExtra(ChooseLockGenericFragment.MINIMUM_QUALITY_KEY, 65536);
            intent.putExtra(ChooseLockGenericFragment.HIDE_DISABLED_PREFS, true);
            intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, true);
            intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, this.mChallenge);
            startActivityForResult(intent, 102);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.mGotoAppLockerClick) {
            if ((requestCode == 102 || requestCode == 104) && (resultCode == 1 || resultCode == -1)) {
                gotoAppLockerPage();
            }
            this.mGotoAppLockerClick = false;
        }
    }

    public void gotoAppLockerPage() {
        Intent intent = null;
        try {
            intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.Settings$OPAppLockerActivity");
            getActivity().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("No activity found for ");
            stringBuilder.append(intent);
            Log.d(str, stringBuilder.toString());
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        Intent intents;
        if (KEY_ONEPLUS_MULTI_APP.equals(preference.getKey())) {
            try {
                intents = new Intent();
                intents.setAction("oneplus.intent.action.ONEPLUS_MULTI_APP_LIST_ACTION");
                getPrefContext().startActivity(intents);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
            return true;
        } else if (KEY_SWITCH.equals(preference.getKey())) {
            try {
                intents = new Intent();
                intents.setClassName("com.oneplus.backuprestore", "com.oneplus.backuprestore.activity.BootActivity");
                intents.setFlags(268435456);
                getPrefContext().startActivity(intents);
            } catch (ActivityNotFoundException e2) {
                e2.printStackTrace();
            }
            return true;
        } else if (!KEY_APP_LOCKER.equals(preference.getKey())) {
            return false;
        } else {
            Log.d(TAG, "App -> Locker");
            this.mGotoAppLockerClick = true;
            launchChooseOrConfirmLock(104);
            return true;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!preference.getKey().equals(ANTI_MISOPERATION_SCREEN_TOUCH)) {
            return false;
        }
        System.putInt(getContentResolver(), "oem_acc_anti_misoperation_screen", ((Boolean) newValue).booleanValue());
        return true;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    public boolean checkIfNeedPasswordToPowerOn() {
        return Global.getInt(getActivity().getContentResolver(), "require_password_to_decrypt", 0) == 1;
    }
}
