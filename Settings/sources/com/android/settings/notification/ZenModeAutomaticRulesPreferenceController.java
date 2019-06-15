package com.android.settings.notification;

import android.app.AutomaticZenRule;
import android.app.Fragment;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.Map.Entry;

public class ZenModeAutomaticRulesPreferenceController extends AbstractZenModeAutomaticRulePreferenceController {
    protected static final String KEY = "zen_mode_automatic_rules";
    @VisibleForTesting
    protected PreferenceCategory mPreferenceCategory;

    public ZenModeAutomaticRulesPreferenceController(Context context, Fragment parent, Lifecycle lifecycle) {
        super(context, KEY, parent, lifecycle);
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public boolean isAvailable() {
        return true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreferenceCategory = (PreferenceCategory) screen.findPreference(getPreferenceKey());
        this.mPreferenceCategory.setPersistent(false);
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        this.mPreferenceCategory.removeAll();
        for (Entry<String, AutomaticZenRule> sortedRule : sortedRules()) {
            this.mPreferenceCategory.addPreference(new ZenRulePreference(this.mPreferenceCategory.getContext(), sortedRule, this.mParent, this.mMetricsFeatureProvider));
        }
    }
}
