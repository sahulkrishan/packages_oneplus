package com.android.settings.notification;

import android.app.AutomaticZenRule;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.service.notification.ZenModeConfig;
import android.support.v7.preference.Preference;
import android.util.Pair;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.notification.ZenRuleNameDialog.PositiveClickListener;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.oneplus.settings.SettingsBaseApplication;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public abstract class AbstractZenModeAutomaticRulePreferenceController extends AbstractZenModePreferenceController implements PreferenceControllerMixin {
    private static final Comparator<Entry<String, AutomaticZenRule>> RULE_COMPARATOR = new Comparator<Entry<String, AutomaticZenRule>>() {
        public int compare(Entry<String, AutomaticZenRule> lhs, Entry<String, AutomaticZenRule> rhs) {
            boolean lhsIsDefaultRule = AbstractZenModeAutomaticRulePreferenceController.getDefaultRuleIds().contains(lhs.getKey());
            if (lhsIsDefaultRule != AbstractZenModeAutomaticRulePreferenceController.getDefaultRuleIds().contains(rhs.getKey())) {
                return lhsIsDefaultRule ? -1 : 1;
            }
            int byDate = Long.compare(((AutomaticZenRule) lhs.getValue()).getCreationTime(), ((AutomaticZenRule) rhs.getValue()).getCreationTime());
            if (byDate != 0) {
                return byDate;
            }
            return key((AutomaticZenRule) lhs.getValue()).compareTo(key((AutomaticZenRule) rhs.getValue()));
        }

        private String key(AutomaticZenRule rule) {
            int type = ZenModeConfig.isValidScheduleConditionId(rule.getConditionId()) ? 1 : ZenModeConfig.isValidEventConditionId(rule.getConditionId()) ? 2 : 3;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(type);
            stringBuilder.append(rule.getName().toString());
            return stringBuilder.toString();
        }
    };
    private static List<String> mDefaultRuleIds;
    protected ZenModeBackend mBackend;
    protected Fragment mParent;
    protected PackageManager mPm = this.mContext.getPackageManager();
    protected Set<Entry<String, AutomaticZenRule>> mRules;

    public class RuleNameChangeListener implements PositiveClickListener {
        ZenRuleInfo mRuleInfo;

        public RuleNameChangeListener(ZenRuleInfo ruleInfo) {
            this.mRuleInfo = ruleInfo;
        }

        public void onOk(String ruleName, Fragment parent) {
            AbstractZenModeAutomaticRulePreferenceController.this.mMetricsFeatureProvider.action(AbstractZenModeAutomaticRulePreferenceController.this.mContext, 1267, new Pair[0]);
            String savedRuleId = AbstractZenModeAutomaticRulePreferenceController.this.mBackend.addZenRule(new AutomaticZenRule(ruleName, this.mRuleInfo.serviceComponent, this.mRuleInfo.defaultConditionId, 2, true));
            if (savedRuleId != null) {
                parent.startActivity(AbstractZenModeAutomaticRulePreferenceController.getRuleIntent(this.mRuleInfo.settingsAction, null, savedRuleId));
            }
        }
    }

    public AbstractZenModeAutomaticRulePreferenceController(Context context, String key, Fragment parent, Lifecycle lifecycle) {
        super(context, key, lifecycle);
        this.mBackend = ZenModeBackend.getInstance(context);
        this.mParent = parent;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        this.mRules = getZenModeRules();
    }

    private static List<String> getDefaultRuleIds() {
        if (mDefaultRuleIds == null) {
            mDefaultRuleIds = ZenModeConfig.DEFAULT_RULE_IDS;
        }
        return mDefaultRuleIds;
    }

    private Set<Entry<String, AutomaticZenRule>> getZenModeRules() {
        return NotificationManager.from(SettingsBaseApplication.mApplication.getApplicationContext()).getAutomaticZenRules().entrySet();
    }

    /* Access modifiers changed, original: protected */
    public void showNameRuleDialog(ZenRuleInfo ri, Fragment parent) {
        ZenRuleNameDialog.show(parent, null, ri.defaultConditionId, new RuleNameChangeListener(ri));
    }

    /* Access modifiers changed, original: protected */
    public Entry<String, AutomaticZenRule>[] sortedRules() {
        if (this.mRules == null) {
            this.mRules = getZenModeRules();
        }
        Entry[] rt = (Entry[]) this.mRules.toArray(new Entry[this.mRules.size()]);
        Arrays.sort(rt, RULE_COMPARATOR);
        return rt;
    }

    protected static Intent getRuleIntent(String settingsAction, ComponentName configurationActivity, String ruleId) {
        Intent intent = new Intent().addFlags(67108864).putExtra("android.service.notification.extra.RULE_ID", ruleId);
        if (configurationActivity != null) {
            intent.setComponent(configurationActivity);
        } else {
            intent.setAction(settingsAction);
        }
        return intent;
    }

    public static ZenRuleInfo getRuleInfo(PackageManager pm, ServiceInfo si) {
        if (si == null || si.metaData == null) {
            return null;
        }
        String ruleType = si.metaData.getString("android.service.zen.automatic.ruleType");
        ComponentName configurationActivity = getSettingsActivity(si);
        if (ruleType == null || ruleType.trim().isEmpty() || configurationActivity == null) {
            return null;
        }
        ZenRuleInfo ri = new ZenRuleInfo();
        ri.serviceComponent = new ComponentName(si.packageName, si.name);
        ri.settingsAction = "android.settings.ZEN_MODE_EXTERNAL_RULE_SETTINGS";
        ri.title = ruleType;
        ri.packageName = si.packageName;
        ri.configurationActivity = getSettingsActivity(si);
        ri.packageLabel = si.applicationInfo.loadLabel(pm);
        ri.ruleInstanceLimit = si.metaData.getInt("android.service.zen.automatic.ruleInstanceLimit", -1);
        return ri;
    }

    protected static ComponentName getSettingsActivity(ServiceInfo si) {
        if (si == null || si.metaData == null) {
            return null;
        }
        String configurationActivity = si.metaData.getString("android.service.zen.automatic.configurationActivity");
        if (configurationActivity != null) {
            return ComponentName.unflattenFromString(configurationActivity);
        }
        return null;
    }
}
