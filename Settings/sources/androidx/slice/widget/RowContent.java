package androidx.slice.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.text.TextUtils;
import android.util.Log;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceAction;
import androidx.slice.core.SliceActionImpl;
import androidx.slice.core.SliceHints;
import androidx.slice.core.SliceQuery;
import androidx.slice.view.R;
import com.android.settings.inputmethod.UserDictionaryAddWordContents;
import java.util.ArrayList;
import java.util.List;

@RestrictTo({Scope.LIBRARY_GROUP})
public class RowContent {
    private static final String TAG = "RowContent";
    private SliceItem mContentDescr;
    private ArrayList<SliceItem> mEndItems = new ArrayList();
    private boolean mEndItemsContainAction;
    private boolean mIsHeader;
    private SliceItem mLayoutDirItem;
    private int mLineCount = 0;
    private int mMaxHeight;
    private int mMinHeight;
    private SliceItem mPrimaryAction;
    private SliceItem mRange;
    private int mRangeHeight;
    private SliceItem mRowSlice;
    private SliceItem mStartItem;
    private SliceItem mSubtitleItem;
    private SliceItem mSummaryItem;
    private SliceItem mTitleItem;
    private ArrayList<SliceAction> mToggleItems = new ArrayList();

    public RowContent(Context context, SliceItem rowSlice, boolean isHeader) {
        populate(rowSlice, isHeader);
        if (context != null) {
            this.mMaxHeight = context.getResources().getDimensionPixelSize(R.dimen.abc_slice_row_max_height);
            this.mMinHeight = context.getResources().getDimensionPixelSize(R.dimen.abc_slice_row_min_height);
            this.mRangeHeight = context.getResources().getDimensionPixelSize(R.dimen.abc_slice_row_range_height);
        }
    }

    private boolean populate(SliceItem rowSlice, boolean isHeader) {
        this.mIsHeader = isHeader;
        this.mRowSlice = rowSlice;
        if (isValidRow(rowSlice)) {
            determineStartAndPrimaryAction(rowSlice);
            this.mContentDescr = SliceQuery.findSubtype(rowSlice, "text", "content_description");
            ArrayList<SliceItem> rowItems = filterInvalidItems(rowSlice);
            if (rowItems.size() == 1 && (("action".equals(((SliceItem) rowItems.get(0)).getFormat()) || "slice".equals(((SliceItem) rowItems.get(0)).getFormat())) && !((SliceItem) rowItems.get(0)).hasAnyHints(UserDictionaryAddWordContents.EXTRA_SHORTCUT, "title") && isValidRow((SliceItem) rowItems.get(0)))) {
                rowSlice = (SliceItem) rowItems.get(0);
                rowItems = filterInvalidItems(rowSlice);
            }
            SliceItem sliceItem = null;
            this.mLayoutDirItem = SliceQuery.findTopLevelItem(rowSlice.getSlice(), "int", "layout_direction", null, null);
            if (this.mLayoutDirItem != null) {
                if (SliceViewUtil.resolveLayoutDirection(this.mLayoutDirItem.getInt()) != -1) {
                    sliceItem = this.mLayoutDirItem;
                }
                this.mLayoutDirItem = sliceItem;
            }
            if ("range".equals(rowSlice.getSubType())) {
                this.mRange = rowSlice;
            }
            if (rowItems.size() > 0) {
                int i;
                if (this.mStartItem != null) {
                    rowItems.remove(this.mStartItem);
                }
                if (this.mPrimaryAction != null) {
                    rowItems.remove(this.mPrimaryAction);
                }
                ArrayList<SliceItem> endItems = new ArrayList();
                for (i = 0; i < rowItems.size(); i++) {
                    SliceItem item = (SliceItem) rowItems.get(i);
                    if (!"text".equals(item.getFormat())) {
                        endItems.add(item);
                    } else if ((this.mTitleItem == null || !this.mTitleItem.hasHint("title")) && item.hasHint("title") && !item.hasHint("summary")) {
                        this.mTitleItem = item;
                    } else if (this.mSubtitleItem == null && !item.hasHint("summary")) {
                        this.mSubtitleItem = item;
                    } else if (this.mSummaryItem == null && item.hasHint("summary")) {
                        this.mSummaryItem = item;
                    }
                }
                if (hasText(this.mTitleItem)) {
                    this.mLineCount++;
                }
                if (hasText(this.mSubtitleItem)) {
                    this.mLineCount++;
                }
                boolean hasTimestamp = this.mStartItem != null && "long".equals(this.mStartItem.getFormat());
                boolean hasTimestamp2 = hasTimestamp;
                for (i = 0; i < endItems.size(); i++) {
                    sliceItem = (SliceItem) endItems.get(i);
                    boolean isAction = SliceQuery.find(sliceItem, "action") != null;
                    if (!"long".equals(sliceItem.getFormat())) {
                        processContent(sliceItem, isAction);
                    } else if (!hasTimestamp2) {
                        hasTimestamp2 = true;
                        this.mEndItems.add(sliceItem);
                    }
                }
            }
            return isValid();
        }
        Log.w(TAG, "Provided SliceItem is invalid for RowContent");
        return false;
    }

    private void processContent(@NonNull SliceItem item, boolean isAction) {
        if (isAction) {
            SliceAction ac = new SliceActionImpl(item);
            if (ac.isToggle()) {
                this.mToggleItems.add(ac);
            }
        }
        this.mEndItems.add(item);
        this.mEndItemsContainAction |= isAction;
    }

    private void determineStartAndPrimaryAction(@NonNull SliceItem rowSlice) {
        List<SliceItem> possibleStartItems = SliceQuery.findAll(rowSlice, null, "title", null);
        if (possibleStartItems.size() > 0) {
            String format = ((SliceItem) possibleStartItems.get(0)).getFormat();
            if (("action".equals(format) && SliceQuery.find((SliceItem) possibleStartItems.get(0), "image") != null) || "slice".equals(format) || "long".equals(format) || "image".equals(format)) {
                this.mStartItem = (SliceItem) possibleStartItems.get(0);
            }
        }
        List<SliceItem> possiblePrimaries = SliceQuery.findAll(rowSlice, "slice", new String[]{UserDictionaryAddWordContents.EXTRA_SHORTCUT, "title"}, null);
        if (possiblePrimaries.isEmpty() && "action".equals(rowSlice.getFormat()) && rowSlice.getSlice().getItems().size() == 1) {
            this.mPrimaryAction = rowSlice;
        } else if (this.mStartItem != null && possiblePrimaries.size() > 1 && possiblePrimaries.get(0) == this.mStartItem) {
            this.mPrimaryAction = (SliceItem) possiblePrimaries.get(1);
        } else if (possiblePrimaries.size() > 0) {
            this.mPrimaryAction = (SliceItem) possiblePrimaries.get(0);
        }
    }

    @NonNull
    public SliceItem getSlice() {
        return this.mRowSlice;
    }

    @Nullable
    public SliceItem getLayoutDirItem() {
        return this.mLayoutDirItem;
    }

    @Nullable
    public SliceItem getRange() {
        return this.mRange;
    }

    @Nullable
    public SliceItem getInputRangeThumb() {
        if (this.mRange != null) {
            List<SliceItem> items = this.mRange.getSlice().getItems();
            for (int i = 0; i < items.size(); i++) {
                if ("image".equals(((SliceItem) items.get(i)).getFormat())) {
                    return (SliceItem) items.get(i);
                }
            }
        }
        return null;
    }

    @Nullable
    public SliceItem getPrimaryAction() {
        return this.mPrimaryAction;
    }

    @Nullable
    public SliceItem getStartItem() {
        return this.mIsHeader ? null : this.mStartItem;
    }

    @Nullable
    public SliceItem getTitleItem() {
        return this.mTitleItem;
    }

    @Nullable
    public SliceItem getSubtitleItem() {
        return this.mSubtitleItem;
    }

    @Nullable
    public SliceItem getSummaryItem() {
        return this.mSummaryItem == null ? this.mSubtitleItem : this.mSummaryItem;
    }

    public ArrayList<SliceItem> getEndItems() {
        return this.mEndItems;
    }

    public ArrayList<SliceAction> getToggleItems() {
        return this.mToggleItems;
    }

    @Nullable
    public CharSequence getContentDescription() {
        return this.mContentDescr != null ? this.mContentDescr.getText() : null;
    }

    public boolean endItemsContainAction() {
        return this.mEndItemsContainAction;
    }

    public int getLineCount() {
        return this.mLineCount;
    }

    public int getSmallHeight() {
        return getRange() != null ? getActualHeight() : this.mMaxHeight;
    }

    public int getActualHeight() {
        if (!isValid()) {
            return 0;
        }
        int rowHeight = (getLineCount() > 1 || this.mIsHeader) ? this.mMaxHeight : this.mMinHeight;
        if (getRange() != null) {
            if (getLineCount() > 0) {
                rowHeight += this.mRangeHeight;
            } else {
                rowHeight = this.mIsHeader ? this.mMaxHeight : this.mRangeHeight;
            }
        }
        return rowHeight;
    }

    private static boolean hasText(SliceItem textSlice) {
        return textSlice != null && (textSlice.hasHint("partial") || !TextUtils.isEmpty(textSlice.getText()));
    }

    public boolean isDefaultSeeMore() {
        return "action".equals(this.mRowSlice.getFormat()) && this.mRowSlice.getSlice().hasHint("see_more") && this.mRowSlice.getSlice().getItems().isEmpty();
    }

    public boolean isValid() {
        return (this.mStartItem == null && this.mPrimaryAction == null && this.mTitleItem == null && this.mSubtitleItem == null && this.mEndItems.size() <= 0 && this.mRange == null && !isDefaultSeeMore()) ? false : true;
    }

    private static boolean isValidRow(SliceItem rowSlice) {
        if (rowSlice == null) {
            return false;
        }
        if ("slice".equals(rowSlice.getFormat()) || "action".equals(rowSlice.getFormat())) {
            List<SliceItem> rowItems = rowSlice.getSlice().getItems();
            if (rowSlice.hasHint("see_more") && rowItems.isEmpty()) {
                return true;
            }
            for (int i = 0; i < rowItems.size(); i++) {
                if (isValidRowContent(rowSlice, (SliceItem) rowItems.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static ArrayList<SliceItem> filterInvalidItems(SliceItem rowSlice) {
        ArrayList<SliceItem> filteredList = new ArrayList();
        for (SliceItem i : rowSlice.getSlice().getItems()) {
            if (isValidRowContent(rowSlice, i)) {
                filteredList.add(i);
            }
        }
        return filteredList;
    }

    private static boolean isValidRowContent(SliceItem slice, SliceItem item) {
        boolean z = false;
        if (item.hasAnyHints("keywords", SliceHints.HINT_TTL, SliceHints.HINT_LAST_UPDATED, "horizontal") || "content_description".equals(item.getSubType())) {
            return false;
        }
        String itemFormat = item.getFormat();
        if ("image".equals(itemFormat) || "text".equals(itemFormat) || "long".equals(itemFormat) || "action".equals(itemFormat) || "input".equals(itemFormat) || "slice".equals(itemFormat) || ("int".equals(itemFormat) && "range".equals(slice.getSubType()))) {
            z = true;
        }
        return z;
    }
}
