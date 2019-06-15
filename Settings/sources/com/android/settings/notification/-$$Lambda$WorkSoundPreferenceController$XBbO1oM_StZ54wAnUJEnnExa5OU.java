package com.android.settings.notification;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$WorkSoundPreferenceController$XBbO1oM_StZ54wAnUJEnnExa5OU implements OnPreferenceChangeListener {
    private final /* synthetic */ WorkSoundPreferenceController f$0;

    public /* synthetic */ -$$Lambda$WorkSoundPreferenceController$XBbO1oM_StZ54wAnUJEnnExa5OU(WorkSoundPreferenceController workSoundPreferenceController) {
        this.f$0 = workSoundPreferenceController;
    }

    public final boolean onPreferenceChange(Preference preference, Object obj) {
        return WorkSoundPreferenceController.lambda$updateWorkPreferences$0(this.f$0, preference, obj);
    }
}
