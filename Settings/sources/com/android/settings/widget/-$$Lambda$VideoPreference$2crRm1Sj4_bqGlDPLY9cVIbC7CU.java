package com.android.settings.widget;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$VideoPreference$2crRm1Sj4_bqGlDPLY9cVIbC7CU implements OnPreparedListener {
    public static final /* synthetic */ -$$Lambda$VideoPreference$2crRm1Sj4_bqGlDPLY9cVIbC7CU INSTANCE = new -$$Lambda$VideoPreference$2crRm1Sj4_bqGlDPLY9cVIbC7CU();

    private /* synthetic */ -$$Lambda$VideoPreference$2crRm1Sj4_bqGlDPLY9cVIbC7CU() {
    }

    public final void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.setLooping(true);
    }
}
