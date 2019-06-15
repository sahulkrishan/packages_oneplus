package com.android.settings.deviceinfo.firmwareversion;

import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settingslib.DeviceInfoUtils;

public class KernelVersionDialogController {
    @VisibleForTesting
    static int KERNEL_VERSION_VALUE_ID = R.id.kernel_version_value;
    private final FirmwareVersionDialogFragment mDialog;

    public KernelVersionDialogController(FirmwareVersionDialogFragment dialog) {
        this.mDialog = dialog;
    }

    public void initialize() {
        this.mDialog.setText(KERNEL_VERSION_VALUE_ID, DeviceInfoUtils.getFormattedKernelVersion(this.mDialog.getContext()));
    }
}
