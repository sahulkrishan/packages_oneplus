package com.oneplus.settings.timer.timepower;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View.OnClickListener;
import com.android.settings.R;

public class TimepowerPreference extends Preference {
    private OnClickListener mSettingsViewClicklistener;

    public void setViewClickListener(OnClickListener listener) {
        this.mSettingsViewClicklistener = listener;
    }

    public TimepowerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.op_timepower_preference_layout);
    }

    public TimepowerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842894);
    }

    public TimepowerPreference(Context context) {
        this(context, null);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.findViewById(R.id.time_power_pref).setClickable(false);
        view.findViewById(R.id.time_power_settings).setOnClickListener(this.mSettingsViewClicklistener);
    }
}
