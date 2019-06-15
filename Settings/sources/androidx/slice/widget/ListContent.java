package androidx.slice.widget;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.util.AttributeSet;
import androidx.slice.Slice;
import androidx.slice.SliceItem;
import androidx.slice.SliceMetadata;
import androidx.slice.core.SliceAction;
import androidx.slice.core.SliceActionImpl;
import androidx.slice.core.SliceHints;
import androidx.slice.core.SliceQuery;
import androidx.slice.view.R;
import com.android.settings.inputmethod.UserDictionaryAddWordContents;
import java.util.ArrayList;
import java.util.List;

@RestrictTo({Scope.LIBRARY_GROUP})
public class ListContent {
    private SliceItem mColorItem;
    private Context mContext;
    private int mGridBottomPadding;
    private int mGridSubtitleSize;
    private int mGridTitleSize;
    private int mGridTopPadding;
    private SliceItem mHeaderItem;
    private int mHeaderSubtitleSize;
    private int mHeaderTitleSize;
    private int mLargeHeight;
    private SliceItem mLayoutDirItem;
    private int mMinScrollHeight;
    private ArrayList<SliceItem> mRowItems;
    private SliceItem mSeeMoreItem;
    private Slice mSlice;
    private List<SliceAction> mSliceActions;
    private int mSubtitleSize;
    private int mTitleSize;
    private int mVerticalGridTextPadding;
    private int mVerticalHeaderTextPadding;
    private int mVerticalTextPadding;

    public ListContent(Context context, Slice slice) {
        this(context, slice, null, 0, 0);
    }

    public ListContent(Context context, Slice slice, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mRowItems = new ArrayList();
        this.mSlice = slice;
        if (this.mSlice != null) {
            this.mContext = context;
            if (context != null) {
                this.mMinScrollHeight = context.getResources().getDimensionPixelSize(R.dimen.abc_slice_row_min_height);
                this.mLargeHeight = context.getResources().getDimensionPixelSize(R.dimen.abc_slice_large_height);
                Theme theme = context.getTheme();
                if (theme != null) {
                    TypedArray a = theme.obtainStyledAttributes(attrs, R.styleable.SliceView, defStyleAttr, defStyleRes);
                    try {
                        this.mHeaderTitleSize = (int) a.getDimension(R.styleable.SliceView_headerTitleSize, 0.0f);
                        this.mHeaderSubtitleSize = (int) a.getDimension(R.styleable.SliceView_headerSubtitleSize, 0.0f);
                        this.mVerticalHeaderTextPadding = (int) a.getDimension(R.styleable.SliceView_headerTextVerticalPadding, 0.0f);
                        this.mTitleSize = (int) a.getDimension(R.styleable.SliceView_titleSize, 0.0f);
                        this.mSubtitleSize = (int) a.getDimension(R.styleable.SliceView_subtitleSize, 0.0f);
                        this.mVerticalTextPadding = (int) a.getDimension(R.styleable.SliceView_textVerticalPadding, 0.0f);
                        this.mGridTitleSize = (int) a.getDimension(R.styleable.SliceView_gridTitleSize, 0.0f);
                        this.mGridSubtitleSize = (int) a.getDimension(R.styleable.SliceView_gridSubtitleSize, 0.0f);
                        this.mVerticalGridTextPadding = (int) a.getDimension(R.styleable.SliceView_gridTextVerticalPadding, (float) context.getResources().getDimensionPixelSize(R.dimen.abc_slice_grid_text_inner_padding));
                        this.mGridTopPadding = (int) a.getDimension(R.styleable.SliceView_gridTopPadding, 0.0f);
                        this.mGridBottomPadding = (int) a.getDimension(R.styleable.SliceView_gridTopPadding, 0.0f);
                    } finally {
                        a.recycle();
                    }
                }
            }
            populate(slice);
        }
    }

    private boolean populate(Slice slice) {
        if (slice == null) {
            return false;
        }
        SliceItem sliceItem = null;
        this.mColorItem = SliceQuery.findTopLevelItem(slice, "int", "color", null, null);
        this.mLayoutDirItem = SliceQuery.findTopLevelItem(slice, "int", "layout_direction", null, null);
        if (this.mLayoutDirItem != null) {
            if (SliceViewUtil.resolveLayoutDirection(this.mLayoutDirItem.getInt()) != -1) {
                sliceItem = this.mLayoutDirItem;
            }
            this.mLayoutDirItem = sliceItem;
        }
        this.mSliceActions = SliceMetadata.getSliceActions(slice);
        this.mHeaderItem = findHeaderItem(slice);
        if (this.mHeaderItem != null) {
            this.mRowItems.add(this.mHeaderItem);
        }
        this.mSeeMoreItem = getSeeMoreItem(slice);
        List<SliceItem> children = slice.getItems();
        for (int i = 0; i < children.size(); i++) {
            sliceItem = (SliceItem) children.get(i);
            String format = sliceItem.getFormat();
            if (!sliceItem.hasAnyHints(new String[]{"actions", "see_more", "keywords", SliceHints.HINT_TTL, SliceHints.HINT_LAST_UPDATED}) && ("action".equals(format) || "slice".equals(format))) {
                if (this.mHeaderItem == null && !sliceItem.hasHint("list_item")) {
                    this.mHeaderItem = sliceItem;
                    this.mRowItems.add(0, sliceItem);
                } else if (sliceItem.hasHint("list_item")) {
                    this.mRowItems.add(sliceItem);
                }
            }
        }
        if (this.mHeaderItem == null && this.mRowItems.size() >= 1) {
            this.mHeaderItem = (SliceItem) this.mRowItems.get(0);
        }
        return isValid();
    }

    public int getSmallHeight() {
        return getHeight(this.mHeaderItem, true, 0, 1, 1);
    }

    public int getLargeHeight(int maxHeight, boolean scrollable) {
        int desiredHeight = getListHeight(this.mRowItems);
        int maxLargeHeight = maxHeight != -1 ? maxHeight : this.mLargeHeight;
        int height = desiredHeight - maxLargeHeight >= this.mMinScrollHeight ? maxLargeHeight : maxHeight == -1 ? desiredHeight : Math.min(maxLargeHeight, desiredHeight);
        if (scrollable) {
            return height;
        }
        return getListHeight(getItemsForNonScrollingList(height));
    }

    public int getListHeight(List<SliceItem> listItems) {
        if (listItems == null || this.mContext == null) {
            return 0;
        }
        boolean hasRealHeader = false;
        SliceItem maybeHeader = null;
        if (!listItems.isEmpty()) {
            maybeHeader = (SliceItem) listItems.get(0);
            hasRealHeader = maybeHeader.hasAnyHints("list_item", "horizontal") ^ 1;
        }
        if (listItems.size() == 1 && !maybeHeader.hasHint("horizontal")) {
            return getHeight(maybeHeader, true, 0, 1, 2);
        }
        int rowCount = listItems.size();
        int height = 0;
        int i = 0;
        while (i < listItems.size()) {
            SliceItem sliceItem = (SliceItem) listItems.get(i);
            boolean z = i == 0 && hasRealHeader;
            height += getHeight(sliceItem, z, i, rowCount, 2);
            i++;
        }
        return height;
    }

    @NonNull
    public ArrayList<SliceItem> getItemsForNonScrollingList(int height) {
        ArrayList<SliceItem> visibleItems = new ArrayList();
        if (this.mRowItems == null || this.mRowItems.size() == 0) {
            return visibleItems;
        }
        int minItemCount = hasHeader() ? 2 : 1;
        int visibleHeight = 0;
        if (this.mSeeMoreItem != null) {
            visibleHeight = 0 + new RowContent(this.mContext, this.mSeeMoreItem, false).getActualHeight();
        }
        int rowCount = this.mRowItems.size();
        int visibleHeight2 = visibleHeight;
        visibleHeight = 0;
        while (visibleHeight < rowCount) {
            int itemHeight = getHeight((SliceItem) this.mRowItems.get(visibleHeight), visibleHeight == 0, visibleHeight, rowCount, 2);
            if (height > 0 && visibleHeight2 + itemHeight > height) {
                break;
            }
            visibleHeight2 += itemHeight;
            visibleItems.add(this.mRowItems.get(visibleHeight));
            visibleHeight++;
        }
        if (!(this.mSeeMoreItem == null || visibleItems.size() < minItemCount || visibleItems.size() == rowCount)) {
            visibleItems.add(this.mSeeMoreItem);
        }
        if (visibleItems.size() == 0) {
            visibleItems.add(this.mRowItems.get(0));
        }
        return visibleItems;
    }

    private int getHeight(SliceItem item, boolean isHeader, int index, int count, int mode) {
        int bottomPadding = 0;
        if (this.mContext == null || item == null) {
            return 0;
        }
        if (item.hasHint("horizontal")) {
            GridContent gc = new GridContent(this.mContext, item);
            int topPadding = (gc.isAllImages() && index == 0) ? this.mGridTopPadding : 0;
            if (gc.isAllImages() && index == count - 1) {
                bottomPadding = this.mGridBottomPadding;
            }
            return ((mode == 1 ? gc.getSmallHeight() : gc.getActualHeight()) + topPadding) + bottomPadding;
        }
        RowContent rc = new RowContent(this.mContext, item, isHeader);
        return mode == 1 ? rc.getSmallHeight() : rc.getActualHeight();
    }

    public boolean isValid() {
        return this.mSlice != null && this.mRowItems.size() > 0;
    }

    @Nullable
    public Slice getSlice() {
        return this.mSlice;
    }

    @Nullable
    public SliceItem getLayoutDirItem() {
        return this.mLayoutDirItem;
    }

    @Nullable
    public SliceItem getColorItem() {
        return this.mColorItem;
    }

    @Nullable
    public SliceItem getHeaderItem() {
        return this.mHeaderItem;
    }

    @Nullable
    public List<SliceAction> getSliceActions() {
        return this.mSliceActions;
    }

    @Nullable
    public SliceItem getSeeMoreItem() {
        return this.mSeeMoreItem;
    }

    @NonNull
    public ArrayList<SliceItem> getRowItems() {
        return this.mRowItems;
    }

    public boolean hasHeader() {
        return this.mHeaderItem != null && isValidHeader(this.mHeaderItem);
    }

    public int getHeaderTemplateType() {
        return getRowType(this.mContext, this.mHeaderItem, true, this.mSliceActions);
    }

    public static int getRowType(Context context, SliceItem rowItem, boolean isHeader, List<SliceAction> actions) {
        int i = 0;
        if (rowItem == null) {
            return 0;
        }
        if (rowItem.hasHint("horizontal")) {
            return 1;
        }
        RowContent rc = new RowContent(context, rowItem, isHeader);
        SliceItem actionItem = rc.getPrimaryAction();
        SliceAction primaryAction = null;
        if (actionItem != null) {
            primaryAction = new SliceActionImpl(actionItem);
        }
        if (rc.getRange() != null) {
            return "action".equals(rc.getRange().getFormat()) ? 4 : 5;
        } else if (primaryAction != null && primaryAction.isToggle()) {
            return 3;
        } else {
            if (!isHeader || actions == null) {
                if (rc.getToggleItems().size() > 0) {
                    i = 3;
                }
                return i;
            }
            for (int i2 = 0; i2 < actions.size(); i2++) {
                if (((SliceAction) actions.get(i2)).isToggle()) {
                    return 3;
                }
            }
            return 0;
        }
    }

    @Nullable
    public SliceItem getPrimaryAction() {
        if (this.mHeaderItem == null) {
            return null;
        }
        if (this.mHeaderItem.hasHint("horizontal")) {
            return new GridContent(this.mContext, this.mHeaderItem).getContentIntent();
        }
        return new RowContent(this.mContext, this.mHeaderItem, false).getPrimaryAction();
    }

    @Nullable
    private static SliceItem findHeaderItem(@NonNull Slice slice) {
        SliceItem header = SliceQuery.find(slice, "slice", null, new String[]{"list_item", UserDictionaryAddWordContents.EXTRA_SHORTCUT, "actions", "keywords", SliceHints.HINT_TTL, SliceHints.HINT_LAST_UPDATED, "horizontal"});
        if (header == null || !isValidHeader(header)) {
            return null;
        }
        return header;
    }

    @Nullable
    private static SliceItem getSeeMoreItem(@NonNull Slice slice) {
        SliceItem item = SliceQuery.findTopLevelItem(slice, null, null, new String[]{"see_more"}, null);
        if (item == null || !"slice".equals(item.getFormat())) {
            return null;
        }
        List<SliceItem> items = item.getSlice().getItems();
        if (items.size() == 1 && "action".equals(((SliceItem) items.get(0)).getFormat())) {
            return (SliceItem) items.get(0);
        }
        return item;
    }

    public static boolean isValidHeader(SliceItem sliceItem) {
        boolean z = false;
        if (!"slice".equals(sliceItem.getFormat()) || sliceItem.hasAnyHints("list_item", "actions", "keywords", "see_more")) {
            return false;
        }
        if (SliceQuery.find(sliceItem, "text", null, null) != null) {
            z = true;
        }
        return z;
    }
}
