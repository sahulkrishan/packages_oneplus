package com.android.settings.fuelgauge;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.R;
import com.android.settings.fuelgauge.BatteryInfo.BatteryDataParser;
import com.android.settings.graph.UsageView;

public class BatteryHistoryPreference extends Preference {
    private static final String TAG = "BatteryHistoryPreference";
    @VisibleForTesting
    boolean hideSummary;
    @VisibleForTesting
    BatteryInfo mBatteryInfo;
    private CharSequence mSummary;
    private TextView mSummaryView;

    public BatteryHistoryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.battery_usage_graph);
        setSelectable(false);
    }

    public void setStats(BatteryStatsHelper batteryStats) {
        BatteryInfo.getBatteryInfo(getContext(), new -$$Lambda$BatteryHistoryPreference$OfN0YWKsw9YRrCqoEdP8dybAPU0(this), batteryStats.getStats(), false);
    }

    public static /* synthetic */ void lambda$setStats$0(BatteryHistoryPreference batteryHistoryPreference, BatteryInfo info) {
        batteryHistoryPreference.mBatteryInfo = info;
        batteryHistoryPreference.notifyChanged();
    }

    public void setBottomSummary(CharSequence text) {
        this.mSummary = text;
        if (this.mSummaryView != null) {
            this.mSummaryView.setVisibility(0);
            this.mSummaryView.setText(this.mSummary);
        }
        this.hideSummary = false;
    }

    public void hideBottomSummary() {
        if (this.mSummaryView != null) {
            this.mSummaryView.setVisibility(8);
        }
        this.hideSummary = true;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        long startTime = System.currentTimeMillis();
        if (this.mBatteryInfo != null) {
            ((TextView) view.findViewById(R.id.charge)).setText(this.mBatteryInfo.batteryPercentString);
            this.mSummaryView = (TextView) view.findViewById(R.id.bottom_summary);
            if (this.mSummary != null) {
                this.mSummaryView.setText(this.mSummary);
            }
            if (this.hideSummary) {
                this.mSummaryView.setVisibility(8);
            }
            UsageView usageView = (UsageView) view.findViewById(R.id.battery_usage);
            usageView.findViewById(R.id.label_group).setAlpha(0.7f);
            this.mBatteryInfo.bindHistory(usageView, new BatteryDataParser[0]);
            BatteryUtils.logRuntime(TAG, "onBindViewHolder", startTime);
        }
    }
}
