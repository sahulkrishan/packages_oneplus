package com.android.settings.notification;

import android.app.AutomaticZenRule;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.service.notification.ZenModeConfig;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.util.Pair;
import android.util.Slog;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.notification.ZenRuleNameDialog.PositiveClickListener;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenAutomaticRuleHeaderPreferenceController extends AbstractZenModePreferenceController implements PreferenceControllerMixin {
    private final String KEY = EntityHeaderController.PREF_KEY_APP_HEADER;
    private EntityHeaderController mController;
    private final PreferenceFragment mFragment;
    private String mId;
    private AutomaticZenRule mRule;

    public class RuleNameChangeListener implements PositiveClickListener {
        public void onOk(String ruleName, Fragment parent) {
            ZenAutomaticRuleHeaderPreferenceController.this.mMetricsFeatureProvider.action(ZenAutomaticRuleHeaderPreferenceController.this.mContext, 1267, new Pair[0]);
            ZenAutomaticRuleHeaderPreferenceController.this.mRule.setName(ruleName);
            ZenAutomaticRuleHeaderPreferenceController.this.mBackend.setZenRule(ZenAutomaticRuleHeaderPreferenceController.this.mId, ZenAutomaticRuleHeaderPreferenceController.this.mRule);
        }
    }

    public ZenAutomaticRuleHeaderPreferenceController(Context context, PreferenceFragment fragment, Lifecycle lifecycle) {
        super(context, EntityHeaderController.PREF_KEY_APP_HEADER, lifecycle);
        this.mFragment = fragment;
    }

    public String getPreferenceKey() {
        return EntityHeaderController.PREF_KEY_APP_HEADER;
    }

    public boolean isAvailable() {
        return this.mRule != null;
    }

    public void updateState(Preference preference) {
        if (!(this.mRule == null || this.mFragment == null)) {
            LayoutPreference pref = (LayoutPreference) preference;
            if (this.mController == null) {
                this.mController = EntityHeaderController.newInstance(this.mFragment.getActivity(), this.mFragment, pref.findViewById(R.id.entity_header));
                this.mController.setEditZenRuleNameListener(new OnClickListener() {
                    public void onClick(View v) {
                        ZenRuleNameDialog.show(ZenAutomaticRuleHeaderPreferenceController.this.mFragment, ZenAutomaticRuleHeaderPreferenceController.this.mRule.getName(), null, new RuleNameChangeListener());
                    }
                });
            }
            this.mController.setIcon(getIcon()).setLabel(this.mRule.getName()).setPackageName(this.mRule.getOwner().getPackageName()).setUid(this.mContext.getUserId()).setHasAppInfoLink(false).setButtonActions(2, 0).done(this.mFragment.getActivity(), this.mContext).findViewById(R.id.entity_header).setVisibility(0);
        }
    }

    private Drawable getIcon() {
        try {
            PackageManager packageManager = this.mContext.getPackageManager();
            ApplicationInfo info = packageManager.getApplicationInfo(this.mRule.getOwner().getPackageName(), 0);
            if (info.isSystemApp()) {
                if (ZenModeConfig.isValidScheduleConditionId(this.mRule.getConditionId())) {
                    return this.mContext.getDrawable(R.drawable.ic_timelapse);
                }
                if (ZenModeConfig.isValidEventConditionId(this.mRule.getConditionId())) {
                    return this.mContext.getDrawable(R.drawable.ic_event);
                }
            }
            return info.loadIcon(packageManager);
        } catch (NameNotFoundException e) {
            Slog.w(PreferenceControllerMixin.TAG, "Unable to load icon - PackageManager.NameNotFoundException");
            return null;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onResume(AutomaticZenRule rule, String id) {
        this.mRule = rule;
        this.mId = id;
    }
}
