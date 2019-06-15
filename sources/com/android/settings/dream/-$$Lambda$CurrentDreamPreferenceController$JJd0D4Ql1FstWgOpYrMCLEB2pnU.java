package com.android.settings.dream;

import com.android.settingslib.dream.DreamBackend.DreamInfo;
import java.util.function.Predicate;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$CurrentDreamPreferenceController$JJd0D4Ql1FstWgOpYrMCLEB2pnU implements Predicate {
    public static final /* synthetic */ -$$Lambda$CurrentDreamPreferenceController$JJd0D4Ql1FstWgOpYrMCLEB2pnU INSTANCE = new -$$Lambda$CurrentDreamPreferenceController$JJd0D4Ql1FstWgOpYrMCLEB2pnU();

    private /* synthetic */ -$$Lambda$CurrentDreamPreferenceController$JJd0D4Ql1FstWgOpYrMCLEB2pnU() {
    }

    public final boolean test(Object obj) {
        return ((DreamInfo) obj).isActive;
    }
}
