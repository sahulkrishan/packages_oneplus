package com.android.settingslib.suggestions;

import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.settingslib.drawer.Tile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SuggestionList {
    private List<Tile> mSuggestionList;
    private final Map<SuggestionCategory, List<Tile>> mSuggestions = new ArrayMap();

    public void addSuggestions(SuggestionCategory category, List<Tile> suggestions) {
        this.mSuggestions.put(category, suggestions);
    }

    public List<Tile> getSuggestions() {
        if (this.mSuggestionList != null) {
            return this.mSuggestionList;
        }
        this.mSuggestionList = new ArrayList();
        for (List<Tile> suggestions : this.mSuggestions.values()) {
            this.mSuggestionList.addAll(suggestions);
        }
        dedupeSuggestions(this.mSuggestionList);
        return this.mSuggestionList;
    }

    public boolean isExclusiveSuggestionCategory() {
        if (this.mSuggestions.size() != 1) {
            return false;
        }
        for (SuggestionCategory category : this.mSuggestions.keySet()) {
            if (category.exclusive) {
                return true;
            }
        }
        return false;
    }

    private void dedupeSuggestions(List<Tile> suggestions) {
        Set<String> intents = new ArraySet();
        for (int i = suggestions.size() - 1; i >= 0; i--) {
            String intentUri = ((Tile) suggestions.get(i)).intent.toUri(1);
            if (intents.contains(intentUri)) {
                suggestions.remove(i);
            } else {
                intents.add(intentUri);
            }
        }
    }
}
