package android.support.v17.leanback.widget;

import android.os.Build.VERSION;
import android.support.v17.leanback.R;
import android.view.View;

final class RoundedRectHelper {
    static boolean supportsRoundedCorner() {
        return VERSION.SDK_INT >= 21;
    }

    static void setClipToRoundedOutline(View view, boolean clip, int radius) {
        if (VERSION.SDK_INT >= 21) {
            RoundedRectHelperApi21.setClipToRoundedOutline(view, clip, radius);
        }
    }

    static void setClipToRoundedOutline(View view, boolean clip) {
        if (VERSION.SDK_INT >= 21) {
            RoundedRectHelperApi21.setClipToRoundedOutline(view, clip, view.getResources().getDimensionPixelSize(R.dimen.lb_rounded_rect_corner_radius));
        }
    }

    private RoundedRectHelper() {
    }
}
