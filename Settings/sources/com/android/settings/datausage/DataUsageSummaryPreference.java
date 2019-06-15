package com.android.settings.datausage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.format.Formatter.BytesResult;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settingslib.Utils;
import com.android.settingslib.utils.StringUtil;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DataUsageSummaryPreference extends Preference {
    private static final float LARGER_FONT_RATIO = 2.25f;
    private static final long MILLIS_IN_A_DAY = TimeUnit.DAYS.toMillis(1);
    @VisibleForTesting
    static final Typeface SANS_SERIF_MEDIUM = Typeface.create("sans-serif-medium", 0);
    private static final float SMALLER_FONT_RATIO = 1.0f;
    private static final long WARNING_AGE = TimeUnit.HOURS.toMillis(6);
    private CharSequence mCarrierName;
    private boolean mChartEnabled = true;
    private long mCycleEndTimeMs;
    private long mDataplanSize;
    private long mDataplanUse;
    private int mDefaultTextColor;
    private boolean mDefaultTextColorSet;
    private CharSequence mEndLabel;
    private boolean mHasMobileData;
    private Intent mLaunchIntent;
    private String mLimitInfoText;
    private int mNumPlans;
    private float mProgress;
    private long mSnapshotTimeMs;
    private CharSequence mStartLabel;
    private String mUsagePeriod;
    private boolean mWifiMode;

    public DataUsageSummaryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.data_usage_summary_preference);
    }

    public void setLimitInfo(String text) {
        if (!Objects.equals(text, this.mLimitInfoText)) {
            this.mLimitInfoText = text;
            notifyChanged();
        }
    }

    public void setProgress(float progress) {
        this.mProgress = progress;
        notifyChanged();
    }

    public void setUsageInfo(long cycleEnd, long snapshotTime, CharSequence carrierName, int numPlans, Intent launchIntent) {
        this.mCycleEndTimeMs = cycleEnd;
        this.mSnapshotTimeMs = snapshotTime;
        this.mCarrierName = carrierName;
        this.mNumPlans = numPlans;
        this.mLaunchIntent = launchIntent;
        notifyChanged();
    }

    public void setChartEnabled(boolean enabled) {
        if (this.mChartEnabled != enabled) {
            this.mChartEnabled = enabled;
            notifyChanged();
        }
    }

    public void setLabels(CharSequence start, CharSequence end) {
        this.mStartLabel = start;
        this.mEndLabel = end;
        notifyChanged();
    }

    /* Access modifiers changed, original: 0000 */
    public void setUsageNumbers(long used, long dataPlanSize, boolean hasMobileData) {
        this.mDataplanUse = used;
        this.mDataplanSize = dataPlanSize;
        this.mHasMobileData = hasMobileData;
        notifyChanged();
    }

    /* Access modifiers changed, original: 0000 */
    public void setWifiMode(boolean isWifiMode, String usagePeriod) {
        this.mWifiMode = isWifiMode;
        this.mUsagePeriod = usagePeriod;
        notifyChanged();
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ProgressBar bar = (ProgressBar) holder.findViewById(R.id.determinateBar);
        int i = 8;
        if (!this.mChartEnabled || (TextUtils.isEmpty(this.mStartLabel) && TextUtils.isEmpty(this.mEndLabel))) {
            bar.setVisibility(8);
            holder.findViewById(R.id.label_bar).setVisibility(8);
        } else {
            bar.setVisibility(0);
            holder.findViewById(R.id.label_bar).setVisibility(0);
            bar.setProgress((int) (this.mProgress * 100.0f));
            ((TextView) holder.findViewById(16908308)).setText(this.mStartLabel);
            ((TextView) holder.findViewById(16908309)).setText(this.mEndLabel);
        }
        updateDataUsageLabels(holder);
        TextView usageTitle = (TextView) holder.findViewById(R.id.usage_title);
        TextView carrierInfo = (TextView) holder.findViewById(R.id.carrier_and_update);
        Button launchButton = (Button) holder.findViewById(R.id.launch_mdp_app_button);
        TextView limitInfo = (TextView) holder.findViewById(R.id.data_limits);
        if (this.mWifiMode) {
            usageTitle.setText(R.string.data_usage_wifi_title);
            usageTitle.setVisibility(0);
            ((TextView) holder.findViewById(R.id.cycle_left_time)).setText(this.mUsagePeriod);
            carrierInfo.setVisibility(8);
            limitInfo.setVisibility(8);
            launchButton.setOnClickListener(new -$$Lambda$DataUsageSummaryPreference$zBjNn20lFyV2SqYMtfKeIRkAo7w(this));
            launchButton.setText(R.string.launch_wifi_text);
            launchButton.setVisibility(0);
            return;
        }
        usageTitle.setVisibility(this.mNumPlans > 1 ? 0 : 8);
        updateCycleTimeText(holder);
        updateCarrierInfo(carrierInfo);
        if (this.mLaunchIntent != null) {
            launchButton.setOnClickListener(new -$$Lambda$DataUsageSummaryPreference$1NKWVGupHVFnsudApVgFBRMGUJg(this));
            launchButton.setVisibility(0);
        } else {
            launchButton.setVisibility(8);
        }
        if (!TextUtils.isEmpty(this.mLimitInfoText)) {
            i = 0;
        }
        limitInfo.setVisibility(i);
        limitInfo.setText(this.mLimitInfoText);
    }

    private static void launchWifiDataUsage(Context context) {
        Bundle args = new Bundle(1);
        args.putParcelable("network_template", NetworkTemplate.buildTemplateWifiWildcard());
        SubSettingLauncher launcher = new SubSettingLauncher(context).setArguments(args).setDestination(DataUsageList.class.getName()).setSourceMetricsCategory(0);
        launcher.setTitle(context.getString(R.string.wifi_data_usage));
        launcher.launch();
    }

    private void updateDataUsageLabels(PreferenceViewHolder holder) {
        PreferenceViewHolder preferenceViewHolder = holder;
        TextView usageNumberField = (TextView) preferenceViewHolder.findViewById(R.id.data_usage_view);
        BytesResult usedResult = Formatter.formatBytes(getContext().getResources(), this.mDataplanUse, 10);
        SpannableString usageNumberText = new SpannableString(usedResult.value);
        usageNumberText.setSpan(new AbsoluteSizeSpan(getContext().getResources().getDimensionPixelSize(R.dimen.usage_number_text_size)), 0, usageNumberText.length(), 33);
        usageNumberField.setText(TextUtils.expandTemplate(getContext().getText(R.string.data_used_formatted), new CharSequence[]{usageNumberText, usedResult.units}));
        MeasurableLinearLayout layout = (MeasurableLinearLayout) preferenceViewHolder.findViewById(R.id.usage_layout);
        SpannableString spannableString;
        if (!this.mHasMobileData || this.mNumPlans < 0 || this.mDataplanSize <= 0) {
            spannableString = usageNumberText;
            layout.setChildren(usageNumberField, null);
            return;
        }
        TextView usageRemainingField;
        MeasurableLinearLayout layout2 = layout;
        TextView usageRemainingField2 = (TextView) preferenceViewHolder.findViewById(R.id.data_remaining_view);
        long dataRemaining = this.mDataplanSize - this.mDataplanUse;
        CharSequence expandTemplate;
        if (dataRemaining >= 0) {
            expandTemplate = TextUtils.expandTemplate(getContext().getText(R.string.data_remaining), new CharSequence[]{DataUsageUtils.formatDataUsage(getContext(), dataRemaining)});
            usageRemainingField = usageRemainingField2;
            usageRemainingField.setText(expandTemplate);
            usageRemainingField.setTextColor(Utils.getColorAttr(getContext(), 16843829));
            BytesResult bytesResult = usedResult;
            spannableString = usageNumberText;
        } else {
            usageRemainingField = usageRemainingField2;
            expandTemplate = getContext().getText(R.string.data_overusage);
            CharSequence[] charSequenceArr = new CharSequence[1];
            charSequenceArr[0] = DataUsageUtils.formatDataUsage(getContext(), -dataRemaining);
            usageRemainingField.setText(TextUtils.expandTemplate(expandTemplate, charSequenceArr));
            usageRemainingField.setTextColor(Utils.getColorAttr(getContext(), 16844099));
        }
        layout2.setChildren(usageNumberField, usageRemainingField);
    }

    private void updateCycleTimeText(PreferenceViewHolder holder) {
        TextView cycleTime = (TextView) holder.findViewById(R.id.cycle_left_time);
        long millisLeft = this.mCycleEndTimeMs - System.currentTimeMillis();
        if (millisLeft <= 0) {
            cycleTime.setText(getContext().getString(R.string.billing_cycle_none_left));
            return;
        }
        CharSequence string;
        int daysLeft = (int) (millisLeft / MILLIS_IN_A_DAY);
        if (daysLeft < 1) {
            string = getContext().getString(R.string.billing_cycle_less_than_one_day_left);
        } else {
            string = getContext().getResources().getQuantityString(R.plurals.billing_cycle_days_left, daysLeft, new Object[]{Integer.valueOf(daysLeft)});
        }
        cycleTime.setText(string);
    }

    private void updateCarrierInfo(TextView carrierInfo) {
        if (this.mNumPlans <= 0 || this.mSnapshotTimeMs < 0) {
            carrierInfo.setVisibility(8);
            return;
        }
        int textResourceId;
        carrierInfo.setVisibility(0);
        long updateAgeMillis = calculateTruncatedUpdateAge();
        CharSequence updateTime = null;
        if (updateAgeMillis != 0) {
            if (this.mCarrierName != null) {
                textResourceId = R.string.carrier_and_update_text;
            } else {
                textResourceId = R.string.no_carrier_update_text;
            }
            updateTime = StringUtil.formatElapsedTime(getContext(), (double) updateAgeMillis, false);
        } else if (this.mCarrierName != null) {
            textResourceId = R.string.carrier_and_update_now_text;
        } else {
            textResourceId = R.string.no_carrier_update_now_text;
        }
        carrierInfo.setText(TextUtils.expandTemplate(getContext().getText(textResourceId), new CharSequence[]{this.mCarrierName, updateTime}));
        if (updateAgeMillis <= WARNING_AGE) {
            setCarrierInfoTextStyle(carrierInfo, 16842808, Typeface.SANS_SERIF);
        } else {
            setCarrierInfoTextStyle(carrierInfo, 16844099, SANS_SERIF_MEDIUM);
        }
    }

    private long calculateTruncatedUpdateAge() {
        long updateAgeMillis = System.currentTimeMillis() - this.mSnapshotTimeMs;
        if (updateAgeMillis >= TimeUnit.DAYS.toMillis(1)) {
            return (updateAgeMillis / TimeUnit.DAYS.toMillis(1)) * TimeUnit.DAYS.toMillis(1);
        }
        if (updateAgeMillis >= TimeUnit.HOURS.toMillis(1)) {
            return (updateAgeMillis / TimeUnit.HOURS.toMillis(1)) * TimeUnit.HOURS.toMillis(1);
        }
        if (updateAgeMillis >= TimeUnit.MINUTES.toMillis(1)) {
            return (updateAgeMillis / TimeUnit.MINUTES.toMillis(1)) * TimeUnit.MINUTES.toMillis(1);
        }
        return 0;
    }

    private void setCarrierInfoTextStyle(TextView carrierInfo, int colorId, Typeface typeface) {
        carrierInfo.setTextColor(Utils.getColorAttr(getContext(), colorId));
        carrierInfo.setTypeface(typeface);
    }
}
