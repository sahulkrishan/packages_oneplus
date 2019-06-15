package com.oneplus.settings.ui;

import android.content.Context;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settingslib.RestrictedPreference;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.better.OPReadingMode;

public class OPScreenColorModeSummary extends RestrictedPreference {
    private Context mContext;
    private TextView mSummary;

    public OPScreenColorModeSummary(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        setLayoutResource(R.layout.op_screen_color_mode_summary);
    }

    public OPScreenColorModeSummary(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OPScreenColorModeSummary(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OPScreenColorModeSummary(Context context) {
        this(context, null);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mSummary = (TextView) view.findViewById(R.id.summary);
        boolean readingmodeenabled = true;
        boolean nightmodeenabled = Secure.getInt(this.mContext.getContentResolver(), "night_display_activated", 0) != 1;
        if (System.getInt(this.mContext.getContentResolver(), OPReadingMode.READING_MODE_STATUS_MANUAL, 0) == 1) {
            readingmodeenabled = false;
        }
        if (!nightmodeenabled) {
            this.mSummary.setText(SettingsBaseApplication.mApplication.getText(R.string.oneplus_screen_color_mode_title_summary));
        }
        if (!readingmodeenabled) {
            this.mSummary.setText(SettingsBaseApplication.mApplication.getText(R.string.oneplus_screen_color_mode_reading_mode_on_summary));
        }
    }

    public void setTextSummary(String summary) {
        if (this.mSummary != null) {
            this.mSummary.setText(summary);
        }
        notifyChanged();
    }

    public void setSummary(CharSequence summary) {
        setTextSummary(summary.toString());
    }
}
