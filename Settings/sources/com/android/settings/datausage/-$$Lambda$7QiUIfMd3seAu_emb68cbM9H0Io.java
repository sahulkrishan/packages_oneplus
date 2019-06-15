package com.android.settings.datausage;

import android.app.Activity;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProvider;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$7QiUIfMd3seAu_emb68cbM9H0Io implements SummaryProviderFactory {
    public static final /* synthetic */ -$$Lambda$7QiUIfMd3seAu_emb68cbM9H0Io INSTANCE = new -$$Lambda$7QiUIfMd3seAu_emb68cbM9H0Io();

    private /* synthetic */ -$$Lambda$7QiUIfMd3seAu_emb68cbM9H0Io() {
    }

    public final SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
        return new SummaryProvider(activity, summaryLoader);
    }
}
