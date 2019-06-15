package com.android.settings.datetime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class TimeChangeListenerMixin extends BroadcastReceiver implements LifecycleObserver, OnPause, OnResume {
    private final UpdateTimeAndDateCallback mCallback;
    private final Context mContext;

    public TimeChangeListenerMixin(Context context, UpdateTimeAndDateCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
    }

    public void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_TICK");
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        this.mContext.registerReceiver(this, filter, null, null);
    }

    public void onPause() {
        this.mContext.unregisterReceiver(this);
    }

    public void onReceive(Context context, Intent intent) {
        if (this.mCallback != null) {
            this.mCallback.updateTimeAndDateDisplay(this.mContext);
        }
    }
}
