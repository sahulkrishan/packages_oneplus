package com.android.settings.widget;

import android.content.Context;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import com.android.settings.R;

public class AppSwitchPreference extends SwitchPreference {
    public AppSwitchPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference_app);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.findViewById(R.id.summary_container).setVisibility(TextUtils.isEmpty(getSummary()) ? 8 : 0);
    }
}
