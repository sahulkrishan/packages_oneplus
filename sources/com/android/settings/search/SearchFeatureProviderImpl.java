package com.android.settings.search;

import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.indexing.IndexData;
import java.util.Locale;

public class SearchFeatureProviderImpl implements SearchFeatureProvider {
    private static final String METRICS_ACTION_SETTINGS_INDEX = "search_synchronous_indexing";
    private static final String TAG = "SearchFeatureProvider";
    private DatabaseIndexingManager mDatabaseIndexingManager;
    private SearchIndexableResources mSearchIndexableResources;

    public void verifyLaunchSearchResultPageCaller(Context context, ComponentName caller) {
        if (caller != null) {
            String packageName = caller.getPackageName();
            boolean isSettingsPackage = TextUtils.equals(packageName, context.getPackageName()) || TextUtils.equals(getSettingsIntelligencePkgName(), packageName);
            boolean isWhitelistedPackage = isSignatureWhitelisted(context, caller.getPackageName());
            if (!isSettingsPackage && !isWhitelistedPackage) {
                throw new SecurityException("Search result intents must be called with from a whitelisted package.");
            }
            return;
        }
        throw new IllegalArgumentException("ExternalSettingsTrampoline intents must be called with startActivityForResult");
    }

    public DatabaseIndexingManager getIndexingManager(Context context) {
        if (this.mDatabaseIndexingManager == null) {
            this.mDatabaseIndexingManager = new DatabaseIndexingManager(context.getApplicationContext());
        }
        return this.mDatabaseIndexingManager;
    }

    public void updateIndex(Context context) {
        long indexStartTime = System.currentTimeMillis();
        getIndexingManager(context).performIndexing();
        FeatureFactory.getFactory(context).getMetricsFeatureProvider().histogram(context, METRICS_ACTION_SETTINGS_INDEX, (int) (System.currentTimeMillis() - indexStartTime));
    }

    public SearchIndexableResources getSearchIndexableResources() {
        if (this.mSearchIndexableResources == null) {
            this.mSearchIndexableResources = new SearchIndexableResourcesImpl();
        }
        return this.mSearchIndexableResources;
    }

    /* Access modifiers changed, original: protected */
    public boolean isSignatureWhitelisted(Context context, String callerPackage) {
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String cleanQuery(String query) {
        if (TextUtils.isEmpty(query)) {
            return null;
        }
        if (Locale.getDefault().equals(Locale.JAPAN)) {
            query = IndexData.normalizeJapaneseString(query);
        }
        return query.trim();
    }
}
