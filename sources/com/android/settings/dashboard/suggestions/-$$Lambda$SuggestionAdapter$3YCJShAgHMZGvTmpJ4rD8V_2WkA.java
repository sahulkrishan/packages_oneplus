package com.android.settings.dashboard.suggestions;

import android.service.settings.suggestions.Suggestion;
import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SuggestionAdapter$3YCJShAgHMZGvTmpJ4rD8V_2WkA implements OnClickListener {
    private final /* synthetic */ SuggestionAdapter f$0;
    private final /* synthetic */ Suggestion f$1;

    public /* synthetic */ -$$Lambda$SuggestionAdapter$3YCJShAgHMZGvTmpJ4rD8V_2WkA(SuggestionAdapter suggestionAdapter, Suggestion suggestion) {
        this.f$0 = suggestionAdapter;
        this.f$1 = suggestion;
    }

    public final void onClick(View view) {
        SuggestionAdapter.lambda$onBindViewHolder$0(this.f$0, this.f$1, view);
    }
}
