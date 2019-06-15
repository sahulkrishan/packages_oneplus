package android.support.v17.leanback.widget;

import android.graphics.Paint;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.R;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RowHeaderPresenter extends Presenter {
    private final boolean mAnimateSelect;
    private final Paint mFontMeasurePaint;
    private final int mLayoutResourceId;
    private boolean mNullItemVisibilityGone;

    public static class ViewHolder extends android.support.v17.leanback.widget.Presenter.ViewHolder {
        TextView mDescriptionView;
        int mOriginalTextColor;
        float mSelectLevel;
        RowHeaderView mTitleView;
        float mUnselectAlpha;

        public ViewHolder(View view) {
            super(view);
            this.mTitleView = (RowHeaderView) view.findViewById(R.id.row_header);
            this.mDescriptionView = (TextView) view.findViewById(R.id.row_header_description);
            initColors();
        }

        @RestrictTo({Scope.LIBRARY_GROUP})
        public ViewHolder(RowHeaderView view) {
            super(view);
            this.mTitleView = view;
            initColors();
        }

        /* Access modifiers changed, original: 0000 */
        public void initColors() {
            if (this.mTitleView != null) {
                this.mOriginalTextColor = this.mTitleView.getCurrentTextColor();
            }
            this.mUnselectAlpha = this.view.getResources().getFraction(R.fraction.lb_browse_header_unselect_alpha, 1, 1);
        }

        public final float getSelectLevel() {
            return this.mSelectLevel;
        }
    }

    public RowHeaderPresenter() {
        this(R.layout.lb_row_header);
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public RowHeaderPresenter(int layoutResourceId) {
        this(layoutResourceId, true);
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public RowHeaderPresenter(int layoutResourceId, boolean animateSelect) {
        this.mFontMeasurePaint = new Paint(1);
        this.mLayoutResourceId = layoutResourceId;
        this.mAnimateSelect = animateSelect;
    }

    public void setNullItemVisibilityGone(boolean nullItemVisibilityGone) {
        this.mNullItemVisibilityGone = nullItemVisibilityGone;
    }

    public boolean isNullItemVisibilityGone() {
        return this.mNullItemVisibilityGone;
    }

    public android.support.v17.leanback.widget.Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(this.mLayoutResourceId, parent, false));
        if (this.mAnimateSelect) {
            setSelectLevel(viewHolder, 0.0f);
        }
        return viewHolder;
    }

    public void onBindViewHolder(android.support.v17.leanback.widget.Presenter.ViewHolder viewHolder, Object item) {
        HeaderItem headerItem = item == null ? null : ((Row) item).getHeaderItem();
        ViewHolder vh = (ViewHolder) viewHolder;
        if (headerItem == null) {
            if (vh.mTitleView != null) {
                vh.mTitleView.setText(null);
            }
            if (vh.mDescriptionView != null) {
                vh.mDescriptionView.setText(null);
            }
            viewHolder.view.setContentDescription(null);
            if (this.mNullItemVisibilityGone) {
                viewHolder.view.setVisibility(8);
                return;
            }
            return;
        }
        if (vh.mTitleView != null) {
            vh.mTitleView.setText(headerItem.getName());
        }
        if (vh.mDescriptionView != null) {
            if (TextUtils.isEmpty(headerItem.getDescription())) {
                vh.mDescriptionView.setVisibility(8);
            } else {
                vh.mDescriptionView.setVisibility(0);
            }
            vh.mDescriptionView.setText(headerItem.getDescription());
        }
        viewHolder.view.setContentDescription(headerItem.getContentDescription());
        viewHolder.view.setVisibility(0);
    }

    public void onUnbindViewHolder(android.support.v17.leanback.widget.Presenter.ViewHolder viewHolder) {
        ViewHolder vh = (ViewHolder) viewHolder;
        if (vh.mTitleView != null) {
            vh.mTitleView.setText(null);
        }
        if (vh.mDescriptionView != null) {
            vh.mDescriptionView.setText(null);
        }
        if (this.mAnimateSelect) {
            setSelectLevel((ViewHolder) viewHolder, 0.0f);
        }
    }

    public final void setSelectLevel(ViewHolder holder, float selectLevel) {
        holder.mSelectLevel = selectLevel;
        onSelectLevelChanged(holder);
    }

    /* Access modifiers changed, original: protected */
    public void onSelectLevelChanged(ViewHolder holder) {
        if (this.mAnimateSelect) {
            holder.view.setAlpha(holder.mUnselectAlpha + (holder.mSelectLevel * (1.0f - holder.mUnselectAlpha)));
        }
    }

    public int getSpaceUnderBaseline(ViewHolder holder) {
        int space = holder.view.getPaddingBottom();
        if (holder.view instanceof TextView) {
            return space + ((int) getFontDescent((TextView) holder.view, this.mFontMeasurePaint));
        }
        return space;
    }

    protected static float getFontDescent(TextView textView, Paint fontMeasurePaint) {
        if (fontMeasurePaint.getTextSize() != textView.getTextSize()) {
            fontMeasurePaint.setTextSize(textView.getTextSize());
        }
        if (fontMeasurePaint.getTypeface() != textView.getTypeface()) {
            fontMeasurePaint.setTypeface(textView.getTypeface());
        }
        return fontMeasurePaint.descent();
    }
}
