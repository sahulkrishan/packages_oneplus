package com.oneplus.settings.widget;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnSeekCompleteListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$OPVideoPreference$ynCil1Vg3ClpXktrurvZlqx29d4 implements OnSeekCompleteListener {
    private final /* synthetic */ OPVideoPreference f$0;

    public /* synthetic */ -$$Lambda$OPVideoPreference$ynCil1Vg3ClpXktrurvZlqx29d4(OPVideoPreference oPVideoPreference) {
        this.f$0 = oPVideoPreference;
    }

    public final void onSeekComplete(MediaPlayer mediaPlayer) {
        this.f$0.mVideoReady = true;
    }
}
