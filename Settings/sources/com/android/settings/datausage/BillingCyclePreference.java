package com.android.settings.datausage;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import android.util.FeatureFlagUtils;
import com.android.settings.R;
import com.android.settings.core.FeatureFlags;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.datausage.CellDataPreference.DataStateListener;
import com.android.settings.datausage.TemplatePreference.NetworkServices;

public class BillingCyclePreference extends Preference implements TemplatePreference {
    private final DataStateListener mListener = new DataStateListener() {
        public void onChange(boolean selfChange) {
            BillingCyclePreference.this.updateEnabled();
        }
    };
    private NetworkServices mServices;
    private int mSubId;
    private NetworkTemplate mTemplate;

    public BillingCyclePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onAttached() {
        super.onAttached();
        this.mListener.setListener(true, this.mSubId, getContext());
    }

    public void onDetached() {
        this.mListener.setListener(false, this.mSubId, getContext());
        super.onDetached();
    }

    public void setTemplate(NetworkTemplate template, int subId, NetworkServices services) {
        this.mTemplate = template;
        this.mSubId = subId;
        this.mServices = services;
        int cycleDay = services.mPolicyEditor.getPolicyCycleDay(this.mTemplate);
        if (FeatureFlagUtils.isEnabled(getContext(), FeatureFlags.DATA_USAGE_SETTINGS_V2)) {
            setSummary(null);
        } else if (cycleDay != -1) {
            setSummary((CharSequence) getContext().getString(R.string.billing_cycle_fragment_summary, new Object[]{Integer.valueOf(cycleDay)}));
        } else {
            setSummary(null);
        }
        setIntent(getIntent());
    }

    private void updateEnabled() {
        try {
            boolean z;
            if (this.mServices.mNetworkService.isBandwidthControlEnabled() && this.mServices.mTelephonyManager.getDataEnabled(this.mSubId) && this.mServices.mUserManager.isAdminUser()) {
                z = true;
            } else {
                z = false;
            }
            setEnabled(z);
        } catch (RemoteException e) {
            setEnabled(false);
        }
    }

    public Intent getIntent() {
        Bundle args = new Bundle();
        args.putParcelable("network_template", this.mTemplate);
        return new SubSettingLauncher(getContext()).setDestination(BillingCycleSettings.class.getName()).setArguments(args).setTitle((int) R.string.billing_cycle).setSourceMetricsCategory(0).toIntent();
    }
}
