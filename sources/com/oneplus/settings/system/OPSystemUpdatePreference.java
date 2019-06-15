package com.oneplus.settings.system;

import android.content.Context;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import com.android.settings.R;

public class OPSystemUpdatePreference extends Preference {
    public static final String HAS_NEW_VERSION_TO_UPDATE = "has_new_version_to_update";
    private Context mContext;

    public OPSystemUpdatePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    public OPSystemUpdatePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPSystemUpdatePreference(Context context) {
        super(context);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        setWidgetLayoutResource(R.layout.op_layout_sys_update_icon);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        int i = 0;
        boolean z = true;
        if (System.getInt(this.mContext.getContentResolver(), "has_new_version_to_update", 0) != 1) {
            z = false;
        }
        boolean systemHasUpdate = z;
        View updateIcon = holder.findViewById(16908312);
        updateIcon.setOnClickListener(null);
        if (!systemHasUpdate) {
            i = 8;
        }
        updateIcon.setVisibility(i);
    }

    public void updateView() {
        notifyChanged();
    }
}
