package com.android.settings.dashboard.suggestions;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.overlay.FeatureFactory;

public class SuggestionStateProvider extends ContentProvider {
    @VisibleForTesting
    static final String EXTRA_CANDIDATE_ID = "candidate_id";
    @VisibleForTesting
    static final String METHOD_GET_SUGGESTION_STATE = "getSuggestionState";
    private static final String RESULT_IS_COMPLETE = "candidate_is_complete";
    private static final String TAG = "SugstStatusProvider";

    public boolean onCreate() {
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException("query operation not supported currently.");
    }

    public String getType(Uri uri) {
        throw new UnsupportedOperationException("getType operation not supported currently.");
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("insert operation not supported currently.");
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("delete operation not supported currently.");
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("update operation not supported currently.");
    }

    public Bundle call(String method, String arg, Bundle extras) {
        Bundle bundle = new Bundle();
        if (METHOD_GET_SUGGESTION_STATE.equals(method)) {
            boolean isComplete;
            String id = extras.getString(EXTRA_CANDIDATE_ID);
            ComponentName cn = (ComponentName) extras.getParcelable("android.intent.extra.COMPONENT_NAME");
            if (cn == null) {
                isComplete = true;
            } else {
                isComplete = getContext();
                isComplete = FeatureFactory.getFactory(isComplete).getSuggestionFeatureProvider(isComplete).isSuggestionComplete(isComplete, cn);
            }
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Suggestion ");
            stringBuilder.append(id);
            stringBuilder.append(" complete: ");
            stringBuilder.append(isComplete);
            Log.d(str, stringBuilder.toString());
            bundle.putBoolean(RESULT_IS_COMPLETE, isComplete);
        }
        return bundle;
    }
}
