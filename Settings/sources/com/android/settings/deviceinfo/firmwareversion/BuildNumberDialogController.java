package com.android.settings.deviceinfo.firmwareversion;

import android.os.Build;
import android.support.annotation.VisibleForTesting;
import android.text.BidiFormatter;
import com.android.settings.R;

public class BuildNumberDialogController {
    @VisibleForTesting
    static final int BUILD_NUMBER_VALUE_ID = 2131361989;
    private final FirmwareVersionDialogFragment mDialog;

    public BuildNumberDialogController(FirmwareVersionDialogFragment dialog) {
        this.mDialog = dialog;
    }

    public void initialize() {
        this.mDialog.setText(R.id.build_number_value, BidiFormatter.getInstance().unicodeWrap(Build.DISPLAY));
    }
}
