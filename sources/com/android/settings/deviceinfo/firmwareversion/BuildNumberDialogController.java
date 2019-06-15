package com.android.settings.deviceinfo.firmwareversion;

import android.os.Build;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.text.BidiFormatter;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;

public class BuildNumberDialogController {
    @VisibleForTesting
    static final int BUILD_NUMBER_VALUE_ID = 2131361990;
    private final FirmwareVersionDialogFragment mDialog;

    public BuildNumberDialogController(FirmwareVersionDialogFragment dialog) {
        this.mDialog = dialog;
    }

    public void initialize() {
        String buildNumber = BidiFormatter.getInstance().unicodeWrap(Build.DISPLAY);
        String defaultVersion = this.mDialog.getResources().getString(R.string.device_info_default);
        if (OPUtils.isSM8150Products()) {
            buildNumber = SystemProperties.get("ro.rom.version", defaultVersion);
        }
        this.mDialog.setText(R.id.build_number_value, buildNumber);
    }
}
