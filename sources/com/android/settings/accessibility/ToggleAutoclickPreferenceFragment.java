package com.android.settings.accessibility;

import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.media.MediaPlayer2;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.widget.Switch;
import com.android.settings.R;
import com.android.settings.widget.SeekBarPreference;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;
import com.android.settings.wifi.ConfigureWifiSettings;

public class ToggleAutoclickPreferenceFragment extends ToggleFeaturePreferenceFragment implements OnSwitchChangeListener, OnPreferenceChangeListener {
    private static final int AUTOCLICK_DELAY_STEP = 100;
    private static final int MAX_AUTOCLICK_DELAY = 1000;
    private static final int MIN_AUTOCLICK_DELAY = 200;
    private static final int[] mAutoclickPreferenceSummaries = new int[]{R.plurals.accessibilty_autoclick_preference_subtitle_extremely_short_delay, R.plurals.accessibilty_autoclick_preference_subtitle_very_short_delay, R.plurals.accessibilty_autoclick_preference_subtitle_short_delay, R.plurals.accessibilty_autoclick_preference_subtitle_long_delay, R.plurals.accessibilty_autoclick_preference_subtitle_very_long_delay};
    private SeekBarPreference mDelay;

    static CharSequence getAutoclickPreferenceSummary(Resources resources, int delay) {
        return resources.getQuantityString(mAutoclickPreferenceSummaries[getAutoclickPreferenceSummaryIndex(delay)], delay, new Object[]{Integer.valueOf(delay)});
    }

    private static int getAutoclickPreferenceSummaryIndex(int delay) {
        if (delay <= 200) {
            return 0;
        }
        if (delay >= 1000) {
            return mAutoclickPreferenceSummaries.length - 1;
        }
        return (delay - 200) / (MediaPlayer2.MEDIA_INFO_BAD_INTERLEAVING / (mAutoclickPreferenceSummaries.length - 1));
    }

    /* Access modifiers changed, original: protected */
    public void onPreferenceToggled(String preferenceKey, boolean enabled) {
        Secure.putInt(getContentResolver(), preferenceKey, enabled);
        this.mDelay.setEnabled(enabled);
    }

    public int getMetricsCategory() {
        return 335;
    }

    public int getHelpResource() {
        return R.string.help_url_autoclick;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.accessibility_autoclick_settings;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int delay = Secure.getInt(getContentResolver(), "accessibility_autoclick_delay", ConfigureWifiSettings.WIFI_WAKEUP_REQUEST_CODE);
        this.mDelay = (SeekBarPreference) findPreference("autoclick_delay");
        this.mDelay.setMax(delayToSeekBarProgress(1000));
        this.mDelay.setProgress(delayToSeekBarProgress(delay));
        this.mDelay.setOnPreferenceChangeListener(this);
        this.mFooterPreferenceMixin.createFooterPreference().setTitle((int) R.string.accessibility_autoclick_description);
    }

    /* Access modifiers changed, original: protected */
    public void onInstallSwitchBarToggleSwitch() {
        super.onInstallSwitchBarToggleSwitch();
        boolean z = false;
        int value = Secure.getInt(getContentResolver(), "accessibility_autoclick_enabled", 0);
        this.mSwitchBar.setCheckedInternal(value == 1);
        this.mSwitchBar.addOnSwitchChangeListener(this);
        SeekBarPreference seekBarPreference = this.mDelay;
        if (value == 1) {
            z = true;
        }
        seekBarPreference.setEnabled(z);
    }

    /* Access modifiers changed, original: protected */
    public void onRemoveSwitchBarToggleSwitch() {
        super.onRemoveSwitchBarToggleSwitch();
        this.mSwitchBar.removeOnSwitchChangeListener(this);
    }

    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        onPreferenceToggled("accessibility_autoclick_enabled", isChecked);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference != this.mDelay || !(newValue instanceof Integer)) {
            return false;
        }
        Secure.putInt(getContentResolver(), "accessibility_autoclick_delay", seekBarProgressToDelay(((Integer) newValue).intValue()));
        return true;
    }

    private int seekBarProgressToDelay(int progress) {
        return (progress * 100) + 200;
    }

    private int delayToSeekBarProgress(int delay) {
        return (delay - 200) / 100;
    }
}
