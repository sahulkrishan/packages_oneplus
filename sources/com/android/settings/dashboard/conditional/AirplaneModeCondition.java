package com.android.settings.dashboard.conditional;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.util.Log;
import com.android.settings.R;
import com.android.settingslib.WirelessUtils;

public class AirplaneModeCondition extends Condition {
    private static final IntentFilter AIRPLANE_MODE_FILTER = new IntentFilter("android.intent.action.AIRPLANE_MODE");
    public static String TAG = "APM_Condition";
    private final Receiver mReceiver = new Receiver();

    public static class Receiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.AIRPLANE_MODE".equals(intent.getAction())) {
                ((AirplaneModeCondition) ConditionManager.get(context).getCondition(AirplaneModeCondition.class)).refreshState();
            }
        }
    }

    public AirplaneModeCondition(ConditionManager conditionManager) {
        super(conditionManager);
    }

    public void refreshState() {
        Log.d(TAG, "APM condition refreshed");
        setActive(WirelessUtils.isAirplaneModeOn(this.mManager.getContext()));
    }

    /* Access modifiers changed, original: protected */
    public BroadcastReceiver getReceiver() {
        return this.mReceiver;
    }

    /* Access modifiers changed, original: protected */
    public IntentFilter getIntentFilter() {
        return AIRPLANE_MODE_FILTER;
    }

    public Drawable getIcon() {
        return this.mManager.getContext().getDrawable(R.drawable.ic_airplane);
    }

    /* Access modifiers changed, original: protected */
    public void setActive(boolean active) {
        super.setActive(active);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setActive was called with ");
        stringBuilder.append(active);
        Log.d(str, stringBuilder.toString());
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(R.string.condition_airplane_title);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getString(R.string.condition_airplane_summary);
    }

    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(R.string.condition_turn_off)};
    }

    public void onPrimaryClick() {
        this.mManager.getContext().startActivity(new Intent("android.settings.WIRELESS_SETTINGS").addFlags(268435456));
    }

    public void onActionClick(int index) {
        if (index == 0) {
            ConnectivityManager.from(this.mManager.getContext()).setAirplaneMode(false);
            setActive(false);
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected index ");
        stringBuilder.append(index);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public int getMetricsConstant() {
        return 377;
    }
}
