package com.android.settings.fuelgauge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceScreen;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;

public class BatteryHeaderPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnStart {
    @VisibleForTesting
    static final String KEY_BATTERY_HEADER = "battery_header";
    private final Activity mActivity;
    private LayoutPreference mBatteryLayoutPref;
    @VisibleForTesting
    BatteryMeterView mBatteryMeterView;
    @VisibleForTesting
    TextView mBatteryPercentText;
    private final PreferenceFragment mHost;
    private final Lifecycle mLifecycle;
    @VisibleForTesting
    TextView mSummary1;
    @VisibleForTesting
    TextView mSummary2;

    public BatteryHeaderPreferenceController(Context context, Activity activity, PreferenceFragment host, Lifecycle lifecycle) {
        super(context);
        this.mActivity = activity;
        this.mHost = host;
        this.mLifecycle = lifecycle;
        if (this.mLifecycle != null) {
            this.mLifecycle.addObserver(this);
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mBatteryLayoutPref = (LayoutPreference) screen.findPreference(KEY_BATTERY_HEADER);
        this.mBatteryMeterView = (BatteryMeterView) this.mBatteryLayoutPref.findViewById(R.id.battery_header_icon);
        this.mBatteryPercentText = (TextView) this.mBatteryLayoutPref.findViewById(R.id.battery_percent);
        this.mSummary1 = (TextView) this.mBatteryLayoutPref.findViewById(R.id.summary1);
        this.mSummary2 = (TextView) this.mBatteryLayoutPref.findViewById(R.id.summary2);
        quickUpdateHeaderPreference();
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_BATTERY_HEADER;
    }

    public void onStart() {
        EntityHeaderController.newInstance(this.mActivity, this.mHost, this.mBatteryLayoutPref.findViewById(R.id.battery_entity_header)).setRecyclerView(this.mHost.getListView(), this.mLifecycle).styleActionBar(this.mActivity);
    }

    public void updateHeaderPreference(BatteryInfo info) {
        this.mBatteryPercentText.setText(Utils.formatPercentage(info.batteryLevel));
        if (info.remainingLabel == null) {
            this.mSummary1.setText(info.statusLabel);
        } else {
            this.mSummary1.setText(info.remainingLabel);
        }
        this.mSummary2.setText("");
        this.mBatteryMeterView.setBatteryLevel(info.batteryLevel);
        this.mBatteryMeterView.setCharging(info.discharging ^ 1);
    }

    public void quickUpdateHeaderPreference() {
        Intent batteryBroadcast = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        int batteryLevel = Utils.getBatteryLevel(batteryBroadcast);
        boolean z = false;
        boolean discharging = batteryBroadcast.getIntExtra("plugged", -1) == 0;
        this.mBatteryMeterView.setBatteryLevel(batteryLevel);
        BatteryMeterView batteryMeterView = this.mBatteryMeterView;
        if (!discharging) {
            z = true;
        }
        batteryMeterView.setCharging(z);
        this.mBatteryPercentText.setText(Utils.formatPercentage(batteryLevel));
        this.mSummary1.setText("");
        this.mSummary2.setText("");
    }
}
