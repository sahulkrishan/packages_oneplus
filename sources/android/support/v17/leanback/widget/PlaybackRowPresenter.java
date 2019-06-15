package android.support.v17.leanback.widget;

import android.view.View;

public abstract class PlaybackRowPresenter extends RowPresenter {

    public static class ViewHolder extends android.support.v17.leanback.widget.RowPresenter.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }

    public void onReappear(android.support.v17.leanback.widget.RowPresenter.ViewHolder rowViewHolder) {
    }
}
