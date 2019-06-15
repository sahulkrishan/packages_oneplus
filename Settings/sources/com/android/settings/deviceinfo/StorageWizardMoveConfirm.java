package com.android.settings.deviceinfo;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.android.internal.util.Preconditions;
import com.android.settings.R;
import com.android.settings.password.ChooseLockSettingsHelper;

public class StorageWizardMoveConfirm extends StorageWizardBase {
    private static final int REQUEST_CREDENTIAL = 100;
    private ApplicationInfo mApp;
    private String mPackageName;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mVolume == null) {
            finish();
            return;
        }
        setContentView(R.layout.storage_wizard_generic);
        try {
            this.mPackageName = getIntent().getStringExtra("android.intent.extra.PACKAGE_NAME");
            this.mApp = getPackageManager().getApplicationInfo(this.mPackageName, 0);
            Preconditions.checkState(getPackageManager().getPackageCandidateVolumes(this.mApp).contains(this.mVolume));
            String appName = getPackageManager().getApplicationLabel(this.mApp).toString();
            String volumeName = this.mStorage.getBestVolumeDescription(this.mVolume);
            setIcon(R.drawable.ic_swap_horiz);
            setHeaderText(R.string.storage_wizard_move_confirm_title, appName);
            setBodyText(R.string.storage_wizard_move_confirm_body, appName, volumeName);
            setNextButtonText(R.string.move_app, new CharSequence[0]);
        } catch (NameNotFoundException e) {
            finish();
        }
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
        String appName = getPackageManager().getApplicationLabel(this.mApp).toString();
        int moveId = getPackageManager().movePackage(this.mPackageName, this.mVolume);
        Intent intent = new Intent(this, StorageWizardMoveProgress.class);
        intent.putExtra("android.content.pm.extra.MOVE_ID", moveId);
        intent.putExtra("android.intent.extra.TITLE", appName);
        intent.putExtra("android.os.storage.extra.VOLUME_ID", this.mVolume.getId());
        startActivity(intent);
        finishAffinity();
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
