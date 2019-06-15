package com.android.settings.search;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toolbar;
import com.android.settings.overlay.FeatureFactory;

public interface SearchFeatureProvider {
    public static final Intent SEARCH_UI_INTENT = new Intent("com.android.settings.action.SETTINGS_SEARCH");

    DatabaseIndexingManager getIndexingManager(Context context);

    SearchIndexableResources getSearchIndexableResources();

    void updateIndex(Context context);

    void verifyLaunchSearchResultPageCaller(Context context, ComponentName componentName) throws SecurityException, IllegalArgumentException;

    String getSettingsIntelligencePkgName() {
        return "com.android.settings.intelligence";
    }

    void initSearchToolbar(Activity activity, Toolbar toolbar) {
        if (activity != null && toolbar != null) {
            toolbar.setOnClickListener(new -$$Lambda$SearchFeatureProvider$7ZGLG3tZpGqHgt7m_jMbwikwfJM(this, activity));
        }
    }

    static /* synthetic */ void lambda$initSearchToolbar$0(SearchFeatureProvider searchFeatureProvider, Activity activity, View tb) {
        Intent intent = SEARCH_UI_INTENT;
        intent.setPackage(searchFeatureProvider.getSettingsIntelligencePkgName());
        FeatureFactory.getFactory(activity.getApplicationContext()).getSlicesFeatureProvider().indexSliceDataAsync(activity.getApplicationContext());
        activity.startActivityForResult(intent, 0);
    }

    void initSearchLayout(Activity activity, View view) {
        if (activity != null && view != null) {
            view.setOnClickListener(new -$$Lambda$SearchFeatureProvider$LUYpWGGDky_gomwAj2mlHv6ihaM(this, activity));
        }
    }

    static /* synthetic */ void lambda$initSearchLayout$1(SearchFeatureProvider searchFeatureProvider, Activity activity, View tb) {
        Intent intent = SEARCH_UI_INTENT;
        intent.setPackage(searchFeatureProvider.getSettingsIntelligencePkgName());
        FeatureFactory.getFactory(activity.getApplicationContext()).getSlicesFeatureProvider().indexSliceDataAsync(activity.getApplicationContext());
        activity.startActivityForResult(intent, 0);
    }
}
