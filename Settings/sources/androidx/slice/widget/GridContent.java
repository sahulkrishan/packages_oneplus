package androidx.slice.widget;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import androidx.slice.SliceItem;
import androidx.slice.core.SliceActionImpl;
import androidx.slice.core.SliceHints;
import androidx.slice.core.SliceQuery;
import androidx.slice.view.R;
import com.android.settings.inputmethod.UserDictionaryAddWordContents;
import java.util.ArrayList;
import java.util.List;

@RestrictTo({Scope.LIBRARY_GROUP})
public class GridContent {
    private boolean mAllImages;
    private int mAllImagesHeight;
    private int mBigPicMaxHeight;
    private int mBigPicMinHeight;
    private SliceItem mColorItem;
    private SliceItem mContentDescr;
    private ArrayList<CellContent> mGridContent = new ArrayList();
    private boolean mHasImage;
    private int mImageTextHeight;
    private int mLargestImageMode = -1;
    private SliceItem mLayoutDirItem;
    private int mMaxCellLineCount;
    private int mMaxHeight;
    private int mMinHeight;
    private SliceItem mPrimaryAction;
    private SliceItem mSeeMoreItem;
    private SliceItem mTitleItem;

    @RestrictTo({Scope.LIBRARY_GROUP})
    public static class CellContent {
        private ArrayList<SliceItem> mCellItems = new ArrayList();
        private SliceItem mContentDescr;
        private SliceItem mContentIntent;
        private boolean mHasImage;
        private int mImageMode = -1;
        private int mTextCount;
        private SliceItem mTitleItem;

        public CellContent(SliceItem cellItem) {
            populate(cellItem);
        }

        public boolean populate(SliceItem cellItem) {
            String format = cellItem.getFormat();
            if (!cellItem.hasHint(UserDictionaryAddWordContents.EXTRA_SHORTCUT) && ("slice".equals(format) || "action".equals(format))) {
                List<SliceItem> items = cellItem.getSlice().getItems();
                if (items.size() == 1 && ("action".equals(((SliceItem) items.get(0)).getFormat()) || "slice".equals(((SliceItem) items.get(0)).getFormat()))) {
                    this.mContentIntent = (SliceItem) items.get(0);
                    items = ((SliceItem) items.get(0)).getSlice().getItems();
                }
                if ("action".equals(format)) {
                    this.mContentIntent = cellItem;
                }
                this.mTextCount = 0;
                int imageCount = 0;
                for (int i = 0; i < items.size(); i++) {
                    SliceItem item = (SliceItem) items.get(i);
                    String itemFormat = item.getFormat();
                    if ("content_description".equals(item.getSubType())) {
                        this.mContentDescr = item;
                    } else {
                        int i2 = 2;
                        if (this.mTextCount < 2 && ("text".equals(itemFormat) || "long".equals(itemFormat))) {
                            this.mTextCount++;
                            this.mCellItems.add(item);
                            if (this.mTitleItem == null || (!this.mTitleItem.hasHint("title") && item.hasHint("title"))) {
                                this.mTitleItem = item;
                            }
                        } else if (imageCount < 1 && "image".equals(item.getFormat())) {
                            if (item.hasHint("no_tint")) {
                                if (!item.hasHint("large")) {
                                    i2 = 1;
                                }
                                this.mImageMode = i2;
                            } else {
                                this.mImageMode = 0;
                            }
                            imageCount++;
                            this.mHasImage = true;
                            this.mCellItems.add(item);
                        }
                    }
                }
            } else if (isValidCellContent(cellItem)) {
                this.mCellItems.add(cellItem);
            }
            return isValid();
        }

        @Nullable
        public SliceItem getTitleItem() {
            return this.mTitleItem;
        }

        public SliceItem getContentIntent() {
            return this.mContentIntent;
        }

        public ArrayList<SliceItem> getCellItems() {
            return this.mCellItems;
        }

        private boolean isValidCellContent(SliceItem cellItem) {
            String format = cellItem.getFormat();
            boolean isNonCellContent = "content_description".equals(cellItem.getSubType()) || cellItem.hasAnyHints("keywords", SliceHints.HINT_TTL, SliceHints.HINT_LAST_UPDATED);
            if (isNonCellContent || (!"text".equals(format) && !"long".equals(format) && !"image".equals(format))) {
                return false;
            }
            return true;
        }

        public boolean isValid() {
            return this.mCellItems.size() > 0 && this.mCellItems.size() <= 3;
        }

        public boolean isImageOnly() {
            return this.mCellItems.size() == 1 && "image".equals(((SliceItem) this.mCellItems.get(0)).getFormat());
        }

        public int getTextCount() {
            return this.mTextCount;
        }

        public boolean hasImage() {
            return this.mHasImage;
        }

        public int getImageMode() {
            return this.mImageMode;
        }

        @Nullable
        public CharSequence getContentDescription() {
            return this.mContentDescr != null ? this.mContentDescr.getText() : null;
        }
    }

    public GridContent(Context context, SliceItem gridItem) {
        populate(gridItem);
        if (context != null) {
            Resources res = context.getResources();
            this.mBigPicMinHeight = res.getDimensionPixelSize(R.dimen.abc_slice_big_pic_min_height);
            this.mBigPicMaxHeight = res.getDimensionPixelSize(R.dimen.abc_slice_big_pic_max_height);
            this.mAllImagesHeight = res.getDimensionPixelSize(R.dimen.abc_slice_grid_image_only_height);
            this.mImageTextHeight = res.getDimensionPixelSize(R.dimen.abc_slice_grid_image_text_height);
            this.mMinHeight = res.getDimensionPixelSize(R.dimen.abc_slice_grid_min_height);
            this.mMaxHeight = res.getDimensionPixelSize(R.dimen.abc_slice_grid_max_height);
        }
    }

    private boolean populate(SliceItem gridItem) {
        this.mColorItem = SliceQuery.findSubtype(gridItem, "int", "color");
        if ("slice".equals(gridItem.getFormat()) || "action".equals(gridItem.getFormat())) {
            this.mLayoutDirItem = SliceQuery.findTopLevelItem(gridItem.getSlice(), "int", "layout_direction", null, null);
            if (this.mLayoutDirItem != null) {
                this.mLayoutDirItem = SliceViewUtil.resolveLayoutDirection(this.mLayoutDirItem.getInt()) != -1 ? this.mLayoutDirItem : null;
            }
        }
        this.mSeeMoreItem = SliceQuery.find(gridItem, null, "see_more", null);
        int i = 0;
        if (this.mSeeMoreItem != null && "slice".equals(this.mSeeMoreItem.getFormat())) {
            this.mSeeMoreItem = (SliceItem) this.mSeeMoreItem.getSlice().getItems().get(0);
        }
        this.mPrimaryAction = SliceQuery.find(gridItem, "slice", new String[]{UserDictionaryAddWordContents.EXTRA_SHORTCUT, "title"}, new String[]{"actions"});
        this.mAllImages = true;
        if ("slice".equals(gridItem.getFormat())) {
            List<SliceItem> items = gridItem.getSlice().getItems();
            if (items.size() == 1 && "slice".equals(((SliceItem) items.get(0)).getFormat())) {
                items = ((SliceItem) items.get(0)).getSlice().getItems();
            }
            items = filterAndProcessItems(items);
            if (items.size() == 1 && ((SliceItem) items.get(0)).getFormat().equals("slice")) {
                items = ((SliceItem) items.get(0)).getSlice().getItems();
            }
            while (i < items.size()) {
                SliceItem item = (SliceItem) items.get(i);
                if ("content_description".equals(item.getSubType())) {
                    this.mContentDescr = item;
                } else {
                    processContent(new CellContent(item));
                }
                i++;
            }
        } else {
            processContent(new CellContent(gridItem));
        }
        return isValid();
    }

    private void processContent(CellContent cc) {
        if (cc.isValid()) {
            if (this.mTitleItem == null && cc.getTitleItem() != null) {
                this.mTitleItem = cc.getTitleItem();
            }
            this.mGridContent.add(cc);
            if (!cc.isImageOnly()) {
                this.mAllImages = false;
            }
            this.mMaxCellLineCount = Math.max(this.mMaxCellLineCount, cc.getTextCount());
            this.mHasImage |= cc.hasImage();
            this.mLargestImageMode = Math.max(this.mLargestImageMode, cc.getImageMode());
        }
    }

    @Nullable
    public CharSequence getTitle() {
        if (this.mTitleItem != null) {
            return this.mTitleItem.getText();
        }
        if (this.mPrimaryAction != null) {
            return new SliceActionImpl(this.mPrimaryAction).getTitle();
        }
        return null;
    }

    @NonNull
    public ArrayList<CellContent> getGridContent() {
        return this.mGridContent;
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
    public SliceItem getContentIntent() {
        return this.mPrimaryAction;
    }

    @Nullable
    public SliceItem getSeeMoreItem() {
        return this.mSeeMoreItem;
    }

    @Nullable
    public CharSequence getContentDescription() {
        return this.mContentDescr != null ? this.mContentDescr.getText() : null;
    }

    public boolean isValid() {
        return this.mGridContent.size() > 0;
    }

    public boolean isAllImages() {
        return this.mAllImages;
    }

    public int getLargestImageMode() {
        return this.mLargestImageMode;
    }

    private List<SliceItem> filterAndProcessItems(List<SliceItem> items) {
        List<SliceItem> filteredItems = new ArrayList();
        for (int i = 0; i < items.size(); i++) {
            SliceItem item = (SliceItem) items.get(i);
            boolean isNonCellContent = true;
            if (!((SliceQuery.find(item, null, "see_more", null) != null) || item.hasAnyHints(UserDictionaryAddWordContents.EXTRA_SHORTCUT, "see_more", "keywords", SliceHints.HINT_TTL, SliceHints.HINT_LAST_UPDATED))) {
                isNonCellContent = false;
            }
            if ("content_description".equals(item.getSubType())) {
                this.mContentDescr = item;
            } else if (!isNonCellContent) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }

    public int getMaxCellLineCount() {
        return this.mMaxCellLineCount;
    }

    public boolean hasImage() {
        return this.mHasImage;
    }

    public int getSmallHeight() {
        return getHeight(true);
    }

    public int getActualHeight() {
        return getHeight(false);
    }

    private int getHeight(boolean isSmall) {
        boolean z = false;
        if (!isValid()) {
            return 0;
        }
        if (this.mAllImages) {
            int i = this.mGridContent.size() == 1 ? isSmall ? this.mBigPicMinHeight : this.mBigPicMaxHeight : this.mLargestImageMode == 0 ? this.mMinHeight : this.mAllImagesHeight;
            return i;
        }
        int i2;
        if (getMaxCellLineCount() > 1) {
            z = true;
        }
        boolean twoLines = z;
        z = hasImage();
        if (!twoLines || isSmall) {
            if (this.mLargestImageMode != 0) {
                i2 = this.mImageTextHeight;
                return i2;
            }
        } else if (z) {
            i2 = this.mMaxHeight;
            return i2;
        }
        i2 = this.mMinHeight;
        return i2;
    }
}
