package androidx.slice.widget;

import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceQuery;
import androidx.slice.view.R;
import androidx.slice.widget.GridContent.CellContent;
import androidx.slice.widget.SliceView.OnSliceActionListener;
import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestrictTo({Scope.LIBRARY})
public class GridRowView extends SliceChildView implements OnClickListener {
    private static final int MAX_CELL_IMAGES = 1;
    private static final int MAX_CELL_TEXT = 2;
    private static final int MAX_CELL_TEXT_SMALL = 1;
    private static final String TAG = "GridView";
    private static final int TEXT_LAYOUT = R.layout.abc_slice_secondary_text;
    private static final int TITLE_TEXT_LAYOUT = R.layout.abc_slice_title;
    private GridContent mGridContent;
    private int mGutter;
    private int mIconSize;
    private int mLargeImageHeight;
    private boolean mMaxCellUpdateScheduled;
    private int mMaxCells;
    private OnPreDrawListener mMaxCellsUpdater;
    private int mRowCount;
    private int mRowIndex;
    private int mSmallImageMinWidth;
    private int mSmallImageSize;
    private int mTextPadding;
    private LinearLayout mViewContainer;

    public GridRowView(Context context) {
        this(context, null);
    }

    public GridRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mMaxCells = -1;
        this.mMaxCellsUpdater = new OnPreDrawListener() {
            public boolean onPreDraw() {
                GridRowView.this.mMaxCells = GridRowView.this.getMaxCells();
                GridRowView.this.populateViews();
                GridRowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                GridRowView.this.mMaxCellUpdateScheduled = false;
                return true;
            }
        };
        Resources res = getContext().getResources();
        this.mViewContainer = new LinearLayout(getContext());
        this.mViewContainer.setOrientation(0);
        addView(this.mViewContainer, new LayoutParams(-1, -1));
        this.mViewContainer.setGravity(16);
        this.mIconSize = res.getDimensionPixelSize(R.dimen.abc_slice_icon_size);
        this.mSmallImageSize = res.getDimensionPixelSize(R.dimen.abc_slice_small_image_size);
        this.mLargeImageHeight = res.getDimensionPixelSize(R.dimen.abc_slice_grid_image_only_height);
        this.mSmallImageMinWidth = res.getDimensionPixelSize(R.dimen.abc_slice_grid_image_min_width);
        this.mGutter = res.getDimensionPixelSize(R.dimen.abc_slice_grid_gutter);
        this.mTextPadding = res.getDimensionPixelSize(R.dimen.abc_slice_grid_text_padding);
    }

    public int getSmallHeight() {
        if (this.mGridContent == null) {
            return 0;
        }
        return (this.mGridContent.getSmallHeight() + getExtraTopPadding()) + getExtraBottomPadding();
    }

    public int getActualHeight() {
        if (this.mGridContent == null) {
            return 0;
        }
        return (this.mGridContent.getActualHeight() + getExtraTopPadding()) + getExtraBottomPadding();
    }

    private int getExtraTopPadding() {
        if (this.mGridContent != null && this.mGridContent.isAllImages() && this.mRowIndex == 0) {
            return this.mGridTopPadding;
        }
        return 0;
    }

    private int getExtraBottomPadding() {
        if (this.mGridContent != null && this.mGridContent.isAllImages() && (this.mRowIndex == this.mRowCount - 1 || getMode() == 1)) {
            return this.mGridBottomPadding;
        }
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getMode() == 1 ? getSmallHeight() : getActualHeight();
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, Ints.MAX_POWER_OF_TWO);
        this.mViewContainer.getLayoutParams().height = height;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setTint(@ColorInt int tintColor) {
        super.setTint(tintColor);
        if (this.mGridContent != null) {
            resetView();
            populateViews();
        }
    }

    public void setSliceItem(SliceItem slice, boolean isHeader, int rowIndex, int rowCount, OnSliceActionListener observer) {
        resetView();
        setSliceActionListener(observer);
        this.mRowIndex = rowIndex;
        this.mRowCount = rowCount;
        this.mGridContent = new GridContent(getContext(), slice);
        if (!scheduleMaxCellsUpdate()) {
            populateViews();
        }
        this.mViewContainer.setPadding(0, getExtraTopPadding(), 0, getExtraBottomPadding());
    }

    private boolean scheduleMaxCellsUpdate() {
        if (this.mGridContent == null || !this.mGridContent.isValid()) {
            return true;
        }
        if (getWidth() == 0) {
            this.mMaxCellUpdateScheduled = true;
            getViewTreeObserver().addOnPreDrawListener(this.mMaxCellsUpdater);
            return true;
        }
        this.mMaxCells = getMaxCells();
        return false;
    }

    private int getMaxCells() {
        if (this.mGridContent == null || !this.mGridContent.isValid() || getWidth() == 0) {
            return -1;
        }
        if (this.mGridContent.getGridContent().size() <= 1) {
            return 1;
        }
        return getWidth() / (this.mGutter + (this.mGridContent.getLargestImageMode() == 2 ? this.mLargeImageHeight : this.mSmallImageMinWidth));
    }

    private void populateViews() {
        if (this.mGridContent == null || !this.mGridContent.isValid()) {
            resetView();
        } else if (!scheduleMaxCellsUpdate()) {
            if (this.mGridContent.getLayoutDirItem() != null) {
                setLayoutDirection(this.mGridContent.getLayoutDirItem().getInt());
            }
            boolean hasSeeMore = true;
            if (this.mGridContent.getContentIntent() != null) {
                this.mViewContainer.setTag(new Pair(this.mGridContent.getContentIntent(), new EventInfo(getMode(), 3, 1, this.mRowIndex)));
                makeClickable(this.mViewContainer, true);
            }
            CharSequence contentDescr = this.mGridContent.getContentDescription();
            if (contentDescr != null) {
                this.mViewContainer.setContentDescription(contentDescr);
            }
            ArrayList<CellContent> cells = this.mGridContent.getGridContent();
            if (this.mGridContent.getLargestImageMode() == 2) {
                this.mViewContainer.setGravity(48);
            } else {
                this.mViewContainer.setGravity(16);
            }
            int maxCells = this.mMaxCells;
            int i = 0;
            if (this.mGridContent.getSeeMoreItem() == null) {
                hasSeeMore = false;
            }
            while (true) {
                int i2 = i;
                if (i2 >= cells.size()) {
                    break;
                } else if (this.mViewContainer.getChildCount() < maxCells) {
                    addCell((CellContent) cells.get(i2), i2, Math.min(cells.size(), maxCells));
                    i = i2 + 1;
                } else if (hasSeeMore) {
                    addSeeMoreCount(cells.size() - maxCells);
                }
            }
        }
    }

    private void addSeeMoreCount(int numExtra) {
        View last = this.mViewContainer.getChildAt(this.mViewContainer.getChildCount() - 1);
        this.mViewContainer.removeView(last);
        SliceItem seeMoreItem = this.mGridContent.getSeeMoreItem();
        int index = this.mViewContainer.getChildCount();
        int total = this.mMaxCells;
        if (("slice".equals(seeMoreItem.getFormat()) || "action".equals(seeMoreItem.getFormat())) && seeMoreItem.getSlice().getItems().size() > 0) {
            addCell(new CellContent(seeMoreItem), index, total);
            return;
        }
        ViewGroup seeMoreView;
        TextView extraText;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (this.mGridContent.isAllImages()) {
            seeMoreView = (FrameLayout) inflater.inflate(R.layout.abc_slice_grid_see_more_overlay, this.mViewContainer, false);
            seeMoreView.addView(last, 0, new LayoutParams(-1, -1));
            extraText = (TextView) seeMoreView.findViewById(R.id.text_see_more_count);
        } else {
            seeMoreView = (LinearLayout) inflater.inflate(R.layout.abc_slice_grid_see_more, this.mViewContainer, false);
            extraText = (TextView) seeMoreView.findViewById(R.id.text_see_more_count);
            TextView moreText = (TextView) seeMoreView.findViewById(R.id.text_see_more);
            moreText.setTextSize(0, (float) this.mGridTitleSize);
            moreText.setTextColor(this.mTitleColor);
        }
        this.mViewContainer.addView(seeMoreView, new LinearLayout.LayoutParams(0, -1, 1.0f));
        extraText.setText(getResources().getString(R.string.abc_slice_more_content, new Object[]{Integer.valueOf(numExtra)}));
        EventInfo info = new EventInfo(getMode(), 4, 1, this.mRowIndex);
        info.setPosition(2, index, total);
        seeMoreView.setTag(new Pair(seeMoreItem, info));
        makeClickable(seeMoreView, true);
    }

    private void addCell(CellContent cell, int index, int total) {
        SliceItem cellItem;
        int imageCount;
        int textCount;
        int i = index;
        int i2 = total;
        int maxCellText = getMode() == 1 ? 1 : 2;
        ViewGroup cellContainer = new LinearLayout(getContext());
        cellContainer.setOrientation(1);
        cellContainer.setGravity(1);
        ArrayList<SliceItem> cellItems = cell.getCellItems();
        SliceItem contentIntentItem = cell.getContentIntent();
        boolean isSingleItem = cellItems.size() == 1;
        List<SliceItem> textItems = null;
        if (!isSingleItem && getMode() == 1) {
            textItems = new ArrayList();
            Iterator it = cellItems.iterator();
            while (it.hasNext()) {
                cellItem = (SliceItem) it.next();
                if ("text".equals(cellItem.getFormat())) {
                    textItems.add(cellItem);
                }
            }
            Iterator<SliceItem> iterator = textItems.iterator();
            while (textItems.size() > 1) {
                if (!((SliceItem) iterator.next()).hasAnyHints("title", "large")) {
                    iterator.remove();
                }
            }
        }
        List<SliceItem> textItems2 = textItems;
        int textCount2 = 0;
        int imageCount2 = 0;
        boolean added = false;
        SliceItem prevItem = null;
        int i3 = 0;
        while (true) {
            int i4 = i3;
            if (i4 >= cellItems.size()) {
                break;
            }
            SliceItem item;
            int i5;
            SliceItem item2 = (SliceItem) cellItems.get(i4);
            String itemFormat = item2.getFormat();
            int padding = determinePadding(prevItem);
            if (textCount2 >= maxCellText) {
                item = item2;
                i5 = i4;
                imageCount = imageCount2;
                textCount = textCount2;
            } else if (!"text".equals(itemFormat) && !"long".equals(itemFormat)) {
                String str = itemFormat;
                item = item2;
                i5 = i4;
                imageCount = imageCount2;
                textCount = textCount2;
            } else if (textItems2 == null || textItems2.contains(item2)) {
                item = item2;
                i5 = i4;
                imageCount = imageCount2;
                textCount = textCount2;
                if (addItem(item2, this.mTintColor, cellContainer, padding, isSingleItem)) {
                    textCount2 = textCount + 1;
                    prevItem = item;
                    added = true;
                    imageCount2 = imageCount;
                    i3 = i5 + 1;
                }
                imageCount2 = imageCount;
                textCount2 = textCount;
                i3 = i5 + 1;
            } else {
                i5 = i4;
                imageCount = imageCount2;
                textCount = textCount2;
                imageCount2 = imageCount;
                textCount2 = textCount;
                i3 = i5 + 1;
            }
            if (imageCount < 1) {
                cellItem = item;
                if ("image".equals(cellItem.getFormat())) {
                    item = cellItem;
                    if (addItem(cellItem, this.mTintColor, cellContainer, 0, isSingleItem)) {
                        imageCount2 = imageCount + 1;
                        prevItem = item;
                        added = true;
                        textCount2 = textCount;
                        i3 = i5 + 1;
                    }
                }
            }
            imageCount2 = imageCount;
            textCount2 = textCount;
            i3 = i5 + 1;
        }
        imageCount = imageCount2;
        textCount = textCount2;
        if (added) {
            CharSequence contentDescr = cell.getContentDescription();
            if (contentDescr != null) {
                cellContainer.setContentDescription(contentDescr);
            }
            this.mViewContainer.addView(cellContainer, new LinearLayout.LayoutParams(0, -2, 1.0f));
            if (i != i2 - 1) {
                MarginLayoutParams lp = (MarginLayoutParams) cellContainer.getLayoutParams();
                lp.setMarginEnd(this.mGutter);
                cellContainer.setLayoutParams(lp);
            }
            if (contentIntentItem != null) {
                EventInfo info = new EventInfo(getMode(), 1, 1, this.mRowIndex);
                info.setPosition(2, i, i2);
                cellContainer.setTag(new Pair(contentIntentItem, info));
                makeClickable(cellContainer, true);
            }
        }
    }

    private boolean addItem(SliceItem item, int color, ViewGroup container, int padding, boolean isSingle) {
        String format = item.getFormat();
        View addedView = null;
        if ("text".equals(format) || "long".equals(format)) {
            CharSequence text;
            boolean title = SliceQuery.hasAnyHints(item, new String[]{"large", "title"});
            View tv = (TextView) LayoutInflater.from(getContext()).inflate(title ? TITLE_TEXT_LAYOUT : TEXT_LAYOUT, null);
            tv.setTextSize(0, (float) (title ? this.mGridTitleSize : this.mGridSubtitleSize));
            tv.setTextColor(title ? this.mTitleColor : this.mSubtitleColor);
            if ("long".equals(format)) {
                text = SliceViewUtil.getRelativeTimeString(item.getTimestamp());
            } else {
                text = item.getText();
            }
            tv.setText(text);
            container.addView(tv);
            tv.setPadding(0, padding, 0, 0);
            addedView = tv;
        } else if ("image".equals(format)) {
            LinearLayout.LayoutParams lp;
            View iv = new ImageView(getContext());
            iv.setImageDrawable(item.getIcon().loadDrawable(getContext()));
            if (item.hasHint("large")) {
                iv.setScaleType(ScaleType.CENTER_CROP);
                lp = new LinearLayout.LayoutParams(-1, isSingle ? -1 : this.mLargeImageHeight);
            } else {
                boolean isIcon = item.hasHint("no_tint") ^ true;
                int size = isIcon ? this.mIconSize : this.mSmallImageSize;
                iv.setScaleType(isIcon ? ScaleType.CENTER_INSIDE : ScaleType.CENTER_CROP);
                lp = new LinearLayout.LayoutParams(size, size);
            }
            LinearLayout.LayoutParams lp2 = lp;
            if (!(color == -1 || item.hasHint("no_tint"))) {
                iv.setColorFilter(color);
            }
            container.addView(iv, lp2);
            addedView = iv;
        }
        if (addedView != null) {
            return true;
        }
        return false;
    }

    private int determinePadding(SliceItem prevItem) {
        if (prevItem == null) {
            return 0;
        }
        if ("image".equals(prevItem.getFormat())) {
            return this.mTextPadding;
        }
        if ("text".equals(prevItem.getFormat()) || "long".equals(prevItem.getFormat())) {
            return this.mVerticalGridTextPadding;
        }
        return 0;
    }

    private void makeClickable(View layout, boolean isClickable) {
        Drawable drawable = null;
        layout.setOnClickListener(isClickable ? this : null);
        if (isClickable) {
            drawable = SliceViewUtil.getDrawable(getContext(), 16843534);
        }
        layout.setBackground(drawable);
        layout.setClickable(isClickable);
    }

    public void onClick(View view) {
        Pair<SliceItem, EventInfo> tagItem = (Pair) view.getTag();
        SliceItem actionItem = tagItem.first;
        EventInfo info = tagItem.second;
        if (actionItem != null && "action".equals(actionItem.getFormat())) {
            try {
                actionItem.fireAction(null, null);
                if (this.mObserver != null) {
                    this.mObserver.onSliceAction(info, actionItem);
                }
            } catch (CanceledException e) {
                Log.e(TAG, "PendingIntent for slice cannot be sent", e);
            }
        }
    }

    public void resetView() {
        if (this.mMaxCellUpdateScheduled) {
            this.mMaxCellUpdateScheduled = false;
            getViewTreeObserver().removeOnPreDrawListener(this.mMaxCellsUpdater);
        }
        this.mViewContainer.removeAllViews();
        setLayoutDirection(2);
        makeClickable(this.mViewContainer, false);
    }
}
