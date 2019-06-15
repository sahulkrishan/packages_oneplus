package com.oneplus.settings.ui;

import android.content.Context;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.settings.R;

public class OPNightModeLevelPreference extends Preference {
    private static final int SEEKBAR_MAX = 600;
    private Context mContext;
    OPNightModeLevelPreferenceChangeListener mOPNightModeLevelPreferenceChangeListener;
    private SeekBar mSeekBar;

    public interface OPNightModeLevelPreferenceChangeListener {
        void onProgressChanged(SeekBar seekBar, int i, boolean z);

        void onStartTrackingTouch(SeekBar seekBar);

        void onStopTrackingTouch(SeekBar seekBar);
    }

    public OPNightModeLevelPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        setLayoutResource(R.layout.op_night_mode_level_preference);
    }

    public OPNightModeLevelPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OPNightModeLevelPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OPNightModeLevelPreference(Context context) {
        this(context, null);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        initSeekBar(view);
    }

    private void initSeekBar(PreferenceViewHolder view) {
        this.mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        this.mSeekBar.setMax(600);
        this.mSeekBar.setProgress(System.getInt(this.mContext.getContentResolver(), "oem_nightmode_progress_status", 400));
        this.mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (OPNightModeLevelPreference.this.mOPNightModeLevelPreferenceChangeListener != null) {
                    OPNightModeLevelPreference.this.mOPNightModeLevelPreferenceChangeListener.onProgressChanged(seekBar, progress, fromUser);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                if (OPNightModeLevelPreference.this.mOPNightModeLevelPreferenceChangeListener != null) {
                    OPNightModeLevelPreference.this.mOPNightModeLevelPreferenceChangeListener.onStartTrackingTouch(seekBar);
                }
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (OPNightModeLevelPreference.this.mOPNightModeLevelPreferenceChangeListener != null) {
                    OPNightModeLevelPreference.this.mOPNightModeLevelPreferenceChangeListener.onStopTrackingTouch(seekBar);
                }
            }
        });
    }

    public void setOPColorModeSeekBarChangeListener(OPNightModeLevelPreferenceChangeListener listener) {
        this.mOPNightModeLevelPreferenceChangeListener = listener;
    }
}
