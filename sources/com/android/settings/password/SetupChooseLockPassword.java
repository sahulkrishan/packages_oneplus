package com.android.settings.password;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.SetupRedactionInterstitial;
import com.android.settings.SetupWizardUtils;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.password.ChooseLockPassword.ChooseLockPasswordFragment;
import com.android.settings.password.ChooseLockTypeDialogFragment.OnLockTypeSelectedListener;
import com.oneplus.settings.utils.OPUtils;

public class SetupChooseLockPassword extends ChooseLockPassword {
    private static final String TAG = "SetupChooseLockPassword";

    public static class SetupChooseLockPasswordFragment extends ChooseLockPasswordFragment implements OnLockTypeSelectedListener {
        @Nullable
        private Button mOptionsButton;

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.setup_choose_lock_password, container, false);
        }

        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            Activity activity = getActivity();
            boolean anyOptionsShown = new ChooseLockGenericController(activity, this.mUserId).getVisibleScreenLockTypes(65536, false).size() > 0;
            boolean showOptionsButton = activity.getIntent().getBooleanExtra(ChooseLockGenericFragment.EXTRA_SHOW_OPTIONS_BUTTON, false);
            if (!anyOptionsShown) {
                Log.w(SetupChooseLockPassword.TAG, "Visible screen lock types is empty!");
            }
            if (showOptionsButton && anyOptionsShown) {
                this.mOptionsButton = (Button) view.findViewById(R.id.screen_lock_options);
                this.mOptionsButton.setVisibility(0);
                this.mOptionsButton.setOnClickListener(this);
            }
            TextView message = (TextView) view.findViewById(R.id.message);
            if (message != null) {
                message.setTextColor(getResources().getColor(R.color.oneplus_contorl_text_color_primary_light));
            }
            if (this.mAutoCheckPinLengthCheckBox != null) {
                this.mAutoCheckPinLengthCheckBox.setTextColor(getResources().getColor(R.color.oneplus_contorl_text_color_secondary_light));
            }
        }

        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.screen_lock_options) {
                ChooseLockTypeDialogFragment.newInstance(this.mUserId).show(getChildFragmentManager(), null);
            } else if (id != R.id.skip_button) {
                super.onClick(v);
            } else {
                SetupSkipDialog.newInstance(getActivity().getIntent().getBooleanExtra(SetupSkipDialog.EXTRA_FRP_SUPPORTED, false)).show(getFragmentManager());
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

        public void onLockTypeSelected(ScreenLockType lock) {
            if (lock != (this.mIsAlphaMode ? ScreenLockType.PASSWORD : ScreenLockType.PIN)) {
                startChooseLockActivity(lock, getActivity());
            }
        }

        /* Access modifiers changed, original: protected */
        public void updateUi() {
            super.updateUi();
            int i = 0;
            this.mSkipButton.setVisibility(this.mForFingerprint ? 8 : 0);
            if (this.mOptionsButton != null) {
                Button button = this.mOptionsButton;
                if (this.mUiStage != Stage.Introduction) {
                    i = 8;
                }
                button.setVisibility(i);
            }
        }
    }

    public static Intent modifyIntentForSetup(Context context, Intent chooseLockPasswordIntent) {
        chooseLockPasswordIntent.setClass(context, SetupChooseLockPassword.class);
        chooseLockPasswordIntent.putExtra("extra_prefs_show_button_bar", false);
        return chooseLockPasswordIntent;
    }

    /* Access modifiers changed, original: protected */
    public void onApplyThemeResource(Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, SetupWizardUtils.getTheme(getIntent()), first);
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        return SetupChooseLockPasswordFragment.class.getName().equals(fragmentName);
    }

    /* Access modifiers changed, original: 0000 */
    public Class<? extends Fragment> getFragmentClass() {
        return SetupChooseLockPasswordFragment.class;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstance) {
        setTheme(SetupWizardUtils.getTheme(getIntent()));
        super.onCreate(savedInstance);
        LinearLayout layout = (LinearLayout) findViewById(R.id.content_parent);
        if (OPUtils.isO2()) {
            getWindow().getDecorView().setSystemUiVisibility(0);
            layout.setFitsSystemWindows(false);
            return;
        }
        getWindow().getDecorView().setSystemUiVisibility(8192);
        layout.setFitsSystemWindows(true);
    }
}
