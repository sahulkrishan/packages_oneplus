package android.support.v17.leanback.transition;

import android.graphics.Rect;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;

@RestrictTo({Scope.LIBRARY})
public abstract class TransitionEpicenterCallback {
    public abstract Rect onGetEpicenter(Object obj);
}
