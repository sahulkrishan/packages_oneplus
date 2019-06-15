package com.android.settings.deviceinfo;

import android.util.SparseArray;
import com.android.settings.deviceinfo.storage.UserIconLoader.UserIconHandler;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.function.Consumer;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$StorageDashboardFragment$IconLoaderCallbacks$Jn0eBlqBHbuO-2COJ4jEmaXSJJc implements Consumer {
    private final /* synthetic */ SparseArray f$0;

    public /* synthetic */ -$$Lambda$StorageDashboardFragment$IconLoaderCallbacks$Jn0eBlqBHbuO-2COJ4jEmaXSJJc(SparseArray sparseArray) {
        this.f$0 = sparseArray;
    }

    public final void accept(Object obj) {
        ((UserIconHandler) ((AbstractPreferenceController) obj)).handleUserIcons(this.f$0);
    }
}
