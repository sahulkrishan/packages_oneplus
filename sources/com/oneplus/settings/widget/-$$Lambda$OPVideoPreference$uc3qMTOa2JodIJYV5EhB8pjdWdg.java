package com.oneplus.settings.widget;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$OPVideoPreference$uc3qMTOa2JodIJYV5EhB8pjdWdg implements OnPreparedListener {
    public static final /* synthetic */ -$$Lambda$OPVideoPreference$uc3qMTOa2JodIJYV5EhB8pjdWdg INSTANCE = new -$$Lambda$OPVideoPreference$uc3qMTOa2JodIJYV5EhB8pjdWdg();

    private /* synthetic */ -$$Lambda$OPVideoPreference$uc3qMTOa2JodIJYV5EhB8pjdWdg() {
    }

    public final void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.setLooping(true);
    }
}
