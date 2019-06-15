package com.android.settings.notification;

import android.app.NotificationManager.Policy;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeVisEffectsCustomPreferenceController extends AbstractZenModePreferenceController {
    protected static final int INTERRUPTIVE_EFFECTS = 156;
    private final String KEY;
    private ZenCustomRadioButtonPreference mPreference;

    public ZenModeVisEffectsCustomPreferenceController(Context context, Lifecycle lifecycle, String key) {
        super(context, key, lifecycle);
        this.KEY = key;
    }

    public boolean isAvailable() {
        return true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (ZenCustomRadioButtonPreference) screen.findPreference(this.KEY);
        this.mPreference.setOnGearClickListener(new -$$Lambda$ZenModeVisEffectsCustomPreferenceController$hYHNs4-TKsGpjPSCluD3oYAyplI(this));
        this.mPreference.setOnRadioButtonClickListener(new -$$Lambda$ZenModeVisEffectsCustomPreferenceController$anmhCczZGnQRUAoXVehKNMc66b4(this));
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        this.mPreference.setChecked(areCustomOptionsSelected());
    }

    /* Access modifiers changed, original: protected */
    public boolean areCustomOptionsSelected() {
        boolean allEffectsSuppressed = Policy.areAllVisualEffectsSuppressed(this.mBackend.mPolicy.suppressedVisualEffects);
        boolean noEffectsSuppressed = this.mBackend.mPolicy.suppressedVisualEffects == 0;
        if (allEffectsSuppressed || noEffectsSuppressed) {
            return false;
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void select() {
        this.mMetricsFeatureProvider.action(this.mContext, 1399, true);
    }

    private void launchCustomSettings() {
        select();
        new SubSettingLauncher(this.mContext).setDestination(ZenModeBlockedEffectsSettings.class.getName()).setTitle((int) R.string.zen_mode_what_to_block_title).setSourceMetricsCategory(1400).launch();
    }
}
