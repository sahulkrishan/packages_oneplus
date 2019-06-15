package com.android.settings.deviceinfo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.overlay.FeatureFactory;

public class StorageWizardInit extends StorageWizardBase {
    private Button mExternal;
    private Button mInternal;
    private boolean mIsPermittedToAdopt;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mDisk == null) {
            finish();
            return;
        }
        setContentView(R.layout.storage_wizard_init);
        boolean z = UserManager.get(this).isAdminUser() && !ActivityManager.isUserAMonkey();
        this.mIsPermittedToAdopt = z;
        setHeaderText(R.string.storage_wizard_init_v2_title, getDiskShortDescription());
        this.mExternal = (Button) requireViewById(R.id.storage_wizard_init_external);
        this.mInternal = (Button) requireViewById(R.id.storage_wizard_init_internal);
        setBackButtonText(R.string.storage_wizard_init_v2_later, new CharSequence[0]);
        if (!this.mDisk.isAdoptable()) {
            onNavigateExternal(null);
        } else if (!this.mIsPermittedToAdopt) {
            this.mInternal.setEnabled(false);
        }
    }

    public void onNavigateBack(View view) {
        finish();
    }

    public void onNavigateExternal(View view) {
        if (view != null) {
            FeatureFactory.getFactory(this).getMetricsFeatureProvider().action((Context) this, 1407, new Pair[0]);
        }
        if (this.mVolume == null || this.mVolume.getType() != 0 || this.mVolume.getState() == 6) {
            StorageWizardFormatConfirm.showPublic(this, this.mDisk.getId());
            return;
        }
        this.mStorage.setVolumeInited(this.mVolume.getFsUuid(), true);
        Intent intent = new Intent(this, StorageWizardReady.class);
        intent.putExtra("android.os.storage.extra.DISK_ID", this.mDisk.getId());
        startActivity(intent);
        finish();
    }

    public void onNavigateInternal(View view) {
        if (view != null) {
            FeatureFactory.getFactory(this).getMetricsFeatureProvider().action((Context) this, 1408, new Pair[0]);
        }
        StorageWizardFormatConfirm.showPrivate(this, this.mDisk.getId());
    }
}
