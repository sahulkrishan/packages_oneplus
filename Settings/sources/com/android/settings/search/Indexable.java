package com.android.settings.search;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.List;

public interface Indexable {

    public interface SearchIndexProvider {
        List<String> getNonIndexableKeys(Context context);

        List<AbstractPreferenceController> getPreferenceControllers(Context context);

        List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean z);

        List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean z);
    }
}
