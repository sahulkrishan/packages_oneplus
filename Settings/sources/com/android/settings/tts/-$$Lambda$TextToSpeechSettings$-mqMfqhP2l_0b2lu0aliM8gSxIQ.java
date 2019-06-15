package com.android.settings.tts;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$TextToSpeechSettings$-mqMfqhP2l_0b2lu0aliM8gSxIQ implements OnClickListener {
    private final /* synthetic */ TextToSpeechSettings f$0;

    public /* synthetic */ -$$Lambda$TextToSpeechSettings$-mqMfqhP2l_0b2lu0aliM8gSxIQ(TextToSpeechSettings textToSpeechSettings) {
        this.f$0 = textToSpeechSettings;
    }

    public final void onClick(View view) {
        this.f$0.speakSampleText();
    }
}
