package com.android.settingslib.suggestions;

import android.content.Context;
import android.service.settings.suggestions.Suggestion;
import android.util.Log;
import com.android.settingslib.utils.AsyncLoader;
import java.util.List;

public class SuggestionLoader extends AsyncLoader<List<Suggestion>> {
    public static final int LOADER_ID_SUGGESTIONS = 42;
    private static final String TAG = "SuggestionLoader";
    private final SuggestionController mSuggestionController;

    public SuggestionLoader(Context context, SuggestionController controller) {
        super(context);
        this.mSuggestionController = controller;
    }

    /* Access modifiers changed, original: protected */
    public void onDiscardResult(List<Suggestion> list) {
    }

    public List<Suggestion> loadInBackground() {
        List<Suggestion> data = this.mSuggestionController.getSuggestions();
        if (data == null) {
            Log.d(TAG, "data is null");
        } else {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("data size ");
            stringBuilder.append(data.size());
            Log.d(str, stringBuilder.toString());
        }
        return data;
    }
}
