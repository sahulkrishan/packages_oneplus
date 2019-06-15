package com.android.settings.datetime.timezone.model;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import com.android.settingslib.utils.AsyncLoader;

public class TimeZoneDataLoader extends AsyncLoader<TimeZoneData> {

    public static class LoaderCreator implements LoaderCallbacks<TimeZoneData> {
        private final OnDataReadyCallback mCallback;
        private final Context mContext;

        public LoaderCreator(Context context, OnDataReadyCallback callback) {
            this.mContext = context;
            this.mCallback = callback;
        }

        public Loader onCreateLoader(int id, Bundle args) {
            return new TimeZoneDataLoader(this.mContext);
        }

        public void onLoadFinished(Loader<TimeZoneData> loader, TimeZoneData data) {
            if (this.mCallback != null) {
                this.mCallback.onTimeZoneDataReady(data);
            }
        }

        public void onLoaderReset(Loader<TimeZoneData> loader) {
        }
    }

    public interface OnDataReadyCallback {
        void onTimeZoneDataReady(TimeZoneData timeZoneData);
    }

    public TimeZoneDataLoader(Context context) {
        super(context);
    }

    public TimeZoneData loadInBackground() {
        return TimeZoneData.getInstance();
    }

    /* Access modifiers changed, original: protected */
    public void onDiscardResult(TimeZoneData result) {
    }
}
