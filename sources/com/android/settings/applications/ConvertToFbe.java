package com.android.settings.applications;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.password.ChooseLockSettingsHelper;

public class ConvertToFbe extends InstrumentedFragment {
    private static final int KEYGUARD_REQUEST = 55;
    static final String TAG = "ConvertToFBE";

    private boolean runKeyguardConfirmation(int request) {
        return new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(request, getActivity().getResources().getText(R.string.convert_to_file_encryption));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.convert_to_file_encryption);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.convert_fbe, null);
        ((Button) rootView.findViewById(R.id.button_convert_fbe)).setOnClickListener(new -$$Lambda$ConvertToFbe$cKWuNkHe-dkbg8HKJCoDk07_9og(this));
        return rootView;
    }

    public static /* synthetic */ void lambda$onCreateView$0(ConvertToFbe convertToFbe, View v) {
        if (!convertToFbe.runKeyguardConfirmation(55)) {
            convertToFbe.convert();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 55 && resultCode == -1) {
            convert();
        }
    }

    private void convert() {
        new SubSettingLauncher(getContext()).setDestination(ConfirmConvertToFbe.class.getName()).setTitle((int) R.string.convert_to_file_encryption).setSourceMetricsCategory(getMetricsCategory()).launch();
    }

    public int getMetricsCategory() {
        return 402;
    }
}
