package com.android.settings.display;

import android.content.Context;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.app.ColorDisplayController;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class NightDisplayAutoModePreferenceController extends BasePreferenceController implements OnPreferenceChangeListener {
    private ColorDisplayController mController;
    private DropDownPreference mPreference;

    public NightDisplayAutoModePreferenceController(Context context, String key) {
        super(context, key);
        this.mController = new ColorDisplayController(context);
    }

    public int getAvailabilityStatus() {
        return ColorDisplayController.isAvailable(this.mContext) ? 0 : 2;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (DropDownPreference) screen.findPreference(getPreferenceKey());
        this.mPreference.setEntries(new CharSequence[]{this.mContext.getString(R.string.night_display_auto_mode_never), this.mContext.getString(R.string.night_display_auto_mode_custom), this.mContext.getString(R.string.night_display_auto_mode_twilight)});
        this.mPreference.setEntryValues(new CharSequence[]{String.valueOf(0), String.valueOf(1), String.valueOf(2)});
    }

    public final void updateState(Preference preference) {
        this.mPreference.setValue(String.valueOf(this.mController.getAutoMode()));
    }

    public final boolean onPreferenceChange(Preference preference, Object newValue) {
        return this.mController.setAutoMode(Integer.parseInt((String) newValue));
    }
}
