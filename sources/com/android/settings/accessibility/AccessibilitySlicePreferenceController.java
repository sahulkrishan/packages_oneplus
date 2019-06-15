package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings.Secure;
import android.view.accessibility.AccessibilityManager;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.accessibility.AccessibilityUtils;

public class AccessibilitySlicePreferenceController extends TogglePreferenceController {
    private final int OFF = 0;
    private final int ON = 1;
    private final ComponentName mComponentName = ComponentName.unflattenFromString(getPreferenceKey());

    public AccessibilitySlicePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        if (this.mComponentName == null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Illegal Component Name from: ");
            stringBuilder.append(preferenceKey);
            throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    public CharSequence getSummary() {
        AccessibilityServiceInfo serviceInfo = getAccessibilityServiceInfo();
        return serviceInfo == null ? "" : AccessibilitySettings.getServiceSummary(this.mContext, serviceInfo, isChecked());
    }

    public boolean isChecked() {
        boolean z = true;
        if (Secure.getInt(this.mContext.getContentResolver(), "accessibility_enabled", 0) != 1) {
            z = false;
        }
        if (z) {
            return AccessibilityUtils.getEnabledServicesFromSettings(this.mContext).contains(this.mComponentName);
        }
        return false;
    }

    public boolean setChecked(boolean isChecked) {
        boolean z = false;
        if (getAccessibilityServiceInfo() == null) {
            return false;
        }
        AccessibilityUtils.setAccessibilityServiceState(this.mContext, this.mComponentName, isChecked);
        if (isChecked == isChecked()) {
            z = true;
        }
        return z;
    }

    public int getAvailabilityStatus() {
        return getAccessibilityServiceInfo() == null ? 2 : 0;
    }

    public boolean isSliceable() {
        return true;
    }

    private AccessibilityServiceInfo getAccessibilityServiceInfo() {
        for (AccessibilityServiceInfo serviceInfo : ((AccessibilityManager) this.mContext.getSystemService(AccessibilityManager.class)).getInstalledAccessibilityServiceList()) {
            if (this.mComponentName.equals(serviceInfo.getComponentName())) {
                return serviceInfo;
            }
        }
        return null;
    }
}
