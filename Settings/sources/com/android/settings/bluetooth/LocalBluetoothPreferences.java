package com.android.settings.bluetooth;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

final class LocalBluetoothPreferences {
    private static final boolean DEBUG = true;
    private static final int GRACE_PERIOD_TO_SHOW_DIALOGS_IN_FOREGROUND = 60000;
    private static final String KEY_DISCOVERABLE_END_TIMESTAMP = "discoverable_end_timestamp";
    private static final String KEY_LAST_SELECTED_DEVICE = "last_selected_device";
    private static final String KEY_LAST_SELECTED_DEVICE_TIME = "last_selected_device_time";
    private static final String SHARED_PREFERENCES_NAME = "bluetooth_settings";
    private static final String TAG = "LocalBluetoothPreferences";

    private LocalBluetoothPreferences() {
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
    }

    static long getDiscoverableEndTimestamp(Context context) {
        return getSharedPreferences(context).getLong(KEY_DISCOVERABLE_END_TIMESTAMP, 0);
    }

    static boolean shouldShowDialogInForeground(Context context, String deviceAddress, String deviceName) {
        String str = deviceAddress;
        LocalBluetoothManager manager = Utils.getLocalBtManager(context);
        if (manager == null) {
            Log.v(TAG, "manager == null - do not show dialog.");
            return false;
        } else if (manager.isForegroundActivity()) {
            return true;
        } else {
            if ((context.getResources().getConfiguration().uiMode & 5) == 5) {
                Log.v(TAG, "in appliance mode - do not show dialog.");
                return false;
            }
            long currentTimeMillis = System.currentTimeMillis();
            SharedPreferences sharedPreferences = getSharedPreferences(context);
            if (sharedPreferences.getLong(KEY_DISCOVERABLE_END_TIMESTAMP, 0) + 60000 > currentTimeMillis) {
                return true;
            }
            LocalBluetoothAdapter adapter = manager.getBluetoothAdapter();
            if (adapter != null && (adapter.isDiscovering() || adapter.getDiscoveryEndMillis() + 60000 > currentTimeMillis)) {
                return true;
            }
            if (str != null && str.equals(sharedPreferences.getString(KEY_LAST_SELECTED_DEVICE, null)) && 60000 + sharedPreferences.getLong(KEY_LAST_SELECTED_DEVICE_TIME, 0) > currentTimeMillis) {
                return true;
            }
            if (TextUtils.isEmpty(deviceName)) {
                Context context2 = context;
                String str2 = deviceName;
            } else {
                if (deviceName.equals(context.getString(17039730))) {
                    Log.v(TAG, "showing dialog for packaged keyboard");
                    return true;
                }
            }
            Log.v(TAG, "Found no reason to show the dialog - do not show dialog.");
            return false;
        }
    }

    static void persistSelectedDeviceInPicker(Context context, String deviceAddress) {
        Editor editor = getSharedPreferences(context).edit();
        editor.putString(KEY_LAST_SELECTED_DEVICE, deviceAddress);
        editor.putLong(KEY_LAST_SELECTED_DEVICE_TIME, System.currentTimeMillis());
        editor.apply();
    }

    static void persistDiscoverableEndTimestamp(Context context, long endTimestamp) {
        Editor editor = getSharedPreferences(context).edit();
        editor.putLong(KEY_DISCOVERABLE_END_TIMESTAMP, endTimestamp);
        editor.apply();
    }
}
