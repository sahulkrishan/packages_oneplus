package com.android.settings.datausage;

import android.content.Context;
import android.net.NetworkPolicy;
import android.net.NetworkStatsHistory;
import android.net.NetworkStatsHistory.Entry;
import android.os.Build.VERSION;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import com.android.settings.R;
import com.android.settings.graph.UsageView;
import com.android.settingslib.Utils;
import com.oneplus.settings.utils.OPFormatter;
import com.oneplus.settings.utils.OPFormatter.BytesResult;
import com.oneplus.settings.utils.OPUtils;

public class ChartDataUsagePreference extends Preference {
    private static final long RESOLUTION = 524288;
    private long mEnd;
    private final int mLimitColor;
    private NetworkStatsHistory mNetwork;
    private NetworkPolicy mPolicy;
    private int mSecondaryColor;
    private int mSeriesColor;
    private boolean mShowWifi = true;
    private long mStart;
    private int mSubId = 0;
    private final int mWarningColor;

    public void setSubId(int mSubId) {
        this.mSubId = mSubId;
    }

    public void setShowWifi(boolean showWifi) {
        this.mShowWifi = showWifi;
    }

    public ChartDataUsagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSelectable(false);
        this.mLimitColor = Utils.getColorAttr(context, 16844099);
        this.mWarningColor = Utils.getColorAttr(context, 16842808);
        setLayoutResource(R.layout.data_usage_graph);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        UsageView chart = (UsageView) holder.findViewById(R.id.data_usage);
        if (this.mNetwork != null) {
            int top = getTop();
            chart.clearPaths();
            chart.configureGraph(toInt(this.mEnd - this.mStart), top);
            calcPoints(chart);
            chart.setBottomLabels(new CharSequence[]{com.android.settings.Utils.formatDateRange(getContext(), this.mStart, this.mStart), com.android.settings.Utils.formatDateRange(getContext(), this.mEnd, this.mEnd)});
            bindNetworkPolicy(chart, this.mPolicy, top);
        }
    }

    public int getTop() {
        int start = this.mNetwork.getIndexBefore(this.mStart);
        int end = this.mNetwork.getIndexAfter(this.mEnd);
        long totalData = 0;
        Entry entry = null;
        for (int i = start; i <= end; i++) {
            entry = this.mNetwork.getValues(i, entry);
            totalData += entry.rxBytes + entry.txBytes;
        }
        return (int) (Math.max(totalData, this.mPolicy != null ? Math.max(this.mPolicy.limitBytes, this.mPolicy.warningBytes) : 0) / 524288);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void calcPoints(UsageView chart) {
        SparseIntArray points = new SparseIntArray();
        int start = this.mNetwork.getIndexAfter(this.mStart);
        int end = this.mNetwork.getIndexAfter(this.mEnd);
        if (start >= 0) {
            int end2;
            points.put(0, 0);
            long totalData = 0;
            Entry entry = null;
            int i = start;
            while (i <= end) {
                entry = this.mNetwork.getValues(i, entry);
                long startTime = entry.bucketStart;
                long endTime = entry.bucketDuration + startTime;
                int start2 = start;
                totalData += entry.rxBytes + entry.txBytes;
                if (i == 0) {
                    points.put(toInt(startTime - this.mStart) - 1, -1);
                }
                end2 = end;
                points.put(toInt((startTime - this.mStart) + 1), (int) (totalData / 524288));
                points.put(toInt(endTime - this.mStart), (int) (totalData / 524288));
                i++;
                start = start2;
                end = end2;
            }
            end2 = end;
            if (points.size() > 1) {
                chart.addPath(points);
            } else {
                UsageView usageView = chart;
            }
        }
    }

    private int toInt(long l) {
        return (int) (l / 60000);
    }

    private void bindNetworkPolicy(UsageView chart, NetworkPolicy policy, int top) {
        CharSequence[] labels = new CharSequence[3];
        int middleVisibility = 0;
        int topVisibility = 0;
        if (policy != null) {
            if (policy.limitBytes != -1) {
                topVisibility = this.mLimitColor;
                labels[2] = getLabel(policy.limitBytes, R.string.data_usage_sweep_limit, this.mLimitColor);
            }
            if (policy.warningBytes != -1) {
                chart.setDividerLoc((int) (policy.warningBytes / 524288));
                float weight = ((float) (policy.warningBytes / 524288)) / ((float) top);
                chart.setSideLabelWeights(1.0f - weight, weight);
                middleVisibility = this.mWarningColor;
                labels[1] = getLabel(policy.warningBytes, R.string.data_usage_sweep_warning, this.mWarningColor);
            }
            chart.setSideLabels(labels);
            chart.setDividerColors(middleVisibility, topVisibility);
        }
    }

    private CharSequence getLabel(long bytes, int str, int mLimitColor) {
        BytesResult result = OPFormatter.formatBytes(getContext().getResources(), bytes, 1);
        String unit = result.units;
        if (VERSION.SDK_INT > 26) {
            unit = OPUtils.replaceFileSize(result.units);
        }
        return new SpannableStringBuilder().append(TextUtils.expandTemplate(getContext().getText(str), new CharSequence[]{result.value, unit}), new ForegroundColorSpan(mLimitColor), 0);
    }

    public void setNetworkPolicy(NetworkPolicy policy) {
        this.mPolicy = policy;
        if (this.mPolicy != null) {
            int warnState = OPDataUsageUtils.getDataWarnState(getContext(), this.mSubId);
            long warnBytes = OPDataUsageUtils.getDataWarnBytes(getContext(), this.mSubId);
            if (warnState == 1) {
                this.mPolicy.warningBytes = warnBytes;
            } else {
                this.mPolicy.warningBytes = -1;
            }
        }
        notifyChanged();
    }

    public void setVisibleRange(long start, long end) {
        this.mStart = start;
        this.mEnd = end;
        notifyChanged();
    }

    public long getInspectStart() {
        return this.mStart;
    }

    public long getInspectEnd() {
        return this.mEnd;
    }

    public void setNetworkStats(NetworkStatsHistory network) {
        this.mNetwork = network;
        notifyChanged();
    }

    public void setColors(int seriesColor, int secondaryColor) {
        this.mSeriesColor = seriesColor;
        this.mSecondaryColor = secondaryColor;
        notifyChanged();
    }
}
