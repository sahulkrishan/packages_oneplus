package com.android.settingslib.deviceinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import com.android.internal.util.ArrayUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import java.lang.ref.WeakReference;

public abstract class AbstractConnectivityPreferenceController extends AbstractPreferenceController implements LifecycleObserver, OnStart, OnStop {
    private static final int EVENT_UPDATE_CONNECTIVITY = 600;
    private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (ArrayUtils.contains(AbstractConnectivityPreferenceController.this.getConnectivityIntents(), intent.getAction())) {
                AbstractConnectivityPreferenceController.this.getHandler().sendEmptyMessage(600);
            }
        }
    };
    private Handler mHandler;

    private static class ConnectivityEventHandler extends Handler {
        private WeakReference<AbstractConnectivityPreferenceController> mPreferenceController;

        public ConnectivityEventHandler(AbstractConnectivityPreferenceController activity) {
            this.mPreferenceController = new WeakReference(activity);
        }

        public void handleMessage(Message msg) {
            AbstractConnectivityPreferenceController preferenceController = (AbstractConnectivityPreferenceController) this.mPreferenceController.get();
            if (preferenceController != null) {
                if (msg.what == 600) {
                    preferenceController.updateConnectivity();
                    return;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unknown message ");
                stringBuilder.append(msg.what);
                throw new IllegalStateException(stringBuilder.toString());
            }
        }
    }

    public abstract String[] getConnectivityIntents();

    public abstract void updateConnectivity();

    public AbstractConnectivityPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void onStop() {
        this.mContext.unregisterReceiver(this.mConnectivityReceiver);
    }

    public void onStart() {
        IntentFilter connectivityIntentFilter = new IntentFilter();
        for (String intent : getConnectivityIntents()) {
            connectivityIntentFilter.addAction(intent);
        }
        this.mContext.registerReceiver(this.mConnectivityReceiver, connectivityIntentFilter, "android.permission.CHANGE_NETWORK_STATE", null);
    }

    private Handler getHandler() {
        if (this.mHandler == null) {
            this.mHandler = new ConnectivityEventHandler(this);
        }
        return this.mHandler;
    }
}
