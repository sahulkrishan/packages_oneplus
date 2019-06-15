package com.android.settings.search.indexing;

import android.provider.SearchIndexableData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PreIndexData {
    public List<SearchIndexableData> dataToUpdate;
    public Map<String, Set<String>> nonIndexableKeys;

    public PreIndexData() {
        this.dataToUpdate = new ArrayList();
        this.nonIndexableKeys = new HashMap();
    }

    public PreIndexData(PreIndexData other) {
        this.dataToUpdate = new ArrayList(other.dataToUpdate);
        this.nonIndexableKeys = new HashMap(other.nonIndexableKeys);
    }

    public PreIndexData copy() {
        return new PreIndexData(this);
    }

    public void clear() {
        this.dataToUpdate.clear();
        this.nonIndexableKeys.clear();
    }
}
