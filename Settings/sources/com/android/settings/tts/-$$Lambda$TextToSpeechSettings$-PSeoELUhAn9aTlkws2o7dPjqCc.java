package com.android.settings.tts;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$TextToSpeechSettings$-PSeoELUhAn9aTlkws2o7dPjqCc implements OnClickListener {
    private final /* synthetic */ TextToSpeechSettings f$0;

    public /* synthetic */ -$$Lambda$TextToSpeechSettings$-PSeoELUhAn9aTlkws2o7dPjqCc(TextToSpeechSettings textToSpeechSettings) {
        this.f$0 = textToSpeechSettings;
    }

    public final void onClick(View view) {
        this.f$0.resetTts();
    }
}
