package com.oneplus.settings.better;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnSeekCompleteListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$OPFnaticWallPapers$addNcANW3El0e9XKqAj5iPLWAoU implements OnSeekCompleteListener {
    private final /* synthetic */ OPFnaticWallPapers f$0;

    public /* synthetic */ -$$Lambda$OPFnaticWallPapers$addNcANW3El0e9XKqAj5iPLWAoU(OPFnaticWallPapers oPFnaticWallPapers) {
        this.f$0 = oPFnaticWallPapers;
    }

    public final void onSeekComplete(MediaPlayer mediaPlayer) {
        this.f$0.mVideoReady = true;
    }
}
