package androidx.slice.builders;

import android.app.PendingIntent;
import android.graphics.drawable.Icon;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import androidx.slice.Slice;
import androidx.slice.Slice.Builder;
import androidx.slice.core.SliceActionImpl;

public class SliceAction implements androidx.slice.core.SliceAction {
    private SliceActionImpl mSliceAction;

    @RequiresApi(23)
    @Deprecated
    public SliceAction(@NonNull PendingIntent action, @NonNull Icon actionIcon, @NonNull CharSequence actionTitle) {
        this(action, actionIcon, 0, actionTitle);
    }

    @RequiresApi(23)
    @Deprecated
    public SliceAction(@NonNull PendingIntent action, @NonNull Icon actionIcon, int imageMode, @NonNull CharSequence actionTitle) {
        this(action, IconCompat.createFromIcon(actionIcon), imageMode, actionTitle);
    }

    @RequiresApi(23)
    @Deprecated
    public SliceAction(@NonNull PendingIntent action, @NonNull Icon actionIcon, @NonNull CharSequence actionTitle, boolean isChecked) {
        this(action, IconCompat.createFromIcon(actionIcon), actionTitle, isChecked);
    }

    public SliceAction(@NonNull PendingIntent action, @NonNull IconCompat actionIcon, @NonNull CharSequence actionTitle) {
        this(action, actionIcon, 0, actionTitle);
    }

    public SliceAction(@NonNull PendingIntent action, @NonNull IconCompat actionIcon, int imageMode, @NonNull CharSequence actionTitle) {
        this.mSliceAction = new SliceActionImpl(action, actionIcon, imageMode, actionTitle);
    }

    public SliceAction(@NonNull PendingIntent action, @NonNull IconCompat actionIcon, @NonNull CharSequence actionTitle, boolean isChecked) {
        this.mSliceAction = new SliceActionImpl(action, actionIcon, actionTitle, isChecked);
    }

    public SliceAction(@NonNull PendingIntent action, @NonNull CharSequence actionTitle, boolean isChecked) {
        this.mSliceAction = new SliceActionImpl(action, actionTitle, isChecked);
    }

    @NonNull
    public SliceAction setContentDescription(@NonNull CharSequence description) {
        this.mSliceAction.setContentDescription(description);
        return this;
    }

    @NonNull
    public SliceAction setChecked(boolean isChecked) {
        this.mSliceAction.setChecked(isChecked);
        return this;
    }

    @NonNull
    public SliceAction setPriority(@IntRange(from = 0) int priority) {
        this.mSliceAction.setPriority(priority);
        return this;
    }

    @NonNull
    public PendingIntent getAction() {
        return this.mSliceAction.getAction();
    }

    @Nullable
    public IconCompat getIcon() {
        return this.mSliceAction.getIcon();
    }

    @NonNull
    public CharSequence getTitle() {
        return this.mSliceAction.getTitle();
    }

    @Nullable
    public CharSequence getContentDescription() {
        return this.mSliceAction.getContentDescription();
    }

    public int getPriority() {
        return this.mSliceAction.getPriority();
    }

    public boolean isToggle() {
        return this.mSliceAction.isToggle();
    }

    public boolean isChecked() {
        return this.mSliceAction.isChecked();
    }

    public int getImageMode() {
        return this.mSliceAction.getImageMode();
    }

    public boolean isDefaultToggle() {
        return this.mSliceAction.isDefaultToggle();
    }

    @RestrictTo({Scope.LIBRARY})
    @NonNull
    public Slice buildSlice(@NonNull Builder builder) {
        return this.mSliceAction.buildSlice(builder);
    }

    @RestrictTo({Scope.LIBRARY})
    @NonNull
    public SliceActionImpl getImpl() {
        return this.mSliceAction;
    }
}
