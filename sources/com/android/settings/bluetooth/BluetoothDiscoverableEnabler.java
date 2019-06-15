package com.android.settings.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import com.android.settings.R;
import com.android.settingslib.accessibility.AccessibilityUtils;
import com.android.settingslib.bluetooth.BluetoothDiscoverableTimeoutReceiver;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;

final class BluetoothDiscoverableEnabler implements OnPreferenceClickListener {
    static final int DEFAULT_DISCOVERABLE_TIMEOUT = 120;
    private static final int DISCOVERABLE_TIMEOUT_FIVE_MINUTES = 300;
    static final int DISCOVERABLE_TIMEOUT_NEVER = 0;
    private static final int DISCOVERABLE_TIMEOUT_ONE_HOUR = 3600;
    private static final int DISCOVERABLE_TIMEOUT_TWO_MINUTES = 120;
    private static final String KEY_DISCOVERABLE_TIMEOUT = "bt_discoverable_timeout";
    private static final String SYSTEM_PROPERTY_DISCOVERABLE_TIMEOUT = "debug.bt.discoverable_time";
    private static final String TAG = "BluetoothDiscoverableEnabler";
    private static final String VALUE_DISCOVERABLE_TIMEOUT_FIVE_MINUTES = "fivemin";
    private static final String VALUE_DISCOVERABLE_TIMEOUT_NEVER = "never";
    private static final String VALUE_DISCOVERABLE_TIMEOUT_ONE_HOUR = "onehour";
    private static final String VALUE_DISCOVERABLE_TIMEOUT_TWO_MINUTES = "twomin";
    private Context mContext;
    private boolean mDiscoverable;
    private final Preference mDiscoveryPreference;
    private final LocalBluetoothAdapter mLocalAdapter;
    private int mNumberOfPairedDevices;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.bluetooth.adapter.action.SCAN_MODE_CHANGED".equals(intent.getAction())) {
                int mode = intent.getIntExtra("android.bluetooth.adapter.extra.SCAN_MODE", Integer.MIN_VALUE);
                if (mode != Integer.MIN_VALUE) {
                    BluetoothDiscoverableEnabler.this.handleModeChanged(mode);
                }
            }
        }
    };
    private final SharedPreferences mSharedPreferences;
    private int mTimeoutSecs = -1;
    private final Handler mUiHandler = new Handler();
    private final Runnable mUpdateCountdownSummaryRunnable = new Runnable() {
        public void run() {
            BluetoothDiscoverableEnabler.this.updateCountdownSummary();
        }
    };

    BluetoothDiscoverableEnabler(LocalBluetoothAdapter adapter, Preference discoveryPreference) {
        this.mLocalAdapter = adapter;
        this.mDiscoveryPreference = discoveryPreference;
        this.mSharedPreferences = discoveryPreference.getSharedPreferences();
        discoveryPreference.setPersistent(false);
    }

    public void resume(Context context) {
        if (this.mLocalAdapter != null) {
            if (this.mContext != context) {
                this.mContext = context;
            }
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.adapter.action.SCAN_MODE_CHANGED"));
            this.mDiscoveryPreference.setOnPreferenceClickListener(this);
            handleModeChanged(this.mLocalAdapter.getScanMode());
        }
    }

    public void pause() {
        if (this.mLocalAdapter != null) {
            this.mUiHandler.removeCallbacks(this.mUpdateCountdownSummaryRunnable);
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mDiscoveryPreference.setOnPreferenceClickListener(null);
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        this.mDiscoverable ^= 1;
        setEnabled(this.mDiscoverable);
        return true;
    }

    private void setEnabled(boolean enable) {
        if (enable) {
            int timeout = getDiscoverableTimeout();
            long endTimestamp = System.currentTimeMillis() + (((long) timeout) * 1000);
            LocalBluetoothPreferences.persistDiscoverableEndTimestamp(this.mContext, endTimestamp);
            updateCountdownSummary();
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("setEnabled(): enabled = ");
            stringBuilder.append(enable);
            stringBuilder.append("timeout = ");
            stringBuilder.append(timeout);
            Log.d(str, stringBuilder.toString());
            if (timeout > 0) {
                BluetoothDiscoverableTimeoutReceiver.setDiscoverableAlarm(this.mContext, endTimestamp);
                return;
            } else {
                BluetoothDiscoverableTimeoutReceiver.cancelDiscoverableAlarm(this.mContext);
                return;
            }
        }
        BluetoothDiscoverableTimeoutReceiver.cancelDiscoverableAlarm(this.mContext);
    }

    private void updateTimerDisplay(int timeout) {
        if (getDiscoverableTimeout() == 0) {
            this.mDiscoveryPreference.setSummary((int) R.string.bluetooth_is_discoverable_always);
            return;
        }
        String textTimeout = formatTimeRemaining(timeout);
        this.mDiscoveryPreference.setSummary(this.mContext.getString(R.string.bluetooth_is_discoverable, new Object[]{textTimeout}));
    }

    private static String formatTimeRemaining(int timeout) {
        StringBuilder sb = new StringBuilder(6);
        int min = timeout / 60;
        sb.append(min);
        sb.append(AccessibilityUtils.ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
        int sec = timeout - (min * 60);
        if (sec < 10) {
            sb.append('0');
        }
        sb.append(sec);
        return sb.toString();
    }

    /* Access modifiers changed, original: 0000 */
    public void setDiscoverableTimeout(int index) {
        String timeoutValue;
        switch (index) {
            case 1:
                this.mTimeoutSecs = 300;
                timeoutValue = VALUE_DISCOVERABLE_TIMEOUT_FIVE_MINUTES;
                break;
            case 2:
                this.mTimeoutSecs = 3600;
                timeoutValue = VALUE_DISCOVERABLE_TIMEOUT_ONE_HOUR;
                break;
            case 3:
                this.mTimeoutSecs = 0;
                timeoutValue = VALUE_DISCOVERABLE_TIMEOUT_NEVER;
                break;
            default:
                this.mTimeoutSecs = 120;
                timeoutValue = VALUE_DISCOVERABLE_TIMEOUT_TWO_MINUTES;
                break;
        }
        this.mSharedPreferences.edit().putString(KEY_DISCOVERABLE_TIMEOUT, timeoutValue).apply();
        setEnabled(true);
    }

    private int getDiscoverableTimeout() {
        if (this.mTimeoutSecs != -1) {
            return this.mTimeoutSecs;
        }
        int timeout = SystemProperties.getInt(SYSTEM_PROPERTY_DISCOVERABLE_TIMEOUT, -1);
        if (timeout < 0) {
            String timeoutValue = this.mSharedPreferences.getString(KEY_DISCOVERABLE_TIMEOUT, VALUE_DISCOVERABLE_TIMEOUT_TWO_MINUTES);
            if (timeoutValue.equals(VALUE_DISCOVERABLE_TIMEOUT_NEVER)) {
                timeout = 0;
            } else if (timeoutValue.equals(VALUE_DISCOVERABLE_TIMEOUT_ONE_HOUR)) {
                timeout = 3600;
            } else if (timeoutValue.equals(VALUE_DISCOVERABLE_TIMEOUT_FIVE_MINUTES)) {
                timeout = 300;
            } else {
                timeout = 120;
            }
        }
        this.mTimeoutSecs = timeout;
        return timeout;
    }

    /* Access modifiers changed, original: 0000 */
    public int getDiscoverableTimeoutIndex() {
        int timeout = getDiscoverableTimeout();
        if (timeout == 0) {
            return 3;
        }
        if (timeout == 300) {
            return 1;
        }
        if (timeout != 3600) {
            return 0;
        }
        return 2;
    }

    /* Access modifiers changed, original: 0000 */
    public void setNumberOfPairedDevices(int pairedDevices) {
        this.mNumberOfPairedDevices = pairedDevices;
        handleModeChanged(this.mLocalAdapter.getScanMode());
    }

    /* Access modifiers changed, original: 0000 */
    public void handleModeChanged(int mode) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("handleModeChanged(): mode = ");
        stringBuilder.append(mode);
        Log.d(str, stringBuilder.toString());
        if (mode == 23) {
            this.mDiscoverable = true;
            updateCountdownSummary();
            return;
        }
        this.mDiscoverable = false;
        setSummaryNotDiscoverable();
    }

    private void setSummaryNotDiscoverable() {
        if (this.mNumberOfPairedDevices != 0) {
            this.mDiscoveryPreference.setSummary((int) R.string.bluetooth_only_visible_to_paired_devices);
        } else {
            this.mDiscoveryPreference.setSummary((int) R.string.bluetooth_not_visible_to_other_devices);
        }
    }

    private void updateCountdownSummary() {
        if (this.mLocalAdapter.getScanMode() == 23) {
            long currentTimestamp = System.currentTimeMillis();
            long endTimestamp = LocalBluetoothPreferences.getDiscoverableEndTimestamp(this.mContext);
            if (currentTimestamp > endTimestamp) {
                updateTimerDisplay(0);
                return;
            }
            updateTimerDisplay((int) ((endTimestamp - currentTimestamp) / 1000));
            synchronized (this) {
                this.mUiHandler.removeCallbacks(this.mUpdateCountdownSummaryRunnable);
                this.mUiHandler.postDelayed(this.mUpdateCountdownSummaryRunnable, 1000);
            }
        }
    }
}
