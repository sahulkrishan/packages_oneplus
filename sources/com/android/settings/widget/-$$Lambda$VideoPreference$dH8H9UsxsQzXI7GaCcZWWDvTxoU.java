package com.android.settings.widget;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnSeekCompleteListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$VideoPreference$dH8H9UsxsQzXI7GaCcZWWDvTxoU implements OnSeekCompleteListener {
    private final /* synthetic */ VideoPreference f$0;

    public /* synthetic */ -$$Lambda$VideoPreference$dH8H9UsxsQzXI7GaCcZWWDvTxoU(VideoPreference videoPreference) {
        this.f$0 = videoPreference;
    }

    public final void onSeekComplete(MediaPlayer mediaPlayer) {
        this.f$0.mVideoReady = true;
    }
}
