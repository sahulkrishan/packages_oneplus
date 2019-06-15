package com.android.settings.datausage;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.widget.ProgressBar;
import com.android.settings.R;
import com.android.settings.widget.AppPreference;
import com.android.settingslib.AppItem;
import com.android.settingslib.net.UidDetail;
import com.android.settingslib.net.UidDetailProvider;
import com.android.settingslib.utils.ThreadUtils;

public class AppDataUsagePreference extends AppPreference {
    private UidDetail mDetail;
    private final AppItem mItem;
    private final int mPercent;

    public AppDataUsagePreference(Context context, AppItem item, int percent, UidDetailProvider provider) {
        super(context);
        this.mItem = item;
        this.mPercent = percent;
        if (!item.restricted || item.total > 0) {
            setSummary(DataUsageUtils.formatDataUsage(context, item.total));
        } else {
            setSummary((int) R.string.data_usage_app_restricted);
        }
        this.mDetail = provider.getUidDetail(item.key, false);
        if (this.mDetail != null) {
            setAppInfo();
        } else {
            ThreadUtils.postOnBackgroundThread(new -$$Lambda$AppDataUsagePreference$1CecIqCNArEHKTwkPb92cZEWQPk(this, provider));
        }
    }

    public static /* synthetic */ void lambda$new$1(AppDataUsagePreference appDataUsagePreference, UidDetailProvider provider) {
        Log.d("AppDataUsagePre", "postOnBackgroundThread getUidDetail start");
        appDataUsagePreference.mDetail = provider.getUidDetail(appDataUsagePreference.mItem.key, true);
        ThreadUtils.postOnMainThread(new -$$Lambda$AppDataUsagePreference$xD2zZCrk9HJ-DejIPEhSoFp3K8o(appDataUsagePreference));
        Log.d("AppDataUsagePre", "postOnBackgroundThread getUidDetail end");
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ProgressBar progress = (ProgressBar) holder.findViewById(16908301);
        if (!this.mItem.restricted || this.mItem.total > 0) {
            progress.setVisibility(0);
        } else {
            progress.setVisibility(8);
        }
        progress.setProgress(this.mPercent);
    }

    private void setAppInfo() {
        if (this.mDetail != null) {
            setIcon(this.mDetail.icon);
            setTitle(this.mDetail.label);
            return;
        }
        setIcon(null);
        setTitle(null);
    }

    public AppItem getItem() {
        return this.mItem;
    }
}
