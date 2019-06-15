package androidx.slice;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.math.MathUtils;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import androidx.slice.core.SliceAction;
import androidx.slice.core.SliceActionImpl;
import androidx.slice.core.SliceHints;
import androidx.slice.core.SliceQuery;
import androidx.slice.widget.GridContent;
import androidx.slice.widget.ListContent;
import androidx.slice.widget.RowContent;
import com.android.settings.inputmethod.UserDictionaryAddWordContents;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class SliceMetadata {
    public static final int LOADED_ALL = 2;
    public static final int LOADED_NONE = 0;
    public static final int LOADED_PARTIAL = 1;
    private Context mContext;
    private long mExpiry;
    private SliceItem mHeaderItem;
    private long mLastUpdated;
    private ListContent mListContent;
    private SliceActionImpl mPrimaryAction;
    private Slice mSlice;
    private List<SliceAction> mSliceActions;
    private int mTemplateType;

    @RestrictTo({Scope.LIBRARY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SliceLoadingState {
    }

    public static SliceMetadata from(@NonNull Context context, @NonNull Slice slice) {
        return new SliceMetadata(context, slice);
    }

    private SliceMetadata(@NonNull Context context, @NonNull Slice slice) {
        this.mSlice = slice;
        this.mContext = context;
        SliceItem ttlItem = SliceQuery.find(slice, "long", SliceHints.HINT_TTL, null);
        if (ttlItem != null) {
            this.mExpiry = ttlItem.getTimestamp();
        }
        SliceItem updatedItem = SliceQuery.find(slice, "long", SliceHints.HINT_LAST_UPDATED, null);
        if (updatedItem != null) {
            this.mLastUpdated = updatedItem.getTimestamp();
        }
        this.mListContent = new ListContent(context, slice, null, 0, 0);
        this.mHeaderItem = this.mListContent.getHeaderItem();
        this.mTemplateType = this.mListContent.getHeaderTemplateType();
        this.mSliceActions = this.mListContent.getSliceActions();
        SliceItem action = this.mListContent.getPrimaryAction();
        if (action != null) {
            this.mPrimaryAction = new SliceActionImpl(action);
        }
    }

    @Nullable
    public CharSequence getTitle() {
        CharSequence title = null;
        if (this.mHeaderItem != null) {
            if (this.mHeaderItem.hasHint("horizontal")) {
                title = new GridContent(this.mContext, this.mHeaderItem).getTitle();
            } else {
                RowContent rc = new RowContent(this.mContext, this.mHeaderItem, true);
                if (rc.getTitleItem() != null) {
                    title = rc.getTitleItem().getText();
                }
            }
        }
        if (!TextUtils.isEmpty(title) || this.mPrimaryAction == null) {
            return title;
        }
        return this.mPrimaryAction.getTitle();
    }

    @Nullable
    public CharSequence getSubtitle() {
        if (!(this.mHeaderItem == null || this.mHeaderItem.hasHint("horizontal"))) {
            RowContent rc = new RowContent(this.mContext, this.mHeaderItem, true);
            if (rc.getSubtitleItem() != null) {
                return rc.getSubtitleItem().getText();
            }
        }
        return null;
    }

    @Nullable
    public List<SliceAction> getSliceActions() {
        return this.mSliceActions;
    }

    @Nullable
    public SliceAction getPrimaryAction() {
        return this.mPrimaryAction;
    }

    public int getHeaderType() {
        return this.mTemplateType;
    }

    public boolean hasLargeMode() {
        boolean isHeaderFullGrid = false;
        if (this.mHeaderItem != null && this.mHeaderItem.hasHint("horizontal")) {
            GridContent gc = new GridContent(this.mContext, this.mHeaderItem);
            boolean z = gc.hasImage() && gc.getMaxCellLineCount() > 1;
            isHeaderFullGrid = z;
        }
        if (this.mListContent.getRowItems().size() > 1 || isHeaderFullGrid) {
            return true;
        }
        return false;
    }

    public List<SliceAction> getToggles() {
        List<SliceAction> toggles = new ArrayList();
        if (this.mPrimaryAction != null && this.mPrimaryAction.isToggle()) {
            toggles.add(this.mPrimaryAction);
            return toggles;
        } else if (this.mSliceActions == null || this.mSliceActions.size() <= 0) {
            return new RowContent(this.mContext, this.mHeaderItem, true).getToggleItems();
        } else {
            for (int i = 0; i < this.mSliceActions.size(); i++) {
                SliceAction action = (SliceAction) this.mSliceActions.get(i);
                if (action.isToggle()) {
                    toggles.add(action);
                }
            }
            return toggles;
        }
    }

    public boolean sendToggleAction(SliceAction toggleAction, boolean toggleValue) throws CanceledException {
        if (toggleAction == null) {
            return false;
        }
        toggleAction.getAction().send(this.mContext, 0, new Intent().putExtra("android.app.slice.extra.TOGGLE_STATE", toggleValue), null, null);
        return true;
    }

    @Nullable
    public PendingIntent getInputRangeAction() {
        if (this.mTemplateType == 4) {
            SliceItem range = new RowContent(this.mContext, this.mHeaderItem, true).getRange();
            if (range != null) {
                return range.getAction();
            }
        }
        return null;
    }

    public boolean sendInputRangeAction(int newValue) throws CanceledException {
        if (this.mTemplateType == 4) {
            SliceItem range = new RowContent(this.mContext, this.mHeaderItem, true).getRange();
            if (range != null) {
                Pair<Integer, Integer> validRange = getRange();
                range.fireAction(this.mContext, new Intent().putExtra("android.app.slice.extra.RANGE_VALUE", MathUtils.clamp(newValue, ((Integer) validRange.first).intValue(), ((Integer) validRange.second).intValue())));
                return true;
            }
        }
        return false;
    }

    @Nullable
    public Pair<Integer, Integer> getRange() {
        if (this.mTemplateType != 4 && this.mTemplateType != 5) {
            return null;
        }
        SliceItem range = new RowContent(this.mContext, this.mHeaderItem, true).getRange();
        SliceItem maxItem = SliceQuery.findSubtype(range, "int", "max");
        SliceItem minItem = SliceQuery.findSubtype(range, "int", SliceHints.SUBTYPE_MIN);
        return new Pair(Integer.valueOf(minItem != null ? minItem.getInt() : 0), Integer.valueOf(maxItem != null ? maxItem.getInt() : 100));
    }

    @NonNull
    public int getRangeValue() {
        int i = -1;
        if (this.mTemplateType != 4 && this.mTemplateType != 5) {
            return -1;
        }
        SliceItem currentItem = SliceQuery.findSubtype(new RowContent(this.mContext, this.mHeaderItem, true).getRange(), "int", "value");
        if (currentItem != null) {
            i = currentItem.getInt();
        }
        return i;
    }

    @Nullable
    public List<String> getSliceKeywords() {
        SliceItem keywordGroup = SliceQuery.find(this.mSlice, "slice", "keywords", null);
        if (keywordGroup != null) {
            List<SliceItem> itemList = SliceQuery.findAll(keywordGroup, "text");
            if (itemList != null) {
                ArrayList<String> stringList = new ArrayList();
                for (int i = 0; i < itemList.size(); i++) {
                    String keyword = (String) ((SliceItem) itemList.get(i)).getText();
                    if (!TextUtils.isEmpty(keyword)) {
                        stringList.add(keyword);
                    }
                }
                return stringList;
            }
        }
        return null;
    }

    public int getLoadingState() {
        boolean hasHintPartial = SliceQuery.find(this.mSlice, null, "partial", null) != null;
        if (!this.mListContent.isValid()) {
            return 0;
        }
        if (hasHintPartial) {
            return 1;
        }
        return 2;
    }

    public long getExpiry() {
        return this.mExpiry;
    }

    public long getLastUpdatedTime() {
        return this.mLastUpdated;
    }

    public boolean isPermissionSlice() {
        return this.mSlice.hasHint(SliceHints.HINT_PERMISSION_REQUEST);
    }

    public boolean isErrorSlice() {
        return this.mSlice.hasHint("error");
    }

    @Nullable
    @RestrictTo({Scope.LIBRARY})
    public static List<SliceAction> getSliceActions(@NonNull Slice slice) {
        SliceItem actionGroup = SliceQuery.find(slice, "slice", "actions", null);
        List<SliceItem> items = actionGroup != null ? SliceQuery.findAll(actionGroup, "slice", new String[]{"actions", UserDictionaryAddWordContents.EXTRA_SHORTCUT}, null) : null;
        if (items == null) {
            return null;
        }
        List<SliceAction> actions = new ArrayList(items.size());
        for (int i = 0; i < items.size(); i++) {
            actions.add(new SliceActionImpl((SliceItem) items.get(i)));
        }
        return actions;
    }
}
