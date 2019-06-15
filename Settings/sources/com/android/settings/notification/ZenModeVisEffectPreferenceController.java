package com.android.settings.notification;

import android.content.Context;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.DisabledCheckBoxPreference;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeVisEffectPreferenceController extends AbstractZenModePreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    protected final int mEffect;
    protected final String mKey;
    protected final int mMetricsCategory;
    protected final int[] mParentSuppressedEffects;
    private PreferenceScreen mScreen;

    public ZenModeVisEffectPreferenceController(Context context, Lifecycle lifecycle, String key, int visualEffect, int metricsCategory, int[] parentSuppressedEffects) {
        super(context, key, lifecycle);
        this.mKey = key;
        this.mEffect = visualEffect;
        this.mMetricsCategory = metricsCategory;
        this.mParentSuppressedEffects = parentSuppressedEffects;
    }

    public String getPreferenceKey() {
        return this.mKey;
    }

    public boolean isAvailable() {
        if (this.mEffect == 8) {
            return this.mContext.getResources().getBoolean(17956988);
        }
        return true;
    }

    public void displayPreference(PreferenceScreen screen) {
        this.mScreen = screen;
        super.displayPreference(screen);
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        boolean suppressed = this.mBackend.isVisualEffectSuppressed(this.mEffect);
        boolean parentSuppressed = false;
        if (this.mParentSuppressedEffects != null) {
            boolean parentSuppressed2 = false;
            for (int parentEffect : this.mParentSuppressedEffects) {
                parentSuppressed2 |= this.mBackend.isVisualEffectSuppressed(parentEffect);
            }
            parentSuppressed = parentSuppressed2;
        }
        if (parentSuppressed) {
            ((CheckBoxPreference) preference).setChecked(parentSuppressed);
            onPreferenceChange(preference, Boolean.valueOf(parentSuppressed));
            ((DisabledCheckBoxPreference) preference).enableCheckbox(false);
            ((DisabledCheckBoxPreference) preference).setEnabled(false);
            return;
        }
        ((DisabledCheckBoxPreference) preference).enableCheckbox(true);
        ((DisabledCheckBoxPreference) preference).setEnabled(true);
        ((CheckBoxPreference) preference).setChecked(suppressed);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean suppressEffect = ((Boolean) newValue).booleanValue();
        this.mMetricsFeatureProvider.action(this.mContext, this.mMetricsCategory, suppressEffect);
        this.mBackend.saveVisualEffectsPolicy(this.mEffect, suppressEffect);
        return true;
    }
}
