package com.android.settings.deviceinfo;

import android.app.Activity;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProvider;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$StorageSettings$pjFUgWj2HWW95DLVydfI8EgfTdg implements SummaryProviderFactory {
    public static final /* synthetic */ -$$Lambda$StorageSettings$pjFUgWj2HWW95DLVydfI8EgfTdg INSTANCE = new -$$Lambda$StorageSettings$pjFUgWj2HWW95DLVydfI8EgfTdg();

    private /* synthetic */ -$$Lambda$StorageSettings$pjFUgWj2HWW95DLVydfI8EgfTdg() {
    }

    public final SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
        return new SummaryProvider(activity, summaryLoader, null);
    }
}
