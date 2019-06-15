package com.android.settings.dashboard.conditional;

import android.graphics.drawable.Drawable;
import android.os.PowerManager;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.fuelgauge.BatterySaverReceiver;
import com.android.settings.fuelgauge.BatterySaverReceiver.BatterySaverListener;
import com.android.settings.fuelgauge.batterysaver.BatterySaverSettings;
import com.android.settingslib.fuelgauge.BatterySaverUtils;

public class BatterySaverCondition extends Condition implements BatterySaverListener {
    private final BatterySaverReceiver mReceiver;

    public BatterySaverCondition(ConditionManager manager) {
        super(manager);
        this.mReceiver = new BatterySaverReceiver(manager.getContext());
        this.mReceiver.setBatterySaverListener(this);
    }

    public void refreshState() {
        setActive(((PowerManager) this.mManager.getContext().getSystemService(PowerManager.class)).isPowerSaveMode());
    }

    public Drawable getIcon() {
        return this.mManager.getContext().getDrawable(R.drawable.ic_battery_saver_accent_24dp);
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(R.string.condition_battery_title);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getString(R.string.condition_battery_summary);
    }

    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(R.string.condition_turn_off)};
    }

    public void onPrimaryClick() {
        new SubSettingLauncher(this.mManager.getContext()).setDestination(BatterySaverSettings.class.getName()).setSourceMetricsCategory(35).setTitle((int) R.string.battery_saver).addFlags(268435456).launch();
    }

    public void onActionClick(int index) {
        if (index == 0) {
            BatterySaverUtils.setPowerSaveMode(this.mManager.getContext(), false, false);
            refreshState();
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected index ");
        stringBuilder.append(index);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public int getMetricsConstant() {
        return 379;
    }

    public void onResume() {
        this.mReceiver.setListening(true);
    }

    public void onPause() {
        this.mReceiver.setListening(false);
    }

    public void onPowerSaveModeChanged() {
        ((BatterySaverCondition) ConditionManager.get(this.mManager.getContext()).getCondition(BatterySaverCondition.class)).refreshState();
    }

    public void onBatteryChanged(boolean pluggedIn) {
    }
}
