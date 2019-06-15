package com.android.settings.fuelgauge;

import android.content.Intent;
import android.os.BatteryStats;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.fuelgauge.BatteryActiveView.BatteryActiveProvider;
import com.android.settings.graph.UsageView;

public class BatteryHistoryDetail extends SettingsPreferenceFragment {
    public static final String BATTERY_HISTORY_FILE = "tmp_bat_history.bin";
    public static final String EXTRA_BROADCAST = "broadcast";
    public static final String EXTRA_STATS = "stats";
    private Intent mBatteryBroadcast;
    private BatteryFlagParser mCameraParser;
    private BatteryFlagParser mChargingParser;
    private BatteryFlagParser mCpuParser;
    private BatteryFlagParser mFlashlightParser;
    private BatteryFlagParser mGpsParser;
    private BatteryCellParser mPhoneParser;
    private BatteryFlagParser mScreenOn;
    private BatteryStats mStats;
    private BatteryWifiParser mWifiParser;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mStats = BatteryStatsHelper.statsFromFile(getActivity(), getArguments().getString(EXTRA_STATS));
        this.mBatteryBroadcast = (Intent) getArguments().getParcelable(EXTRA_BROADCAST);
        TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(16843829, value, true);
        int accentColor = getContext().getColor(value.resourceId);
        this.mChargingParser = new BatteryFlagParser(accentColor, false, 524288);
        this.mScreenOn = new BatteryFlagParser(accentColor, false, 1048576);
        this.mGpsParser = new BatteryFlagParser(accentColor, false, 536870912);
        this.mFlashlightParser = new BatteryFlagParser(accentColor, true, 134217728);
        this.mCameraParser = new BatteryFlagParser(accentColor, true, 2097152);
        this.mWifiParser = new BatteryWifiParser(accentColor);
        this.mCpuParser = new BatteryFlagParser(accentColor, false, Integer.MIN_VALUE);
        this.mPhoneParser = new BatteryCellParser();
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.battery_history_detail, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateEverything();
    }

    private void updateEverything() {
        BatteryInfo.getBatteryInfo(getContext(), new -$$Lambda$BatteryHistoryDetail$ZIvw_m8MPrnAuz9tJSzFmSFxa_8(this), this.mStats, false);
    }

    public static /* synthetic */ void lambda$updateEverything$0(BatteryHistoryDetail batteryHistoryDetail, BatteryInfo info) {
        View view = batteryHistoryDetail.getView();
        info.bindHistory((UsageView) view.findViewById(R.id.battery_usage), batteryHistoryDetail.mChargingParser, batteryHistoryDetail.mScreenOn, batteryHistoryDetail.mGpsParser, batteryHistoryDetail.mFlashlightParser, batteryHistoryDetail.mCameraParser, batteryHistoryDetail.mWifiParser, batteryHistoryDetail.mCpuParser, batteryHistoryDetail.mPhoneParser);
        ((TextView) view.findViewById(R.id.charge)).setText(info.batteryPercentString);
        ((TextView) view.findViewById(R.id.estimation)).setText(info.remainingLabel);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mChargingParser, R.string.battery_stats_charging_label, R.id.charging_group);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mScreenOn, R.string.battery_stats_screen_on_label, R.id.screen_on_group);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mGpsParser, R.string.battery_stats_gps_on_label, R.id.gps_group);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mFlashlightParser, R.string.battery_stats_flashlight_on_label, R.id.flashlight_group);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mCameraParser, R.string.battery_stats_camera_on_label, R.id.camera_group);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mWifiParser, R.string.battery_stats_wifi_running_label, R.id.wifi_group);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mCpuParser, R.string.battery_stats_wake_lock_label, R.id.cpu_group);
        batteryHistoryDetail.bindData(batteryHistoryDetail.mPhoneParser, R.string.battery_stats_phone_signal_label, R.id.cell_network_group);
    }

    private void bindData(BatteryActiveProvider provider, int label, int groupId) {
        View group = getView().findViewById(groupId);
        group.setVisibility(provider.hasData() ? 0 : 8);
        ((TextView) group.findViewById(16908310)).setText(label);
        ((BatteryActiveView) group.findViewById(R.id.battery_active)).setProvider(provider);
    }

    public int getMetricsCategory() {
        return 51;
    }
}
