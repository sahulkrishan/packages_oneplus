package com.oneplus.settings.im;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.text.TextUtils;
import android.view.View;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.oneplus.settings.apploader.OPApplicationLoader;
import com.oneplus.settings.better.OPAppModel;
import com.oneplus.settings.quickpay.QuickPayLottieAnimPreference.OnPreferenceViewClickListener;
import com.oneplus.settings.ui.OPViewPagerGuideCategory;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OPQuickReplySettings extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener, OnPreferenceViewClickListener, Indexable {
    public static final String ENABLE_FREEFORM_SUPPORT = "enable_freeform_support";
    private static final String KEY_ENABLE_QUICK_REPLY = "key_enable_quick_reply";
    private static final String KEY_ONEPLUS_NO_SURPPORTED_APPS = "oneplus_no_surpported_apps";
    private static final String KEY_ONEPLUS_SURPPORTED_APPS = "oneplus_surpported_apps";
    private static final String KEY_QUICK_REPLY_INSTRUCTIONS = "key_quick_reply_instructions";
    public static final String OP_QUICKREPLY_IME_ADJUST = "op_quickreply_ime_adjust";
    public static final String OP_QUICKREPLY_IM_LIST = "op_quickreply_im_list";
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_quickreply_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            return new ArrayList();
        }
    };
    private SettingsActivity mActivity;
    private AppOpsManager mAppOpsManager;
    private Context mContext;
    private List<OPAppModel> mDefaultQuickReplyAppList = new ArrayList();
    private SwitchPreference mEnableQuickReply;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (OPQuickReplySettings.this.mAdapter != null && OPQuickReplySettings.this.mOPApplicationLoader != null) {
                OPQuickReplySettings.this.mSupportedApps.removeAll();
                OPQuickReplySettings.this.mDefaultQuickReplyAppList.clear();
                OPQuickReplySettings.this.mDefaultQuickReplyAppList.addAll(OPQuickReplySettings.this.mOPApplicationLoader.getAppListByType(msg.what));
                for (final OPAppModel model : OPQuickReplySettings.this.mDefaultQuickReplyAppList) {
                    SwitchPreference itemPref = new SwitchPreference(OPQuickReplySettings.this.mContext);
                    itemPref.setLayoutResource(R.layout.op_preference_material);
                    itemPref.setWidgetLayoutResource(R.layout.op_preference_widget_switch);
                    String pkgName = model.getPkgName();
                    if (OPConstants.PACKAGE_WECHAT.equals(pkgName) || OPConstants.PACKAGE_WHATSAPP.equals(pkgName) || OPConstants.PACKAGE_INSTAGRAM.equals(pkgName) || OPConstants.PACKAGE_MOBILEQQ.equals(pkgName) || OPConstants.PACKAGE_MESSENGER_LITE.equals(pkgName) || OPConstants.PACKAGE_TENCENT_TIM.equals(pkgName)) {
                        itemPref.setKey(pkgName);
                        itemPref.setTitle((CharSequence) model.getLabel());
                        itemPref.setIcon(model.getAppIcon());
                        itemPref.setChecked(OPUtils.isQuickReplyAppSelected(model));
                        itemPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                            public boolean onPreferenceChange(Preference preference, Object newValue) {
                                boolean enable = ((Boolean) newValue).booleanValue();
                                StringBuilder quickReplyApp = new StringBuilder(OPUtils.getQuickReplyAppListString(OPQuickReplySettings.this.mContext));
                                String replyApp = OPUtils.getQuickReplyAppString(model);
                                if (enable) {
                                    quickReplyApp.append(replyApp);
                                } else {
                                    int index = quickReplyApp.indexOf(replyApp);
                                    quickReplyApp.delete(index, replyApp.length() + index);
                                }
                                if (TextUtils.isEmpty(quickReplyApp)) {
                                    Global.putInt(OPQuickReplySettings.this.getContentResolver(), OPQuickReplySettings.ENABLE_FREEFORM_SUPPORT, 0);
                                } else {
                                    Global.putInt(OPQuickReplySettings.this.getContentResolver(), OPQuickReplySettings.ENABLE_FREEFORM_SUPPORT, 1);
                                }
                                OPUtils.saveQuickReplyAppLisStrings(OPQuickReplySettings.this.mContext, quickReplyApp.toString());
                                OPUtils.sendAppTrackerForQuickReplyIMStatus();
                                return true;
                            }
                        });
                        OPQuickReplySettings.this.mSupportedApps.addPreference(itemPref);
                    }
                }
                if (OPQuickReplySettings.this.mSupportedApps.getPreferenceCount() == 0) {
                    OPQuickReplySettings.this.mSupportedApps.addPreference(OPQuickReplySettings.this.mNoSupportedApps);
                }
            }
        }
    };
    private boolean mHasFingerprint;
    private Preference mNoSupportedApps;
    private OPApplicationLoader mOPApplicationLoader;
    private PackageManager mPackageManager;
    private Preference mQuickLaunchPreferece;
    private OPViewPagerGuideCategory mQuickReplyGuide;
    private PreferenceCategory mSupportedApps;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mActivity = (SettingsActivity) getActivity();
        if (this.mContext != null) {
            this.mPackageManager = this.mContext.getPackageManager();
            this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
            this.mOPApplicationLoader = new OPApplicationLoader(this.mContext, this.mAppOpsManager, this.mPackageManager);
        }
        initPreference();
        initData();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mActivity = (SettingsActivity) getActivity();
    }

    public void onResume() {
        super.onResume();
        updatePreferenceState();
        if (this.mQuickReplyGuide != null) {
            this.mQuickReplyGuide.startAnim();
        }
    }

    private void initData() {
        this.mOPApplicationLoader.setNeedLoadWorkProfileApps(false);
        this.mOPApplicationLoader.initData(4, this.mHandler);
    }

    private void updatePreferenceState() {
        boolean z = false;
        int value = System.getInt(getContentResolver(), OP_QUICKREPLY_IME_ADJUST, 0);
        SwitchPreference switchPreference = this.mEnableQuickReply;
        if (value == 1) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    public void onPause() {
        super.onPause();
        if (this.mQuickReplyGuide != null) {
            this.mQuickReplyGuide.stopAnim();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mQuickReplyGuide != null) {
            this.mQuickReplyGuide.releaseAnim();
        }
    }

    private void initPreference() {
        addPreferencesFromResource(R.xml.op_quickreply_settings);
        this.mSupportedApps = (PreferenceCategory) findPreference(KEY_ONEPLUS_SURPPORTED_APPS);
        this.mNoSupportedApps = findPreference(KEY_ONEPLUS_NO_SURPPORTED_APPS);
        this.mEnableQuickReply = (SwitchPreference) findPreference(KEY_ENABLE_QUICK_REPLY);
        this.mEnableQuickReply.setOnPreferenceChangeListener(this);
        this.mQuickReplyGuide = (OPViewPagerGuideCategory) findPreference(KEY_QUICK_REPLY_INSTRUCTIONS);
        this.mQuickReplyGuide.setType(2);
        this.mQuickReplyGuide.setAnimationWhiteResources(new String[]{"op_quick_reply_guide_light.json"});
        this.mQuickReplyGuide.setAnimationDarkResources(new String[]{"op_quick_reply_guide_dark.json"});
        this.mQuickReplyGuide.setTitleResources(new int[]{R.string.oneplus_quick_reply});
        if (OPUtils.isO2()) {
            this.mQuickReplyGuide.setDescriptionIdResources(new int[]{R.string.oneplus_quick_reply_description_O2});
            return;
        }
        this.mQuickReplyGuide.setDescriptionIdResources(new int[]{R.string.oneplus_quick_reply_description});
    }

    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        if (preference != this.mEnableQuickReply) {
            return false;
        }
        System.putInt(getContentResolver(), OP_QUICKREPLY_IME_ADJUST, ((Boolean) obj).booleanValue());
        OPUtils.sendAppTrackerForQuickReplyKeyboardStatus();
        return true;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    public void onPreferenceViewClick(View view) {
    }
}
