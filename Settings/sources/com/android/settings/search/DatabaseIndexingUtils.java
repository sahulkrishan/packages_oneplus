package com.android.settings.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import com.android.settings.SettingsActivity;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.List;
import java.util.Map;

public class DatabaseIndexingUtils {
    public static final String FIELD_NAME_SEARCH_INDEX_DATA_PROVIDER = "SEARCH_INDEX_DATA_PROVIDER";
    private static final String TAG = "IndexingUtil";

    public static Intent buildSearchResultPageIntent(Context context, String className, String key, String screenTitle) {
        return buildSearchResultPageIntent(context, className, key, screenTitle, 34);
    }

    public static Intent buildSearchResultPageIntent(Context context, String className, String key, String screenTitle, int sourceMetricsCategory) {
        Bundle args = new Bundle();
        args.putString(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY, key);
        Intent searchDestination = new SubSettingLauncher(context).setDestination(className).setArguments(args).setTitle((CharSequence) screenTitle).setSourceMetricsCategory(sourceMetricsCategory).toIntent();
        searchDestination.putExtra(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY, key).setAction("com.android.settings.SEARCH_RESULT_TRAMPOLINE").setComponent(null);
        return searchDestination;
    }

    public static Map<String, ResultPayload> getPayloadKeyMap(String className, Context context) {
        ArrayMap<String, ResultPayload> map = new ArrayMap();
        if (context == null) {
            return map;
        }
        Class<?> clazz = getIndexableClass(className);
        if (clazz == null) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SearchIndexableResource '");
            stringBuilder.append(className);
            stringBuilder.append("' should implement the ");
            stringBuilder.append(Indexable.class.getName());
            stringBuilder.append(" interface!");
            Log.d(str, stringBuilder.toString());
            return map;
        }
        List<AbstractPreferenceController> controllers = getSearchIndexProvider(clazz).getPreferenceControllers(context);
        if (controllers == null) {
            return map;
        }
        for (AbstractPreferenceController controller : controllers) {
            ResultPayload payload;
            if (controller instanceof PreferenceControllerMixin) {
                payload = ((PreferenceControllerMixin) controller).getResultPayload();
            } else if (controller instanceof BasePreferenceController) {
                payload = ((BasePreferenceController) controller).getResultPayload();
            } else {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(controller.getClass().getName());
                stringBuilder2.append(" must implement ");
                stringBuilder2.append(PreferenceControllerMixin.class.getName());
                throw new IllegalStateException(stringBuilder2.toString());
            }
            if (payload != null) {
                map.put(controller.getPreferenceKey(), payload);
            }
        }
        return map;
    }

    public static Class<?> getIndexableClass(String className) {
        Class<?> cls = null;
        try {
            Class<?> clazz = Class.forName(className);
            if (isIndexableClass(clazz)) {
                cls = clazz;
            }
            return cls;
        } catch (ClassNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot find class: ");
            stringBuilder.append(className);
            Log.d(str, stringBuilder.toString());
            return null;
        }
    }

    public static boolean isIndexableClass(Class<?> clazz) {
        return clazz != null && Indexable.class.isAssignableFrom(clazz);
    }

    public static SearchIndexProvider getSearchIndexProvider(Class<?> clazz) {
        try {
            return (SearchIndexProvider) clazz.getField(FIELD_NAME_SEARCH_INDEX_DATA_PROVIDER).get(null);
        } catch (NoSuchFieldException e) {
            Log.d(TAG, "Cannot find field 'SEARCH_INDEX_DATA_PROVIDER'");
            return null;
        } catch (SecurityException e2) {
            Log.d(TAG, "Security exception for field 'SEARCH_INDEX_DATA_PROVIDER'");
            return null;
        } catch (IllegalAccessException e3) {
            Log.d(TAG, "Illegal access to field 'SEARCH_INDEX_DATA_PROVIDER'");
            return null;
        } catch (IllegalArgumentException e4) {
            Log.d(TAG, "Illegal argument when accessing field 'SEARCH_INDEX_DATA_PROVIDER'");
            return null;
        }
    }
}
