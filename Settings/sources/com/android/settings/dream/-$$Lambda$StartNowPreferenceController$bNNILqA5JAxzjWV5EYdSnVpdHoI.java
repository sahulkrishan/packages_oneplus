package com.android.settings.dream;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$StartNowPreferenceController$bNNILqA5JAxzjWV5EYdSnVpdHoI implements OnClickListener {
    private final /* synthetic */ StartNowPreferenceController f$0;

    public /* synthetic */ -$$Lambda$StartNowPreferenceController$bNNILqA5JAxzjWV5EYdSnVpdHoI(StartNowPreferenceController startNowPreferenceController) {
        this.f$0 = startNowPreferenceController;
    }

    public final void onClick(View view) {
        this.f$0.mBackend.startDreaming();
    }
}
