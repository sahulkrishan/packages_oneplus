package com.android.settings.password;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SetupEncryptionInterstitial;
import com.android.settings.SetupWizardUtils;
import com.android.settings.fingerprint.SetupFingerprintEnrollFindSensor;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.utils.SettingsDividerItemDecoration;
import com.android.setupwizardlib.GlifPreferenceLayout;
import com.oneplus.settings.utils.OPUtils;

public class SetupChooseLockGeneric extends ChooseLockGeneric {
    private static final String KEY_UNLOCK_SET_DO_LATER = "unlock_set_do_later";

    public static class SetupChooseLockGenericFragment extends ChooseLockGenericFragment {
        public static final String EXTRA_PASSWORD_QUALITY = ":settings:password_quality";

        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            GlifPreferenceLayout layout = (GlifPreferenceLayout) view;
            layout.setDividerItemDecoration(new SettingsDividerItemDecoration(getContext()));
            layout.setDividerInset(getContext().getResources().getDimensionPixelSize(R.dimen.suw_items_glif_text_divider_inset));
            layout.setIcon(getContext().getDrawable(R.drawable.ic_lock));
            int titleResource = this.mForFingerprint ? R.string.lock_settings_picker_title : R.string.setup_lock_settings_picker_title;
            if (getActivity() != null) {
                getActivity().setTitle(titleResource);
            }
            layout.setHeaderText(titleResource);
            setDivider(null);
        }

        /* Access modifiers changed, original: protected */
        public void addHeaderView() {
            if (this.mForFingerprint) {
                setHeaderView((int) R.layout.setup_choose_lock_generic_fingerprint_header);
            } else {
                setHeaderView((int) R.layout.setup_choose_lock_generic_header);
            }
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (data == null) {
                data = new Intent();
            }
            data.putExtra(EXTRA_PASSWORD_QUALITY, new LockPatternUtils(getActivity()).getKeyguardStoredPasswordQuality(UserHandle.myUserId()));
            super.onActivityResult(requestCode, resultCode, data);
        }

        public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
            return ((GlifPreferenceLayout) parent).onCreateRecyclerView(inflater, parent, savedInstanceState);
        }

        /* Access modifiers changed, original: protected */
        public boolean canRunBeforeDeviceProvisioned() {
            return true;
        }

        /* Access modifiers changed, original: protected */
        public void disableUnusablePreferences(int quality, boolean hideDisabled) {
            super.disableUnusablePreferencesImpl(Math.max(quality, 65536), true);
        }

        /* Access modifiers changed, original: protected */
        public void addPreferences() {
            if (this.mForFingerprint) {
                super.addPreferences();
            } else {
                addPreferencesFromResource(R.xml.setup_security_settings_picker);
            }
        }

        public boolean onPreferenceTreeClick(Preference preference) {
            if (!SetupChooseLockGeneric.KEY_UNLOCK_SET_DO_LATER.equals(preference.getKey())) {
                return super.onPreferenceTreeClick(preference);
            }
            SetupSkipDialog.newInstance(getActivity().getIntent().getBooleanExtra(SetupSkipDialog.EXTRA_FRP_SUPPORTED, false)).show(getFragmentManager());
            return true;
        }

        /* Access modifiers changed, original: protected */
        public Intent getLockPasswordIntent(int quality, int minLength, int maxLength) {
            Intent intent = SetupChooseLockPassword.modifyIntentForSetup(getContext(), super.getLockPasswordIntent(quality, minLength, maxLength));
            SetupWizardUtils.copySetupExtras(getActivity().getIntent(), intent);
            return intent;
        }

        /* Access modifiers changed, original: protected */
        public Intent getLockPatternIntent() {
            Intent intent = SetupChooseLockPattern.modifyIntentForSetup(getContext(), super.getLockPatternIntent());
            SetupWizardUtils.copySetupExtras(getActivity().getIntent(), intent);
            return intent;
        }

        /* Access modifiers changed, original: protected */
        public Intent getEncryptionInterstitialIntent(Context context, int quality, boolean required, Intent unlockMethodIntent) {
            Intent intent = SetupEncryptionInterstitial.createStartIntent(context, quality, required, unlockMethodIntent);
            SetupWizardUtils.copySetupExtras(getActivity().getIntent(), intent);
            return intent;
        }

        /* Access modifiers changed, original: protected */
        public Intent getFindSensorIntent(Context context) {
            Intent intent = new Intent(context, SetupFingerprintEnrollFindSensor.class);
            SetupWizardUtils.copySetupExtras(getActivity().getIntent(), intent);
            return intent;
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        return SetupChooseLockGenericFragment.class.getName().equals(fragmentName);
    }

    /* Access modifiers changed, original: 0000 */
    public Class<? extends PreferenceFragment> getFragmentClass() {
        return SetupChooseLockGenericFragment.class;
    }

    /* Access modifiers changed, original: protected */
    public void onApplyThemeResource(Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, SetupWizardUtils.getTheme(getIntent()), first);
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstance) {
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
