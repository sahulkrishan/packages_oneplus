package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settingslib.Utils;

public class BasebandVersionDialogController {
    @VisibleForTesting
    static final String BASEBAND_PROPERTY = "gsm.version.baseband";
    @VisibleForTesting
    static final int BASEBAND_VERSION_LABEL_ID = 2131361939;
    @VisibleForTesting
    static final int BASEBAND_VERSION_VALUE_ID = 2131361940;
    private final FirmwareVersionDialogFragment mDialog;

    public BasebandVersionDialogController(FirmwareVersionDialogFragment dialog) {
        this.mDialog = dialog;
    }

    public void initialize() {
        Context context = this.mDialog.getContext();
        if (Utils.isWifiOnly(context)) {
            this.mDialog.removeSettingFromScreen(R.id.baseband_version_label);
            this.mDialog.removeSettingFromScreen(R.id.baseband_version_value);
            return;
        }
        this.mDialog.setText(R.id.baseband_version_value, SystemProperties.get(BASEBAND_PROPERTY, context.getString(R.string.device_info_default)));
    }
}
