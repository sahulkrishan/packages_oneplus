package com.android.settings.development;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.PowerManager;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class BluetoothA2dpHwOffloadRebootDialog extends InstrumentedDialogFragment implements OnClickListener {
    public static final String TAG = "BluetoothA2dpHwOffloadReboot";

    public interface OnA2dpHwDialogConfirmedListener {
        void onA2dpHwDialogConfirmed();
    }

    public static void show(DevelopmentSettingsDashboardFragment host, BluetoothA2dpHwOffloadPreferenceController controller) {
        FragmentManager manager = host.getActivity().getFragmentManager();
        if (manager.findFragmentByTag(TAG) == null) {
            BluetoothA2dpHwOffloadRebootDialog dialog = new BluetoothA2dpHwOffloadRebootDialog();
            dialog.setTargetFragment(host, 0);
            dialog.show(manager, TAG);
        }
    }

    public int getMetricsCategory() {
        return 1441;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity()).setMessage(R.string.bluetooth_disable_a2dp_hw_offload_dialog_message).setTitle(R.string.bluetooth_disable_a2dp_hw_offload_dialog_title).setPositiveButton(R.string.bluetooth_disable_a2dp_hw_offload_dialog_confirm, this).setNegativeButton(R.string.bluetooth_disable_a2dp_hw_offload_dialog_cancel, this).create();
    }

    public void onClick(DialogInterface dialog, int which) {
        OnA2dpHwDialogConfirmedListener host = (OnA2dpHwDialogConfirmedListener) getTargetFragment();
        if (host != null && which == -1) {
            host.onA2dpHwDialogConfirmed();
            ((PowerManager) getContext().getSystemService(PowerManager.class)).reboot(null);
        }
    }
}
