package com.android.settings.search;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.SearchIndexableResource;
import android.provider.SearchIndexablesContract;
import android.provider.SearchIndexablesProvider;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.settings.SettingsActivity;
import com.android.settings.dashboard.DashboardFragmentRegistry;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.Tile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SettingsSearchIndexablesProvider extends SearchIndexablesProvider {
    public static final boolean DEBUG = false;
    private static final Collection<String> INVALID_KEYS = new ArraySet();
    public static final String SYSPROP_CRASH_ON_ERROR = "debug.com.android.settings.search.crash_on_error";
    private static final String TAG = "SettingsSearchProvider";

    static {
        INVALID_KEYS.add(null);
        INVALID_KEYS.add("");
    }

    public boolean onCreate() {
        return true;
    }

    public Cursor queryXmlResources(String[] projection) {
        MatrixCursor cursor = new MatrixCursor(SearchIndexablesContract.INDEXABLES_XML_RES_COLUMNS);
        for (SearchIndexableResource val : getSearchIndexableResourcesFromProvider(getContext())) {
            Object[] ref = new Object[SearchIndexablesContract.INDEXABLES_XML_RES_COLUMNS.length];
            ref[0] = Integer.valueOf(val.rank);
            ref[1] = Integer.valueOf(val.xmlResId);
            ref[2] = val.className;
            ref[3] = Integer.valueOf(val.iconResId);
            ref[4] = val.intentAction;
            ref[5] = val.intentTargetPackage;
            ref[6] = null;
            cursor.addRow(ref);
        }
        return cursor;
    }

    public Cursor queryRawData(String[] projection) {
        MatrixCursor cursor = new MatrixCursor(SearchIndexablesContract.INDEXABLES_RAW_COLUMNS);
        for (SearchIndexableRaw val : getSearchIndexableRawFromProvider(getContext())) {
            Object[] ref = new Object[SearchIndexablesContract.INDEXABLES_RAW_COLUMNS.length];
            ref[1] = val.title;
            ref[2] = val.summaryOn;
            ref[3] = val.summaryOff;
            ref[4] = val.entries;
            ref[5] = val.keywords;
            ref[6] = val.screenTitle;
            ref[7] = val.className;
            ref[8] = Integer.valueOf(val.iconResId);
            ref[9] = val.intentAction;
            ref[10] = val.intentTargetPackage;
            ref[11] = val.intentTargetClass;
            ref[12] = val.key;
            ref[13] = Integer.valueOf(val.userId);
            cursor.addRow(ref);
        }
        return cursor;
    }

    public Cursor queryNonIndexableKeys(String[] projection) {
        MatrixCursor cursor = new MatrixCursor(SearchIndexablesContract.NON_INDEXABLES_KEYS_COLUMNS);
        for (String nik : getNonIndexableKeysFromProvider(getContext())) {
            Object[] ref = new Object[SearchIndexablesContract.NON_INDEXABLES_KEYS_COLUMNS.length];
            ref[0] = nik;
            cursor.addRow(ref);
        }
        return cursor;
    }

    public Cursor querySiteMapPairs() {
        MatrixCursor cursor = new MatrixCursor(SearchIndexablesContract.SITE_MAP_COLUMNS);
        Context context = getContext();
        for (DashboardCategory category : FeatureFactory.getFactory(context).getDashboardFeatureProvider(context).getAllCategories()) {
            String parentClass = (String) DashboardFragmentRegistry.CATEGORY_KEY_TO_PARENT_MAP.get(category.key);
            if (parentClass != null) {
                for (Tile tile : category.getTiles()) {
                    String childClass = null;
                    if (tile.metaData != null) {
                        childClass = tile.metaData.getString(SettingsActivity.META_DATA_KEY_FRAGMENT_CLASS);
                    }
                    if (childClass != null) {
                        cursor.newRow().add("parent_class", parentClass).add("child_class", childClass);
                    }
                }
            }
        }
        return cursor;
    }

    private List<String> getNonIndexableKeysFromProvider(Context context) {
        Collection<Class> values = FeatureFactory.getFactory(context).getSearchFeatureProvider().getSearchIndexableResources().getProviderValues();
        List<String> nonIndexableKeys = new ArrayList();
        for (Class<?> clazz : values) {
            long startTime = System.currentTimeMillis();
            SearchIndexProvider provider = DatabaseIndexingUtils.getSearchIndexProvider(clazz);
            String str;
            StringBuilder stringBuilder;
            try {
                List<String> providerNonIndexableKeys = provider.getNonIndexableKeys(context);
                if (providerNonIndexableKeys != null) {
                    if (!providerNonIndexableKeys.isEmpty()) {
                        if (providerNonIndexableKeys.removeAll(INVALID_KEYS)) {
                            str = TAG;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append(provider);
                            stringBuilder.append(" tried to add an empty non-indexable key");
                            Log.v(str, stringBuilder.toString());
                        }
                        nonIndexableKeys.addAll(providerNonIndexableKeys);
                    }
                }
            } catch (Exception e) {
                if (System.getProperty(SYSPROP_CRASH_ON_ERROR) == null) {
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Error trying to get non-indexable keys from: ");
                    stringBuilder.append(clazz.getName());
                    Log.e(str, stringBuilder.toString(), e);
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
        return nonIndexableKeys;
    }

    private List<SearchIndexableResource> getSearchIndexableResourcesFromProvider(Context context) {
        Collection<Class> values = FeatureFactory.getFactory(context).getSearchFeatureProvider().getSearchIndexableResources().getProviderValues();
        List<SearchIndexableResource> resourceList = new ArrayList();
        for (Class<?> clazz : values) {
            SearchIndexProvider provider = DatabaseIndexingUtils.getSearchIndexProvider(clazz);
            if (provider != null) {
                List<SearchIndexableResource> resList = provider.getXmlResourcesToIndex(context, true);
                if (resList != null) {
                    for (SearchIndexableResource item : resList) {
                        String name;
                        if (TextUtils.isEmpty(item.className)) {
                            name = clazz.getName();
                        } else {
                            name = item.className;
                        }
                        item.className = name;
                    }
                    resourceList.addAll(resList);
                }
            }
        }
        return resourceList;
    }

    private List<SearchIndexableRaw> getSearchIndexableRawFromProvider(Context context) {
        Collection<Class> values = FeatureFactory.getFactory(context).getSearchFeatureProvider().getSearchIndexableResources().getProviderValues();
        List<SearchIndexableRaw> rawList = new ArrayList();
        for (Class<?> clazz : values) {
            List<SearchIndexableRaw> providerRaws = DatabaseIndexingUtils.getSearchIndexProvider(clazz).getRawDataToIndex(context, true);
            if (providerRaws != null) {
                for (SearchIndexableRaw raw : providerRaws) {
                    raw.className = clazz.getName();
                }
                rawList.addAll(providerRaws);
            }
        }
        return rawList;
    }
}
