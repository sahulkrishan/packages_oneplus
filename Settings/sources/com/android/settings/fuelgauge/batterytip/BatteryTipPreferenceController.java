package com.android.settings.fuelgauge.batterytip;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.SettingsActivity;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.fuelgauge.batterytip.actions.BatteryTipAction;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.fuelgauge.batterytip.tips.SummaryTip;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatteryTipPreferenceController extends BasePreferenceController {
    private static final String KEY_BATTERY_TIPS = "key_battery_tips";
    private static final int REQUEST_ANOMALY_ACTION = 0;
    private static final String TAG = "BatteryTipPreferenceController";
    private BatteryTipListener mBatteryTipListener;
    private Map<String, BatteryTip> mBatteryTipMap;
    private List<BatteryTip> mBatteryTips;
    InstrumentedPreferenceFragment mFragment;
    private MetricsFeatureProvider mMetricsFeatureProvider;
    private boolean mNeedUpdate;
    @VisibleForTesting
    Context mPrefContext;
    @VisibleForTesting
    PreferenceGroup mPreferenceGroup;
    private SettingsActivity mSettingsActivity;

    public interface BatteryTipListener {
        void onBatteryTipHandled(BatteryTip batteryTip);
    }

    public BatteryTipPreferenceController(Context context, String preferenceKey) {
        this(context, preferenceKey, null, null, null);
    }

    public BatteryTipPreferenceController(Context context, String preferenceKey, SettingsActivity settingsActivity, InstrumentedPreferenceFragment fragment, BatteryTipListener batteryTipListener) {
        super(context, preferenceKey);
        this.mBatteryTipListener = batteryTipListener;
        this.mBatteryTipMap = new HashMap();
        this.mFragment = fragment;
        this.mSettingsActivity = settingsActivity;
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
        this.mNeedUpdate = true;
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPrefContext = screen.getContext();
        this.mPreferenceGroup = (PreferenceGroup) screen.findPreference(getPreferenceKey());
        this.mPreferenceGroup.addPreference(new SummaryTip(0, -1).buildPreference(this.mPrefContext));
    }

    public void updateBatteryTips(List<BatteryTip> batteryTips) {
        if (batteryTips != null) {
            int size;
            int i;
            if (this.mBatteryTips == null) {
                this.mBatteryTips = batteryTips;
            } else {
                size = batteryTips.size();
                for (i = 0; i < size; i++) {
                    ((BatteryTip) this.mBatteryTips.get(i)).updateState((BatteryTip) batteryTips.get(i));
                }
            }
            this.mPreferenceGroup.removeAll();
            size = batteryTips.size();
            for (i = 0; i < size; i++) {
                BatteryTip batteryTip = (BatteryTip) this.mBatteryTips.get(i);
                if (batteryTip.getState() != 2) {
                    Preference preference = batteryTip.buildPreference(this.mPrefContext);
                    this.mBatteryTipMap.put(preference.getKey(), batteryTip);
                    this.mPreferenceGroup.addPreference(preference);
                    batteryTip.log(this.mContext, this.mMetricsFeatureProvider);
                    this.mNeedUpdate = batteryTip.needUpdate();
                    break;
                }
            }
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        BatteryTip batteryTip = (BatteryTip) this.mBatteryTipMap.get(preference.getKey());
        if (batteryTip == null) {
            return super.handlePreferenceTreeClick(preference);
        }
        if (batteryTip.shouldShowDialog()) {
            BatteryTipDialogFragment dialogFragment = BatteryTipDialogFragment.newInstance(batteryTip, this.mFragment.getMetricsCategory());
            dialogFragment.setTargetFragment(this.mFragment, 0);
            dialogFragment.show(this.mFragment.getFragmentManager(), TAG);
        } else {
            BatteryTipAction action = BatteryTipUtils.getActionForBatteryTip(batteryTip, this.mSettingsActivity, this.mFragment);
            if (action != null) {
                action.handlePositiveAction(this.mFragment.getMetricsCategory());
            }
            if (this.mBatteryTipListener != null) {
                this.mBatteryTipListener.onBatteryTipHandled(batteryTip);
            }
        }
        return true;
    }

    public void restoreInstanceState(Bundle bundle) {
        if (bundle != null) {
            updateBatteryTips(bundle.getParcelableArrayList(KEY_BATTERY_TIPS));
        }
    }

    public void saveInstanceState(Bundle outState) {
        outState.putParcelableList(KEY_BATTERY_TIPS, this.mBatteryTips);
    }

    public boolean needUpdate() {
        return this.mNeedUpdate;
    }
}
