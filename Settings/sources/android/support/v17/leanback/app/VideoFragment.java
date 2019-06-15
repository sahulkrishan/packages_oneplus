package android.support.v17.leanback.app;

import android.os.Bundle;
import android.support.v17.leanback.R;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

@Deprecated
public class VideoFragment extends PlaybackFragment {
    static final int SURFACE_CREATED = 1;
    static final int SURFACE_NOT_CREATED = 0;
    Callback mMediaPlaybackCallback;
    int mState = 0;
    SurfaceView mVideoSurface;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        this.mVideoSurface = (SurfaceView) LayoutInflater.from(FragmentUtil.getContext(this)).inflate(R.layout.lb_video_surface, root, false);
        root.addView(this.mVideoSurface, 0);
        this.mVideoSurface.getHolder().addCallback(new Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                if (VideoFragment.this.mMediaPlaybackCallback != null) {
                    VideoFragment.this.mMediaPlaybackCallback.surfaceCreated(holder);
                }
                VideoFragment.this.mState = 1;
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (VideoFragment.this.mMediaPlaybackCallback != null) {
                    VideoFragment.this.mMediaPlaybackCallback.surfaceChanged(holder, format, width, height);
                }
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                if (VideoFragment.this.mMediaPlaybackCallback != null) {
                    VideoFragment.this.mMediaPlaybackCallback.surfaceDestroyed(holder);
                }
                VideoFragment.this.mState = 0;
            }
        });
        setBackgroundType(2);
        return root;
    }

    public void setSurfaceHolderCallback(Callback callback) {
        this.mMediaPlaybackCallback = callback;
        if (callback != null && this.mState == 1) {
            this.mMediaPlaybackCallback.surfaceCreated(this.mVideoSurface.getHolder());
        }
    }

    /* Access modifiers changed, original: protected */
    public void onVideoSizeChanged(int width, int height) {
        int screenWidth = getView().getWidth();
        int screenHeight = getView().getHeight();
        LayoutParams p = this.mVideoSurface.getLayoutParams();
        if (screenWidth * height > width * screenHeight) {
            p.height = screenHeight;
            p.width = (screenHeight * width) / height;
        } else {
            p.width = screenWidth;
            p.height = (screenWidth * height) / width;
        }
        this.mVideoSurface.setLayoutParams(p);
    }

    public SurfaceView getSurfaceView() {
        return this.mVideoSurface;
    }

    public void onDestroyView() {
        this.mVideoSurface = null;
        this.mState = 0;
        super.onDestroyView();
    }
}
