package android.support.v17.leanback.app;

import android.support.v17.leanback.media.SurfaceHolderGlueHost;
import android.view.SurfaceHolder.Callback;

public class VideoSupportFragmentGlueHost extends PlaybackSupportFragmentGlueHost implements SurfaceHolderGlueHost {
    private final VideoSupportFragment mFragment;

    public VideoSupportFragmentGlueHost(VideoSupportFragment fragment) {
        super(fragment);
        this.mFragment = fragment;
    }

    public void setSurfaceHolderCallback(Callback callback) {
        this.mFragment.setSurfaceHolderCallback(callback);
    }
}
