package android.support.v17.leanback.transition;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

@RequiresApi(21)
@RestrictTo({Scope.LIBRARY_GROUP})
public class ParallaxTransition extends Visibility {
    static Interpolator sInterpolator = new LinearInterpolator();

    public ParallaxTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Incorrect type for fill-array insn 0x000f, element type: float, insn element type: null */
    public android.animation.Animator createAnimator(android.view.View r4) {
        /*
        r3 = this;
        r0 = android.support.v17.leanback.R.id.lb_parallax_source;
        r0 = r4.getTag(r0);
        r0 = (android.support.v17.leanback.widget.Parallax) r0;
        if (r0 != 0) goto L_0x000c;
    L_0x000a:
        r1 = 0;
        return r1;
    L_0x000c:
        r1 = 2;
        r1 = new float[r1];
        r1 = {0, 1065353216};
        r1 = android.animation.ValueAnimator.ofFloat(r1);
        r2 = sInterpolator;
        r1.setInterpolator(r2);
        r2 = new android.support.v17.leanback.transition.ParallaxTransition$1;
        r2.<init>(r0);
        r1.addUpdateListener(r2);
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v17.leanback.transition.ParallaxTransition.createAnimator(android.view.View):android.animation.Animator");
    }

    public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        if (endValues == null) {
            return null;
        }
        return createAnimator(view);
    }

    public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null) {
            return null;
        }
        return createAnimator(view);
    }
}
