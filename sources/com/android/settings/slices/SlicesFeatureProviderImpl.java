package com.android.settings.slices;

import android.content.Context;
import android.util.Log;
import com.android.settings.wifi.calling.WifiCallingSliceHelper;
import com.android.settingslib.utils.ThreadUtils;

public class SlicesFeatureProviderImpl implements SlicesFeatureProvider {
    private SliceDataConverter mSliceDataConverter;
    private SlicesIndexer mSlicesIndexer;

    public SlicesIndexer getSliceIndexer(Context context) {
        if (this.mSlicesIndexer == null) {
            this.mSlicesIndexer = new SlicesIndexer(context);
        }
        return this.mSlicesIndexer;
    }

    public SliceDataConverter getSliceDataConverter(Context context) {
        if (this.mSliceDataConverter == null) {
            this.mSliceDataConverter = new SliceDataConverter(context.getApplicationContext());
        }
        return this.mSliceDataConverter;
    }

    public void indexSliceDataAsync(Context context) {
        Log.d("SlicesFeatureProvider", "postOnBackgroundThread SlicesIndexer start");
        ThreadUtils.postOnBackgroundThread(getSliceIndexer(context));
    }

    public void indexSliceData(Context context) {
        getSliceIndexer(context).indexSliceData();
    }

    public WifiCallingSliceHelper getNewWifiCallingSliceHelper(Context context) {
        return new WifiCallingSliceHelper(context);
    }
}
