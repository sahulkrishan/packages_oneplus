package com.android.settings;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DisplaySettings$qOh46548JQf3cUmLta2I9UEyRo4 implements AnimatorUpdateListener {
    private final /* synthetic */ DisplaySettings f$0;

    public /* synthetic */ -$$Lambda$DisplaySettings$qOh46548JQf3cUmLta2I9UEyRo4(DisplaySettings displaySettings) {
        this.f$0 = displaySettings;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        this.f$0.mBrightPreference.setBrightness(((Integer) this.f$0.mSliderAnimator.getAnimatedValue()).intValue());
    }
}
