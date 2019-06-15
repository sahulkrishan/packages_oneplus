package com.android.settings.widget;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$HighlightablePreferenceGroupAdapter$HMY634RMu5R2WoggcFMdrEe8uA0 implements AnimatorUpdateListener {
    private final /* synthetic */ View f$0;

    public /* synthetic */ -$$Lambda$HighlightablePreferenceGroupAdapter$HMY634RMu5R2WoggcFMdrEe8uA0(View view) {
        this.f$0 = view;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        this.f$0.setBackgroundColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
    }
}
