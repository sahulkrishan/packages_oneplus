package com.android.settings.deviceinfo;

import com.android.settings.deviceinfo.storage.UserIconLoader.UserIconHandler;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.function.Predicate;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$StorageDashboardFragment$IconLoaderCallbacks$7UIHa462aQ5cO1d2zsPI99b5Y1Y implements Predicate {
    public static final /* synthetic */ -$$Lambda$StorageDashboardFragment$IconLoaderCallbacks$7UIHa462aQ5cO1d2zsPI99b5Y1Y INSTANCE = new -$$Lambda$StorageDashboardFragment$IconLoaderCallbacks$7UIHa462aQ5cO1d2zsPI99b5Y1Y();

    private /* synthetic */ -$$Lambda$StorageDashboardFragment$IconLoaderCallbacks$7UIHa462aQ5cO1d2zsPI99b5Y1Y() {
    }

    public final boolean test(Object obj) {
        return (((AbstractPreferenceController) obj) instanceof UserIconHandler);
    }
}
