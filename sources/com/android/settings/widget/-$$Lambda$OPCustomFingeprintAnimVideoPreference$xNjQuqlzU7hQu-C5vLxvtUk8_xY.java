package com.android.settings.widget;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnSeekCompleteListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$OPCustomFingeprintAnimVideoPreference$xNjQuqlzU7hQu-C5vLxvtUk8_xY implements OnSeekCompleteListener {
    private final /* synthetic */ OPCustomFingeprintAnimVideoPreference f$0;

    public /* synthetic */ -$$Lambda$OPCustomFingeprintAnimVideoPreference$xNjQuqlzU7hQu-C5vLxvtUk8_xY(OPCustomFingeprintAnimVideoPreference oPCustomFingeprintAnimVideoPreference) {
        this.f$0 = oPCustomFingeprintAnimVideoPreference;
    }

    public final void onSeekComplete(MediaPlayer mediaPlayer) {
        this.f$0.mVideoReady = true;
    }
}
