package com.android.settings;

import android.content.Intent;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import com.android.setupwizardlib.util.WizardManagerHelper;
import com.oneplus.settings.utils.OPUtils;

public class SetupWizardUtils {
    @VisibleForTesting
    static final String SYSTEM_PROP_SETUPWIZARD_THEME = "setupwizard.theme";

    public static int getTheme(Intent intent) {
        if (intent.getStringExtra(WizardManagerHelper.EXTRA_THEME) == null) {
            String theme = SystemProperties.get(SYSTEM_PROP_SETUPWIZARD_THEME);
        }
        if (OPUtils.isO2()) {
            return R.style.f960Theme.Oneplus.SetupWizardTheme.Oxygen;
        }
        return R.style.f957Theme.Oneplus.SetupWizardTheme.Hydrogen;
    }

    public static int getSettingTheme() {
        return R.style.f955Theme.Oneplus.SetupFingerprintEnroll;
    }

    public static int getOxygenTheme(Intent intent) {
        if (OPUtils.isO2()) {
            return R.style.f959Theme.Oneplus.SetupWizardTheme.Light.Oxygen;
        }
        return R.style.f958Theme.Oneplus.SetupWizardTheme.Light.Hydrogen;
    }

    public static int getOxygenSettingTheme() {
        return R.style.f953Theme.Oneplus.Oxygen.SetupFingerprintEnroll;
    }

    public static int getTransparentTheme(Intent intent) {
        int suwTheme = getTheme(intent);
        if (suwTheme == R.style.GlifV3Theme) {
            return R.style.f183GlifV3Theme.Transparent;
        }
        if (suwTheme == R.style.f181GlifV3Theme.Light) {
            return R.style.f182GlifV3Theme.Light.Transparent;
        }
        if (suwTheme == R.style.GlifV2Theme) {
            return R.style.f179GlifV2Theme.Transparent;
        }
        if (suwTheme == R.style.f176GlifTheme.Light) {
            return R.style.f745SetupWizardTheme.Light.Transparent;
        }
        if (suwTheme == R.style.GlifTheme) {
            return R.style.f746SetupWizardTheme.Transparent;
        }
        return R.style.f178GlifV2Theme.Light.Transparent;
    }

    public static void copySetupExtras(Intent fromIntent, Intent toIntent) {
        toIntent.putExtra(WizardManagerHelper.EXTRA_THEME, fromIntent.getStringExtra(WizardManagerHelper.EXTRA_THEME));
        toIntent.putExtra(WizardManagerHelper.EXTRA_USE_IMMERSIVE_MODE, fromIntent.getBooleanExtra(WizardManagerHelper.EXTRA_USE_IMMERSIVE_MODE, false));
    }
}
