package com.android.settings.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class ForgetDeviceDialogFragment extends InstrumentedDialogFragment {
    private static final String KEY_DEVICE_ADDRESS = "device_address";
    public static final String TAG = "ForgetBluetoothDevice";
    private CachedBluetoothDevice mDevice;

    public static ForgetDeviceDialogFragment newInstance(String deviceAddress) {
        Bundle args = new Bundle(1);
        args.putString("device_address", deviceAddress);
        ForgetDeviceDialogFragment dialog = new ForgetDeviceDialogFragment();
        dialog.setArguments(args);
        return dialog;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public CachedBluetoothDevice getDevice(Context context) {
        String deviceAddress = getArguments().getString("device_address");
        LocalBluetoothManager manager = Utils.getLocalBtManager(context);
        return manager.getCachedDeviceManager().findDevice(manager.getBluetoothAdapter().getRemoteDevice(deviceAddress));
    }

    public int getMetricsCategory() {
        return 1031;
    }

    public Dialog onCreateDialog(Bundle inState) {
        OnClickListener onConfirm = new -$$Lambda$ForgetDeviceDialogFragment$EDf2UTKPcHIZGnJUVoyf7QwuxfU(this);
        Context context = getContext();
        this.mDevice = getDevice(context);
        AlertDialog dialog = new Builder(context).setPositiveButton(R.string.bluetooth_unpair_dialog_forget_confirm_button, onConfirm).setNegativeButton(17039360, null).create();
        dialog.setTitle(R.string.bluetooth_unpair_dialog_title);
        Object[] objArr = new Object[1];
        objArr[0] = this.mDevice != null ? this.mDevice.getName() : "";
        dialog.setMessage(context.getString(R.string.bluetooth_unpair_dialog_body, objArr));
        return dialog;
    }

    public static /* synthetic */ void lambda$onCreateDialog$0(ForgetDeviceDialogFragment forgetDeviceDialogFragment, DialogInterface dialog, int which) {
        forgetDeviceDialogFragment.mDevice.unpair();
        Activity activity = forgetDeviceDialogFragment.getActivity();
        if (activity != null) {
            activity.finish();
        }
    }
}
