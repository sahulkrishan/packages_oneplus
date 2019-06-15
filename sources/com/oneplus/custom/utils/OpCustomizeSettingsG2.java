package com.oneplus.custom.utils;

import android.content.Context;
import android.util.Log;
import com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_TYPE;

public class OpCustomizeSettingsG2 extends OpCustomizeSettings {
    private static final String SUPPORT_GET_SECURE_WP_PKG_1 = "net.oneplus.wallpaperresources";

    /* Access modifiers changed, original: protected */
    public CUSTOM_TYPE getCustomization() {
        CUSTOM_TYPE result = CUSTOM_TYPE.NONE;
        int custFlagVal = ParamReader.getCustFlagVal();
        if (custFlagVal == 3) {
            return CUSTOM_TYPE.AVG;
        }
        switch (custFlagVal) {
            case 6:
                return CUSTOM_TYPE.MCL;
            case 7:
                return CUSTOM_TYPE.OPR_RETAIL;
            default:
                return result;
        }
    }

    /* Access modifiers changed, original: protected */
    public com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_BACK_COVER_TYPE getCustomBackCoverType() {
        /*
        r3 = this;
        r0 = com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_BACK_COVER_TYPE.NONE;
        r1 = com.oneplus.custom.utils.ParamReader.getBackCoverColorVal();
        r2 = r1.hashCode();
        switch(r2) {
            case -766480814: goto L_0x005f;
            case -747948807: goto L_0x0055;
            case -736897359: goto L_0x004b;
            case -639734052: goto L_0x0041;
            case 590384415: goto L_0x0037;
            case 666808411: goto L_0x002c;
            case 680320567: goto L_0x0022;
            case 682228274: goto L_0x0018;
            case 724156130: goto L_0x000e;
            default: goto L_0x000d;
        };
    L_0x000d:
        goto L_0x0069;
    L_0x000e:
        r2 = "fffe3d3e";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x0069;
    L_0x0016:
        r1 = 3;
        goto L_0x006a;
    L_0x0018:
        r2 = "fff6f7f7";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x0069;
    L_0x0020:
        r1 = 2;
        goto L_0x006a;
    L_0x0022:
        r2 = "fff4d6b9";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x0069;
    L_0x002a:
        r1 = 7;
        goto L_0x006a;
    L_0x002c:
        r2 = "ffde0d39";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x0069;
    L_0x0034:
        r1 = 8;
        goto L_0x006a;
    L_0x0037:
        r2 = "ffc199b3";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x0069;
    L_0x003f:
        r1 = 4;
        goto L_0x006a;
    L_0x0041:
        r2 = "ff828da7";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x0069;
    L_0x0049:
        r1 = 6;
        goto L_0x006a;
    L_0x004b:
        r2 = "ff3d3740";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x0069;
    L_0x0053:
        r1 = 0;
        goto L_0x006a;
    L_0x0055:
        r2 = "ff49484d";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x0069;
    L_0x005d:
        r1 = 5;
        goto L_0x006a;
    L_0x005f:
        r2 = "ff2c2630";
        r1 = r1.equals(r2);
        if (r1 == 0) goto L_0x0069;
    L_0x0067:
        r1 = 1;
        goto L_0x006a;
    L_0x0069:
        r1 = -1;
    L_0x006a:
        switch(r1) {
            case 0: goto L_0x0086;
            case 1: goto L_0x0083;
            case 2: goto L_0x0080;
            case 3: goto L_0x007d;
            case 4: goto L_0x007a;
            case 5: goto L_0x0077;
            case 6: goto L_0x0074;
            case 7: goto L_0x0071;
            case 8: goto L_0x006e;
            default: goto L_0x006d;
        };
    L_0x006d:
        goto L_0x0089;
    L_0x006e:
        r0 = com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_BACK_COVER_TYPE.OPRD;
        goto L_0x0089;
    L_0x0071:
        r0 = com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_BACK_COVER_TYPE.OPGL;
        goto L_0x0089;
    L_0x0074:
        r0 = com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_BACK_COVER_TYPE.OPBL;
        goto L_0x0089;
    L_0x0077:
        r0 = com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_BACK_COVER_TYPE.OPGY;
        goto L_0x0089;
    L_0x007a:
        r0 = com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_BACK_COVER_TYPE.DGZ;
        goto L_0x0089;
    L_0x007d:
        r0 = com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_BACK_COVER_TYPE.HPH;
        goto L_0x0089;
    L_0x0080:
        r0 = com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_BACK_COVER_TYPE.YYB;
        goto L_0x0089;
    L_0x0083:
        r0 = com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_BACK_COVER_TYPE.LCH;
        goto L_0x0089;
    L_0x0086:
        r0 = com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_BACK_COVER_TYPE.MYH;
    L_0x0089:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.custom.utils.OpCustomizeSettingsG2.getCustomBackCoverType():com.oneplus.custom.utils.OpCustomizeSettings$CUSTOM_BACK_COVER_TYPE");
    }

    /* Access modifiers changed, original: protected */
    public long getCustomBackCoverColor() {
        try {
            return Long.parseLong(ParamReader.getBackCoverColorVal(), 16) & -1;
        } catch (Exception e) {
            Log.e("OpCustomizeSettings", e.getMessage());
            return 0;
        }
    }

    /* Access modifiers changed, original: protected */
    public byte[] getSecureWPKey(Context ctx) {
        String packageName = ctx.getPackageName();
        if ("1".equals(SystemProperties.get("ro.remount.time"))) {
            MyLog.w("OpCustomizeSettings", "device was remounted, exit");
            return null;
        } else if (SUPPORT_GET_SECURE_WP_PKG_1.equals(packageName)) {
            return ParamReader.getSecureWPKey();
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("not ready for : ");
            stringBuilder.append(packageName);
            MyLog.w("OpCustomizeSettings", stringBuilder.toString());
            return null;
        }
    }
}
