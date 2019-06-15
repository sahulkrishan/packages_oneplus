package com.android.settings.dream;

import com.android.settings.widget.GearPreference;
import com.android.settings.widget.GearPreference.OnGearClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$CurrentDreamPreferenceController$faOOwvjkeM0i38i1bxACLza6vQ4 implements OnGearClickListener {
    private final /* synthetic */ CurrentDreamPreferenceController f$0;

    public /* synthetic */ -$$Lambda$CurrentDreamPreferenceController$faOOwvjkeM0i38i1bxACLza6vQ4(CurrentDreamPreferenceController currentDreamPreferenceController) {
        this.f$0 = currentDreamPreferenceController;
    }

    public final void onGearClick(GearPreference gearPreference) {
        this.f$0.launchScreenSaverSettings();
    }
}
