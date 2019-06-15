package com.android.setupwizardlib.util;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.support.annotation.StyleRes;
import android.support.annotation.VisibleForTesting;
import com.android.setupwizardlib.R;
import java.util.Arrays;

public class WizardManagerHelper {
    private static final String ACTION_NEXT = "com.android.wizard.NEXT";
    @VisibleForTesting
    static final String EXTRA_ACTION_ID = "actionId";
    @VisibleForTesting
    static final String EXTRA_IS_DEFERRED_SETUP = "deferredSetup";
    @VisibleForTesting
    static final String EXTRA_IS_FIRST_RUN = "firstRun";
    @VisibleForTesting
    static final String EXTRA_IS_PRE_DEFERRED_SETUP = "preDeferredSetup";
    private static final String EXTRA_RESULT_CODE = "com.android.setupwizard.ResultCode";
    @VisibleForTesting
    static final String EXTRA_SCRIPT_URI = "scriptUri";
    public static final String EXTRA_THEME = "theme";
    public static final String EXTRA_USE_IMMERSIVE_MODE = "useImmersiveMode";
    @VisibleForTesting
    static final String EXTRA_WIZARD_BUNDLE = "wizardBundle";
    public static final String SETTINGS_GLOBAL_DEVICE_PROVISIONED = "device_provisioned";
    public static final String SETTINGS_SECURE_USER_SETUP_COMPLETE = "user_setup_complete";
    public static final String THEME_GLIF = "glif";
    public static final String THEME_GLIF_LIGHT = "glif_light";
    public static final String THEME_GLIF_V2 = "glif_v2";
    public static final String THEME_GLIF_V2_LIGHT = "glif_v2_light";
    public static final String THEME_GLIF_V3 = "glif_v3";
    public static final String THEME_GLIF_V3_LIGHT = "glif_v3_light";
    public static final String THEME_HOLO = "holo";
    public static final String THEME_HOLO_LIGHT = "holo_light";
    public static final String THEME_MATERIAL = "material";
    public static final String THEME_MATERIAL_LIGHT = "material_light";

    public static Intent getNextIntent(Intent originalIntent, int resultCode) {
        return getNextIntent(originalIntent, resultCode, null);
    }

    public static Intent getNextIntent(Intent originalIntent, int resultCode, Intent data) {
        Intent intent = new Intent(ACTION_NEXT);
        copyWizardManagerExtras(originalIntent, intent);
        intent.putExtra(EXTRA_RESULT_CODE, resultCode);
        if (!(data == null || data.getExtras() == null)) {
            intent.putExtras(data.getExtras());
        }
        intent.putExtra(EXTRA_THEME, originalIntent.getStringExtra(EXTRA_THEME));
        return intent;
    }

    public static void copyWizardManagerExtras(Intent srcIntent, Intent dstIntent) {
        dstIntent.putExtra(EXTRA_WIZARD_BUNDLE, srcIntent.getBundleExtra(EXTRA_WIZARD_BUNDLE));
        for (String key : Arrays.asList(new String[]{EXTRA_IS_FIRST_RUN, EXTRA_IS_DEFERRED_SETUP, EXTRA_IS_PRE_DEFERRED_SETUP})) {
            dstIntent.putExtra(key, srcIntent.getBooleanExtra(key, false));
        }
        for (String key2 : Arrays.asList(new String[]{EXTRA_THEME, EXTRA_SCRIPT_URI, EXTRA_ACTION_ID})) {
            dstIntent.putExtra(key2, srcIntent.getStringExtra(key2));
        }
    }

    public static boolean isSetupWizardIntent(Intent intent) {
        return intent.getBooleanExtra(EXTRA_IS_FIRST_RUN, false);
    }

    public static boolean isUserSetupComplete(Context context) {
        boolean z = true;
        if (VERSION.SDK_INT >= 17) {
            if (Secure.getInt(context.getContentResolver(), SETTINGS_SECURE_USER_SETUP_COMPLETE, 0) != 1) {
                z = false;
            }
            return z;
        }
        if (Secure.getInt(context.getContentResolver(), SETTINGS_GLOBAL_DEVICE_PROVISIONED, 0) != 1) {
            z = false;
        }
        return z;
    }

    public static boolean isDeviceProvisioned(Context context) {
        boolean z = true;
        if (VERSION.SDK_INT >= 17) {
            if (Global.getInt(context.getContentResolver(), SETTINGS_GLOBAL_DEVICE_PROVISIONED, 0) != 1) {
                z = false;
            }
            return z;
        }
        if (Secure.getInt(context.getContentResolver(), SETTINGS_GLOBAL_DEVICE_PROVISIONED, 0) != 1) {
            z = false;
        }
        return z;
    }

    public static boolean isDeferredSetupWizard(Intent originalIntent) {
        if (originalIntent == null || !originalIntent.getBooleanExtra(EXTRA_IS_DEFERRED_SETUP, false)) {
            return false;
        }
        return true;
    }

    public static boolean isPreDeferredSetupWizard(Intent originalIntent) {
        if (originalIntent == null || !originalIntent.getBooleanExtra(EXTRA_IS_PRE_DEFERRED_SETUP, false)) {
            return false;
        }
        return true;
    }

    public static boolean isLightTheme(Intent intent, boolean def) {
        return isLightTheme(intent.getStringExtra(EXTRA_THEME), def);
    }

    public static boolean isLightTheme(String theme, boolean def) {
        if (THEME_HOLO_LIGHT.equals(theme) || THEME_MATERIAL_LIGHT.equals(theme) || THEME_GLIF_LIGHT.equals(theme) || THEME_GLIF_V2_LIGHT.equals(theme) || THEME_GLIF_V3_LIGHT.equals(theme)) {
            return true;
        }
        if (THEME_HOLO.equals(theme) || THEME_MATERIAL.equals(theme) || THEME_GLIF.equals(theme) || THEME_GLIF_V2.equals(theme) || THEME_GLIF_V3.equals(theme)) {
            return false;
        }
        return def;
    }

    @StyleRes
    public static int getThemeRes(Intent intent, @StyleRes int defaultTheme) {
        return getThemeRes(intent.getStringExtra(EXTRA_THEME), defaultTheme);
    }

    @StyleRes
    public static int getThemeRes(String theme, @StyleRes int defaultTheme) {
        if (theme != null) {
            Object obj = -1;
            switch (theme.hashCode()) {
                case -2128555920:
                    if (theme.equals(THEME_GLIF_V2_LIGHT)) {
                        obj = 2;
                        break;
                    }
                    break;
                case -1270463490:
                    if (theme.equals(THEME_MATERIAL_LIGHT)) {
                        obj = 6;
                        break;
                    }
                    break;
                case -1241052239:
                    if (theme.equals(THEME_GLIF_V3_LIGHT)) {
                        obj = null;
                        break;
                    }
                    break;
                case 3175618:
                    if (theme.equals(THEME_GLIF)) {
                        obj = 5;
                        break;
                    }
                    break;
                case 115650329:
                    if (theme.equals(THEME_GLIF_V2)) {
                        obj = 3;
                        break;
                    }
                    break;
                case 115650330:
                    if (theme.equals(THEME_GLIF_V3)) {
                        obj = 1;
                        break;
                    }
                    break;
                case 299066663:
                    if (theme.equals(THEME_MATERIAL)) {
                        obj = 7;
                        break;
                    }
                    break;
                case 767685465:
                    if (theme.equals(THEME_GLIF_LIGHT)) {
                        obj = 4;
                        break;
                    }
                    break;
            }
            switch (obj) {
                case null:
                    return R.style.SuwThemeGlifV3_Light;
                case 1:
                    return R.style.SuwThemeGlifV3;
                case 2:
                    return R.style.SuwThemeGlifV2_Light;
                case 3:
                    return R.style.SuwThemeGlifV2;
                case 4:
                    return R.style.SuwThemeGlif_Light;
                case 5:
                    return R.style.SuwThemeGlif;
                case 6:
                    return R.style.SuwThemeMaterial_Light;
                case 7:
                    return R.style.SuwThemeMaterial;
            }
        }
        return defaultTheme;
    }
}
