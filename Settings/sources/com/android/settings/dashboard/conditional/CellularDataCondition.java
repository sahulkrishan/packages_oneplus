package com.android.settings.dashboard.conditional;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import com.android.settings.R;

public class CellularDataCondition extends Condition {
    private static final IntentFilter DATA_CONNECTION_FILTER = new IntentFilter("android.intent.action.ANY_DATA_STATE");
    private final Receiver mReceiver = new Receiver();

    public static class Receiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.ANY_DATA_STATE".equals(intent.getAction())) {
                CellularDataCondition condition = (CellularDataCondition) ConditionManager.get(context).getCondition(CellularDataCondition.class);
                if (condition != null) {
                    condition.refreshState();
                }
            }
        }
    }

    public CellularDataCondition(ConditionManager manager) {
        super(manager);
    }

    public void refreshState() {
        TelephonyManager telephony = (TelephonyManager) this.mManager.getContext().getSystemService(TelephonyManager.class);
        if (((ConnectivityManager) this.mManager.getContext().getSystemService(ConnectivityManager.class)).isNetworkSupported(0) && telephony.getSimState() == 5) {
            setActive(telephony.isDataEnabled() ^ 1);
        } else {
            setActive(false);
        }
    }

    /* Access modifiers changed, original: protected */
    public BroadcastReceiver getReceiver() {
        return this.mReceiver;
    }

    /* Access modifiers changed, original: protected */
    public IntentFilter getIntentFilter() {
        return DATA_CONNECTION_FILTER;
    }

    public Drawable getIcon() {
        return this.mManager.getContext().getDrawable(R.drawable.ic_cellular_off);
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(R.string.condition_cellular_title);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getString(R.string.condition_cellular_summary);
    }

    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(R.string.condition_turn_on)};
    }

    public void onPrimaryClick() {
        this.mManager.getContext().startActivity(new Intent("oneplus.intent.action.SIM_AND_NETWORK_SETTINGS").addFlags(268435456));
    }

    public void onActionClick(int index) {
        if (index == 0) {
            ((TelephonyManager) this.mManager.getContext().getSystemService(TelephonyManager.class)).setDataEnabled(true);
            setActive(false);
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected index ");
        stringBuilder.append(index);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public int getMetricsConstant() {
        return 380;
    }
}
