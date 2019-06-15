package com.android.settings.deviceinfo;

import android.os.Bundle;
import android.view.View;
import com.android.settings.R;

public class StorageWizardReady extends StorageWizardBase {
    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mDisk == null) {
            finish();
            return;
        }
        setContentView(R.layout.storage_wizard_generic);
        setHeaderText(R.string.storage_wizard_ready_title, getDiskShortDescription());
        if (findFirstVolume(1) == null) {
            setBodyText(R.string.storage_wizard_ready_v2_external_body, getDiskDescription());
        } else if (getIntent().getBooleanExtra("migrate_skip", false)) {
            setBodyText(R.string.storage_wizard_ready_v2_internal_body, getDiskDescription());
        } else {
            setBodyText(R.string.storage_wizard_ready_v2_internal_moved_body, getDiskDescription(), getDiskShortDescription());
        }
        setNextButtonText(R.string.done, new CharSequence[0]);
    }

    public void onNavigateNext(View view) {
        finishAffinity();
    }
}
