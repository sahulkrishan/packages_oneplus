package com.oneplus.settings.quickpay;

import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.fingerprint.FingerprintEnrollIntroduction;
import com.android.settings.location.RadioButtonPreference;
import com.google.android.collect.Lists;
import com.oneplus.settings.OPButtonsSettings;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.quickpay.QuickPayLottieAnimPreference.OnPreferenceViewClickListener;
import com.oneplus.settings.ui.OPPreferenceDivider;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.List;

public class QuickPaySettings extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener, OnPreferenceViewClickListener {
    public static final int CODE_REQUEST_FINGERPRINT = 1;
    private static final int MY_USER_ID = UserHandle.myUserId();
    public static final int OP_HOME_LONG_ACTION_QUICKPAY = 11;
    public static final String OP_QUICKPAY_DEFAULT_WAY = "op_quickpay_default_way";
    public static final String OP_QUICKPAY_ENABLE = "op_quickpay_enable";
    public static final String OP_QUICKPAY_SHOW = "op_quickpay_show";
    public static final String[] sPayWaysKey = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_quickpay_ways_key);
    public static final int[] sPayWaysValue = SettingsBaseApplication.mApplication.getResources().getIntArray(R.array.oneplus_quickpay_ways_value);
    private final String KEY_FINGERPRINT_LONGPRESS_ACTION_FOR_QUICKPAY = "op_fingerprint_longpress_action_for_quickpay";
    private final String KEY_PREFERENCE_DIVIDER_LINE2 = "preference_divider_line2";
    private final String KEY_QUICKPAY_INSTRUCTIONS = "key_quickpay_instructions";
    private final String KEY_QUICKPAY_SELECT_DEFAULT_WAY_CATEGORY = "key_quickpay_select_default_way_category";
    private final String KEY_QUICKPAY_UNINSTALL_APP_CATEGORY = "key_quickpay_uninstall_app_category";
    private final String KEY_SWITCH_LOCKSCREEN = "key_switch_lockscreen";
    private final String KEY_SWITCH_UNLOCKSCREEN = "key_switch_unlockscreen";
    private final String OP_FINGERPRINT_LONG_PRESS_ACTION = "op_fingerprint_long_press_action";
    private SettingsActivity mActivity;
    private List<RadioButtonPreference> mAllPayWaysPreference = Lists.newArrayList();
    private int mDefaultLongPressOnHomeBehavior;
    private SwitchPreference mFingerprintLongpressQuickpay;
    private FingerprintManager mFingerprintManager;
    private boolean mHasFingerprint;
    private String[] mHomeKeyActionName;
    private String[] mHomeKeyActionValue;
    private IntentFilter mIntentFilter;
    private List<String> mPayWaysKeyList = Lists.newArrayList();
    private String[] mPayWaysName = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_quickpay_ways_name);
    private List<String> mPayWaysNameList = Lists.newArrayList();
    private List<Integer> mPayWaysValueList = Lists.newArrayList();
    private AppInstallAndUninstallReceiver mQuickPayAppsAddOrRemovedReceiver;
    private OPPreferenceDivider preference_divider_line2;
    private QuickPayLottieAnimPreference quickpay_instructions;
    private PreferenceCategory quickpay_select_default_way_category;
    private PreferenceCategory quickpay_uninstall_app_category;
    private RadioButtonPreference quickpay_way_alipay_qrcode;
    private RadioButtonPreference quickpay_way_alipay_scanning;
    private RadioButtonPreference quickpay_way_paytm;
    private RadioButtonPreference quickpay_way_wecaht_qrcode;
    private RadioButtonPreference quickpay_way_wecaht_scanning;
    private SwitchPreference switch_lockscreen;
    private SwitchPreference switch_unlockscreen;

    class AppInstallAndUninstallReceiver extends BroadcastReceiver {
        AppInstallAndUninstallReceiver() {
        }

        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.PACKAGE_REMOVED".equals(action) || "android.intent.action.PACKAGE_ADDED".equals(action)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                if (!TextUtils.isEmpty(packageName)) {
                    if (OPConstants.PACKAGE_WECHAT.equals(packageName) || OPConstants.PACKAGE_ALIPAY.equals(packageName) || OPConstants.PACKAGE_PAYTM.equals(packageName)) {
                        QuickPaySettings.this.updatePreferenceState();
                    }
                }
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        this.mPayWaysName = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_quickpay_ways_name);
        initHomeActionName();
        super.onConfigurationChanged(newConfig);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFingerprintManager = (FingerprintManager) getActivity().getSystemService("fingerprint");
        this.mIntentFilter = new IntentFilter("android.intent.action.PACKAGE_REMOVED");
        this.mIntentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        this.mIntentFilter.addDataScheme("package");
        this.mQuickPayAppsAddOrRemovedReceiver = new AppInstallAndUninstallReceiver();
        initPreference();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mActivity = (SettingsActivity) getActivity();
        initHomeActionName();
    }

    private void initHomeActionName() {
        if (OPButtonsSettings.checkGMS(getPrefContext())) {
            this.mHomeKeyActionName = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.hardware_keys_action_entries_quickpay);
            this.mHomeKeyActionValue = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.hardware_keys_action_values_quickpay);
            return;
        }
        this.mHomeKeyActionName = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.hardware_keys_action_entries_nogms_quickpay);
        this.mHomeKeyActionValue = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.hardware_keys_action_values_nogms_quickpay);
    }

    private int getLongPressHomeActionIndexByValue(int value) {
        for (int i = 0; i < this.mHomeKeyActionValue.length; i++) {
            if (value == Integer.parseInt(this.mHomeKeyActionValue[i])) {
                return i;
            }
        }
        return 0;
    }

    public void onResume() {
        super.onResume();
        checkFingerPrint();
        updatePreferenceState();
        SettingsBaseApplication.mApplication.registerReceiver(this.mQuickPayAppsAddOrRemovedReceiver, this.mIntentFilter);
    }

    private void checkFingerPrint() {
        if (this.mFingerprintManager.getEnrolledFingerprints(MY_USER_ID).size() > 0) {
            this.mHasFingerprint = true;
        } else {
            this.mHasFingerprint = false;
        }
    }

    public void onPause() {
        this.quickpay_instructions.stopAnim();
        SettingsBaseApplication.mApplication.unregisterReceiver(this.mQuickPayAppsAddOrRemovedReceiver);
        super.onPause();
    }

    private void initPreference() {
        addPreferencesFromResource(R.xml.op_quickpay_settings);
        this.switch_lockscreen = (SwitchPreference) findPreference("key_switch_lockscreen");
        this.switch_lockscreen.setOnPreferenceChangeListener(this);
        this.switch_unlockscreen = (SwitchPreference) findPreference("key_switch_unlockscreen");
        this.switch_unlockscreen.setOnPreferenceChangeListener(this);
        if (OPUtils.isSurportBackFingerprint(SettingsBaseApplication.mApplication)) {
            this.switch_lockscreen.setSummary((int) R.string.oneplus_fingerprint_quickpay_lock_swtch_summary);
            removePreference("key_switch_unlockscreen");
        }
        this.quickpay_uninstall_app_category = (PreferenceCategory) findPreference("key_quickpay_uninstall_app_category");
        this.quickpay_select_default_way_category = (PreferenceCategory) findPreference("key_quickpay_select_default_way_category");
        this.quickpay_instructions = (QuickPayLottieAnimPreference) findPreference("key_quickpay_instructions");
        this.quickpay_instructions.setViewOnClick(this);
        this.preference_divider_line2 = (OPPreferenceDivider) findPreference("preference_divider_line2");
        this.quickpay_way_wecaht_qrcode = (RadioButtonPreference) findPreference("key_quickpay_way_wecaht_qrcode");
        this.quickpay_way_wecaht_scanning = (RadioButtonPreference) findPreference("key_quickpay_way_wecaht_scanning");
        this.quickpay_way_alipay_qrcode = (RadioButtonPreference) findPreference("key_quickpay_way_alipay_qrcode");
        this.quickpay_way_alipay_scanning = (RadioButtonPreference) findPreference("key_quickpay_way_alipay_scanning");
        this.quickpay_way_paytm = (RadioButtonPreference) findPreference("key_quickpay_way_paytm");
    }

    private void updatePreferenceState() {
        initPayWayData();
        this.mDefaultLongPressOnHomeBehavior = getActivity().getResources().getInteger(17694802);
        boolean z = false;
        int defaultQuickPayWay = Secure.getIntForUser(getContentResolver(), "op_quickpay_default_way", -1, 0);
        int longPressHomeAction = System.getIntForUser(getContentResolver(), OPConstants.KEY_HOME_LONG_PRESS_ACTION, this.mDefaultLongPressOnHomeBehavior, 0);
        int longPressFingerprintAction = System.getIntForUser(getContentResolver(), "op_fingerprint_long_press_action", this.mDefaultLongPressOnHomeBehavior, 0);
        this.quickpay_select_default_way_category.setVisible(false);
        this.mAllPayWaysPreference.clear();
        if (this.mPayWaysNameList.size() > 0) {
            this.quickpay_uninstall_app_category.setVisible(false);
            this.quickpay_select_default_way_category.setVisible(true);
            boolean isChecked = false;
            for (int i = 0; i < this.mPayWaysNameList.size(); i++) {
                RadioButtonPreference p = (RadioButtonPreference) this.quickpay_select_default_way_category.findPreference((CharSequence) this.mPayWaysKeyList.get(i));
                if (defaultQuickPayWay == ((Integer) this.mPayWaysValueList.get(i)).intValue()) {
                    p.setChecked(true);
                    isChecked = true;
                }
                p.setOnPreferenceClickListener(this);
                this.mAllPayWaysPreference.add(p);
            }
            if (this.mAllPayWaysPreference.size() > 0 && !isChecked) {
                ((RadioButtonPreference) this.mAllPayWaysPreference.get(0)).setChecked(true);
                if (defaultQuickPayWay == -1) {
                    Secure.putInt(getContentResolver(), "op_quickpay_default_way", ((Integer) this.mPayWaysValueList.get(0)).intValue());
                }
            }
        } else {
            this.quickpay_select_default_way_category.setVisible(false);
            this.quickpay_uninstall_app_category.setVisible(true);
        }
        int quickPayState = Secure.getInt(getContentResolver(), "op_quickpay_enable", 0);
        if (quickPayState != 1) {
            this.switch_lockscreen.setChecked(false);
        } else if (this.mHasFingerprint) {
            this.switch_lockscreen.setChecked(true);
        } else {
            Secure.putInt(getContentResolver(), "op_quickpay_enable", 0);
            this.switch_lockscreen.setChecked(false);
            quickPayState = 0;
        }
        boolean onScreenNavKeysEnabled = System.getInt(getContentResolver(), OPConstants.BUTTONS_SHOW_ON_SCREEN_NAVKEYS, 0) != 0;
        boolean solidHomeButtonEnabled = !onScreenNavKeysEnabled || (onScreenNavKeysEnabled && (System.getInt(getContentResolver(), OPConstants.BUTTONS_FORCE_HOME_ENABLED, 0) != 0));
        boolean enable = false;
        if (!(this.switch_lockscreen == null || OPUtils.isSurportBackFingerprint(SettingsBaseApplication.mApplication))) {
            if (longPressHomeAction == 11 && solidHomeButtonEnabled) {
                this.switch_unlockscreen.setChecked(true);
            } else {
                this.switch_unlockscreen.setChecked(false);
                if (!solidHomeButtonEnabled) {
                    this.switch_unlockscreen.setEnabled(false);
                }
            }
            boolean z2 = this.switch_unlockscreen.isChecked() || quickPayState == 1;
            enable = z2;
        }
        if (OPUtils.isSurportBackFingerprint(SettingsBaseApplication.mApplication)) {
            if (quickPayState == 1) {
                z = true;
            }
            enable = z;
        }
        refreshQuickPayEnableUI(enable);
    }

    private void initPayWayData() {
        this.mPayWaysNameList.clear();
        this.mPayWaysKeyList.clear();
        this.mPayWaysValueList.clear();
        if (OPUtils.isAppExist(this.mActivity, OPConstants.PACKAGE_WECHAT)) {
            this.mPayWaysNameList.add(this.mPayWaysName[0]);
            this.mPayWaysKeyList.add(sPayWaysKey[0]);
            this.mPayWaysValueList.add(Integer.valueOf(sPayWaysValue[0]));
            this.mPayWaysNameList.add(this.mPayWaysName[1]);
            this.mPayWaysKeyList.add(sPayWaysKey[1]);
            this.mPayWaysValueList.add(Integer.valueOf(sPayWaysValue[1]));
            this.quickpay_way_wecaht_qrcode.setVisible(true);
            this.quickpay_way_wecaht_scanning.setVisible(true);
        } else {
            this.quickpay_way_wecaht_qrcode.setVisible(false);
            this.quickpay_way_wecaht_scanning.setVisible(false);
        }
        if (OPUtils.isAppExist(this.mActivity, OPConstants.PACKAGE_ALIPAY)) {
            this.mPayWaysNameList.add(this.mPayWaysName[2]);
            this.mPayWaysKeyList.add(sPayWaysKey[2]);
            this.mPayWaysValueList.add(Integer.valueOf(sPayWaysValue[2]));
            this.mPayWaysNameList.add(this.mPayWaysName[3]);
            this.mPayWaysKeyList.add(sPayWaysKey[3]);
            this.mPayWaysValueList.add(Integer.valueOf(sPayWaysValue[3]));
            this.quickpay_way_alipay_qrcode.setVisible(true);
            this.quickpay_way_alipay_scanning.setVisible(true);
        } else {
            this.quickpay_way_alipay_qrcode.setVisible(false);
            this.quickpay_way_alipay_scanning.setVisible(false);
        }
        if (OPUtils.isAppExist(this.mActivity, OPConstants.PACKAGE_PAYTM)) {
            this.mPayWaysNameList.add(this.mPayWaysName[4]);
            this.mPayWaysKeyList.add(sPayWaysKey[4]);
            this.mPayWaysValueList.add(Integer.valueOf(sPayWaysValue[4]));
            this.quickpay_way_paytm.setVisible(true);
            return;
        }
        this.quickpay_way_paytm.setVisible(false);
    }

    private void refreshQuickPayEnableUI(boolean enable) {
        removePreference("preference_divider_line2");
        if (!enable) {
            removePreference("key_quickpay_select_default_way_category");
            removePreference("key_quickpay_uninstall_app_category");
        } else if (this.mPayWaysNameList.size() > 0) {
            getPreferenceScreen().addPreference(this.quickpay_select_default_way_category);
            getPreferenceScreen().addPreference(this.preference_divider_line2);
        } else {
            getPreferenceScreen().addPreference(this.quickpay_uninstall_app_category);
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        for (int i = 0; i < this.mPayWaysKeyList.size(); i++) {
            if (key.equals(this.mPayWaysKeyList.get(i))) {
                Secure.putInt(getContentResolver(), "op_quickpay_default_way", ((Integer) this.mPayWaysValueList.get(i)).intValue());
                for (RadioButtonPreference p : this.mAllPayWaysPreference) {
                    p.setChecked(false);
                }
                ((RadioButtonPreference) preference).setChecked(true);
                return true;
            }
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        boolean state;
        if (preference == this.switch_lockscreen) {
            state = ((Boolean) obj).booleanValue();
            if (!state || this.mHasFingerprint) {
                updateLockHomeAction(state);
                return true;
            }
            gotoFingerprintEnrollIntroduction(1);
            return false;
        } else if (preference == this.switch_unlockscreen) {
            state = ((Boolean) obj).booleanValue();
            int index = getLongPressHomeActionIndexByValue(System.getIntForUser(getContentResolver(), OPConstants.KEY_HOME_LONG_PRESS_ACTION, this.mDefaultLongPressOnHomeBehavior, 0));
            if (!state || index == 0) {
                updateUnLockHomeAction(state);
                return true;
            }
            showConfirmChangeHomeAction(state, index);
            return false;
        } else if (preference != this.mFingerprintLongpressQuickpay) {
            return false;
        } else {
            updateUnLockFingerprintLongpressAction(((Boolean) obj).booleanValue());
            return true;
        }
    }

    private void showConfirmChangeHomeAction(final boolean state, int longPressHomeActionIndex) {
        if (longPressHomeActionIndex >= this.mHomeKeyActionName.length) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("longPressHomeActionIndex is out of max length.longPressHomeActionIndex=");
            stringBuilder.append(longPressHomeActionIndex);
            Log.e("QuickPaySettings", stringBuilder.toString());
            return;
        }
        String longPressHomeActionName = this.mHomeKeyActionName[longPressHomeActionIndex];
        Builder builder = new Builder(this.mActivity);
        builder.setMessage(this.mActivity.getString(R.string.oneplus_quickpay_confirm_changehomebutton, new Object[]{longPressHomeActionName}));
        builder.setPositiveButton(this.mActivity.getString(R.string.oneplus_timer_shutdown_position), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                QuickPaySettings.this.updateUnLockHomeAction(state);
                QuickPaySettings.this.switch_unlockscreen.setChecked(state);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(this.mActivity.getString(R.string.oneplus_timer_shutdown_nagative), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    private void updateUnLockFingerprintLongpressAction(boolean state) {
        boolean enable = false;
        boolean ret = System.putInt(getContentResolver(), "op_fingerprint_long_press_action", state ? 11 : 0);
        if (state || this.switch_lockscreen.isChecked()) {
            enable = true;
        }
        refreshQuickPayEnableUI(enable);
    }

    private void updateUnLockHomeAction(boolean state) {
        boolean enable = false;
        boolean ret = System.putInt(getContentResolver(), OPConstants.KEY_HOME_LONG_PRESS_ACTION, state ? 11 : 0);
        if (state || this.switch_lockscreen.isChecked()) {
            enable = true;
        }
        refreshQuickPayEnableUI(enable);
    }

    private void updateLockHomeAction(boolean state) {
        boolean enable;
        boolean ret = Secure.putInt(getContentResolver(), "op_quickpay_enable", state);
        if (OPUtils.isSurportBackFingerprint(SettingsBaseApplication.mApplication)) {
            enable = state;
        } else {
            boolean z = state || this.switch_unlockscreen.isChecked();
            enable = z;
        }
        refreshQuickPayEnableUI(enable);
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
                refreshQuickPayEnableUI(Secure.putInt(getContentResolver(), "op_quickpay_enable", 1));
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    public static boolean canShowQuickPay(Context context) {
        if (Secure.getIntForUser(context.getContentResolver(), "op_quickpay_show", 0, 0) == 1) {
            return true;
        }
        boolean installedWechat = OPUtils.isAppExist(context, OPConstants.PACKAGE_WECHAT);
        boolean installedAlipay = OPUtils.isAppExist(context, OPConstants.PACKAGE_ALIPAY);
        boolean installedPaytm = OPUtils.isAppExist(context, OPConstants.PACKAGE_PAYTM);
        if (installedAlipay || installedWechat || installedPaytm) {
            return Secure.putInt(context.getContentResolver(), "op_quickpay_show", 1);
        }
        return false;
    }

    public static void gotoQuickPaySettingsPage(Context context) {
        Intent intent = null;
        try {
            intent = new Intent("com.oneplus.action.QUICKPAY_SETTINGS");
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("No activity found for ");
            stringBuilder.append(intent);
            Log.d("QuickPaySettings", stringBuilder.toString());
        }
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    public void onPreferenceViewClick(View view) {
        this.quickpay_instructions.playOrStopAnim();
    }
}
