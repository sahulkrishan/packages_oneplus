package androidx.slice.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceAction;
import androidx.slice.view.R;
import androidx.slice.widget.SliceView.OnSliceActionListener;
import java.util.List;

@RestrictTo({Scope.LIBRARY})
public abstract class SliceChildView extends FrameLayout {
    protected int mGridBottomPadding;
    protected int mGridSubtitleSize;
    protected int mGridTitleSize;
    protected int mGridTopPadding;
    protected int mHeaderSubtitleSize;
    protected int mHeaderTitleSize;
    protected long mLastUpdated;
    protected int mMode;
    protected OnSliceActionListener mObserver;
    protected boolean mShowLastUpdated;
    protected int mSubtitleColor;
    protected int mSubtitleSize;
    protected int mTintColor;
    protected int mTitleColor;
    protected int mTitleSize;
    protected int mVerticalGridTextPadding;
    protected int mVerticalHeaderTextPadding;
    protected int mVerticalTextPadding;

    public abstract void resetView();

    public SliceChildView(@NonNull Context context) {
        super(context);
        this.mTintColor = -1;
        this.mLastUpdated = -1;
    }

    public SliceChildView(Context context, AttributeSet attributeSet) {
        this(context);
    }

    public void setSliceContent(ListContent content) {
    }

    public void setSliceItem(SliceItem slice, boolean isHeader, int rowIndex, int rowCount, OnSliceActionListener observer) {
    }

    public void setSliceActions(List<SliceAction> list) {
    }

    public int getSmallHeight() {
        return 0;
    }

    public int getActualHeight() {
        return 0;
    }

    public void setMode(int mode) {
        this.mMode = mode;
    }

    public int getMode() {
        return this.mMode;
    }

    public void setTint(@ColorInt int tintColor) {
        this.mTintColor = tintColor;
    }

    public void setShowLastUpdated(boolean showLastUpdated) {
        this.mShowLastUpdated = showLastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.mLastUpdated = lastUpdated;
    }

    public void setSliceActionListener(OnSliceActionListener observer) {
        this.mObserver = observer;
    }

    public void setStyle(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SliceView, defStyleAttr, defStyleRes);
        try {
            int themeColor = a.getColor(R.styleable.SliceView_tintColor, -1);
            this.mTintColor = themeColor != -1 ? themeColor : this.mTintColor;
            this.mTitleColor = a.getColor(R.styleable.SliceView_titleColor, 0);
            this.mSubtitleColor = a.getColor(R.styleable.SliceView_subtitleColor, 0);
            this.mHeaderTitleSize = (int) a.getDimension(R.styleable.SliceView_headerTitleSize, 0.0f);
            this.mHeaderSubtitleSize = (int) a.getDimension(R.styleable.SliceView_headerSubtitleSize, 0.0f);
            this.mVerticalHeaderTextPadding = (int) a.getDimension(R.styleable.SliceView_headerTextVerticalPadding, 0.0f);
            this.mTitleSize = (int) a.getDimension(R.styleable.SliceView_titleSize, 0.0f);
            this.mSubtitleSize = (int) a.getDimension(R.styleable.SliceView_subtitleSize, 0.0f);
            this.mVerticalTextPadding = (int) a.getDimension(R.styleable.SliceView_textVerticalPadding, 0.0f);
            this.mGridTitleSize = (int) a.getDimension(R.styleable.SliceView_gridTitleSize, 0.0f);
            this.mGridSubtitleSize = (int) a.getDimension(R.styleable.SliceView_gridSubtitleSize, 0.0f);
            this.mVerticalGridTextPadding = (int) a.getDimension(R.styleable.SliceView_gridTextVerticalPadding, (float) getContext().getResources().getDimensionPixelSize(R.dimen.abc_slice_grid_text_inner_padding));
            this.mGridTopPadding = (int) a.getDimension(R.styleable.SliceView_gridTopPadding, 0.0f);
            this.mGridBottomPadding = (int) a.getDimension(R.styleable.SliceView_gridTopPadding, 0.0f);
        } finally {
            a.recycle();
        }
    }
}
