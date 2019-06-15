package com.android.settings.fingerprint;

import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.SetupWizardUtils;
import com.oneplus.settings.utils.OPUtils;

public class SetupFingerprintEnrollEnrolling extends FingerprintEnrollEnrolling {
    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        this.isSetupPage = true;
        super.onCreate(savedInstanceState);
        this.mNeedHideNavBar = false;
        if (OPUtils.isSupportCustomFingerprint()) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else if (OPUtils.isO2()) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(8192);
        }
        if (!OPUtils.isSupportCustomFingerprint()) {
            if (this.mStartMessage != null) {
                this.mStartMessage.setTextColor(getResources().getColor(R.color.oneplus_contorl_text_color_primary_light));
            }
            if (this.mRepeatMessage != null) {
                this.mRepeatMessage.setTextColor(getResources().getColor(R.color.oneplus_contorl_text_color_primary_light));
            }
            if (this.mOPFingerPrintEnrollView != null) {
                this.mOPFingerPrintEnrollView.setEnrollAnimBgColor("#E9E9E9");
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public Intent getFinishIntent() {
        Intent intent = new Intent(this, SetupFingerprintEnrollFinish.class);
        SetupWizardUtils.copySetupExtras(getIntent(), intent);
        return intent;
    }

    /* Access modifiers changed, original: protected */
    public void onApplyThemeResource(Theme theme, int resid, boolean first) {
        if (OPUtils.isSupportCustomFingerprint()) {
            resid = R.style.f944Theme.Oneplus.SetupWizardTheme.Black;
        } else {
            resid = SetupWizardUtils.getTheme(getIntent());
        }
        super.onApplyThemeResource(theme, resid, first);
    }

    public int getMetricsCategory() {
        return 246;
    }
}
