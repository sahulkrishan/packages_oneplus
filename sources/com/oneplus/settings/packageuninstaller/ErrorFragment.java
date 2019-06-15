package com.oneplus.settings.packageuninstaller;

import android.os.Bundle;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist.Guidance;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v17.leanback.widget.GuidedAction.Builder;
import com.android.settings.R;
import java.util.List;

public class ErrorFragment extends GuidedStepFragment {
    public static final String TEXT = "com.android.packageinstaller.arg.text";
    public static final String TITLE = "com.android.packageinstaller.arg.title";

    public int onProvideTheme() {
        return R.style.f932Theme.Leanback.GuidedStep;
    }

    public Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new Guidance(getString(getArguments().getInt("com.android.packageinstaller.arg.title")), getString(getArguments().getInt("com.android.packageinstaller.arg.text")), null, null);
    }

    public void onCreateActions(List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(((Builder) new Builder(getContext()).clickAction(-4)).build());
    }

    public void onGuidedActionClicked(GuidedAction action) {
        if (isAdded()) {
            if (getActivity() instanceof UninstallerActivity) {
                ((UninstallerActivity) getActivity()).dispatchAborted();
            }
            getActivity().setResult(1);
            getActivity().finish();
        }
    }
}
