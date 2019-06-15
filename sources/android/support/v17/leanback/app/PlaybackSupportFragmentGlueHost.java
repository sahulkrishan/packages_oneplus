package android.support.v17.leanback.app;

import android.support.v17.leanback.media.PlaybackGlueHost;
import android.support.v17.leanback.media.PlaybackGlueHost.HostCallback;
import android.support.v17.leanback.media.PlaybackGlueHost.PlayerCallback;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.PlaybackRowPresenter;
import android.support.v17.leanback.widget.PlaybackSeekUi;
import android.support.v17.leanback.widget.PlaybackSeekUi.Client;
import android.support.v17.leanback.widget.Presenter.ViewHolder;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.view.View.OnKeyListener;

public class PlaybackSupportFragmentGlueHost extends PlaybackGlueHost implements PlaybackSeekUi {
    private final PlaybackSupportFragment mFragment;
    final PlayerCallback mPlayerCallback = new PlayerCallback() {
        public void onBufferingStateChanged(boolean start) {
            PlaybackSupportFragmentGlueHost.this.mFragment.onBufferingStateChanged(start);
        }

        public void onError(int errorCode, CharSequence errorMessage) {
            PlaybackSupportFragmentGlueHost.this.mFragment.onError(errorCode, errorMessage);
        }

        public void onVideoSizeChanged(int videoWidth, int videoHeight) {
            PlaybackSupportFragmentGlueHost.this.mFragment.onVideoSizeChanged(videoWidth, videoHeight);
        }
    };

    public PlaybackSupportFragmentGlueHost(PlaybackSupportFragment fragment) {
        this.mFragment = fragment;
    }

    public void setControlsOverlayAutoHideEnabled(boolean enabled) {
        this.mFragment.setControlsOverlayAutoHideEnabled(enabled);
    }

    public boolean isControlsOverlayAutoHideEnabled() {
        return this.mFragment.isControlsOverlayAutoHideEnabled();
    }

    public void setOnKeyInterceptListener(OnKeyListener onKeyListener) {
        this.mFragment.setOnKeyInterceptListener(onKeyListener);
    }

    public void setOnActionClickedListener(final OnActionClickedListener listener) {
        if (listener == null) {
            this.mFragment.setOnPlaybackItemViewClickedListener(null);
        } else {
            this.mFragment.setOnPlaybackItemViewClickedListener(new OnItemViewClickedListener() {
                public void onItemClicked(ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                    if (item instanceof Action) {
                        listener.onActionClicked((Action) item);
                    }
                }
            });
        }
    }

    public void setHostCallback(HostCallback callback) {
        this.mFragment.setHostCallback(callback);
    }

    public void notifyPlaybackRowChanged() {
        this.mFragment.notifyPlaybackRowChanged();
    }

    public void setPlaybackRowPresenter(PlaybackRowPresenter presenter) {
        this.mFragment.setPlaybackRowPresenter(presenter);
    }

    public void setPlaybackRow(Row row) {
        this.mFragment.setPlaybackRow(row);
    }

    public void fadeOut() {
        this.mFragment.fadeOut();
    }

    public boolean isControlsOverlayVisible() {
        return this.mFragment.isControlsOverlayVisible();
    }

    public void hideControlsOverlay(boolean runAnimation) {
        this.mFragment.hideControlsOverlay(runAnimation);
    }

    public void showControlsOverlay(boolean runAnimation) {
        this.mFragment.showControlsOverlay(runAnimation);
    }

    public void setPlaybackSeekUiClient(Client client) {
        this.mFragment.setPlaybackSeekUiClient(client);
    }

    public PlayerCallback getPlayerCallback() {
        return this.mPlayerCallback;
    }
}
