package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.settings.R;

public class FixedLineSummaryPreference extends Preference {
    private int mSummaryLineCount;

    public FixedLineSummaryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FixedLineSummaryPreference, 0, 0);
        if (a.hasValue(0)) {
            this.mSummaryLineCount = a.getInteger(0, 1);
        } else {
            this.mSummaryLineCount = 1;
        }
        a.recycle();
    }

    public void setSummaryLineCount(int count) {
        this.mSummaryLineCount = count;
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView summary = (TextView) holder.findViewById(16908304);
        if (summary != null) {
            summary.setMinLines(this.mSummaryLineCount);
            summary.setMaxLines(this.mSummaryLineCount);
            summary.setEllipsize(TruncateAt.END);
        }
    }
}
