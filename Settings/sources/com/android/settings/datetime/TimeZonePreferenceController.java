package com.android.settings.datetime;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.util.FeatureFlagUtils;
import com.android.settings.core.FeatureFlags;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.datetime.timezone.TimeZoneSettings;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.datetime.ZoneGetter;
import java.util.Calendar;

public class TimeZonePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_TIMEZONE = "timezone";
    private final AutoTimeZonePreferenceController mAutoTimeZonePreferenceController;
    private final boolean mZonePickerV2 = FeatureFlagUtils.isEnabled(this.mContext, FeatureFlags.ZONE_PICKER_V2);

    public TimeZonePreferenceController(Context context, AutoTimeZonePreferenceController autoTimeZonePreferenceController) {
        super(context);
        this.mAutoTimeZonePreferenceController = autoTimeZonePreferenceController;
    }

    public void updateState(Preference preference) {
        if (preference instanceof RestrictedPreference) {
            if (this.mZonePickerV2) {
                preference.setFragment(TimeZoneSettings.class.getName());
            }
            preference.setSummary(getTimeZoneOffsetAndName());
            if (!((RestrictedPreference) preference).isDisabledByAdmin()) {
                preference.setEnabled(this.mAutoTimeZonePreferenceController.isEnabled() ^ 1);
            }
        }
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_TIMEZONE;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public CharSequence getTimeZoneOffsetAndName() {
        Calendar now = Calendar.getInstance();
        return ZoneGetter.getTimeZoneOffsetAndName(this.mContext, now.getTimeZone(), now.getTime());
    }
}
