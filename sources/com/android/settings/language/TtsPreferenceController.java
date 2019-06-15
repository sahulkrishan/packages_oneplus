package com.android.settings.language;

import android.content.Context;
import android.speech.tts.TtsEngines;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class TtsPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_TTS_SETTINGS = "tts_settings_summary";
    private static final String KEY_VOICE_CATEGORY = "voice_category";
    private final TtsEngines mTtsEngines;

    public TtsPreferenceController(Context context, TtsEngines ttsEngines) {
        super(context);
        this.mTtsEngines = ttsEngines;
    }

    public boolean isAvailable() {
        return !this.mTtsEngines.getEngines().isEmpty() && this.mContext.getResources().getBoolean(R.bool.config_show_tts_settings_summary);
    }

    public String getPreferenceKey() {
        return KEY_TTS_SETTINGS;
    }
}
