package com.android.settings.applications.defaultapps;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.widget.RadioButtonPickerFragment;
import com.android.settings.widget.RadioButtonPreference;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.widget.CandidateInfo;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public abstract class DefaultAppPickerFragment extends RadioButtonPickerFragment {
    protected PackageManagerWrapper mPm;

    public static class ConfirmationDialogFragment extends InstrumentedDialogFragment implements OnClickListener {
        public static final String EXTRA_KEY = "extra_key";
        public static final String EXTRA_MESSAGE = "extra_message";
        public static final String TAG = "DefaultAppConfirm";
        private OnClickListener mCancelListener;

        public int getMetricsCategory() {
            return 791;
        }

        public void init(DefaultAppPickerFragment parent, String key, CharSequence message) {
            Bundle argument = new Bundle();
            argument.putString(EXTRA_KEY, key);
            argument.putCharSequence(EXTRA_MESSAGE, message);
            setArguments(argument);
            setTargetFragment(parent, 0);
        }

        public void setCancelListener(OnClickListener cancelListener) {
            this.mCancelListener = cancelListener;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new Builder(getActivity()).setMessage(getArguments().getCharSequence(EXTRA_MESSAGE)).setPositiveButton(17039370, this).setNegativeButton(17039360, this.mCancelListener).create();
        }

        public void onClick(DialogInterface dialog, int which) {
            Fragment fragment = getTargetFragment();
            if (fragment instanceof DefaultAppPickerFragment) {
                ((DefaultAppPickerFragment) fragment).onRadioButtonConfirmed(getArguments().getString(EXTRA_KEY));
            }
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mPm = new PackageManagerWrapper(context.getPackageManager());
    }

    public void onRadioButtonClicked(RadioButtonPreference selected) {
        String selectedKey = selected.getKey();
        CharSequence confirmationMessage = getConfirmationMessage(getCandidate(selectedKey));
        Activity activity = getActivity();
        if (TextUtils.isEmpty(confirmationMessage)) {
            super.onRadioButtonClicked(selected);
        } else if (activity != null) {
            newConfirmationDialogFragment(selectedKey, confirmationMessage).show(activity.getFragmentManager(), ConfirmationDialogFragment.TAG);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onRadioButtonConfirmed(String selectedKey) {
        this.mMetricsFeatureProvider.action(getContext(), 1000, selectedKey, Pair.create(Integer.valueOf(833), Integer.valueOf(getMetricsCategory())));
        super.onRadioButtonConfirmed(selectedKey);
    }

    public void bindPreferenceExtra(RadioButtonPreference pref, String key, CandidateInfo info, String defaultKey, String systemDefaultKey) {
        if (info instanceof DefaultAppInfo) {
            if (TextUtils.equals(systemDefaultKey, key)) {
                pref.setSummary((int) R.string.system_app);
            } else if (!TextUtils.isEmpty(((DefaultAppInfo) info).summary)) {
                pref.setSummary((CharSequence) ((DefaultAppInfo) info).summary);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public ConfirmationDialogFragment newConfirmationDialogFragment(String selectedKey, CharSequence confirmationMessage) {
        ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
        fragment.init(this, selectedKey, confirmationMessage);
        return fragment;
    }

    /* Access modifiers changed, original: protected */
    public CharSequence getConfirmationMessage(CandidateInfo info) {
        return null;
    }
}
