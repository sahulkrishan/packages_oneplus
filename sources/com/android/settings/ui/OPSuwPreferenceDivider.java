package com.android.settings.ui;

import android.content.Context;
import android.support.v7.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.util.OpFeatures;
import com.android.settings.R;

public class OPSuwPreferenceDivider extends PreferenceCategory {
    private Context mContext;

    public OPSuwPreferenceDivider(Context context) {
        super(context);
        initViews(context);
    }

    public OPSuwPreferenceDivider(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPSuwPreferenceDivider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        if (OpFeatures.isSupport(new int[]{1})) {
            setLayoutResource(R.layout.op_suw_preference_divider);
        } else {
            setLayoutResource(R.layout.op_ctrl_preference_divider);
        }
    }
}
