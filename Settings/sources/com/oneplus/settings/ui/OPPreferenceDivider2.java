package com.oneplus.settings.ui;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import com.android.settings.R;

public class OPPreferenceDivider2 extends PreferenceCategory {
    public OPPreferenceDivider2(Context context) {
        super(context);
        initViews(context);
    }

    public OPPreferenceDivider2(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPPreferenceDivider2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        setLayoutResource(R.layout.op_preference_divider);
    }
}
