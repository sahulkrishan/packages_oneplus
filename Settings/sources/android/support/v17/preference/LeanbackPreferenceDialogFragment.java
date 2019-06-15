package android.support.v17.preference;

import android.app.Fragment;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.DialogPreference.TargetFragment;

public class LeanbackPreferenceDialogFragment extends Fragment {
    public static final String ARG_KEY = "key";
    private DialogPreference mPreference;

    public LeanbackPreferenceDialogFragment() {
        if (VERSION.SDK_INT >= 21) {
            LeanbackPreferenceFragmentTransitionHelperApi21.addTransitions(this);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment rawFragment = getTargetFragment();
        if (!(rawFragment instanceof TargetFragment)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Target fragment ");
            stringBuilder.append(rawFragment);
            stringBuilder.append(" must implement TargetFragment interface");
            throw new IllegalStateException(stringBuilder.toString());
        }
    }

    public DialogPreference getPreference() {
        if (this.mPreference == null) {
            this.mPreference = (DialogPreference) ((TargetFragment) getTargetFragment()).findPreference(getArguments().getString("key"));
        }
        return this.mPreference;
    }
}
