package androidx.slice.core;

import android.app.PendingIntent;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.IconCompat;

public interface SliceAction {
    @NonNull
    PendingIntent getAction();

    @Nullable
    CharSequence getContentDescription();

    @Nullable
    IconCompat getIcon();

    int getImageMode();

    int getPriority();

    @NonNull
    CharSequence getTitle();

    boolean isChecked();

    boolean isDefaultToggle();

    boolean isToggle();

    SliceAction setChecked(boolean z);

    @Nullable
    SliceAction setContentDescription(@NonNull CharSequence charSequence);

    SliceAction setPriority(@IntRange(from = 0) int i);
}
