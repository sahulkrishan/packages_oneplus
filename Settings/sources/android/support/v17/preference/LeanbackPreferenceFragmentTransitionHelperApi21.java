package android.support.v17.preference;

import android.app.Fragment;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.transition.FadeAndShortSlide;
import android.support.v4.view.GravityCompat;
import android.transition.Transition;

@RequiresApi(21)
@RestrictTo({Scope.LIBRARY_GROUP})
public class LeanbackPreferenceFragmentTransitionHelperApi21 {
    public static void addTransitions(Fragment f) {
        Transition transitionStartEdge = new FadeAndShortSlide(GravityCompat.START);
        Transition transitionEndEdge = new FadeAndShortSlide(GravityCompat.END);
        f.setEnterTransition(transitionEndEdge);
        f.setExitTransition(transitionStartEdge);
        f.setReenterTransition(transitionStartEdge);
        f.setReturnTransition(transitionEndEdge);
    }

    private LeanbackPreferenceFragmentTransitionHelperApi21() {
    }
}
