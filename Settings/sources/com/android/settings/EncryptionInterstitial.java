package com.android.settings;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.setupwizardlib.GlifLayout;
import java.util.List;

public class EncryptionInterstitial extends SettingsActivity {
    private static final int CHOOSE_LOCK_REQUEST = 100;
    protected static final String EXTRA_PASSWORD_QUALITY = "extra_password_quality";
    public static final String EXTRA_REQUIRE_PASSWORD = "extra_require_password";
    protected static final String EXTRA_UNLOCK_METHOD_INTENT = "extra_unlock_method_intent";
    private static final String TAG = EncryptionInterstitial.class.getSimpleName();

    public static class AccessibilityWarningDialogFragment extends InstrumentedDialogFragment implements OnClickListener {
        public static final String TAG = "AccessibilityWarningDialog";

        public static AccessibilityWarningDialogFragment newInstance(int passwordQuality) {
            AccessibilityWarningDialogFragment fragment = new AccessibilityWarningDialogFragment();
            Bundle args = new Bundle(1);
            args.putInt(EncryptionInterstitial.EXTRA_PASSWORD_QUALITY, passwordQuality);
            fragment.setArguments(args);
            return fragment;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int messageId;
            CharSequence exampleAccessibility;
            int i = getArguments().getInt(EncryptionInterstitial.EXTRA_PASSWORD_QUALITY);
            if (i == 65536) {
                i = R.string.encrypt_talkback_dialog_require_pattern;
                messageId = R.string.encrypt_talkback_dialog_message_pattern;
            } else if (i == 131072 || i == 196608) {
                i = R.string.encrypt_talkback_dialog_require_pin;
                messageId = R.string.encrypt_talkback_dialog_message_pin;
            } else {
                i = R.string.encrypt_talkback_dialog_require_password;
                messageId = R.string.encrypt_talkback_dialog_message_password;
            }
            Activity activity = getActivity();
            List<AccessibilityServiceInfo> list = AccessibilityManager.getInstance(activity).getEnabledAccessibilityServiceList(-1);
            if (list.isEmpty()) {
                exampleAccessibility = "";
            } else {
                exampleAccessibility = ((AccessibilityServiceInfo) list.get(0)).getResolveInfo().loadLabel(activity.getPackageManager());
            }
            return new Builder(activity).setTitle(i).setMessage(getString(messageId, new Object[]{exampleAccessibility})).setCancelable(true).setPositiveButton(17039370, this).setNegativeButton(17039360, this).create();
        }

        public int getMetricsCategory() {
            return 581;
        }

        public void onClick(DialogInterface dialog, int which) {
            EncryptionInterstitialFragment fragment = (EncryptionInterstitialFragment) getParentFragment();
            if (fragment == null) {
                return;
            }
            if (which == -1) {
                fragment.setRequirePasswordState(true);
                fragment.startLockIntent();
            } else if (which == -2) {
                fragment.setRequirePasswordState(false);
            }
        }
    }

    public static class EncryptionInterstitialFragment extends InstrumentedFragment implements View.OnClickListener {
        private View mDontRequirePasswordToDecrypt;
        private boolean mPasswordRequired;
        private int mRequestedPasswordQuality;
        private View mRequirePasswordToDecrypt;
        private Intent mUnlockMethodIntent;

        public int getMetricsCategory() {
            return 48;
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.encryption_interstitial, container, false);
        }

        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            this.mRequirePasswordToDecrypt = view.findViewById(R.id.encrypt_require_password);
            this.mDontRequirePasswordToDecrypt = view.findViewById(R.id.encrypt_dont_require_password);
            boolean forFingerprint = getActivity().getIntent().getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_FOR_FINGERPRINT, false);
            Intent intent = getActivity().getIntent();
            this.mRequestedPasswordQuality = intent.getIntExtra(EncryptionInterstitial.EXTRA_PASSWORD_QUALITY, 0);
            this.mUnlockMethodIntent = (Intent) intent.getParcelableExtra(EncryptionInterstitial.EXTRA_UNLOCK_METHOD_INTENT);
            int i = this.mRequestedPasswordQuality;
            if (i != 65536) {
                if (i == 131072 || i == 196608) {
                    if (forFingerprint) {
                        i = R.string.encryption_interstitial_message_pin_for_fingerprint;
                    } else {
                        i = R.string.encryption_interstitial_message_pin;
                    }
                } else if (forFingerprint) {
                    i = R.string.encryption_interstitial_message_password_for_fingerprint;
                } else {
                    i = R.string.encryption_interstitial_message_password;
                }
            } else if (forFingerprint) {
                i = R.string.encryption_interstitial_message_pattern_for_fingerprint;
            } else {
                i = R.string.encryption_interstitial_message_pattern;
            }
            ((TextView) getActivity().findViewById(R.id.encryption_message)).setText(i);
            this.mRequirePasswordToDecrypt.setOnClickListener(this);
            this.mDontRequirePasswordToDecrypt.setOnClickListener(this);
            setRequirePasswordState(getActivity().getIntent().getBooleanExtra(EncryptionInterstitial.EXTRA_REQUIRE_PASSWORD, true));
            ((GlifLayout) view).setHeaderText(getActivity().getTitle());
        }

        /* Access modifiers changed, original: protected */
        public void startLockIntent() {
            if (this.mUnlockMethodIntent != null) {
                this.mUnlockMethodIntent.putExtra(EncryptionInterstitial.EXTRA_REQUIRE_PASSWORD, this.mPasswordRequired);
                startActivityForResult(this.mUnlockMethodIntent, 100);
                return;
            }
            Log.wtf(EncryptionInterstitial.TAG, "no unlock intent to start");
            finish();
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 100 && resultCode != 0) {
                getActivity().setResult(resultCode, data);
                finish();
            }
        }

        public void onClick(View view) {
            if (view != this.mRequirePasswordToDecrypt) {
                setRequirePasswordState(false);
                startLockIntent();
            } else if (!AccessibilityManager.getInstance(getActivity()).isEnabled() || this.mPasswordRequired) {
                setRequirePasswordState(true);
                startLockIntent();
            } else {
                setRequirePasswordState(false);
                AccessibilityWarningDialogFragment.newInstance(this.mRequestedPasswordQuality).show(getChildFragmentManager(), AccessibilityWarningDialogFragment.TAG);
            }
        }

        private void setRequirePasswordState(boolean required) {
            this.mPasswordRequired = required;
        }

        public void finish() {
            Activity activity = getActivity();
            if (activity != null) {
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStack();
                } else {
                    activity.finish();
                }
            }
        }
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, EncryptionInterstitialFragment.class.getName());
        return modIntent;
    }

    /* Access modifiers changed, original: protected */
    public void onApplyThemeResource(Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, SetupWizardUtils.getTheme(getIntent()), first);
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        return EncryptionInterstitialFragment.class.getName().equals(fragmentName);
    }

    public static Intent createStartIntent(Context ctx, int quality, boolean requirePasswordDefault, Intent unlockMethodIntent) {
        return new Intent(ctx, EncryptionInterstitial.class).putExtra(EXTRA_PASSWORD_QUALITY, quality).putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RESID, R.string.encryption_interstitial_header).putExtra(EXTRA_REQUIRE_PASSWORD, requirePasswordDefault).putExtra(EXTRA_UNLOCK_METHOD_INTENT, unlockMethodIntent);
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        ((LinearLayout) findViewById(R.id.content_parent)).setFitsSystemWindows(false);
    }
}
