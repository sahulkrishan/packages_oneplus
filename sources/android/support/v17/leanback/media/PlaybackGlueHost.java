package android.support.v17.leanback.media;

import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.PlaybackRowPresenter;
import android.support.v17.leanback.widget.Row;
import android.view.View.OnKeyListener;

public abstract class PlaybackGlueHost {
    PlaybackGlue mGlue;

    public static abstract class HostCallback {
        public void onHostStart() {
        }

        public void onHostStop() {
        }

        public void onHostPause() {
        }

        public void onHostResume() {
        }

        public void onHostDestroy() {
        }
    }

    public static class PlayerCallback {
        public void onVideoSizeChanged(int videoWidth, int videoHeight) {
        }

        public void onBufferingStateChanged(boolean start) {
        }

        public void onError(int errorCode, CharSequence errorMessage) {
        }
    }

    @Deprecated
    public void setFadingEnabled(boolean enable) {
    }

    public void setControlsOverlayAutoHideEnabled(boolean enabled) {
        setFadingEnabled(enabled);
    }

    public boolean isControlsOverlayAutoHideEnabled() {
        return false;
    }

    @Deprecated
    public void fadeOut() {
    }

    public boolean isControlsOverlayVisible() {
        return true;
    }

    public void hideControlsOverlay(boolean runAnimation) {
    }

    public void showControlsOverlay(boolean runAnimation) {
    }

    public void setOnKeyInterceptListener(OnKeyListener onKeyListener) {
    }

    public void setOnActionClickedListener(OnActionClickedListener listener) {
    }

    public void setHostCallback(HostCallback callback) {
    }

    public void notifyPlaybackRowChanged() {
    }

    public void setPlaybackRowPresenter(PlaybackRowPresenter presenter) {
    }

    public void setPlaybackRow(Row row) {
    }

    /* Access modifiers changed, original: final */
    public final void attachToGlue(PlaybackGlue glue) {
        if (this.mGlue != null) {
            this.mGlue.onDetachedFromHost();
        }
        this.mGlue = glue;
        if (this.mGlue != null) {
            this.mGlue.onAttachedToHost(this);
        }
    }

    public PlayerCallback getPlayerCallback() {
        return null;
    }
}
