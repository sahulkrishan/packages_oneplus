package android.support.v17.leanback.widget;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

@RestrictTo({Scope.LIBRARY_GROUP})
public class Util {
    public static boolean isDescendant(ViewGroup parent, View child) {
        while (child != null) {
            if (child == parent) {
                return true;
            }
            ViewParent p = child.getParent();
            if (!(p instanceof View)) {
                return false;
            }
            child = (View) p;
        }
        return false;
    }

    private Util() {
    }
}
