package com.android.settings.deviceinfo;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.MoveCallback;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.provider.FontsContractCompat.FontRequestCallback;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;

public class StorageWizardMoveProgress extends StorageWizardBase {
    private final MoveCallback mCallback = new MoveCallback() {
        public void onStatusChanged(int moveId, int status, long estMillis) {
            if (StorageWizardMoveProgress.this.mMoveId == moveId) {
                if (PackageManager.isMoveStatusFinished(status)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Finished with status ");
                    stringBuilder.append(status);
                    Log.d("StorageSettings", stringBuilder.toString());
                    if (status != -100) {
                        Toast.makeText(StorageWizardMoveProgress.this, StorageWizardMoveProgress.this.moveStatusToMessage(status), 1).show();
                    }
                    StorageWizardMoveProgress.this.finishAffinity();
                } else {
                    StorageWizardMoveProgress.this.setCurrentProgress(status);
                }
            }
        }
    };
    private int mMoveId;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mVolume == null) {
            finish();
            return;
        }
        setContentView(R.layout.storage_wizard_progress);
        this.mMoveId = getIntent().getIntExtra("android.content.pm.extra.MOVE_ID", -1);
        String appName = getIntent().getStringExtra("android.intent.extra.TITLE");
        String volumeName = this.mStorage.getBestVolumeDescription(this.mVolume);
        setIcon(R.drawable.ic_swap_horiz);
        setHeaderText(R.string.storage_wizard_move_progress_title, appName);
        setBodyText(R.string.storage_wizard_move_progress_body, volumeName, appName);
        getPackageManager().registerMoveCallback(this.mCallback, new Handler());
        this.mCallback.onStatusChanged(this.mMoveId, getPackageManager().getMoveStatus(this.mMoveId), -1);
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        super.onDestroy();
        getPackageManager().unregisterMoveCallback(this.mCallback);
    }

    private CharSequence moveStatusToMessage(int returnCode) {
        if (returnCode == -8) {
            return getString(R.string.move_error_device_admin);
        }
        switch (returnCode) {
            case -5:
                return getString(R.string.invalid_location);
            case FontRequestCallback.FAIL_REASON_SECURITY_VIOLATION /*-4*/:
                return getString(R.string.app_forward_locked);
            case FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR /*-3*/:
                return getString(R.string.system_package);
            case -2:
                return getString(R.string.does_not_exist);
            case -1:
                return getString(R.string.insufficient_storage);
            default:
                return getString(R.string.insufficient_storage);
        }
    }
}
