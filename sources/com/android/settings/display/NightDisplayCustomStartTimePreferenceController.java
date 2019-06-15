package com.android.settings.display;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.internal.app.ColorDisplayController;
import com.android.settings.core.BasePreferenceController;

public class NightDisplayCustomStartTimePreferenceController extends BasePreferenceController {
    private ColorDisplayController mController;
    private NightDisplayTimeFormatter mTimeFormatter;

    public NightDisplayCustomStartTimePreferenceController(Context context, String key) {
        super(context, key);
        this.mController = new ColorDisplayController(context);
        this.mTimeFormatter = new NightDisplayTimeFormatter(context);
    }

    public int getAvailabilityStatus() {
        return ColorDisplayController.isAvailable(this.mContext) ? 0 : 2;
    }

    public final void updateState(Preference preference) {
        boolean z = true;
        if (this.mController.getAutoMode() != 1) {
            z = false;
        }
        preference.setVisible(z);
        preference.setSummary(this.mTimeFormatter.getFormattedTimeString(this.mController.getCustomStartTime()));
    }
}
