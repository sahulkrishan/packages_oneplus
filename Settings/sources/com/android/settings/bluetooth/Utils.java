package com.android.settings.bluetooth;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothManager.BluetoothManagerCallback;
import com.android.settingslib.bluetooth.Utils.ErrorListener;

public final class Utils {
    static final boolean D = true;
    static final boolean V = false;
    private static final ErrorListener mErrorListener = new ErrorListener() {
        public void onShowError(Context context, String name, int messageResId) {
            Utils.showError(context, name, messageResId);
        }
    };
    private static final BluetoothManagerCallback mOnInitCallback = new BluetoothManagerCallback() {
        public void onBluetoothManagerInitialized(Context appContext, LocalBluetoothManager bluetoothManager) {
            com.android.settingslib.bluetooth.Utils.setErrorListener(Utils.mErrorListener);
        }
    };

    private Utils() {
    }

    public static int getConnectionStateSummary(int connectionState) {
        switch (connectionState) {
            case 0:
                return R.string.bluetooth_disconnected;
            case 1:
                return R.string.bluetooth_connecting;
            case 2:
                return R.string.bluetooth_connected;
            case 3:
                return R.string.bluetooth_disconnecting;
            default:
                return 0;
        }
    }

    static AlertDialog showDisconnectDialog(Context context, AlertDialog dialog, OnClickListener disconnectListener, CharSequence title, CharSequence message) {
        if (dialog == null) {
            dialog = new Builder(context).setPositiveButton(17039370, disconnectListener).setNegativeButton(17039360, null).create();
        } else {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            dialog.setButton(-1, context.getText(17039370), disconnectListener);
        }
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();
        return dialog;
    }

    static void showConnectingError(Context context, String name) {
        showConnectingError(context, name, getLocalBtManager(context));
    }

    @VisibleForTesting
    static void showConnectingError(Context context, String name, LocalBluetoothManager manager) {
        FeatureFactory.getFactory(context).getMetricsFeatureProvider().visible(context, 0, 869);
        showError(context, name, R.string.bluetooth_connecting_error_message, manager);
    }

    static void showError(Context context, String name, int messageResId) {
        showError(context, name, messageResId, getLocalBtManager(context));
    }

    private static void showError(Context context, String name, int messageResId, LocalBluetoothManager manager) {
        String message = context.getString(messageResId, new Object[]{name});
        Context activity = manager.getForegroundActivity();
        if (manager.isForegroundActivity()) {
            new Builder(activity).setTitle(R.string.bluetooth_error_title).setMessage(message).setPositiveButton(17039370, null).show();
        } else {
            Toast.makeText(context, message, 0).show();
        }
    }

    public static LocalBluetoothManager getLocalBtManager(Context context) {
        return LocalBluetoothManager.getInstance(context, mOnInitCallback);
    }

    public static String createRemoteName(Context context, BluetoothDevice device) {
        String mRemoteName = device != null ? device.getAliasName() : null;
        if (mRemoteName == null) {
            return context.getString(R.string.unknown);
        }
        return mRemoteName;
    }

    public static boolean isBluetoothScanningEnabled(Context context) {
        return Global.getInt(context.getContentResolver(), "ble_scan_always_enabled", 0) == 1;
    }
}
