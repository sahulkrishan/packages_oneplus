package com.oneplus.settings.timer.timepower;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReceiverPowerOn extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Intent mIntent = new Intent(SettingsUtil.ACTION_SET_CHANGED);
        mIntent.setFlags(285212672);
        context.sendBroadcast(mIntent);
    }
}
