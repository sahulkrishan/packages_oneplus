package com.oneplus.settings.ui;

import android.content.Context;
import android.util.AttributeSet;
import com.android.settings.R;
import com.android.settingslib.RestrictedSwitchPreference;

public class OPRestrictedSwitchPreference extends RestrictedSwitchPreference {
    public OPRestrictedSwitchPreference(Context context) {
        super(context);
        initViews(context);
    }

    public OPRestrictedSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPRestrictedSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        setLayoutResource(R.layout.op_preference_material);
        setWidgetLayoutResource(R.layout.op_preference_widget_switch);
    }
}
