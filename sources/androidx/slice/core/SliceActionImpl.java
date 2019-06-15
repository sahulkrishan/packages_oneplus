package androidx.slice.core;

import android.app.PendingIntent;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import androidx.slice.Slice;
import androidx.slice.Slice.Builder;
import androidx.slice.SliceItem;
import com.android.settings.inputmethod.UserDictionaryAddWordContents;

@RestrictTo({Scope.LIBRARY_GROUP})
public class SliceActionImpl implements SliceAction {
    private PendingIntent mAction;
    private SliceItem mActionItem;
    private CharSequence mContentDescription;
    private IconCompat mIcon;
    private int mImageMode;
    private boolean mIsChecked;
    private boolean mIsToggle;
    private int mPriority;
    private SliceItem mSliceItem;
    private CharSequence mTitle;

    public SliceActionImpl(@NonNull PendingIntent action, @NonNull IconCompat actionIcon, @NonNull CharSequence actionTitle) {
        this(action, actionIcon, 0, actionTitle);
    }

    public SliceActionImpl(@NonNull PendingIntent action, @NonNull IconCompat actionIcon, int imageMode, @NonNull CharSequence actionTitle) {
        this.mImageMode = 3;
        this.mPriority = -1;
        this.mAction = action;
        this.mIcon = actionIcon;
        this.mTitle = actionTitle;
        this.mImageMode = imageMode;
    }

    public SliceActionImpl(@NonNull PendingIntent action, @NonNull IconCompat actionIcon, @NonNull CharSequence actionTitle, boolean isChecked) {
        this(action, actionIcon, 0, actionTitle);
        this.mIsChecked = isChecked;
        this.mIsToggle = true;
    }

    public SliceActionImpl(@NonNull PendingIntent action, @NonNull CharSequence actionTitle, boolean isChecked) {
        this.mImageMode = 3;
        this.mPriority = -1;
        this.mAction = action;
        this.mTitle = actionTitle;
        this.mIsToggle = true;
        this.mIsChecked = isChecked;
    }

    @RestrictTo({Scope.LIBRARY})
    public SliceActionImpl(SliceItem slice) {
        this.mImageMode = 3;
        int i = -1;
        this.mPriority = -1;
        this.mSliceItem = slice;
        SliceItem actionItem = SliceQuery.find(slice, "action");
        if (actionItem != null) {
            this.mActionItem = actionItem;
            SliceItem iconItem = SliceQuery.find(actionItem.getSlice(), "image");
            if (iconItem != null) {
                this.mIcon = iconItem.getIcon();
                int i2 = iconItem.hasHint("no_tint") ? iconItem.hasHint("large") ? 2 : 1 : 0;
                this.mImageMode = i2;
            }
            SliceItem titleItem = SliceQuery.find(actionItem.getSlice(), "text", "title", null);
            if (titleItem != null) {
                this.mTitle = titleItem.getText();
            }
            SliceItem cdItem = SliceQuery.findSubtype(actionItem.getSlice(), "text", "content_description");
            if (cdItem != null) {
                this.mContentDescription = cdItem.getText();
            }
            this.mIsToggle = "toggle".equals(actionItem.getSubType());
            if (this.mIsToggle) {
                this.mIsChecked = actionItem.hasHint("selected");
            }
            SliceItem priority = SliceQuery.findSubtype(actionItem.getSlice(), "int", "priority");
            if (priority != null) {
                i = priority.getInt();
            }
            this.mPriority = i;
        }
    }

    @Nullable
    public SliceActionImpl setContentDescription(@NonNull CharSequence description) {
        this.mContentDescription = description;
        return this;
    }

    public SliceActionImpl setChecked(boolean isChecked) {
        this.mIsChecked = isChecked;
        return this;
    }

    public SliceActionImpl setPriority(@IntRange(from = 0) int priority) {
        this.mPriority = priority;
        return this;
    }

    @NonNull
    public PendingIntent getAction() {
        return this.mAction != null ? this.mAction : this.mActionItem.getAction();
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public SliceItem getActionItem() {
        return this.mActionItem;
    }

    @Nullable
    public IconCompat getIcon() {
        return this.mIcon;
    }

    @NonNull
    public CharSequence getTitle() {
        return this.mTitle;
    }

    @Nullable
    public CharSequence getContentDescription() {
        return this.mContentDescription;
    }

    public int getPriority() {
        return this.mPriority;
    }

    public boolean isToggle() {
        return this.mIsToggle;
    }

    public boolean isChecked() {
        return this.mIsChecked;
    }

    public int getImageMode() {
        return this.mImageMode;
    }

    public boolean isDefaultToggle() {
        return this.mIsToggle && this.mIcon == null;
    }

    @Nullable
    public SliceItem getSliceItem() {
        return this.mSliceItem;
    }

    @NonNull
    public Slice buildSlice(@NonNull Builder builder) {
        Builder sb = new Builder(builder);
        String str = null;
        if (this.mIcon != null) {
            sb.addIcon(this.mIcon, null, this.mImageMode == 0 ? new String[0] : new String[]{"no_tint"});
        }
        if (this.mTitle != null) {
            sb.addText(this.mTitle, null, "title");
        }
        if (this.mContentDescription != null) {
            sb.addText(this.mContentDescription, "content_description", new String[0]);
        }
        if (this.mIsToggle && this.mIsChecked) {
            sb.addHints("selected");
        }
        if (this.mPriority != -1) {
            sb.addInt(this.mPriority, "priority", new String[0]);
        }
        if (this.mIsToggle) {
            str = "toggle";
        }
        String subtype = str;
        builder.addHints(UserDictionaryAddWordContents.EXTRA_SHORTCUT);
        builder.addAction(this.mAction, sb.build(), subtype);
        return builder.build();
    }
}
