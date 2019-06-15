package com.android.settings.display;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.internal.app.ColorDisplayController;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.widget.FooterPreference;

public class NightDisplayFooterPreferenceController extends BasePreferenceController {
    public NightDisplayFooterPreferenceController(Context context) {
        super(context, FooterPreference.KEY_FOOTER);
    }

    public int getAvailabilityStatus() {
        return ColorDisplayController.isAvailable(this.mContext) ? 0 : 2;
    }

    public void updateState(Preference preference) {
        preference.setTitle((int) R.string.night_display_text);
    }
}
