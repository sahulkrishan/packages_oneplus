package androidx.slice.widget;

import android.content.Context;
import android.os.Build.VERSION;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout.LayoutParams;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceAction;
import androidx.slice.widget.SliceView.OnSliceActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestrictTo({Scope.LIBRARY})
public class LargeTemplateView extends SliceChildView {
    private final LargeSliceAdapter mAdapter;
    private ArrayList<SliceItem> mDisplayedItems = new ArrayList();
    private int mDisplayedItemsHeight = 0;
    private final View mForeground;
    private ListContent mListContent;
    private int[] mLoc = new int[2];
    private SliceView mParent;
    private final RecyclerView mRecyclerView = new RecyclerView(getContext());
    private boolean mScrollingEnabled;

    public LargeTemplateView(Context context) {
        super(context);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.mAdapter = new LargeSliceAdapter(context);
        this.mRecyclerView.setAdapter(this.mAdapter);
        addView(this.mRecyclerView);
        this.mForeground = new View(getContext());
        this.mForeground.setBackground(SliceViewUtil.getDrawable(getContext(), 16843534));
        addView(this.mForeground);
        LayoutParams lp = (LayoutParams) this.mForeground.getLayoutParams();
        lp.width = -1;
        lp.height = -1;
        this.mForeground.setLayoutParams(lp);
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mParent = (SliceView) getParent();
        this.mAdapter.setParents(this.mParent, this);
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (!(this.mScrollingEnabled || this.mDisplayedItems.size() <= 0 || this.mDisplayedItemsHeight == height)) {
            updateDisplayedItems(height);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void onForegroundActivated(MotionEvent event) {
        if (this.mParent == null || this.mParent.isSliceViewClickable()) {
            if (VERSION.SDK_INT >= 21) {
                this.mForeground.getLocationOnScreen(this.mLoc);
                this.mForeground.getBackground().setHotspot((float) ((int) (event.getRawX() - ((float) this.mLoc[0]))), (float) ((int) (event.getRawY() - ((float) this.mLoc[1]))));
            }
            int action = event.getActionMasked();
            if (action == 0) {
                this.mForeground.setPressed(true);
            } else if (action == 3 || action == 1 || action == 2) {
                this.mForeground.setPressed(false);
            }
            return;
        }
        this.mForeground.setPressed(false);
    }

    public void setMode(int newMode) {
        if (this.mMode != newMode) {
            this.mMode = newMode;
            if (this.mListContent != null && this.mListContent.isValid()) {
                updateDisplayedItems(this.mListContent.getLargeHeight(-1, this.mScrollingEnabled));
            }
        }
    }

    public int getActualHeight() {
        return this.mDisplayedItemsHeight;
    }

    public int getSmallHeight() {
        if (this.mListContent == null || !this.mListContent.isValid()) {
            return 0;
        }
        return this.mListContent.getSmallHeight();
    }

    public void setTint(int tint) {
        super.setTint(tint);
        updateDisplayedItems(getMeasuredHeight());
    }

    public void setSliceActionListener(OnSliceActionListener observer) {
        this.mObserver = observer;
        if (this.mAdapter != null) {
            this.mAdapter.setSliceObserver(this.mObserver);
        }
    }

    public void setSliceActions(List<SliceAction> actions) {
        this.mAdapter.setSliceActions(actions);
    }

    public void setSliceContent(ListContent sliceContent) {
        this.mListContent = sliceContent;
        updateDisplayedItems(this.mListContent.getLargeHeight(-1, this.mScrollingEnabled));
    }

    public void setStyle(AttributeSet attrs, int defStyleAttrs, int defStyleRes) {
        super.setStyle(attrs, defStyleAttrs, defStyleRes);
        this.mAdapter.setStyle(attrs, defStyleAttrs, defStyleRes);
    }

    public void setShowLastUpdated(boolean showLastUpdated) {
        super.setShowLastUpdated(showLastUpdated);
        this.mAdapter.setShowLastUpdated(showLastUpdated);
    }

    public void setLastUpdated(long lastUpdated) {
        super.setLastUpdated(lastUpdated);
        this.mAdapter.setLastUpdated(lastUpdated);
    }

    public void setScrollable(boolean scrollingEnabled) {
        if (this.mScrollingEnabled != scrollingEnabled) {
            this.mScrollingEnabled = scrollingEnabled;
            if (this.mListContent != null && this.mListContent.isValid()) {
                updateDisplayedItems(this.mListContent.getLargeHeight(-1, this.mScrollingEnabled));
            }
        }
    }

    private void updateDisplayedItems(int height) {
        if (this.mListContent == null || !this.mListContent.isValid()) {
            resetView();
            return;
        }
        int mode = getMode();
        if (mode == 1) {
            this.mDisplayedItems = new ArrayList(Arrays.asList(new SliceItem[]{(SliceItem) this.mListContent.getRowItems().get(0)}));
        } else if (this.mScrollingEnabled || height == 0) {
            this.mDisplayedItems = this.mListContent.getRowItems();
        } else {
            this.mDisplayedItems = this.mListContent.getItemsForNonScrollingList(height);
        }
        this.mDisplayedItemsHeight = this.mListContent.getListHeight(this.mDisplayedItems);
        this.mAdapter.setSliceItems(this.mDisplayedItems, this.mTintColor, mode);
        updateOverscroll();
    }

    private void updateOverscroll() {
        int i = 1;
        boolean scrollable = this.mDisplayedItemsHeight > getMeasuredHeight();
        RecyclerView recyclerView = this.mRecyclerView;
        if (!(this.mScrollingEnabled && scrollable)) {
            i = 2;
        }
        recyclerView.setOverScrollMode(i);
    }

    public void resetView() {
        this.mDisplayedItemsHeight = 0;
        this.mDisplayedItems.clear();
        this.mAdapter.setSliceItems(null, -1, getMode());
        this.mListContent = null;
    }
}
