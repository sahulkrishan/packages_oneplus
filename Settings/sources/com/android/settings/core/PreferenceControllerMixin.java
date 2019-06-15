package com.android.settings.core;

import android.text.TextUtils;
import android.util.Log;
import com.android.settings.search.ResultPayload;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.List;

public interface PreferenceControllerMixin {
    public static final String TAG = "PrefControllerMixin";

    void updateNonIndexableKeys(List<String> keys) {
        if ((this instanceof AbstractPreferenceController) && !((AbstractPreferenceController) this).isAvailable()) {
            String key = ((AbstractPreferenceController) this).getPreferenceKey();
            if (TextUtils.isEmpty(key)) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Skipping updateNonIndexableKeys due to empty key ");
                stringBuilder.append(toString());
                Log.w(str, stringBuilder.toString());
                return;
            }
            keys.add(key);
        }
    }

    void updateRawDataToIndex(List<SearchIndexableRaw> list) {
    }

    @Deprecated
    ResultPayload getResultPayload() {
        return null;
    }
}
