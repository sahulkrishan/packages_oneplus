package android.support.v17.leanback.widget;

import android.support.v17.leanback.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class DetailsOverviewLogoPresenter extends Presenter {

    public static class ViewHolder extends android.support.v17.leanback.widget.Presenter.ViewHolder {
        protected FullWidthDetailsOverviewRowPresenter mParentPresenter;
        protected android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter.ViewHolder mParentViewHolder;
        private boolean mSizeFromDrawableIntrinsic;

        public ViewHolder(View view) {
            super(view);
        }

        public FullWidthDetailsOverviewRowPresenter getParentPresenter() {
            return this.mParentPresenter;
        }

        public android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter.ViewHolder getParentViewHolder() {
            return this.mParentViewHolder;
        }

        public boolean isSizeFromDrawableIntrinsic() {
            return this.mSizeFromDrawableIntrinsic;
        }

        public void setSizeFromDrawableIntrinsic(boolean sizeFromDrawableIntrinsic) {
            this.mSizeFromDrawableIntrinsic = sizeFromDrawableIntrinsic;
        }
    }

    public View onCreateView(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.lb_fullwidth_details_overview_logo, parent, false);
    }

    public android.support.v17.leanback.widget.Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = onCreateView(parent);
        ViewHolder vh = new ViewHolder(view);
        LayoutParams lp = view.getLayoutParams();
        boolean z = lp.width == -2 && lp.height == -2;
        vh.setSizeFromDrawableIntrinsic(z);
        return vh;
    }

    public void setContext(ViewHolder viewHolder, android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter.ViewHolder parentViewHolder, FullWidthDetailsOverviewRowPresenter parentPresenter) {
        viewHolder.mParentViewHolder = parentViewHolder;
        viewHolder.mParentPresenter = parentPresenter;
    }

    public boolean isBoundToImage(ViewHolder viewHolder, DetailsOverviewRow row) {
        return (row == null || row.getImageDrawable() == null) ? false : true;
    }

    public void onBindViewHolder(android.support.v17.leanback.widget.Presenter.ViewHolder viewHolder, Object item) {
        DetailsOverviewRow row = (DetailsOverviewRow) item;
        ImageView imageView = viewHolder.view;
        imageView.setImageDrawable(row.getImageDrawable());
        if (isBoundToImage((ViewHolder) viewHolder, row)) {
            ViewHolder vh = (ViewHolder) viewHolder;
            if (vh.isSizeFromDrawableIntrinsic()) {
                LayoutParams lp = imageView.getLayoutParams();
                lp.width = row.getImageDrawable().getIntrinsicWidth();
                lp.height = row.getImageDrawable().getIntrinsicHeight();
                if (imageView.getMaxWidth() > 0 || imageView.getMaxHeight() > 0) {
                    float maxScaleWidth = 1.0f;
                    if (imageView.getMaxWidth() > 0 && lp.width > imageView.getMaxWidth()) {
                        maxScaleWidth = ((float) imageView.getMaxWidth()) / ((float) lp.width);
                    }
                    float maxScaleHeight = 1.0f;
                    if (imageView.getMaxHeight() > 0 && lp.height > imageView.getMaxHeight()) {
                        maxScaleHeight = ((float) imageView.getMaxHeight()) / ((float) lp.height);
                    }
                    float scale = Math.min(maxScaleWidth, maxScaleHeight);
                    lp.width = (int) (((float) lp.width) * scale);
                    lp.height = (int) (((float) lp.height) * scale);
                }
                imageView.setLayoutParams(lp);
            }
            vh.mParentPresenter.notifyOnBindLogo(vh.mParentViewHolder);
        }
    }

    public void onUnbindViewHolder(android.support.v17.leanback.widget.Presenter.ViewHolder viewHolder) {
    }
}
