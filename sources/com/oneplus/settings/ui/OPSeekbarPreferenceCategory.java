package com.oneplus.settings.ui;

import android.content.Context;
import android.util.AttributeSet;
import com.android.settings.R;
import com.android.settingslib.RestrictedPreference;

public class OPSeekbarPreferenceCategory extends RestrictedPreference {
    public OPSeekbarPreferenceCategory(Context context) {
        super(context);
        initViews(context);
    }

    public OPSeekbarPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPSeekbarPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        setLayoutResource(R.layout.op_seekbar_preference_category);
    }
}
