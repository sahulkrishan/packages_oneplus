package android.support.v17.leanback.widget;

import android.os.Build.VERSION;
import android.view.View;

final class ShadowHelper {
    private ShadowHelper() {
    }

    static boolean supportsDynamicShadow() {
        return VERSION.SDK_INT >= 21;
    }

    static Object addDynamicShadow(View shadowContainer, float unfocusedZ, float focusedZ, int roundedCornerRadius) {
        if (VERSION.SDK_INT >= 21) {
            return ShadowHelperApi21.addDynamicShadow(shadowContainer, unfocusedZ, focusedZ, roundedCornerRadius);
        }
        return null;
    }

    static void setShadowFocusLevel(Object impl, float level) {
        if (VERSION.SDK_INT >= 21) {
            ShadowHelperApi21.setShadowFocusLevel(impl, level);
        }
    }
}
