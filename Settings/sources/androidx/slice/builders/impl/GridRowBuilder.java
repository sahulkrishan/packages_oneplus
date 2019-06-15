package androidx.slice.builders.impl;

import android.app.PendingIntent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import androidx.slice.builders.SliceAction;

@RestrictTo({Scope.LIBRARY})
public interface GridRowBuilder {

    public interface CellBuilder {
        @NonNull
        void addImage(@NonNull IconCompat iconCompat, int i);

        @NonNull
        void addImage(@NonNull IconCompat iconCompat, int i, boolean z);

        @NonNull
        void addText(@NonNull CharSequence charSequence);

        @NonNull
        void addText(@Nullable CharSequence charSequence, boolean z);

        @NonNull
        void addTitleText(@NonNull CharSequence charSequence);

        @NonNull
        void addTitleText(@Nullable CharSequence charSequence, boolean z);

        void setContentDescription(CharSequence charSequence);

        @NonNull
        void setContentIntent(@NonNull PendingIntent pendingIntent);
    }

    void addCell(TemplateBuilderImpl templateBuilderImpl);

    TemplateBuilderImpl createGridRowBuilder();

    TemplateBuilderImpl createGridRowBuilder(Uri uri);

    void setContentDescription(CharSequence charSequence);

    void setLayoutDirection(int i);

    void setPrimaryAction(SliceAction sliceAction);

    void setSeeMoreAction(PendingIntent pendingIntent);

    void setSeeMoreCell(TemplateBuilderImpl templateBuilderImpl);
}
