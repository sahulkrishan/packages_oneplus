package com.android.settings.datausage;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionPlan;
import android.text.BidiFormatter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.MenuItem;
import com.android.settings.R;
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

public class DataUsageSummary extends DataUsageBaseFragment implements Indexable, DataUsageEditController {
    public static final String KEY_MOBILE_BILLING_CYCLE = "billing_preference";
    public static final String KEY_MOBILE_DATA_USAGE = "cellular_data_usage";
    public static final String KEY_MOBILE_DATA_USAGE_TOGGLE = "data_usage_enable";
    public static final String KEY_MOBILE_USAGE_TITLE = "mobile_category";
    public static final String KEY_RESTRICT_BACKGROUND = "restrict_background";
    private static final String KEY_STATUS_HEADER = "status_header";
    public static final String KEY_WIFI_DATA_USAGE = "wifi_data_usage";
    public static final String KEY_WIFI_USAGE_TITLE = "wifi_category";
    static final boolean LOGD = false;
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> resources = new ArrayList();
            SearchIndexableResource resource = new SearchIndexableResource(context);
            resource.xmlResId = R.xml.data_usage;
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
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = -$$Lambda$YwlDb-ChrdnT61OB-L_A63UT4To.INSTANCE;
    private static final String TAG = "DataUsageSummary";
    private NetworkTemplate mDefaultTemplate;
    private DataUsageSummaryPreferenceController mSummaryController;
    private DataUsageSummaryPreference mSummaryPreference;

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
            if (!listening) {
                return;
            }
            if (DataUsageUtils.hasSim(this.mActivity)) {
                this.mSummaryLoader.setSummary(this, this.mActivity.getString(R.string.data_usage_summary_format, new Object[]{formatUsedData()}));
                return;
            }
            DataUsageInfo info = this.mDataController.getDataUsageInfo(NetworkTemplate.buildTemplateWifiWildcard());
            if (info == null) {
                this.mSummaryLoader.setSummary(this, null);
                return;
            }
            CharSequence wifiFormat = this.mActivity.getText(R.string.data_usage_wifi_format);
            CharSequence sizeText = DataUsageUtils.formatDataUsage(this.mActivity, info.usageLevel);
            this.mSummaryLoader.setSummary(this, TextUtils.expandTemplate(wifiFormat, new CharSequence[]{sizeText}));
        }

        private CharSequence formatUsedData() {
            SubscriptionManager subscriptionManager = (SubscriptionManager) this.mActivity.getSystemService("telephony_subscription_service");
            int defaultSubId = SubscriptionManager.getDefaultSubscriptionId();
            if (defaultSubId == -1) {
                return formatFallbackData();
            }
            SubscriptionPlan dfltPlan = DataUsageSummaryPreferenceController.getPrimaryPlan(subscriptionManager, defaultSubId);
            if (dfltPlan == null) {
                return formatFallbackData();
            }
            if (DataUsageSummaryPreferenceController.unlimited(dfltPlan.getDataLimitBytes())) {
                return DataUsageUtils.formatDataUsage(this.mActivity, dfltPlan.getDataUsageBytes());
            }
            return Utils.formatPercentage(dfltPlan.getDataUsageBytes(), dfltPlan.getDataLimitBytes());
        }

        private CharSequence formatFallbackData() {
            DataUsageInfo info = this.mDataController.getDataUsageInfo();
            if (info == null) {
                return DataUsageUtils.formatDataUsage(this.mActivity, 0);
            }
            if (info.limitLevel <= 0) {
                return DataUsageUtils.formatDataUsage(this.mActivity, info.usageLevel);
            }
            return Utils.formatPercentage(info.usageLevel, info.limitLevel);
        }
    }

    public int getHelpResource() {
        return R.string.help_url_data_usage;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Context context = getContext();
        boolean hasMobileData = DataUsageUtils.hasMobileData(context);
        int defaultSubId = DataUsageUtils.getDefaultSubscriptionId(context);
        if (defaultSubId == -1) {
            hasMobileData = false;
        }
        this.mDefaultTemplate = DataUsageUtils.getDefaultTemplate(context, defaultSubId);
        this.mSummaryPreference = (DataUsageSummaryPreference) findPreference(KEY_STATUS_HEADER);
        if (!(hasMobileData && isAdmin())) {
            removePreference(KEY_RESTRICT_BACKGROUND);
        }
        boolean hasWifiRadio = DataUsageUtils.hasWifiRadio(context);
        if (hasMobileData) {
            List<SubscriptionInfo> subscriptions = this.services.mSubscriptionManager.getActiveSubscriptionInfoList();
            if (subscriptions == null || subscriptions.size() == 0) {
                addMobileSection(defaultSubId);
            }
            int i = 0;
            while (subscriptions != null && i < subscriptions.size()) {
                SubscriptionInfo subInfo = (SubscriptionInfo) subscriptions.get(i);
                if (subscriptions.size() > 1) {
                    addMobileSection(subInfo.getSubscriptionId(), subInfo);
                } else {
                    addMobileSection(subInfo.getSubscriptionId());
                }
                i++;
            }
            if (DataUsageUtils.hasSim(context) && hasWifiRadio) {
                addWifiSection();
            }
        } else if (hasWifiRadio) {
            addWifiSection();
        }
        if (DataUsageUtils.hasEthernet(context)) {
            addEthernetSection();
        }
        setHasOptionsMenu(true);
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
        return R.xml.data_usage;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        Activity activity = getActivity();
        ArrayList<AbstractPreferenceController> controllers = new ArrayList();
        this.mSummaryController = new DataUsageSummaryPreferenceController(activity, getLifecycle(), this);
        controllers.add(this.mSummaryController);
        getLifecycle().addObserver(this.mSummaryController);
        return controllers;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void addMobileSection(int subId) {
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

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void addWifiSection() {
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
        return formatUsage(context, template, usageLevel, 1.5625f, 0.64f);
    }

    static CharSequence formatUsage(Context context, String template, long usageLevel, float larger, float smaller) {
        SpannableString enlargedValue = new SpannableString(Formatter.formatBytes(context.getResources(), usageLevel, 10).value);
        enlargedValue.setSpan(new RelativeSizeSpan(larger), 0, enlargedValue.length(), 18);
        CharSequence formattedUsage = TextUtils.expandTemplate(new SpannableString(context.getString(17039919).replace("%1$s", "^1").replace("%2$s", "^2")), new CharSequence[]{enlargedValue, usedResult.units});
        SpannableString fullTemplate = new SpannableString(template);
        fullTemplate.setSpan(new RelativeSizeSpan(smaller), 0, fullTemplate.length(), 18);
        return TextUtils.expandTemplate(fullTemplate, new CharSequence[]{BidiFormatter.getInstance().unicodeWrap(formattedUsage.toString())});
    }

    private void updateState() {
        PreferenceScreen screen = getPreferenceScreen();
        for (int i = 1; i < screen.getPreferenceCount(); i++) {
            Preference currentPreference = screen.getPreference(i);
            if (currentPreference instanceof TemplatePreferenceCategory) {
                ((TemplatePreferenceCategory) currentPreference).pushTemplates(this.services);
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
        this.mSummaryController.updateState(this.mSummaryPreference);
    }
}
