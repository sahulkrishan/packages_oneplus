package com.android.settings.applications.defaultapps;

import android.content.Intent;
import com.android.settings.widget.GearPreference;
import com.android.settings.widget.GearPreference.OnGearClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DefaultAppPreferenceController$P93yGe3NhKzPqeqQwHkMaXpVB1M implements OnGearClickListener {
    private final /* synthetic */ DefaultAppPreferenceController f$0;
    private final /* synthetic */ Intent f$1;

    public /* synthetic */ -$$Lambda$DefaultAppPreferenceController$P93yGe3NhKzPqeqQwHkMaXpVB1M(DefaultAppPreferenceController defaultAppPreferenceController, Intent intent) {
        this.f$0 = defaultAppPreferenceController;
        this.f$1 = intent;
    }

    public final void onGearClick(GearPreference gearPreference) {
        this.f$0.mContext.startActivity(this.f$1);
    }
}
