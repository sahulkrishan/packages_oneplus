package com.oneplus.settings.ui;

import android.content.Context;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.settings.R;
import com.oneplus.settings.better.OPScreenColorMode;
import com.oneplus.settings.utils.OPUtils;

public class OPSeekBarPreference extends Preference {
    private Context mContext;
    OPColorModeSeekBarChangeListener mOPColorModeSeekBarChangeListener;
    private SeekBar mSeekBar;

    public interface OPColorModeSeekBarChangeListener {
        void onProgressChanged(SeekBar seekBar, int i, boolean z);

        void onStartTrackingTouch(SeekBar seekBar);

        void onStopTrackingTouch(SeekBar seekBar);
    }

    public OPSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        setLayoutResource(R.layout.op_seekpreference);
    }

    public OPSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OPSeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OPSeekBarPreference(Context context) {
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
        if (OPUtils.isSupportReadingModeInterpolater()) {
            this.mSeekBar.setMax(56);
        } else {
            this.mSeekBar.setMax(100);
        }
        this.mSeekBar.setProgress(System.getInt(this.mContext.getContentResolver(), "oem_screen_better_value", OPScreenColorMode.DEFAULT_COLOR_PROGRESS));
        this.mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (OPSeekBarPreference.this.mOPColorModeSeekBarChangeListener != null) {
                    OPSeekBarPreference.this.mOPColorModeSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                if (OPSeekBarPreference.this.mOPColorModeSeekBarChangeListener != null) {
                    OPSeekBarPreference.this.mOPColorModeSeekBarChangeListener.onStartTrackingTouch(seekBar);
                }
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (OPSeekBarPreference.this.mOPColorModeSeekBarChangeListener != null) {
                    OPSeekBarPreference.this.mOPColorModeSeekBarChangeListener.onStopTrackingTouch(seekBar);
                }
            }
        });
    }

    public void setOPColorModeSeekBarChangeListener(OPColorModeSeekBarChangeListener listener) {
        this.mOPColorModeSeekBarChangeListener = listener;
    }

    public int getSeekBarMax() {
        return this.mSeekBar.getMax();
    }
}
