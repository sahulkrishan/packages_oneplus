package com.oneplus.custom.utils;

import android.content.Context;
import com.oneplus.settings.utils.OPUtils;

public class OpCustomizeSettings {
    protected static final String TAG = "OpCustomizeSettings";
    private static OpCustomizeSettings sOpCustomizeSettings;
    private static final String sProjectName = SystemProperties.get("ro.boot.project_name");

    public enum CUSTOM_BACK_COVER_TYPE {
        NONE,
        LCH,
        MYH,
        YYB,
        HPH,
        DGZ,
        OPGY,
        OPBL,
        OPGL,
        OPRD
    }

    public enum CUSTOM_TYPE {
        NONE,
        JCC,
        SW,
        AVG,
        MCL,
        OPR_RETAIL
    }

    public static CUSTOM_TYPE getCustomType() {
        return getInstance().getCustomization();
    }

    public static CUSTOM_BACK_COVER_TYPE getBackCoverType() {
        return getInstance().getCustomBackCoverType();
    }

    public static long getBackCoverColor() {
        return getInstance().getCustomBackCoverColor();
    }

    public static byte[] getWPKey(Context ctx) {
        return getInstance().getSecureWPKey(ctx);
    }

    private static OpCustomizeSettings getInstance() {
        if (sOpCustomizeSettings == null) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("sProjectName = ");
            stringBuilder.append(sProjectName);
            MyLog.v(str, stringBuilder.toString());
            if ("16859".equals(sProjectName) || "17801".equals(sProjectName)) {
                sOpCustomizeSettings = new OpCustomizeSettingsG1();
            } else if (OPUtils.ONEPLUS_15801.equals(sProjectName) || OPUtils.ONEPLUS_15811.equals(sProjectName)) {
                sOpCustomizeSettings = new OpCustomizeSettings();
            } else {
                sOpCustomizeSettings = new OpCustomizeSettingsG2();
            }
        }
        return sOpCustomizeSettings;
    }

    /* Access modifiers changed, original: protected */
    public CUSTOM_TYPE getCustomization() {
        return CUSTOM_TYPE.NONE;
    }

    /* Access modifiers changed, original: protected */
    public CUSTOM_BACK_COVER_TYPE getCustomBackCoverType() {
        return CUSTOM_BACK_COVER_TYPE.NONE;
    }

    /* Access modifiers changed, original: protected */
    public long getCustomBackCoverColor() {
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public byte[] getSecureWPKey(Context ctx) {
        return null;
    }
}
