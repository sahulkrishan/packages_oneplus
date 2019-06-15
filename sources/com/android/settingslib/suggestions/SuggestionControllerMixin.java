package com.android.settingslib.suggestions;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.service.settings.suggestions.Suggestion;
import android.support.annotation.Nullable;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.suggestions.SuggestionController.ServiceConnectionListener;
import java.util.List;

public class SuggestionControllerMixin implements ServiceConnectionListener, LifecycleObserver, LoaderCallbacks<List<Suggestion>> {
    private static final boolean DEBUG = false;
    private static final String TAG = "SuggestionCtrlMixin";
    private final Context mContext;
    private final SuggestionControllerHost mHost;
    private final SuggestionController mSuggestionController;
    private boolean mSuggestionLoaded;

    public interface SuggestionControllerHost {
        @Nullable
        LoaderManager getLoaderManager();

        void onSuggestionReady(List<Suggestion> list);
    }

    public SuggestionControllerMixin(Context context, SuggestionControllerHost host, Lifecycle lifecycle, ComponentName componentName) {
        this.mContext = context.getApplicationContext();
        this.mHost = host;
        this.mSuggestionController = new SuggestionController(this.mContext, componentName, this);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @OnLifecycleEvent(Event.ON_START)
    public void onStart() {
        this.mSuggestionController.start();
    }

    @OnLifecycleEvent(Event.ON_STOP)
    public void onStop() {
        this.mSuggestionController.stop();
    }

    public void onServiceConnected() {
        LoaderManager loaderManager = this.mHost.getLoaderManager();
        if (loaderManager != null) {
            loaderManager.restartLoader(42, null, this);
        }
    }

    public void onServiceDisconnected() {
        LoaderManager loaderManager = this.mHost.getLoaderManager();
        if (loaderManager != null) {
            loaderManager.destroyLoader(42);
        }
    }

    public Loader<List<Suggestion>> onCreateLoader(int id, Bundle args) {
        if (id == 42) {
            this.mSuggestionLoaded = false;
            return new SuggestionLoader(this.mContext, this.mSuggestionController);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("This loader id is not supported ");
        stringBuilder.append(id);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public void onLoadFinished(Loader<List<Suggestion>> loader, List<Suggestion> data) {
        this.mSuggestionLoaded = true;
        this.mHost.onSuggestionReady(data);
    }

    public void onLoaderReset(Loader<List<Suggestion>> loader) {
        this.mSuggestionLoaded = false;
    }

    public boolean isSuggestionLoaded() {
        return this.mSuggestionLoaded;
    }

    public void dismissSuggestion(Suggestion suggestion) {
        this.mSuggestionController.dismissSuggestions(suggestion);
    }

    public void launchSuggestion(Suggestion suggestion) {
        this.mSuggestionController.launchSuggestion(suggestion);
    }
}
