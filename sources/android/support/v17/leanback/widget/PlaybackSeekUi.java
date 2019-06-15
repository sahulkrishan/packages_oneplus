package android.support.v17.leanback.widget;

public interface PlaybackSeekUi {

    public static class Client {
        public boolean isSeekEnabled() {
            return false;
        }

        public void onSeekStarted() {
        }

        public PlaybackSeekDataProvider getPlaybackSeekDataProvider() {
            return null;
        }

        public void onSeekPositionChanged(long pos) {
        }

        public void onSeekFinished(boolean cancelled) {
        }
    }

    void setPlaybackSeekUiClient(Client client);
}
