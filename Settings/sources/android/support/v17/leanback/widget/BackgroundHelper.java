package android.support.v17.leanback.widget;

import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.view.View;

@RestrictTo({Scope.LIBRARY_GROUP})
public final class BackgroundHelper {
    public static void setBackgroundPreservingAlpha(View view, Drawable drawable) {
        if (VERSION.SDK_INT >= 19) {
            if (view.getBackground() != null) {
                drawable.setAlpha(view.getBackground().getAlpha());
            }
            view.setBackground(drawable);
            return;
        }
        view.setBackground(drawable);
    }

    private BackgroundHelper() {
    }
}
