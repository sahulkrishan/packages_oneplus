package com.android.settings.notification;

import android.content.Context;
import android.content.res.Resources;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class DockAudioMediaPreferenceController extends SettingPrefController {
    private static final int DEFAULT_DOCK_AUDIO_MEDIA = 0;
    private static final int DOCK_AUDIO_MEDIA_DISABLED = 0;
    private static final int DOCK_AUDIO_MEDIA_ENABLED = 1;
    private static final String KEY_DOCK_AUDIO_MEDIA = "dock_audio_media";

    public DockAudioMediaPreferenceController(Context context, SettingsPreferenceFragment parent, Lifecycle lifecycle) {
        super(context, parent, lifecycle);
        this.mPreference = new SettingPref(1, KEY_DOCK_AUDIO_MEDIA, "dock_audio_media_enabled", 0, 0, 1) {
            public boolean isApplicable(Context context) {
                return context.getResources().getBoolean(R.bool.has_dock_settings);
            }

            /* Access modifiers changed, original: protected */
            public String getCaption(Resources res, int value) {
                switch (value) {
                    case 0:
                        return res.getString(R.string.dock_audio_media_disabled);
                    case 1:
                        return res.getString(R.string.dock_audio_media_enabled);
                    default:
                        throw new IllegalArgumentException();
                }
            }
        };
    }
}
