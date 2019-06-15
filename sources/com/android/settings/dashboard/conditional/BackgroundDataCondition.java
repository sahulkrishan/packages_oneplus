package com.android.settings.dashboard.conditional;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.NetworkPolicyManager;
import android.telephony.TelephonyManager;
import com.android.settings.R;
import com.android.settings.datausage.DataSaverBackend;

public class BackgroundDataCondition extends Condition {
    public BackgroundDataCondition(ConditionManager manager) {
        super(manager);
    }

    public void refreshState() {
        setActive(NetworkPolicyManager.from(this.mManager.getContext()).getRestrictBackground());
    }

    public Drawable getIcon() {
        return this.mManager.getContext().getDrawable(R.drawable.ic_data_saver);
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(R.string.condition_bg_data_title);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getString(R.string.condition_bg_data_summary);
    }

    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(R.string.condition_turn_off)};
    }

    public void onPrimaryClick() {
        this.mManager.getContext().startActivity(new Intent("com.oneplus.action.DATAUSAGE_SAVER").addFlags(268435456));
    }

    public int getMetricsConstant() {
        return 378;
    }

    public void onActionClick(int index) {
        if (index == 0) {
            new DataSaverBackend(this.mManager.getContext()).setDataSaverEnabled(false);
            TelephonyManager.setTelephonyProperty("persist.radio.data_saver.state", "0");
            setActive(false);
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected index ");
        stringBuilder.append(index);
        throw new IllegalArgumentException(stringBuilder.toString());
    }
}
