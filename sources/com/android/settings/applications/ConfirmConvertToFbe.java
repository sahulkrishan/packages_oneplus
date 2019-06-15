package com.android.settings.applications;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class ConfirmConvertToFbe extends SettingsPreferenceFragment {
    static final String TAG = "ConfirmConvertToFBE";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.confirm_convert_fbe, null);
        ((Button) rootView.findViewById(R.id.button_confirm_convert_fbe)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.FACTORY_RESET");
                intent.addFlags(268435456);
                intent.setPackage("android");
                intent.putExtra("android.intent.extra.REASON", "convert_fbe");
                ConfirmConvertToFbe.this.getActivity().sendBroadcast(intent);
            }
        });
        return rootView;
    }

    public int getMetricsCategory() {
        return 403;
    }
}
