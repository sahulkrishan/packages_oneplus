package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.os.storage.VolumeInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreateOptionsMenu;
import com.android.settingslib.core.lifecycle.events.OnOptionsItemSelected;
import com.android.settingslib.core.lifecycle.events.OnPrepareOptionsMenu;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.util.Objects;

public class PrivateVolumeOptionMenuController implements LifecycleObserver, OnCreateOptionsMenu, OnPrepareOptionsMenu, OnOptionsItemSelected {
    private static final int OPTIONS_MENU_MIGRATE_DATA = 100;
    private Context mContext;
    private PackageManagerWrapper mPm;
    private VolumeInfo mVolumeInfo;

    public PrivateVolumeOptionMenuController(Context context, VolumeInfo volumeInfo, PackageManagerWrapper packageManager) {
        this.mContext = context;
        this.mVolumeInfo = volumeInfo;
        this.mPm = packageManager;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 100, 0, R.string.storage_menu_migrate);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (this.mVolumeInfo != null) {
            VolumeInfo privateVol = this.mPm.getPrimaryStorageCurrentVolume();
            MenuItem migrate = menu.findItem(100);
            if (migrate != null) {
                boolean z = true;
                if (privateVol == null || privateVol.getType() != 1 || Objects.equals(this.mVolumeInfo, privateVol)) {
                    z = false;
                }
                migrate.setVisible(z);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 100) {
            return false;
        }
        Intent intent = new Intent(this.mContext, StorageWizardMigrateConfirm.class);
        intent.putExtra("android.os.storage.extra.VOLUME_ID", this.mVolumeInfo.getId());
        this.mContext.startActivity(intent);
        return true;
    }
}
