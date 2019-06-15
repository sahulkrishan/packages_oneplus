package com.oneplus.settings.ui;

import android.content.Context;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;
import com.oneplus.lib.widget.OPSeekBar;
import com.oneplus.lib.widget.OPSeekBar.OnSeekBarChangeListener;
import com.oneplus.settings.better.OPNightMode;
import com.oneplus.settings.utils.OPUtils;
import com.oneplus.settings.widget.OPSeekBar2;

public class OPNightModeLevelPreferenceCategory extends Preference {
    public static final int MAX_BRIGHTNESS_PROGRESS = 100;
    private static final int MAX_COLOR_PROGRESS = 132;
    private static final int MAX_COLOR_PROGRESS_INTERPOLATER = 61;
    private Context mContext;
    private boolean mEnabled;
    OPNightModeLevelPreferenceChangeListener mOPNightModeLevelPreferenceChangeListener;
    private OPSeekBar2 mSeekBarBrightness;
    private OPSeekBar2 mSeekBarColor;
    private TableRow mTRBrightness;
    private TableRow mTRBrightnessTitle;
    private TableRow mTRColor;
    private TextView mTVBrightnessStrong;
    private TextView mTVBrightnessTitle;
    private TextView mTVColorStrong;
    private TextView mTVColorTitle;
    private Toast mToastTip;

    public interface OPNightModeLevelPreferenceChangeListener {
        void onBrightnessProgressChanged(int i, boolean z);

        void onBrightnessStartTrackingTouch(int i);

        void onBrightnessStopTrackingTouch(int i);

        void onColorProgressChanged(int i, boolean z);

        void onColorStartTrackingTouch(int i);

        void onColorStopTrackingTouch(int i);
    }

    public OPNightModeLevelPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mEnabled = false;
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
        view.itemView.setBackgroundColor(0);
        initView(view);
    }

    private void initView(PreferenceViewHolder view) {
        int colorProgress = System.getIntForUser(this.mContext.getContentResolver(), "oem_nightmode_progress_status", OPNightMode.DEFAULT_COLOR_PROGRESS, -2);
        float brightnessValue = System.getFloatForUser(this.mContext.getContentResolver(), OPNightMode.KEY_NIGHT_MODE_BRIGHTNESS, 1.0f, -2);
        this.mTVColorTitle = (TextView) view.findViewById(R.id.tv_title_color_temperature);
        this.mTVBrightnessTitle = (TextView) view.findViewById(R.id.tv_title_brightness);
        this.mTVColorStrong = (TextView) view.findViewById(R.id.tv_color_temperature_strong);
        this.mTVBrightnessStrong = (TextView) view.findViewById(R.id.tv_brightness_strong);
        this.mTRColor = (TableRow) view.findViewById(R.id.tr_color_temperature);
        this.mTRBrightness = (TableRow) view.findViewById(R.id.tr_brightness);
        this.mTRBrightnessTitle = (TableRow) view.findViewById(R.id.tr_brightness_title);
        this.mSeekBarColor = (OPSeekBar2) view.findViewById(R.id.seekbar_color_temperature);
        if (OPUtils.isSupportReadingModeInterpolater()) {
            this.mSeekBarColor.setMax(61);
        } else {
            this.mSeekBarColor.setMax(132);
        }
        this.mSeekBarColor.setProgress(colorProgress);
        this.mSeekBarBrightness = (OPSeekBar2) view.findViewById(R.id.seekbar_brightness);
        this.mSeekBarBrightness.setMax(100);
        this.mSeekBarBrightness.setProgress(OPNightMode.transferToBrightnessProgress(brightnessValue));
        this.mSeekBarColor.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(OPSeekBar seekBar, int progress, boolean fromUser) {
                if (OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener != null) {
                    OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener.onColorProgressChanged(progress, fromUser);
                }
            }

            public void onStartTrackingTouch(OPSeekBar seekBar) {
                if (OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener != null) {
                    OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener.onColorStartTrackingTouch(seekBar.getProgress());
                }
            }

            public void onStopTrackingTouch(OPSeekBar seekBar) {
                if (OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener != null) {
                    OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener.onColorStopTrackingTouch(seekBar.getProgress());
                }
            }
        });
        this.mSeekBarBrightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(OPSeekBar seekBar, int progress, boolean fromUser) {
                if (OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener != null) {
                    OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener.onBrightnessProgressChanged(progress, fromUser);
                }
            }

            public void onStartTrackingTouch(OPSeekBar seekBar) {
                if (OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener != null) {
                    OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener.onBrightnessStartTrackingTouch(seekBar.getProgress());
                }
            }

            public void onStopTrackingTouch(OPSeekBar seekBar) {
                if (OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener != null) {
                    OPNightModeLevelPreferenceCategory.this.mOPNightModeLevelPreferenceChangeListener.onBrightnessStopTrackingTouch(seekBar.getProgress());
                }
            }
        });
        this.mTRColor.setOnTouchListener(new -$$Lambda$OPNightModeLevelPreferenceCategory$AJbSS0doH-xFVa-rMVeyp40DQVA(this));
        this.mTRBrightness.setOnTouchListener(new -$$Lambda$OPNightModeLevelPreferenceCategory$1BCHIWEUrGQw8a_X2Omzpk5EIQ0(this));
        if (!OPUtils.isSupportReadingModeInterpolater()) {
            this.mTRBrightness.setVisibility(4);
            this.mTRBrightnessTitle.setVisibility(4);
        }
        boolean z = isNightDisplayActivated() && !isWellbeingGrayscaleActivated();
        setEnabled(z);
    }

    public static /* synthetic */ boolean lambda$initView$0(OPNightModeLevelPreferenceCategory oPNightModeLevelPreferenceCategory, View v, MotionEvent event) {
        if (oPNightModeLevelPreferenceCategory.mEnabled || event.getAction() != 0) {
            return false;
        }
        oPNightModeLevelPreferenceCategory.showTurnOnTip();
        return true;
    }

    public static /* synthetic */ boolean lambda$initView$1(OPNightModeLevelPreferenceCategory oPNightModeLevelPreferenceCategory, View v, MotionEvent event) {
        if (oPNightModeLevelPreferenceCategory.mEnabled || event.getAction() != 0) {
            return false;
        }
        oPNightModeLevelPreferenceCategory.showTurnOnTip();
        return true;
    }

    private void showTurnOnTip() {
        if (this.mToastTip != null) {
            this.mToastTip.cancel();
        }
        if (isWellbeingGrayscaleActivated()) {
            this.mToastTip = Toast.makeText(this.mContext, this.mContext.getString(R.string.oneplus_wellbeing_grayscale_open_tip), 0);
        } else {
            this.mToastTip = Toast.makeText(this.mContext, this.mContext.getString(R.string.oneplus_night_mode_open_tip), 0);
        }
        this.mToastTip.show();
    }

    public int getColorProgress() {
        return this.mSeekBarColor.getProgress();
    }

    public int getBrightnessProgress() {
        return this.mSeekBarBrightness.getProgress();
    }

    public void setProgress(int colorProgress, int brightnessProgress) {
        if (this.mSeekBarColor != null) {
            this.mSeekBarColor.setProgress(colorProgress);
        }
        if (this.mSeekBarBrightness != null) {
            this.mSeekBarBrightness.setProgress(brightnessProgress);
        }
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
        if (this.mSeekBarColor != null) {
            this.mSeekBarColor.setActivated(enabled);
            this.mSeekBarColor.setEnabled(enabled);
        }
        if (this.mSeekBarBrightness != null) {
            this.mSeekBarBrightness.setEnabled(enabled);
            this.mSeekBarBrightness.setEnabled(enabled);
        }
        if (!(this.mTVColorTitle == null || this.mTVBrightnessTitle == null || this.mTVColorStrong == null || this.mTVBrightnessStrong == null || !enabled)) {
            this.mTVColorTitle.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_primary_default));
            this.mTVBrightnessTitle.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_primary_default));
            this.mTVColorStrong.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_secondary_default));
            this.mTVBrightnessStrong.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_secondary_default));
        }
        if (this.mTVColorTitle != null && this.mTVBrightnessTitle != null && this.mTVColorStrong != null && this.mTVBrightnessStrong != null && !enabled) {
            this.mTVColorTitle.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_secondary_default));
            this.mTVBrightnessTitle.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_secondary_default));
            this.mTVColorStrong.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_disable_default));
            this.mTVBrightnessStrong.setTextColor(this.mContext.getResources().getColor(R.color.oneplus_contorl_text_color_disable_default));
        }
    }

    private boolean isNightDisplayActivated() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), "night_display_activated", 0, -2) == 1;
    }

    private boolean isWellbeingGrayscaleActivated() {
        return System.getInt(this.mContext.getContentResolver(), "accessibility_display_grayscale_enabled", 1) == 0;
    }

    public void setOPNightModeLevelSeekBarChangeListener(OPNightModeLevelPreferenceChangeListener listener) {
        this.mOPNightModeLevelPreferenceChangeListener = listener;
    }

    public int getColorProgressMax() {
        return this.mSeekBarColor.getMax();
    }

    public int getBrightnessProgressMax() {
        return this.mSeekBarBrightness.getMax();
    }
}
