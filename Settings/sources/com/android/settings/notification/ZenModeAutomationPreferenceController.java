package com.android.settings.notification;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.notification.ZenModeSettings.SummaryBuilder;
import com.android.settingslib.core.AbstractPreferenceController;

public class ZenModeAutomationPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    protected static final String KEY_ZEN_MODE_AUTOMATION = "zen_mode_automation_settings";
    private final SummaryBuilder mSummaryBuilder;

    public ZenModeAutomationPreferenceController(Context context) {
        super(context);
        this.mSummaryBuilder = new SummaryBuilder(context);
    }

    public String getPreferenceKey() {
        return KEY_ZEN_MODE_AUTOMATION;
    }

    public boolean isAvailable() {
        return true;
    }

    public void updateState(Preference preference) {
        preference.setSummary(this.mSummaryBuilder.getAutomaticRulesSummary());
    }
}
