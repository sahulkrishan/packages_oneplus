package com.android.settings.deviceinfo;

import android.util.SparseArray;
import com.android.settings.deviceinfo.StorageDashboardFragment.IconLoaderCallbacks;
import com.android.settings.deviceinfo.storage.UserIconLoader;
import com.android.settings.deviceinfo.storage.UserIconLoader.FetchUserIconTask;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$StorageDashboardFragment$IconLoaderCallbacks$yGwysNy4Bq4_2nwwvU2QePhZgvU implements FetchUserIconTask {
    private final /* synthetic */ IconLoaderCallbacks f$0;

    public /* synthetic */ -$$Lambda$StorageDashboardFragment$IconLoaderCallbacks$yGwysNy4Bq4_2nwwvU2QePhZgvU(IconLoaderCallbacks iconLoaderCallbacks) {
        this.f$0 = iconLoaderCallbacks;
    }

    public final SparseArray getUserIcons() {
        return UserIconLoader.loadUserIconsWithContext(StorageDashboardFragment.this.getContext());
    }
}
