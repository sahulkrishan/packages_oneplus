package com.android.settings.notification;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.notification.ZenModeSettings.SummaryBuilder;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeBehaviorCallsPreferenceController extends AbstractZenModePreferenceController implements PreferenceControllerMixin {
    protected static final String KEY_BEHAVIOR_SETTINGS = "zen_mode_calls_settings";
    private final SummaryBuilder mSummaryBuilder;

    public ZenModeBehaviorCallsPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY_BEHAVIOR_SETTINGS, lifecycle);
        this.mSummaryBuilder = new SummaryBuilder(context);
    }

    public String getPreferenceKey() {
        return KEY_BEHAVIOR_SETTINGS;
    }

    public boolean isAvailable() {
        return true;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setSummary(this.mSummaryBuilder.getCallsSettingSummary(getPolicy()));
    }
}