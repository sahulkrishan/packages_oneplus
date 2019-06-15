package android.support.v17.leanback.widget;

import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.MultiActionsProvider.MultiAction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

class MediaItemActionPresenter extends Presenter {

    static class ViewHolder extends android.support.v17.leanback.widget.Presenter.ViewHolder {
        final ImageView mIcon;

        public ViewHolder(View view) {
            super(view);
            this.mIcon = (ImageView) view.findViewById(R.id.actionIcon);
        }

        public ImageView getIcon() {
            return this.mIcon;
        }
    }

    MediaItemActionPresenter() {
    }

    public android.support.v17.leanback.widget.Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.lb_row_media_item_action, parent, false));
    }

    public void onBindViewHolder(android.support.v17.leanback.widget.Presenter.ViewHolder viewHolder, Object item) {
        ((ViewHolder) viewHolder).getIcon().setImageDrawable(((MultiAction) item).getCurrentDrawable());
    }

    public void onUnbindViewHolder(android.support.v17.leanback.widget.Presenter.ViewHolder viewHolder) {
    }
}
