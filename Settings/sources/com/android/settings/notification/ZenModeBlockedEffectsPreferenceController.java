package com.android.settings.notification;

import android.content.Context;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.notification.ZenModeSettings.SummaryBuilder;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeBlockedEffectsPreferenceController extends AbstractZenModePreferenceController implements PreferenceControllerMixin {
    protected static final String KEY = "zen_mode_block_effects_settings";
    private final SummaryBuilder mSummaryBuilder;

    public ZenModeBlockedEffectsPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY, lifecycle);
        this.mSummaryBuilder = new SummaryBuilder(context);
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public boolean isAvailable() {
        return true;
    }

    public CharSequence getSummary() {
        return this.mSummaryBuilder.getBlockedEffectsSummary(getPolicy());
    }
}
