package com.oneplus.settings.ui;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import com.android.settings.R;

public class OPPreferenceDivider extends Preference {
    private LayoutInflater inflater;
    private Context mContext;

    public OPPreferenceDivider(Context context) {
        super(context);
        initViews(context);
    }

    public OPPreferenceDivider(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPPreferenceDivider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        setLayoutResource(R.layout.op_preference_divider);
        setEnabled(false);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.setDividerAllowedBelow(false);
        holder.setDividerAllowedAbove(false);
    }
}
