package com.android.settings.dashboard.suggestions;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.service.settings.suggestions.Suggestion;
import android.support.annotation.NonNull;
import android.util.Pair;
import com.android.settingslib.drawer.Tile;
import com.android.settingslib.suggestions.SuggestionControllerMixin;
import java.util.List;

public interface SuggestionFeatureProvider {
    void dismissSuggestion(Context context, SuggestionControllerMixin suggestionControllerMixin, Suggestion suggestion);

    void filterExclusiveSuggestions(List<Tile> list);

    Pair<Integer, Object>[] getLoggingTaggedData(Context context);

    SharedPreferences getSharedPrefs(Context context);

    ComponentName getSuggestionServiceComponent();

    boolean isSmartSuggestionEnabled(Context context);

    boolean isSuggestionComplete(Context context, @NonNull ComponentName componentName);

    boolean isSuggestionEnabled(Context context);
}
