package com.android.settings.notification;

import android.app.NotificationManager.Policy;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenFooterPreferenceController extends AbstractZenModePreferenceController {
    public ZenFooterPreferenceController(Context context, Lifecycle lifecycle, String key) {
        super(context, key, lifecycle);
    }

    public boolean isAvailable() {
        return this.mBackend.mPolicy.suppressedVisualEffects == 0 || Policy.areAllVisualEffectsSuppressed(this.mBackend.mPolicy.suppressedVisualEffects);
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        if (this.mBackend.mPolicy.suppressedVisualEffects == 0) {
            preference.setTitle((int) R.string.zen_mode_restrict_notifications_mute_footer);
        } else if (Policy.areAllVisualEffectsSuppressed(this.mBackend.mPolicy.suppressedVisualEffects)) {
            preference.setTitle((int) R.string.zen_mode_restrict_notifications_hide_footer);
        } else {
            preference.setTitle(null);
        }
    }

    /* Access modifiers changed, original: protected */
    public void hide(PreferenceScreen screen) {
        setVisible(screen, getPreferenceKey(), false);
    }
}
