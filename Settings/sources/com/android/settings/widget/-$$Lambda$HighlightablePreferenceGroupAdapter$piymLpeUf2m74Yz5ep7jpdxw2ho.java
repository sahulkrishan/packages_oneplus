package com.android.settings.widget;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$HighlightablePreferenceGroupAdapter$piymLpeUf2m74Yz5ep7jpdxw2ho implements AnimatorUpdateListener {
    private final /* synthetic */ View f$0;

    public /* synthetic */ -$$Lambda$HighlightablePreferenceGroupAdapter$piymLpeUf2m74Yz5ep7jpdxw2ho(View view) {
        this.f$0 = view;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        this.f$0.setBackgroundColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
    }
}
