package com.android.settings.display;

import android.content.Context;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class AmbientDisplayPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final int MY_USER_ID = UserHandle.myUserId();
    private final AmbientDisplayConfiguration mConfig;
    private final String mKey;

    public AmbientDisplayPreferenceController(Context context, AmbientDisplayConfiguration config, String key) {
        super(context);
        this.mConfig = config;
        this.mKey = key;
    }

    public boolean isAvailable() {
        return this.mConfig.available();
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        if (this.mConfig.alwaysOnEnabled(MY_USER_ID)) {
            preference.setSummary((int) R.string.ambient_display_screen_summary_always_on);
        } else if (this.mConfig.pulseOnNotificationEnabled(MY_USER_ID)) {
            preference.setSummary((int) R.string.ambient_display_screen_summary_notifications);
        } else if (this.mConfig.enabled(MY_USER_ID)) {
            preference.setSummary((int) R.string.switch_on_text);
        } else {
            preference.setSummary((int) R.string.switch_off_text);
        }
    }

    public String getPreferenceKey() {
        return this.mKey;
    }
}
