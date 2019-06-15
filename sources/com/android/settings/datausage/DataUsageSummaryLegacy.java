package com.android.settings.datausage;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkPolicyManager;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.SubscriptionInfo;
import android.text.BidiFormatter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settings.SummaryPreference;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.datausage.BillingCycleSettings.BytesEditorFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.NetworkPolicyEditor;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.net.DataUsageController;
import com.android.settingslib.net.DataUsageController.DataUsageInfo;
import java.util.ArrayList;
import java.util.List;

public class DataUsageSummaryLegacy extends DataUsageBaseFragment implements Indexable, DataUsageEditController {
    private static final String KEY_LIMIT_SUMMARY = "limit_summary";
    public static final String KEY_MOBILE_BILLING_CYCLE = "billing_preference";
    public static final String KEY_MOBILE_DATA_USAGE = "cellular_data_usage";
    public static final String KEY_MOBILE_DATA_USAGE_TOGGLE = "data_usage_enable";
    public static final String KEY_MOBILE_USAGE_TITLE = "mobile_category";
    public static final String KEY_RESTRICT_BACKGROUND = "restrict_background_legacy";
    private static final String KEY_STATUS_HEADER = "status_header";
    public static final String KEY_WIFI_DATA_USAGE = "wifi_data_usage";
    public static final String KEY_WIFI_USAGE_TITLE = "wifi_category";
    static final boolean LOGD = false;
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> resources = new ArrayList();
            SearchIndexableResource resource = new SearchIndexableResource(context);
            resource.xmlResId = R.xml.data_usage_legacy;
            resources.add(resource);
            resource = new SearchIndexableResource(context);
            resource.xmlResId = R.xml.data_usage_cellular;
            resources.add(resource);
            resource = new SearchIndexableResource(context);
            resource.xmlResId = R.xml.data_usage_wifi;
            resources.add(resource);
            return resources;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            if (!DataUsageUtils.hasMobileData(context)) {
                keys.add("mobile_category");
                keys.add("data_usage_enable");
                keys.add("cellular_data_usage");
                keys.add("billing_preference");
            }
            if (!DataUsageUtils.hasWifiRadio(context)) {
                keys.add("wifi_data_usage");
            }
            keys.add("wifi_category");
            return keys;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = -$$Lambda$7QiUIfMd3seAu_emb68cbM9H0Io.INSTANCE;
    private static final String TAG = "DataUsageSummaryLegacy";
    private DataUsageInfoController mDataInfoController;
    private DataUsageController mDataUsageController;
    private int mDataUsageTemplate;
    private NetworkTemplate mDefaultTemplate;
    private Preference mLimitPreference;
    private NetworkPolicyEditor mPolicyEditor;
    private SummaryPreference mSummaryPreference;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Activity mActivity;
        private final DataUsageController mDataController;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            this.mActivity = activity;
            this.mSummaryLoader = summaryLoader;
            this.mDataController = new DataUsageController(activity);
        }

        public void setListening(boolean listening) {
            if (listening) {
                String used;
                DataUsageInfo info = this.mDataController.getDataUsageInfo();
                if (info == null) {
                    used = Formatter.formatFileSize(this.mActivity, 0);
                } else if (info.limitLevel <= 0) {
                    used = Formatter.formatFileSize(this.mActivity, info.usageLevel);
                } else {
                    used = Utils.formatPercentage(info.usageLevel, info.limitLevel);
                }
                this.mSummaryLoader.setSummary(this, this.mActivity.getString(R.string.data_usage_summary_format, new Object[]{used}));
            }
        }
    }

    public int getHelpResource() {
        return R.string.help_url_data_usage;
    }

    public void onCreate(Bundle icicle) {
        int i;
        super.onCreate(icicle);
        Context context = getContext();
        this.mPolicyEditor = new NetworkPolicyEditor(NetworkPolicyManager.from(context));
        boolean hasMobileData = DataUsageUtils.hasMobileData(context);
        this.mDataUsageController = new DataUsageController(context);
        this.mDataInfoController = new DataUsageInfoController();
        int defaultSubId = DataUsageUtils.getDefaultSubscriptionId(context);
        if (defaultSubId == -1) {
            hasMobileData = false;
        }
        this.mDefaultTemplate = DataUsageUtils.getDefaultTemplate(context, defaultSubId);
        this.mSummaryPreference = (SummaryPreference) findPreference(KEY_STATUS_HEADER);
        if (!(hasMobileData && isAdmin())) {
            removePreference(KEY_RESTRICT_BACKGROUND);
        }
        int i2 = 0;
        if (hasMobileData) {
            this.mLimitPreference = findPreference(KEY_LIMIT_SUMMARY);
            List<SubscriptionInfo> subscriptions = this.services.mSubscriptionManager.getActiveSubscriptionInfoList();
            if (subscriptions == null || subscriptions.size() == 0) {
                addMobileSection(defaultSubId);
            }
            while (subscriptions != null && i2 < subscriptions.size()) {
                SubscriptionInfo subInfo = (SubscriptionInfo) subscriptions.get(i2);
                if (subscriptions.size() > 1) {
                    addMobileSection(subInfo.getSubscriptionId(), subInfo);
                } else {
                    addMobileSection(subInfo.getSubscriptionId());
                }
                i2++;
            }
            this.mSummaryPreference.setSelectable(true);
        } else {
            removePreference(KEY_LIMIT_SUMMARY);
            this.mSummaryPreference.setSelectable(false);
        }
        boolean hasWifiRadio = DataUsageUtils.hasWifiRadio(context);
        if (hasWifiRadio) {
            addWifiSection();
        }
        if (hasEthernet(context)) {
            addEthernetSection();
        }
        if (hasMobileData) {
            i = R.string.cell_data_template;
        } else if (hasWifiRadio) {
            i = R.string.wifi_data_template;
        } else {
            i = R.string.ethernet_data_template;
        }
        this.mDataUsageTemplate = i;
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (UserManager.get(getContext()).isAdminUser()) {
            inflater.inflate(R.menu.data_usage, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.data_usage_menu_cellular_networks) {
            return false;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        if (com.android.settings.Utils.isNetworkSettingsApkAvailable()) {
            intent.setComponent(new ComponentName("com.qualcomm.qti.networksetting", "com.qualcomm.qti.networksetting.MobileNetworkSettings"));
        } else {
            Log.d(TAG, "vendor MobileNetworkSettings is not available");
            intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.MobileNetworkSettings"));
        }
        startActivity(intent);
        return true;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference != findPreference(KEY_STATUS_HEADER)) {
            return super.onPreferenceTreeClick(preference);
        }
        BytesEditorFragment.show(this, false);
        return false;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.data_usage_legacy;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return null;
    }

    private void addMobileSection(int subId) {
        addMobileSection(subId, null);
    }

    private void addMobileSection(int subId, SubscriptionInfo subInfo) {
        TemplatePreferenceCategory category = (TemplatePreferenceCategory) inflatePreferences(R.xml.data_usage_cellular);
        category.setTemplate(getNetworkTemplate(subId), subId, this.services);
        category.pushTemplates(this.services);
        if (subInfo != null && !TextUtils.isEmpty(subInfo.getDisplayName())) {
            category.findPreference("mobile_category").setTitle(subInfo.getDisplayName());
        }
    }

    private void addWifiSection() {
        ((TemplatePreferenceCategory) inflatePreferences(R.xml.data_usage_wifi)).setTemplate(NetworkTemplate.buildTemplateWifiWildcard(), 0, this.services);
    }

    private void addEthernetSection() {
        ((TemplatePreferenceCategory) inflatePreferences(R.xml.data_usage_ethernet)).setTemplate(NetworkTemplate.buildTemplateEthernet(), 0, this.services);
    }

    private Preference inflatePreferences(int resId) {
        PreferenceScreen rootPreferences = getPreferenceManager().inflateFromResource(getPrefContext(), resId, null);
        Preference pref = rootPreferences.getPreference(null);
        rootPreferences.removeAll();
        PreferenceScreen screen = getPreferenceScreen();
        pref.setOrder(screen.getPreferenceCount());
        screen.addPreference(pref);
        return pref;
    }

    private NetworkTemplate getNetworkTemplate(int subscriptionId) {
        return NetworkTemplate.normalize(NetworkTemplate.buildTemplateMobileAll(this.services.mTelephonyManager.getSubscriberId(subscriptionId)), this.services.mTelephonyManager.getMergedSubscriberIds());
    }

    public void onResume() {
        super.onResume();
        updateState();
    }

    @VisibleForTesting
    static CharSequence formatUsage(Context context, String template, long usageLevel) {
        SpannableString enlargedValue = new SpannableString(Formatter.formatBytes(context.getResources(), usageLevel, 2).value);
        enlargedValue.setSpan(new RelativeSizeSpan(1.5625f), 0, enlargedValue.length(), 18);
        CharSequence formattedUsage = TextUtils.expandTemplate(new SpannableString(context.getString(17039924).replace("%1$s", "^1").replace("%2$s", "^2")), new CharSequence[]{enlargedValue, usedResult.units});
        SpannableString fullTemplate = new SpannableString(template);
        fullTemplate.setSpan(new RelativeSizeSpan(0.64f), 0, fullTemplate.length(), 18);
        return TextUtils.expandTemplate(fullTemplate, new CharSequence[]{BidiFormatter.getInstance().unicodeWrap(formattedUsage.toString())});
    }

    private void updateState() {
        DataUsageInfo info = this.mDataUsageController.getDataUsageInfo(this.mDefaultTemplate);
        Context context = getContext();
        this.mDataInfoController.updateDataLimit(info, this.services.mPolicyEditor.getPolicy(this.mDefaultTemplate));
        int i = 1;
        if (this.mSummaryPreference != null) {
            this.mSummaryPreference.setTitle(formatUsage(context, getString(this.mDataUsageTemplate), info.usageLevel));
            long limit = this.mDataInfoController.getSummaryLimit(info);
            this.mSummaryPreference.setSummary((CharSequence) info.period);
            if (limit <= 0) {
                this.mSummaryPreference.setChartEnabled(false);
            } else {
                this.mSummaryPreference.setChartEnabled(true);
                this.mSummaryPreference.setLabels(Formatter.formatFileSize(context, 0), Formatter.formatFileSize(context, limit));
                this.mSummaryPreference.setRatios(((float) info.usageLevel) / ((float) limit), 0.0f, ((float) (limit - info.usageLevel)) / ((float) limit));
            }
        }
        if (this.mLimitPreference != null && (info.warningLevel > 0 || info.limitLevel > 0)) {
            int i2;
            String warning = Formatter.formatFileSize(context, info.warningLevel);
            String limit2 = Formatter.formatFileSize(context, info.limitLevel);
            Preference preference = this.mLimitPreference;
            if (info.limitLevel <= 0) {
                i2 = R.string.cell_warning_only;
            } else {
                i2 = R.string.cell_warning_and_limit;
            }
            preference.setSummary(getString(i2, new Object[]{warning, limit2}));
        } else if (this.mLimitPreference != null) {
            this.mLimitPreference.setSummary(null);
        }
        PreferenceScreen screen = getPreferenceScreen();
        while (true) {
            int i3 = i;
            if (i3 < screen.getPreferenceCount()) {
                ((TemplatePreferenceCategory) screen.getPreference(i3)).pushTemplates(this.services);
                i = i3 + 1;
            } else {
                return;
            }
        }
    }

    public int getMetricsCategory() {
        return 37;
    }

    public NetworkPolicyEditor getNetworkPolicyEditor() {
        return this.services.mPolicyEditor;
    }

    public NetworkTemplate getNetworkTemplate() {
        return this.mDefaultTemplate;
    }

    public void updateDataUsage() {
        updateState();
    }
}
