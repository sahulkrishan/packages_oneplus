package com.android.settingslib.drawer;

import java.util.Comparator;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DashboardCategory$hMIMtvkEGTs2t-7RyY7SqwVmOgI implements Comparator {
    private final /* synthetic */ String f$0;

    public /* synthetic */ -$$Lambda$DashboardCategory$hMIMtvkEGTs2t-7RyY7SqwVmOgI(String str) {
        this.f$0 = str;
    }

    public final int compare(Object obj, Object obj2) {
        return DashboardCategory.lambda$sortTiles$0(this.f$0, (Tile) obj, (Tile) obj2);
    }
}
