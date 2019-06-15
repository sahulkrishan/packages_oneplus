package com.android.settings.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.widget.SwitchWidgetController;
import com.android.settings.widget.SwitchWidgetController.OnSwitchChangeListener;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.WirelessUtils;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

public final class BluetoothEnabler implements OnSwitchChangeListener {
    private static final String EVENT_DATA_IS_BT_ON = "is_bluetooth_on";
    private static final int EVENT_UPDATE_INDEX = 0;
    private OnSwitchChangeListener mCallback;
    private Context mContext;
    private final IntentFilter mIntentFilter;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final int mMetricsEvent;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private final BroadcastReceiver mReceiver;
    private final RestrictionUtils mRestrictionUtils;
    private final SwitchWidgetController mSwitchController;
    private boolean mValidListener;

    public BluetoothEnabler(Context context, SwitchWidgetController switchController, MetricsFeatureProvider metricsFeatureProvider, LocalBluetoothManager manager, int metricsEvent) {
        this(context, switchController, metricsFeatureProvider, manager, metricsEvent, new RestrictionUtils());
    }

    public BluetoothEnabler(Context context, SwitchWidgetController switchController, MetricsFeatureProvider metricsFeatureProvider, LocalBluetoothManager manager, int metricsEvent, RestrictionUtils restrictionUtils) {
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                BluetoothEnabler.this.handleStateChanged(intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE));
            }
        };
        this.mContext = context;
        this.mMetricsFeatureProvider = metricsFeatureProvider;
        this.mSwitchController = switchController;
        this.mSwitchController.setListener(this);
        this.mValidListener = false;
        this.mMetricsEvent = metricsEvent;
        if (manager == null) {
            this.mLocalAdapter = null;
            this.mSwitchController.setEnabled(false);
        } else {
            this.mLocalAdapter = manager.getBluetoothAdapter();
        }
        this.mIntentFilter = new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED");
        this.mRestrictionUtils = restrictionUtils;
    }

    public void setupSwitchController() {
        this.mSwitchController.setupView();
    }

    public void teardownSwitchController() {
        this.mSwitchController.teardownView();
    }

    public void resume(Context context) {
        if (this.mContext != context) {
            this.mContext = context;
        }
        boolean restricted = maybeEnforceRestrictions();
        if (this.mLocalAdapter == null) {
            this.mSwitchController.setEnabled(false);
            return;
        }
        if (!restricted) {
            handleStateChanged(this.mLocalAdapter.getBluetoothState());
        }
        this.mSwitchController.startListening();
        this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
        this.mValidListener = true;
    }

    public void pause() {
        if (this.mLocalAdapter != null && this.mValidListener) {
            this.mSwitchController.stopListening();
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mValidListener = false;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void handleStateChanged(int state) {
        switch (state) {
            case 10:
                this.mSwitchController.setEnabled(true);
                setChecked(false);
                return;
            case 11:
                this.mSwitchController.setEnabled(false);
                return;
            case 12:
                this.mSwitchController.setEnabled(true);
                setChecked(true);
                return;
            case 13:
                this.mSwitchController.setEnabled(false);
                return;
            default:
                this.mSwitchController.setEnabled(true);
                setChecked(false);
                return;
        }
    }

    private void setChecked(boolean isChecked) {
        if (isChecked != this.mSwitchController.isChecked()) {
            if (this.mValidListener) {
                this.mSwitchController.stopListening();
            }
            this.mSwitchController.setChecked(isChecked);
            if (this.mValidListener) {
                this.mSwitchController.startListening();
            }
        }
    }

    public boolean onSwitchToggled(boolean isChecked) {
        if (maybeEnforceRestrictions()) {
            triggerParentPreferenceCallback(isChecked);
            return true;
        } else if (!isChecked || WirelessUtils.isRadioAllowed(this.mContext, "bluetooth")) {
            this.mMetricsFeatureProvider.action(this.mContext, this.mMetricsEvent, isChecked);
            if (this.mLocalAdapter != null) {
                boolean status = this.mLocalAdapter.setBluetoothEnabled(isChecked);
                if (isChecked && !status) {
                    this.mSwitchController.setChecked(false);
                    this.mSwitchController.setEnabled(true);
                    this.mSwitchController.updateTitle(false);
                    triggerParentPreferenceCallback(false);
                    return false;
                }
            }
            this.mSwitchController.setEnabled(false);
            triggerParentPreferenceCallback(isChecked);
            return true;
        } else {
            Toast.makeText(this.mContext, R.string.wifi_in_airplane_mode, 0).show();
            this.mSwitchController.setChecked(false);
            triggerParentPreferenceCallback(false);
            return false;
        }
    }

    public void setToggleCallback(OnSwitchChangeListener listener) {
        this.mCallback = listener;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean maybeEnforceRestrictions() {
        EnforcedAdmin admin = getEnforcedAdmin(this.mRestrictionUtils, this.mContext);
        this.mSwitchController.setDisabledByAdmin(admin);
        if (admin != null) {
            this.mSwitchController.setChecked(false);
            this.mSwitchController.setEnabled(false);
        }
        if (admin != null) {
            return true;
        }
        return false;
    }

    public static EnforcedAdmin getEnforcedAdmin(RestrictionUtils mRestrictionUtils, Context mContext) {
        EnforcedAdmin admin = mRestrictionUtils.checkIfRestrictionEnforced(mContext, "no_bluetooth");
        if (admin == null) {
            return mRestrictionUtils.checkIfRestrictionEnforced(mContext, "no_config_bluetooth");
        }
        return admin;
    }

    private void triggerParentPreferenceCallback(boolean isChecked) {
        if (this.mCallback != null) {
            this.mCallback.onSwitchToggled(isChecked);
        }
    }
}
