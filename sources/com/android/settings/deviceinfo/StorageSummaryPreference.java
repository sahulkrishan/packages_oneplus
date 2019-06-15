package com.android.settings.deviceinfo;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.MathUtils;
import android.widget.ProgressBar;
import com.android.settings.R;

public class StorageSummaryPreference extends Preference {
    private int mPercent = -1;

    public StorageSummaryPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.storage_summary);
        setEnabled(false);
    }

    public void setPercent(long usedBytes, long totalBytes) {
        this.mPercent = MathUtils.constrain((int) ((100 * usedBytes) / totalBytes), usedBytes > 0 ? 1 : 0, 100);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        ProgressBar progress = (ProgressBar) view.findViewById(16908301);
        if (this.mPercent != -1) {
            progress.setVisibility(0);
            progress.setProgress(this.mPercent);
            progress.setScaleY(7.0f);
        } else {
            progress.setVisibility(8);
        }
        super.onBindViewHolder(view);
    }
}
