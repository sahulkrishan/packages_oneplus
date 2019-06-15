package com.android.settings.fingerprint;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.android.settings.R;
import com.android.settings.SetupWizardUtils;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.oneplus.custom.utils.OpCustomizeSettings;
import com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_TYPE;
import com.oneplus.settings.utils.OPUtils;

public class SetupFingerprintEnrollFindSensor extends FingerprintEnrollFindSensor {

    public static class SkipFingerprintDialog extends InstrumentedDialogFragment implements OnClickListener {
        private static final String TAG_SKIP_DIALOG = "skip_dialog";

        public int getMetricsCategory() {
            return 573;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return onCreateDialogBuilder().create();
        }

        @NonNull
        public Builder onCreateDialogBuilder() {
            return new Builder(getContext()).setTitle(R.string.setup_fingerprint_enroll_skip_title).setPositiveButton(R.string.skip_anyway_button_label, this).setNegativeButton(R.string.go_back_button_label, this).setMessage(R.string.setup_fingerprint_enroll_skip_after_adding_lock_text);
        }

        public void onClick(DialogInterface dialog, int button) {
            if (button == -1) {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                if (OPUtils.isO2()) {
                    activity.setResult(11);
                    activity.finish();
                    return;
                }
                try {
                    ComponentName componentName;
                    Intent intent = new Intent();
                    if (OPUtils.isGuestMode()) {
                        componentName = new ComponentName("com.oneplus.provision", "com.oneplus.provision.UserSettingSuccess");
                    } else {
                        componentName = new ComponentName("com.oneplus.provision", "com.oneplus.provision.GesturesActivity");
                    }
                    intent.setComponent(componentName);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.op_slide_in, R.anim.op_slide_out);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    activity.finish();
                }
            }
        }

        public void show(FragmentManager manager) {
            show(manager, TAG_SKIP_DIALOG);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (OPUtils.isO2()) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(8192);
        }
        if (this.functionalTermsButton != null) {
            if (OPUtils.isO2()) {
                this.functionalTermsButton.setTextColor(getResources().getColor(R.color.op_setupwizard_oxygen_accent_color));
            } else {
                this.functionalTermsButton.setTextColor(getResources().getColor(R.color.op_setupwizard_hydrogen_accent_color));
            }
        }
        if (OPUtils.isSupportCustomFingerprint()) {
            this.mEnrollTipsAnimView = (LottieAnimationView) findViewById(R.id.op_fingerprint_enroll_tips_view);
            if (CUSTOM_TYPE.MCL.equals(OpCustomizeSettings.getCustomType())) {
                this.mEnrollTipsAnimView.setAnimation("op_fingerprint_enroll_tips_light.json");
            } else if (OPUtils.isBlackModeOn(getContentResolver())) {
                this.mEnrollTipsAnimView.setAnimation("op_fingerprint_enroll_tips_dark.json");
            } else {
                this.mEnrollTipsAnimView.setAnimation("op_fingerprint_enroll_tips_light.json");
            }
            this.mEnrollTipsAnimView.loop(true);
            this.mEnrollTipsAnimView.playAnimation();
        }
    }

    /* Access modifiers changed, original: protected */
    public int getContentView() {
        if (OPUtils.isSupportCustomFingerprint()) {
            return R.layout.oneplus_custom_fingerprint_enroll_find_sensor_base;
        }
        if (OPUtils.isSurportBackFingerprint(this)) {
            return R.layout.oneplus_back_fingerprint_enroll_find_sensor_base;
        }
        return R.layout.fingerprint_enroll_find_sensor;
    }

    /* Access modifiers changed, original: protected */
    public void onApplyThemeResource(Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, SetupWizardUtils.getTheme(getIntent()), first);
    }

    /* Access modifiers changed, original: protected */
    public void initViews() {
        super.initViews();
        ((TextView) findViewById(R.id.description_text)).setTextColor(getResources().getColor(R.color.oneplus_contorl_text_color_primary_light));
    }

    /* Access modifiers changed, original: protected */
    public Intent getEnrollingIntent() {
        Intent intent = new Intent(this, SetupFingerprintEnrollEnrolling.class);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, this.mToken);
        if (this.mUserId != -10000) {
            intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        }
        SetupWizardUtils.copySetupExtras(getIntent(), intent);
        return intent;
    }

    /* Access modifiers changed, original: protected */
    public void onSkipButtonClick() {
        new SkipFingerprintDialog().show(getFragmentManager());
    }

    public int getMetricsCategory() {
        return 247;
    }
}
