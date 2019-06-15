package com.android.settings.search;

import android.content.Context;
import android.net.Uri;
import java.util.List;

public class DeviceIndexFeatureProviderImpl implements DeviceIndexFeatureProvider {
    public boolean isIndexingEnabled() {
        return false;
    }

    public void index(Context context, CharSequence title, Uri sliceUri, Uri launchUri, List<String> list) {
    }

    public void clearIndex(Context context) {
    }
}
