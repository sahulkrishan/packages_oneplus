package com.android.settings.display;

import android.content.Context;
import com.android.internal.app.ColorDisplayController;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class NightDisplayPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_NIGHT_DISPLAY = "night_display";

    public NightDisplayPreferenceController(Context context) {
        super(context);
    }

    public static boolean isSuggestionComplete(Context context) {
        boolean z = true;
        if (!context.getResources().getBoolean(R.bool.config_night_light_suggestion_enabled)) {
            return true;
        }
        if (new ColorDisplayController(context).getAutoMode() == 0) {
            z = false;
        }
        return z;
    }

    public boolean isAvailable() {
        return ColorDisplayController.isAvailable(this.mContext);
    }

    public String getPreferenceKey() {
        return KEY_NIGHT_DISPLAY;
    }
}
