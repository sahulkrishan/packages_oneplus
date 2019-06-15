package com.android.settings.datausage;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import android.util.FeatureFlagUtils;
import com.android.settings.R;
import com.android.settings.core.FeatureFlags;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.datausage.TemplatePreference.NetworkServices;
import com.android.settingslib.net.DataUsageController;
import com.android.settingslib.net.DataUsageController.DataUsageInfo;

public class DataUsagePreference extends Preference implements TemplatePreference {
    private int mSubId;
    private NetworkTemplate mTemplate;
    private int mTitleRes;

    public DataUsagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, new int[]{16843233}, TypedArrayUtils.getAttr(context, R.attr.preferenceStyle, 16842894), 0);
        this.mTitleRes = a.getResourceId(0, 0);
        a.recycle();
    }

    public void setTemplate(NetworkTemplate template, int subId, NetworkServices services) {
        this.mTemplate = template;
        this.mSubId = subId;
        DataUsageInfo usageInfo = new DataUsageController(getContext()).getDataUsageInfo(this.mTemplate);
        if (!FeatureFlagUtils.isEnabled(getContext(), FeatureFlags.DATA_USAGE_SETTINGS_V2)) {
            setTitle(this.mTitleRes);
            setSummary((CharSequence) getContext().getString(R.string.data_usage_template, new Object[]{DataUsageUtils.formatDataUsage(getContext(), usageInfo.usageLevel), usageInfo.period}));
        } else if (this.mTemplate.isMatchRuleMobile()) {
            setTitle((int) R.string.app_cellular_data_usage);
        } else {
            setTitle(this.mTitleRes);
            setSummary((CharSequence) getContext().getString(R.string.data_usage_template, new Object[]{DataUsageUtils.formatDataUsage(getContext(), usageInfo.usageLevel), usageInfo.period}));
        }
        setIntent(getIntent());
    }

    public Intent getIntent() {
        Bundle args = new Bundle();
        args.putParcelable("network_template", this.mTemplate);
        args.putInt("sub_id", this.mSubId);
        SubSettingLauncher launcher = new SubSettingLauncher(getContext()).setArguments(args).setDestination(DataUsageList.class.getName()).setSourceMetricsCategory(0);
        if (FeatureFlagUtils.isEnabled(getContext(), FeatureFlags.DATA_USAGE_SETTINGS_V2)) {
            if (this.mTemplate.isMatchRuleMobile()) {
                launcher.setTitle((int) R.string.app_cellular_data_usage);
            } else {
                launcher.setTitle(this.mTitleRes);
            }
        } else if (this.mTitleRes > 0) {
            launcher.setTitle(this.mTitleRes);
        } else {
            launcher.setTitle(getTitle());
        }
        return launcher.toIntent();
    }
}
