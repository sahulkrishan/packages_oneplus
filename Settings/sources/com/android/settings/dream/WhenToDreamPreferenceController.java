package com.android.settings.dream;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.dream.DreamBackend;

public class WhenToDreamPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String WHEN_TO_START = "when_to_start";
    private final DreamBackend mBackend;

    WhenToDreamPreferenceController(Context context) {
        super(context);
        this.mBackend = DreamBackend.getInstance(context);
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setSummary(preference.getContext().getString(DreamSettings.getDreamSettingDescriptionResId(this.mBackend.getWhenToDreamSetting())));
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return WHEN_TO_START;
    }
}
