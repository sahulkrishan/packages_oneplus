package androidx.slice.builders.impl;

import android.app.PendingIntent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import androidx.slice.Slice.Builder;
import androidx.slice.builders.SliceAction;

@RestrictTo({Scope.LIBRARY})
public class GridRowBuilderBasicImpl extends TemplateBuilderImpl implements GridRowBuilder {

    public static final class CellBuilder extends TemplateBuilderImpl implements androidx.slice.builders.impl.GridRowBuilder.CellBuilder {
        public CellBuilder(@NonNull GridRowBuilderBasicImpl parent) {
            super(parent.createChildBuilder(), null);
        }

        public CellBuilder(@NonNull Uri uri) {
            super(new Builder(uri), null);
        }

        @NonNull
        public void addText(@NonNull CharSequence text) {
        }

        public void addText(@Nullable CharSequence text, boolean isLoading) {
        }

        @NonNull
        public void addTitleText(@NonNull CharSequence text) {
        }

        @NonNull
        public void addTitleText(@Nullable CharSequence text, boolean isLoading) {
        }

        @NonNull
        public void addImage(@NonNull IconCompat image, int imageMode) {
        }

        @NonNull
        public void addImage(@Nullable IconCompat image, int imageMode, boolean isLoading) {
        }

        @NonNull
        public void setContentIntent(@NonNull PendingIntent intent) {
        }

        public void setContentDescription(CharSequence description) {
        }

        public void apply(Builder builder) {
        }
    }

    public GridRowBuilderBasicImpl(@NonNull ListBuilderBasicImpl parent) {
        super(parent.createChildBuilder(), null);
    }

    public TemplateBuilderImpl createGridRowBuilder() {
        return new CellBuilder(this);
    }

    public TemplateBuilderImpl createGridRowBuilder(Uri uri) {
        return new CellBuilder(uri);
    }

    public void addCell(TemplateBuilderImpl impl) {
    }

    public void setSeeMoreCell(TemplateBuilderImpl impl) {
    }

    public void setSeeMoreAction(PendingIntent intent) {
    }

    public void setPrimaryAction(SliceAction action) {
    }

    public void setContentDescription(CharSequence description) {
    }

    public void setLayoutDirection(int layoutDirection) {
    }

    public void apply(Builder builder) {
    }
}
