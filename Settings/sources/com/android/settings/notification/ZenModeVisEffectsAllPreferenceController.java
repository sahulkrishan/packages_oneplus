package com.android.settings.notification;

import android.app.NotificationManager.Policy;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.notification.ZenCustomRadioButtonPreference.OnRadioButtonClickListener;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeVisEffectsAllPreferenceController extends AbstractZenModePreferenceController implements OnRadioButtonClickListener {
    protected static final int EFFECTS = 511;
    private final String KEY;
    private ZenCustomRadioButtonPreference mPreference;

    public ZenModeVisEffectsAllPreferenceController(Context context, Lifecycle lifecycle, String key) {
        super(context, key, lifecycle);
        this.KEY = key;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (ZenCustomRadioButtonPreference) screen.findPreference(this.KEY);
        this.mPreference.setOnRadioButtonClickListener(this);
    }

    public boolean isAvailable() {
        return true;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        this.mPreference.setChecked(Policy.areAllVisualEffectsSuppressed(this.mBackend.mPolicy.suppressedVisualEffects));
    }

    public void onRadioButtonClick(ZenCustomRadioButtonPreference p) {
        this.mMetricsFeatureProvider.action(this.mContext, 1397, true);
        this.mBackend.saveVisualEffectsPolicy(511, true);
    }
}
