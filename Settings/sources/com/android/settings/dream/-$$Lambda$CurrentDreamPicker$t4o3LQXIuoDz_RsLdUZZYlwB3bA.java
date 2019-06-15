package com.android.settings.dream;

import android.content.ComponentName;
import com.android.settingslib.dream.DreamBackend.DreamInfo;
import java.util.Map;
import java.util.function.Consumer;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$CurrentDreamPicker$t4o3LQXIuoDz_RsLdUZZYlwB3bA implements Consumer {
    private final /* synthetic */ Map f$0;

    public /* synthetic */ -$$Lambda$CurrentDreamPicker$t4o3LQXIuoDz_RsLdUZZYlwB3bA(Map map) {
        this.f$0 = map;
    }

    public final void accept(Object obj) {
        ((ComponentName) this.f$0.put(((DreamInfo) obj).componentName.flattenToString(), ((DreamInfo) obj).componentName));
    }
}
