package com.oneplus.settings.ui;

import android.content.Context;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.settings.R;

public class OPNightModeLevelPreferenceCategory extends Preference {
    private static final int SEEKBAR_MAX = 132;
    private Context mContext;
    OPNightModeLevelPreferenceChangeListener mOPNightModeLevelPreferenceChangeListener;
    private SeekBar mSeekBar;

    public interface OPNightModeLevelPreferenceChangeListener {
        void onProgressChanged(SeekBar seekBar, int i, boolean z);

        void onStartTrackingTouch(SeekBar seekBar);

        void onStopTrackingTouch(SeekBar seekBar);
    }

    public OPNightModeLevelPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        setLayoutResource(R.layout.op_night_mode_level_preference_category);
    }

    public OPNightModeLevelPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OPNightModeLevelPreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OPNightModeLevelPreferenceCategory(Context context) {
        this(context, null);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.setDividerAllowedBelow(false);
        view.setDividerAllowedAbove(false);
        initSeekBar(view);
    }

    public void setSeekBarProgress(int progress) {
        if (this.mSeekBar != null) {
            this.mSeekBar.setProgress(progress);
        }
    }

    private void initSeekBar(PreferenceViewHolder view) {
        this.mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        this.mSeekBar.setMax(132);
        this.mSeekBar.setProgress(System.getIntForUser(this.mContext.getContentResolver(), "oem_nightmode_progress_status", 103, -2));
        this.mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener != null) {
                    OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener.onProgressChanged(seekBar, progress, fromUser);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                if (OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener != null) {
                    OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener.onStartTrackingTouch(seekBar);
                }
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener != null) {
                    OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener.onStopTrackingTouch(seekBar);
                }
            }
        });
    }

    public void setOPNightModeLevelSeekBarChangeListener(OPNightModeLevelPreferenceChangeListener listener) {
        this.mOPNightModeLevelPreferenceChangeListener = listener;
    }
}
