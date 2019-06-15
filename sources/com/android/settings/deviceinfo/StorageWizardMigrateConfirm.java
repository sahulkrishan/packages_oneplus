package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.password.ChooseLockSettingsHelper;
import java.util.Objects;

public class StorageWizardMigrateConfirm extends StorageWizardBase {
    private static final int REQUEST_CREDENTIAL = 100;
    private MigrateEstimateTask mEstimate;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.storage_wizard_generic);
        if (this.mVolume == null) {
            this.mVolume = findFirstVolume(1);
        }
        if (getPackageManager().getPrimaryStorageCurrentVolume() == null || this.mVolume == null) {
            Log.d("StorageSettings", "Missing either source or target volume");
            finish();
            return;
        }
        setIcon(R.drawable.ic_swap_horiz);
        setHeaderText(R.string.storage_wizard_migrate_v2_title, getDiskShortDescription());
        setBodyText(R.string.memory_calculating_size, new CharSequence[0]);
        setAuxChecklist();
        this.mEstimate = new MigrateEstimateTask(this) {
            public void onPostExecute(String size, String time) {
                StorageWizardMigrateConfirm.this.setBodyText(R.string.storage_wizard_migrate_v2_body, StorageWizardMigrateConfirm.this.getDiskDescription(), size, time);
            }
        };
        this.mEstimate.copyFrom(getIntent());
        this.mEstimate.execute(new Void[0]);
        setBackButtonText(R.string.storage_wizard_migrate_v2_later, new CharSequence[0]);
        setNextButtonText(R.string.storage_wizard_migrate_v2_now, new CharSequence[0]);
    }

    public void onNavigateBack(View view) {
        FeatureFactory.getFactory(this).getMetricsFeatureProvider().action((Context) this, 1413, new Pair[0]);
        Intent intent = new Intent(this, StorageWizardReady.class);
        intent.putExtra("migrate_skip", true);
        startActivity(intent);
    }

    public void onNavigateNext(View view) {
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            for (UserInfo user : ((UserManager) getSystemService(UserManager.class)).getUsers()) {
                if (!StorageManager.isUserKeyUnlocked(user.id)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("User ");
                    stringBuilder.append(user.id);
                    stringBuilder.append(" is currently locked; requesting unlock");
                    Log.d("StorageSettings", stringBuilder.toString());
                    new ChooseLockSettingsHelper(this).launchConfirmationActivityForAnyUser(100, null, null, TextUtils.expandTemplate(getText(R.string.storage_wizard_move_unlock), new CharSequence[]{user.name}), user.id);
                    return;
                }
            }
        }
        try {
            int moveId = getPackageManager().movePrimaryStorage(this.mVolume);
            FeatureFactory.getFactory(this).getMetricsFeatureProvider().action((Context) this, 1412, new Pair[0]);
            Intent intent = new Intent(this, StorageWizardMigrateProgress.class);
            intent.putExtra("android.os.storage.extra.VOLUME_ID", this.mVolume.getId());
            intent.putExtra("android.content.pm.extra.MOVE_ID", moveId);
            startActivity(intent);
            finishAffinity();
        } catch (IllegalArgumentException e) {
            if (Objects.equals(this.mVolume.getFsUuid(), ((StorageManager) getSystemService("storage")).getPrimaryStorageVolume().getUuid())) {
                Intent intent2 = new Intent(this, StorageWizardReady.class);
                intent2.putExtra("android.os.storage.extra.DISK_ID", getIntent().getStringExtra("android.os.storage.extra.DISK_ID"));
                startActivity(intent2);
                finishAffinity();
                return;
            }
            throw e;
        } catch (IllegalStateException e2) {
            Toast.makeText(this, getString(R.string.another_migration_already_in_progress), 1).show();
            finishAffinity();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 100) {
            super.onActivityResult(requestCode, resultCode, data);
        } else if (resultCode == -1) {
            onNavigateNext(null);
        } else {
            Log.w("StorageSettings", "Failed to confirm credentials");
        }
    }
}
