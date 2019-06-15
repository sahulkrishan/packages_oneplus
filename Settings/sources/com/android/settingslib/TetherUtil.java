package com.android.settingslib;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.telephony.CarrierConfigManager;

public class TetherUtil {
    @VisibleForTesting
    static boolean isEntitlementCheckRequired(Context context) {
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        if (configManager == null || configManager.getConfig() == null) {
            return true;
        }
        return configManager.getConfig().getBoolean("require_entitlement_checks_bool");
    }

    /* JADX WARNING: Missing block: B:10:0x0025, code skipped:
            return false;
     */
    public static boolean isProvisioningNeeded(android.content.Context r4) {
        /*
        r0 = r4.getResources();
        r1 = 17236019; // 0x1070033 float:2.4795727E-38 double:8.515725E-317;
        r0 = r0.getStringArray(r1);
        r1 = "net.tethering.noprovisioning";
        r2 = 0;
        r1 = android.os.SystemProperties.getBoolean(r1, r2);
        if (r1 != 0) goto L_0x0025;
    L_0x0014:
        if (r0 != 0) goto L_0x0017;
    L_0x0016:
        goto L_0x0025;
    L_0x0017:
        r1 = isEntitlementCheckRequired(r4);
        if (r1 != 0) goto L_0x001e;
    L_0x001d:
        return r2;
    L_0x001e:
        r1 = r0.length;
        r3 = 2;
        if (r1 != r3) goto L_0x0024;
    L_0x0022:
        r2 = 1;
    L_0x0024:
        return r2;
    L_0x0025:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.TetherUtil.isProvisioningNeeded(android.content.Context):boolean");
    }
}
