package com.android.settings.notification;

import android.app.AutomaticZenRule;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.service.notification.ZenModeConfig;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.R;
import com.android.settings.notification.ZenDeleteRuleDialog.PositiveClickListener;
import com.android.settings.utils.ManagedServiceSettings.Config;
import com.android.settings.utils.ZenServiceListing;
import com.android.settingslib.TwoTargetPreference;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import java.util.Map.Entry;

public class ZenRulePreference extends TwoTargetPreference {
    private static final Config CONFIG = ZenModeAutomationSettings.getConditionProviderConfig();
    boolean appExists;
    final ZenModeBackend mBackend;
    final Context mContext;
    private final OnClickListener mDeleteListener = new OnClickListener() {
        public void onClick(View v) {
            ZenRulePreference.this.showDeleteRuleDialog(ZenRulePreference.this.mParent, ZenRulePreference.this.mId, ZenRulePreference.this.mName.toString());
        }
    };
    final String mId;
    final MetricsFeatureProvider mMetricsFeatureProvider;
    final CharSequence mName;
    final Fragment mParent;
    final PackageManager mPm;
    final Preference mPref;
    final ZenServiceListing mServiceListing;

    public ZenRulePreference(Context context, Entry<String, AutomaticZenRule> ruleEntry, Fragment parent, MetricsFeatureProvider metricsProvider) {
        super(context);
        this.mBackend = ZenModeBackend.getInstance(context);
        this.mContext = context;
        AutomaticZenRule rule = (AutomaticZenRule) ruleEntry.getValue();
        this.mName = rule.getName();
        this.mId = (String) ruleEntry.getKey();
        this.mParent = parent;
        this.mPm = this.mContext.getPackageManager();
        this.mServiceListing = new ZenServiceListing(this.mContext, CONFIG);
        this.mServiceListing.reloadApprovedServices();
        this.mPref = this;
        this.mMetricsFeatureProvider = metricsProvider;
        setAttributes(rule);
        setLayoutResource(R.layout.op_preference_two_target);
    }

    /* Access modifiers changed, original: protected */
    public int getSecondTargetResId() {
        if (this.mId == null || !ZenModeConfig.DEFAULT_RULE_IDS.contains(this.mId)) {
            return R.layout.zen_rule_widget;
        }
        return 0;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        View v = view.findViewById(16908312);
        if (v != null) {
            v.setOnClickListener(this.mDeleteListener);
        }
    }

    private void showDeleteRuleDialog(Fragment parent, String ruleId, String ruleName) {
        ZenDeleteRuleDialog.show(parent, ruleName, ruleId, new PositiveClickListener() {
            public void onOk(String id) {
                ZenRulePreference.this.mMetricsFeatureProvider.action(ZenRulePreference.this.mContext, 175, new Pair[0]);
                ZenRulePreference.this.mBackend.removeZenRule(id);
            }
        });
    }

    /* Access modifiers changed, original: protected */
    public void setAttributes(AutomaticZenRule rule) {
        boolean isSchedule = ZenModeConfig.isValidScheduleConditionId(rule.getConditionId());
        boolean isEvent = ZenModeConfig.isValidEventConditionId(rule.getConditionId());
        boolean z = true;
        boolean isSystemRule = isSchedule || isEvent;
        try {
            setSummary((CharSequence) computeRuleSummary(rule, isSystemRule, this.mPm.getApplicationInfo(rule.getOwner().getPackageName(), 0).loadLabel(this.mPm)));
            this.appExists = true;
            setTitle((CharSequence) rule.getName());
            setPersistent(false);
            String action = isSchedule ? ZenModeScheduleRuleSettings.ACTION : isEvent ? ZenModeEventRuleSettings.ACTION : "";
            ComponentName settingsActivity = AbstractZenModeAutomaticRulePreferenceController.getSettingsActivity(this.mServiceListing.findService(rule.getOwner()));
            setIntent(AbstractZenModeAutomaticRulePreferenceController.getRuleIntent(action, settingsActivity, this.mId));
            if (settingsActivity == null && !isSystemRule) {
                z = false;
            }
            setSelectable(z);
            setKey(this.mId);
        } catch (NameNotFoundException e) {
            this.appExists = false;
        }
    }

    private String computeRuleSummary(AutomaticZenRule rule, boolean isSystemRule, CharSequence providerLabel) {
        if (rule == null || !rule.isEnabled()) {
            return this.mContext.getResources().getString(R.string.switch_off_text);
        }
        return this.mContext.getResources().getString(R.string.switch_on_text);
    }
}
