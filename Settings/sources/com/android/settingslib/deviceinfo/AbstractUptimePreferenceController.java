package com.android.settingslib.deviceinfo;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.format.DateUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import java.lang.ref.WeakReference;

public abstract class AbstractUptimePreferenceController extends AbstractPreferenceController implements LifecycleObserver, OnStart, OnStop {
    private static final int EVENT_UPDATE_STATS = 500;
    @VisibleForTesting
    static final String KEY_UPTIME = "up_time";
    private Handler mHandler;
    private Preference mUptime;

    private static class MyHandler extends Handler {
        private WeakReference<AbstractUptimePreferenceController> mStatus;

        public MyHandler(AbstractUptimePreferenceController activity) {
            this.mStatus = new WeakReference(activity);
        }

        public void handleMessage(Message msg) {
            AbstractUptimePreferenceController status = (AbstractUptimePreferenceController) this.mStatus.get();
            if (status != null) {
                if (msg.what == AbstractUptimePreferenceController.EVENT_UPDATE_STATS) {
                    status.updateTimes();
                    sendEmptyMessageDelayed(AbstractUptimePreferenceController.EVENT_UPDATE_STATS, 1000);
                    return;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unknown message ");
                stringBuilder.append(msg.what);
                throw new IllegalStateException(stringBuilder.toString());
            }
        }
    }

    public AbstractUptimePreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void onStart() {
        getHandler().sendEmptyMessage(EVENT_UPDATE_STATS);
    }

    public void onStop() {
        getHandler().removeMessages(EVENT_UPDATE_STATS);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_UPTIME;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mUptime = screen.findPreference(KEY_UPTIME);
        updateTimes();
    }

    private Handler getHandler() {
        if (this.mHandler == null) {
            this.mHandler = new MyHandler(this);
        }
        return this.mHandler;
    }

    private void updateTimes() {
        this.mUptime.setSummary(DateUtils.formatDuration(SystemClock.elapsedRealtime()));
    }
}
