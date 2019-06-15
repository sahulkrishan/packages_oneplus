package com.android.settings.password;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.SetupRedactionInterstitial;
import com.android.settings.SetupWizardUtils;
import com.android.settings.password.ChooseLockPattern.ChooseLockPatternFragment;
import com.android.settings.password.ChooseLockTypeDialogFragment.OnLockTypeSelectedListener;
import com.android.setupwizardlib.GlifLayout;
import com.oneplus.settings.utils.OPUtils;

public class SetupChooseLockPattern extends ChooseLockPattern {

    public static class SetupChooseLockPatternFragment extends ChooseLockPatternFragment implements OnLockTypeSelectedListener {
        @Nullable
        private Button mOptionsButton;

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            GlifLayout view = (GlifLayout) inflater.inflate(R.layout.op_setup_choose_lock_pattern_common, container, false);
            view.setHeaderText(getActivity().getTitle());
            if (getResources().getBoolean(R.bool.config_lock_pattern_minimal_ui)) {
                View iconView = view.findViewById(R.id.suw_layout_icon);
                if (iconView != null) {
                    iconView.setVisibility(8);
                }
            } else if (this.mForFingerprint) {
                view.setIcon(getActivity().getDrawable(R.drawable.op_ic_lock));
            }
            if (!getResources().getBoolean(R.bool.config_lock_pattern_minimal_ui)) {
                this.mOptionsButton = (Button) view.findViewById(R.id.screen_lock_options);
                this.mOptionsButton.setOnClickListener(new -$$Lambda$SetupChooseLockPattern$SetupChooseLockPatternFragment$oe1sL-LLbUw3chjlv8P3cpGYEWs(this));
            }
            if (!this.mForFingerprint) {
                Button skipButton = (Button) view.findViewById(R.id.skip_button);
                skipButton.setVisibility(0);
                skipButton.setOnClickListener(new -$$Lambda$SetupChooseLockPattern$SetupChooseLockPatternFragment$klleXh-HZ7yoRQxNNtN-WzAt_fY(this));
            }
            TextView message = (TextView) view.findViewById(R.id.message);
            if (message != null) {
                message.setTextColor(getResources().getColor(R.color.oneplus_contorl_text_color_primary_light));
            }
            return view;
        }

        public void onLockTypeSelected(ScreenLockType lock) {
            if (ScreenLockType.PATTERN != lock) {
                startChooseLockActivity(lock, getActivity());
            }
        }

        /* Access modifiers changed, original: protected */
        public void updateStage(Stage stage) {
            super.updateStage(stage);
            if (!getResources().getBoolean(R.bool.config_lock_pattern_minimal_ui) && this.mOptionsButton != null) {
                this.mOptionsButton.setVisibility(stage == Stage.Introduction ? 0 : 4);
            }
        }

        /* Access modifiers changed, original: protected */
        public Intent getRedactionInterstitialIntent(Context context) {
            if (!OPUtils.isSupportUstMode()) {
                return null;
            }
            SetupRedactionInterstitial.setEnabled(context, true);
            return SetupRedactionInterstitial.createStartIntent(context, this.mUserId);
        }
    }

    public static Intent modifyIntentForSetup(Context context, Intent chooseLockPatternIntent) {
        chooseLockPatternIntent.setClass(context, SetupChooseLockPattern.class);
        return chooseLockPatternIntent;
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        return SetupChooseLockPatternFragment.class.getName().equals(fragmentName);
    }

    /* Access modifiers changed, original: protected */
    public void onApplyThemeResource(Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, SetupWizardUtils.getTheme(getIntent()), first);
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        int i;
        super.onCreate(savedInstanceState);
        LinearLayout layout = (LinearLayout) findViewById(R.id.content_parent);
        if (OPUtils.isO2()) {
            getWindow().getDecorView().setSystemUiVisibility(0);
            layout.setFitsSystemWindows(false);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(8192);
            layout.setFitsSystemWindows(true);
        }
        if (getIntent().getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_FOR_FINGERPRINT, false)) {
            i = R.string.lock_settings_picker_fingerprint_message;
        } else {
            i = R.string.lockpassword_choose_your_screen_lock_header;
        }
        setTitle(i);
    }

    /* Access modifiers changed, original: 0000 */
    public Class<? extends Fragment> getFragmentClass() {
        return SetupChooseLockPatternFragment.class;
    }
}
