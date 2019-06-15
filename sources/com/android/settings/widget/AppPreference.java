package com.android.settings.widget;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import com.android.settings.R;

public class AppPreference extends Preference {
    private int mProgress;
    private boolean mProgressVisible;

    public AppPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference_app);
    }

    public AppPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_app);
    }

    public void setProgress(int amount) {
        this.mProgress = amount;
        this.mProgressVisible = true;
        notifyChanged();
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.findViewById(R.id.summary_container).setVisibility(TextUtils.isEmpty(getSummary()) ? 8 : 0);
        ProgressBar progress = (ProgressBar) view.findViewById(16908301);
        if (this.mProgressVisible) {
            progress.setProgress(this.mProgress);
            progress.setVisibility(0);
            return;
        }
        progress.setVisibility(8);
    }
}
