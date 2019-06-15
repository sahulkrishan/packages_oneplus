package androidx.slice.builders.impl;

import android.app.PendingIntent;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import androidx.slice.builders.SliceAction;
import java.util.List;

@RestrictTo({Scope.LIBRARY})
public interface ListBuilder {

    public interface HeaderBuilder {
        void setContentDescription(CharSequence charSequence);

        void setLayoutDirection(int i);

        void setPrimaryAction(SliceAction sliceAction);

        void setSubtitle(CharSequence charSequence, boolean z);

        void setSummary(CharSequence charSequence, boolean z);

        void setTitle(CharSequence charSequence, boolean z);
    }

    public interface RangeBuilder {
        void setContentDescription(CharSequence charSequence);

        void setLayoutDirection(int i);

        void setMax(int i);

        void setMin(int i);

        void setPrimaryAction(@NonNull SliceAction sliceAction);

        void setSubtitle(@NonNull CharSequence charSequence);

        void setTitle(@NonNull CharSequence charSequence);

        void setValue(int i);
    }

    public interface RowBuilder {
        void addEndItem(long j);

        void addEndItem(IconCompat iconCompat, int i);

        void addEndItem(IconCompat iconCompat, int i, boolean z);

        void addEndItem(SliceAction sliceAction);

        void addEndItem(SliceAction sliceAction, boolean z);

        void setContentDescription(CharSequence charSequence);

        void setLayoutDirection(int i);

        void setPrimaryAction(SliceAction sliceAction);

        void setSubtitle(CharSequence charSequence);

        void setSubtitle(CharSequence charSequence, boolean z);

        void setTitle(CharSequence charSequence);

        void setTitle(CharSequence charSequence, boolean z);

        void setTitleItem(long j);

        void setTitleItem(IconCompat iconCompat, int i);

        void setTitleItem(IconCompat iconCompat, int i, boolean z);

        void setTitleItem(SliceAction sliceAction);

        void setTitleItem(SliceAction sliceAction, boolean z);
    }

    public interface InputRangeBuilder extends RangeBuilder {
        void setInputAction(@NonNull PendingIntent pendingIntent);

        void setThumb(@NonNull IconCompat iconCompat);
    }

    void addAction(SliceAction sliceAction);

    void addGridRow(TemplateBuilderImpl templateBuilderImpl);

    void addInputRange(TemplateBuilderImpl templateBuilderImpl);

    void addRange(TemplateBuilderImpl templateBuilderImpl);

    void addRow(TemplateBuilderImpl templateBuilderImpl);

    TemplateBuilderImpl createGridBuilder();

    TemplateBuilderImpl createHeaderBuilder();

    TemplateBuilderImpl createHeaderBuilder(Uri uri);

    TemplateBuilderImpl createInputRangeBuilder();

    TemplateBuilderImpl createRangeBuilder();

    TemplateBuilderImpl createRowBuilder();

    TemplateBuilderImpl createRowBuilder(Uri uri);

    void setColor(@ColorInt int i);

    void setHeader(TemplateBuilderImpl templateBuilderImpl);

    void setIsError(boolean z);

    void setKeywords(List<String> list);

    void setLayoutDirection(int i);

    void setSeeMoreAction(PendingIntent pendingIntent);

    void setSeeMoreRow(TemplateBuilderImpl templateBuilderImpl);

    void setTtl(long j);
}
