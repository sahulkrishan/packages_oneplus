package com.android.settings.dashboard.suggestions;

import android.service.settings.suggestions.Suggestion;
import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SuggestionAdapter$o_nlX1JhE-RQCl3p5ch8A_R_uN0 implements OnClickListener {
    private final /* synthetic */ SuggestionAdapter f$0;
    private final /* synthetic */ String f$1;
    private final /* synthetic */ Suggestion f$2;

    public /* synthetic */ -$$Lambda$SuggestionAdapter$o_nlX1JhE-RQCl3p5ch8A_R_uN0(SuggestionAdapter suggestionAdapter, String str, Suggestion suggestion) {
        this.f$0 = suggestionAdapter;
        this.f$1 = str;
        this.f$2 = suggestion;
    }

    public final void onClick(View view) {
        SuggestionAdapter.lambda$onBindViewHolder$1(this.f$0, this.f$1, this.f$2, view);
    }
}
