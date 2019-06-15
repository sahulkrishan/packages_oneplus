package com.android.settings.development.featureflags;

import android.content.Context;
import android.support.v14.preference.SwitchPreference;
import android.util.FeatureFlagUtils;

public class FeatureFlagPreference extends SwitchPreference {
    private final String mKey;

    public FeatureFlagPreference(Context context, String key) {
        super(context);
        this.mKey = key;
        setKey(key);
        setTitle((CharSequence) key);
        setCheckedInternal(FeatureFlagUtils.isEnabled(context, this.mKey));
    }

    public void setChecked(boolean isChecked) {
        setCheckedInternal(isChecked);
        FeatureFlagUtils.setEnabled(getContext(), this.mKey, isChecked);
    }

    private void setCheckedInternal(boolean isChecked) {
        super.setChecked(isChecked);
        setSummary((CharSequence) Boolean.toString(isChecked));
    }
}
