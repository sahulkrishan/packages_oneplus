package com.oneplus.settings.ui;

import android.content.Context;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.settings.R;

public class OPSuwSeekBarPreference extends Preference {
    private Context mContext;
    OPColorModeSeekBarChangeListener mOPColorModeSeekBarChangeListener;
    private SeekBar mSeekBar;

    public interface OPColorModeSeekBarChangeListener {
        void onProgressChanged(SeekBar seekBar, int i, boolean z);

        void onStartTrackingTouch(SeekBar seekBar);

        void onStopTrackingTouch(SeekBar seekBar);
    }

    public OPSuwSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        setLayoutResource(R.layout.op_suw_seekpreference);
    }

    public OPSuwSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OPSuwSeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OPSuwSeekBarPreference(Context context) {
        this(context, null);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.setDividerAllowedBelow(false);
        view.setDividerAllowedAbove(false);
        initSeekBar(view);
    }

    private void initSeekBar(PreferenceViewHolder view) {
        this.mSeekBar = (SeekBar) view.findViewById(R.id.screen_color_mode_seekbar);
        this.mSeekBar.setMax(100);
        this.mSeekBar.setProgress(System.getInt(this.mContext.getContentResolver(), "oem_screen_better_value", 43));
        this.mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (OPSuwSeekBarPreference.this.mOPColorModeSeekBarChangeListener != null) {
                    OPSuwSeekBarPreference.this.mOPColorModeSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                if (OPSuwSeekBarPreference.this.mOPColorModeSeekBarChangeListener != null) {
                    OPSuwSeekBarPreference.this.mOPColorModeSeekBarChangeListener.onStartTrackingTouch(seekBar);
                }
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (OPSuwSeekBarPreference.this.mOPColorModeSeekBarChangeListener != null) {
                    OPSuwSeekBarPreference.this.mOPColorModeSeekBarChangeListener.onStopTrackingTouch(seekBar);
                }
            }
        });
    }

    public void setOPColorModeSeekBarChangeListener(OPColorModeSeekBarChangeListener listener) {
        this.mOPColorModeSeekBarChangeListener = listener;
    }
}
