package com.android.settingslib.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.provider.Settings.Secure;
import android.view.accessibility.AccessibilityManager;
import java.util.List;

public class AccessibilityButtonHelper {
    public static boolean isRequestedByMagnification(Context ctx) {
        return Secure.getInt(ctx.getContentResolver(), "accessibility_display_magnification_navbar_enabled", 0) == 1;
    }

    public static boolean isRequestedByAccessibilityService(Context ctx) {
        List<AccessibilityServiceInfo> services = ((AccessibilityManager) ctx.getSystemService(AccessibilityManager.class)).getEnabledAccessibilityServiceList(-1);
        if (services != null) {
            int size = services.size();
            for (int i = 0; i < size; i++) {
                if ((((AccessibilityServiceInfo) services.get(i)).flags & 256) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isRequested(Context ctx) {
        return isRequestedByMagnification(ctx) || isRequestedByAccessibilityService(ctx);
    }
}
