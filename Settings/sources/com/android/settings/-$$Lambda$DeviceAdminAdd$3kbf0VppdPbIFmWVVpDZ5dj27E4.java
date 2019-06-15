package com.android.settings;

import android.content.ComponentName;
import java.util.function.Predicate;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DeviceAdminAdd$3kbf0VppdPbIFmWVVpDZ5dj27E4 implements Predicate {
    private final /* synthetic */ String f$0;

    public /* synthetic */ -$$Lambda$DeviceAdminAdd$3kbf0VppdPbIFmWVVpDZ5dj27E4(String str) {
        this.f$0 = str;
    }

    public final boolean test(Object obj) {
        return ((ComponentName) obj).getPackageName().equals(this.f$0);
    }
}
