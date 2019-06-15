package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.R;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public class SecurityPatchLevelDialogController implements OnClickListener {
    private static final Uri INTENT_URI_DATA = Uri.parse("https://source.android.com/security/bulletin/");
    @VisibleForTesting
    static final int SECURITY_PATCH_LABEL_ID = 2131363031;
    @VisibleForTesting
    static final int SECURITY_PATCH_VALUE_ID = 2131363032;
    private static final String TAG = "SecurityPatchCtrl";
    private final Context mContext;
    private final String mCurrentPatch = DeviceInfoUtils.getSecurityPatch();
    private final FirmwareVersionDialogFragment mDialog;
    private final PackageManagerWrapper mPackageManager = new PackageManagerWrapper(this.mContext.getPackageManager());

    public SecurityPatchLevelDialogController(FirmwareVersionDialogFragment dialog) {
        this.mDialog = dialog;
        this.mContext = dialog.getContext();
    }

    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(INTENT_URI_DATA);
        if (this.mPackageManager.queryIntentActivities(intent, 0).isEmpty()) {
            Log.w(TAG, "Stop click action on 2131363032: queryIntentActivities() returns empty");
        } else {
            this.mContext.startActivity(intent);
        }
    }

    public void initialize() {
        if (TextUtils.isEmpty(this.mCurrentPatch)) {
            this.mDialog.removeSettingFromScreen(R.id.security_patch_level_label);
            this.mDialog.removeSettingFromScreen(R.id.security_patch_level_value);
            return;
        }
        registerListeners();
        this.mDialog.setText(R.id.security_patch_level_value, this.mCurrentPatch);
    }

    private void registerListeners() {
        this.mDialog.registerClickListener(R.id.security_patch_level_label, this);
        this.mDialog.registerClickListener(R.id.security_patch_level_value, this);
    }
}
