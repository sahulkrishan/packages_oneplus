package com.android.settings.ui;

import android.content.Context;
import android.support.v7.preference.PreferenceCategory;
import android.util.AttributeSet;
import com.android.settings.R;

public class OPSuwPreferenceCategory extends PreferenceCategory {
    private Context mContext;

    public OPSuwPreferenceCategory(Context context) {
        super(context);
        initViews(context);
    }

    public OPSuwPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPSuwPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        setLayoutResource(R.layout.op_suw_preference_category_material);
    }
}
