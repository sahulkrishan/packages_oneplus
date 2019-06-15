package com.android.settings.accessibility;

import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.widget.Switch;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;

public class ToggleDaltonizerPreferenceFragment extends ToggleFeaturePreferenceFragment implements OnPreferenceChangeListener, OnSwitchChangeListener {
    private static final int DEFAULT_TYPE = 12;
    private static final String ENABLED = "accessibility_display_daltonizer_enabled";
    private static final String TYPE = "accessibility_display_daltonizer";
    private ListPreference mType;

    public int getMetricsCategory() {
        return 5;
    }

    public int getHelpResource() {
        return R.string.help_url_color_correction;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mType = (ListPreference) findPreference("type");
        if (!AccessibilitySettings.isColorTransformAccelerated(getActivity())) {
            this.mFooterPreferenceMixin.createFooterPreference().setTitle((int) R.string.accessibility_display_daltonizer_preference_subtitle);
        }
        initPreferences();
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.accessibility_daltonizer_settings;
    }

    /* Access modifiers changed, original: protected */
    public void onPreferenceToggled(String preferenceKey, boolean enabled) {
        Secure.putInt(getContentResolver(), ENABLED, enabled);
        if (enabled) {
            Toast.makeText(getPrefContext(), R.string.oneplus_screen_features_not_available_toast, 1).show();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mType) {
            Secure.putInt(getContentResolver(), TYPE, Integer.parseInt((String) newValue));
            preference.setSummary((CharSequence) "%s");
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void onInstallSwitchBarToggleSwitch() {
        super.onInstallSwitchBarToggleSwitch();
        SwitchBar switchBar = this.mSwitchBar;
        boolean z = true;
        if (Secure.getInt(getContentResolver(), ENABLED, 0) != 1) {
            z = false;
        }
        switchBar.setCheckedInternal(z);
        this.mSwitchBar.addOnSwitchChangeListener(this);
    }

    /* Access modifiers changed, original: protected */
    public void onRemoveSwitchBarToggleSwitch() {
        super.onRemoveSwitchBarToggleSwitch();
        this.mSwitchBar.removeOnSwitchChangeListener(this);
    }

    /* Access modifiers changed, original: protected */
    public void updateSwitchBarText(SwitchBar switchBar) {
        switchBar.setSwitchBarText(R.string.accessibility_daltonizer_master_switch_title, R.string.accessibility_daltonizer_master_switch_title);
    }

    private void initPreferences() {
        String value = Integer.toString(Secure.getInt(getContentResolver(), TYPE, 12));
        this.mType.setValue(value);
        this.mType.setOnPreferenceChangeListener(this);
        if (this.mType.findIndexOfValue(value) < 0) {
            this.mType.setSummary(getString(R.string.daltonizer_type_overridden, new Object[]{getString(R.string.simulate_color_space)}));
        }
    }

    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        onPreferenceToggled(this.mPreferenceKey, isChecked);
    }
}
