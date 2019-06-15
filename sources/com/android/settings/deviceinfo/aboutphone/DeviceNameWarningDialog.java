package com.android.settings.deviceinfo.aboutphone;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class DeviceNameWarningDialog extends InstrumentedDialogFragment implements OnClickListener {
    public static final String TAG = "DeviceNameWarningDlg";

    public static void show(MyDeviceInfoFragment host) {
        FragmentManager manager = host.getActivity().getFragmentManager();
        if (manager.findFragmentByTag(TAG) == null) {
            DeviceNameWarningDialog dialog = new DeviceNameWarningDialog();
            dialog.setTargetFragment(host, 0);
            dialog.show(manager, TAG);
        }
    }

    public int getMetricsCategory() {
        return 1219;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity()).setTitle(R.string.my_device_info_device_name_preference_title).setMessage(R.string.about_phone_device_name_warning).setCancelable(false).setPositiveButton(17039370, this).setNegativeButton(17039360, this).create();
    }

    public void onClick(DialogInterface dialog, int which) {
        MyDeviceInfoFragment host = (MyDeviceInfoFragment) getTargetFragment();
        if (which == -1) {
            host.onSetDeviceNameConfirm();
        } else if (which == -2) {
            host.onSetDeviceNameCancel();
        }
    }
}
