package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SummaryPreference extends Preference {
    private static final String TAG = "SummaryPreference";
    private String mAmount;
    private boolean mChartEnabled = true;
    private String mEndLabel;
    private float mLeftRatio;
    private float mMiddleRatio;
    private float mRightRatio;
    private String mStartLabel;
    private String mUnits;

    public SummaryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.settings_summary_preference);
    }

    public void setChartEnabled(boolean enabled) {
        if (this.mChartEnabled != enabled) {
            this.mChartEnabled = enabled;
            notifyChanged();
        }
    }

    public void setAmount(String amount) {
        this.mAmount = amount;
        if (this.mAmount != null && this.mUnits != null) {
            setTitle(TextUtils.expandTemplate(getContext().getText(R.string.storage_size_large), new CharSequence[]{this.mAmount, this.mUnits}));
        }
    }

    public void setUnits(String units) {
        this.mUnits = units;
        if (this.mAmount != null && this.mUnits != null) {
            setTitle(TextUtils.expandTemplate(getContext().getText(R.string.storage_size_large), new CharSequence[]{this.mAmount, this.mUnits}));
        }
    }

    public void setLabels(String start, String end) {
        this.mStartLabel = start;
        this.mEndLabel = end;
        notifyChanged();
    }

    public void setRatios(float left, float middle, float right) {
        this.mLeftRatio = left;
        this.mMiddleRatio = middle;
        this.mRightRatio = right;
        notifyChanged();
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ProgressBar colorBar = (ProgressBar) holder.itemView.findViewById(R.id.color_bar);
        if (this.mChartEnabled) {
            colorBar.setVisibility(0);
            int progress = (int) (this.mLeftRatio * 1120403456);
            colorBar.setProgress(progress);
            colorBar.setSecondaryProgress(((int) (this.mMiddleRatio * 100.0f)) + progress);
        } else {
            colorBar.setVisibility(8);
        }
        if (!this.mChartEnabled || (TextUtils.isEmpty(this.mStartLabel) && TextUtils.isEmpty(this.mEndLabel))) {
            holder.findViewById(R.id.label_bar).setVisibility(8);
            return;
        }
        holder.findViewById(R.id.label_bar).setVisibility(0);
        ((TextView) holder.findViewById(16908308)).setText(this.mStartLabel);
        ((TextView) holder.findViewById(16908309)).setText(this.mEndLabel);
    }
}
