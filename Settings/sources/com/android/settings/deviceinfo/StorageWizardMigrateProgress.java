package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.MoveCallback;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;
import com.google.common.primitives.Ints;

public class StorageWizardMigrateProgress extends StorageWizardBase {
    private static final String ACTION_FINISH_WIZARD = "com.android.systemui.action.FINISH_WIZARD";
    private final MoveCallback mCallback = new MoveCallback() {
        public void onStatusChanged(int moveId, int status, long estMillis) {
            if (StorageWizardMigrateProgress.this.mMoveId == moveId) {
                Context context = StorageWizardMigrateProgress.this;
                if (PackageManager.isMoveStatusFinished(status)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Finished with status ");
                    stringBuilder.append(status);
                    Log.d("StorageSettings", stringBuilder.toString());
                    if (status != -100) {
                        Toast.makeText(context, StorageWizardMigrateProgress.this.getString(R.string.insufficient_storage), 1).show();
                    } else if (StorageWizardMigrateProgress.this.mDisk != null) {
                        Intent finishIntent = new Intent(StorageWizardMigrateProgress.ACTION_FINISH_WIZARD);
                        finishIntent.addFlags(Ints.MAX_POWER_OF_TWO);
                        StorageWizardMigrateProgress.this.sendBroadcast(finishIntent);
                        if (!StorageWizardMigrateProgress.this.isFinishing()) {
                            Intent intent = new Intent(context, StorageWizardReady.class);
                            intent.putExtra("android.os.storage.extra.DISK_ID", StorageWizardMigrateProgress.this.mDisk.getId());
                            StorageWizardMigrateProgress.this.startActivity(intent);
                        }
                    }
                    StorageWizardMigrateProgress.this.finishAffinity();
                } else {
                    StorageWizardMigrateProgress.this.setCurrentProgress(status);
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
        setIcon(R.drawable.ic_swap_horiz);
        setHeaderText(R.string.storage_wizard_migrate_progress_v2_title, new CharSequence[0]);
        setAuxChecklist();
        getPackageManager().registerMoveCallback(this.mCallback, new Handler());
        this.mCallback.onStatusChanged(this.mMoveId, getPackageManager().getMoveStatus(this.mMoveId), -1);
    }
}
