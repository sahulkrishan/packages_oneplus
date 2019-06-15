package android.support.v17.leanback.transition;

import android.content.Context;
import android.os.Build.VERSION;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.R;
import android.view.animation.AnimationUtils;

@RestrictTo({Scope.LIBRARY_GROUP})
public class LeanbackTransitionHelper {
    public static Object loadTitleInTransition(Context context) {
        if (VERSION.SDK_INT < 19 || VERSION.SDK_INT >= 21) {
            return TransitionHelper.loadTransition(context, R.transition.lb_title_in);
        }
        SlideKitkat slide = new SlideKitkat();
        slide.setSlideEdge(48);
        slide.setInterpolator(AnimationUtils.loadInterpolator(context, 17432582));
        slide.addTarget(R.id.browse_title_group);
        return slide;
    }

    public static Object loadTitleOutTransition(Context context) {
        if (VERSION.SDK_INT < 19 || VERSION.SDK_INT >= 21) {
            return TransitionHelper.loadTransition(context, R.transition.lb_title_out);
        }
        SlideKitkat slide = new SlideKitkat();
        slide.setSlideEdge(48);
        slide.setInterpolator(AnimationUtils.loadInterpolator(context, R.anim.lb_decelerator_4));
        slide.addTarget(R.id.browse_title_group);
        return slide;
    }

    private LeanbackTransitionHelper() {
    }
}
