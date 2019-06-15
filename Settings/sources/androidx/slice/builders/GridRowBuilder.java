package androidx.slice.builders;

import android.app.PendingIntent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Consumer;
import androidx.slice.builders.impl.TemplateBuilderImpl;

public class GridRowBuilder extends TemplateSliceBuilder {
    private boolean mHasSeeMore;
    private androidx.slice.builders.impl.GridRowBuilder mImpl;

    public static class CellBuilder extends TemplateSliceBuilder {
        private androidx.slice.builders.impl.GridRowBuilder.CellBuilder mImpl;

        public CellBuilder(@NonNull GridRowBuilder parent) {
            super(parent.mImpl.createGridRowBuilder());
        }

        public CellBuilder(@NonNull GridRowBuilder parent, @NonNull Uri uri) {
            super(parent.mImpl.createGridRowBuilder(uri));
        }

        /* Access modifiers changed, original: 0000 */
        public void setImpl(TemplateBuilderImpl impl) {
            this.mImpl = (androidx.slice.builders.impl.GridRowBuilder.CellBuilder) impl;
        }

        @NonNull
        public CellBuilder addText(@NonNull CharSequence text) {
            return addText(text, false);
        }

        @NonNull
        public CellBuilder addText(@Nullable CharSequence text, boolean isLoading) {
            this.mImpl.addText(text, isLoading);
            return this;
        }

        @NonNull
        public CellBuilder addTitleText(@NonNull CharSequence text) {
            return addTitleText(text, false);
        }

        @NonNull
        public CellBuilder addTitleText(@Nullable CharSequence text, boolean isLoading) {
            this.mImpl.addTitleText(text, isLoading);
            return this;
        }

        @NonNull
        public CellBuilder addImage(@NonNull IconCompat image, int imageMode) {
            return addImage(image, imageMode, false);
        }

        @NonNull
        public CellBuilder addImage(@Nullable IconCompat image, int imageMode, boolean isLoading) {
            this.mImpl.addImage(image, imageMode, isLoading);
            return this;
        }

        @NonNull
        public CellBuilder setContentIntent(@NonNull PendingIntent intent) {
            this.mImpl.setContentIntent(intent);
            return this;
        }

        @NonNull
        public CellBuilder setContentDescription(@NonNull CharSequence description) {
            this.mImpl.setContentDescription(description);
            return this;
        }
    }

    public GridRowBuilder(@NonNull ListBuilder parent) {
        super(parent.getImpl().createGridBuilder());
    }

    /* Access modifiers changed, original: 0000 */
    public void setImpl(TemplateBuilderImpl impl) {
        this.mImpl = (androidx.slice.builders.impl.GridRowBuilder) impl;
    }

    @NonNull
    public GridRowBuilder addCell(@NonNull CellBuilder builder) {
        this.mImpl.addCell((TemplateBuilderImpl) builder.mImpl);
        return this;
    }

    @NonNull
    public GridRowBuilder addCell(@NonNull Consumer<CellBuilder> c) {
        CellBuilder b = new CellBuilder(this);
        c.accept(b);
        return addCell(b);
    }

    @NonNull
    public GridRowBuilder setSeeMoreCell(@NonNull CellBuilder builder) {
        if (this.mHasSeeMore) {
            throw new IllegalStateException("Trying to add see more cell when one has already been added");
        }
        this.mImpl.setSeeMoreCell((TemplateBuilderImpl) builder.mImpl);
        this.mHasSeeMore = true;
        return this;
    }

    @NonNull
    public GridRowBuilder setSeeMoreCell(@NonNull Consumer<CellBuilder> c) {
        CellBuilder b = new CellBuilder(this);
        c.accept(b);
        return setSeeMoreCell(b);
    }

    @NonNull
    public GridRowBuilder setSeeMoreAction(@NonNull PendingIntent intent) {
        if (this.mHasSeeMore) {
            throw new IllegalStateException("Trying to add see more action when one has already been added");
        }
        this.mImpl.setSeeMoreAction(intent);
        this.mHasSeeMore = true;
        return this;
    }

    @Deprecated
    @NonNull
    public GridRowBuilder addSeeMoreCell(@NonNull CellBuilder builder) {
        if (this.mHasSeeMore) {
            throw new IllegalStateException("Trying to add see more cell when one has already been added");
        }
        this.mImpl.setSeeMoreCell((TemplateBuilderImpl) builder.mImpl);
        this.mHasSeeMore = true;
        return this;
    }

    @Deprecated
    @NonNull
    public GridRowBuilder addSeeMoreCell(@NonNull Consumer<CellBuilder> c) {
        CellBuilder b = new CellBuilder(this);
        c.accept(b);
        return addSeeMoreCell(b);
    }

    @Deprecated
    @NonNull
    public GridRowBuilder addSeeMoreAction(@NonNull PendingIntent intent) {
        if (this.mHasSeeMore) {
            throw new IllegalStateException("Trying to add see more action when one has already been added");
        }
        this.mImpl.setSeeMoreAction(intent);
        this.mHasSeeMore = true;
        return this;
    }

    @NonNull
    public GridRowBuilder setPrimaryAction(@NonNull SliceAction action) {
        this.mImpl.setPrimaryAction(action);
        return this;
    }

    @NonNull
    public GridRowBuilder setContentDescription(@NonNull CharSequence description) {
        this.mImpl.setContentDescription(description);
        return this;
    }

    @NonNull
    public GridRowBuilder setLayoutDirection(int layoutDirection) {
        this.mImpl.setLayoutDirection(layoutDirection);
        return this;
    }

    @RestrictTo({Scope.LIBRARY})
    public androidx.slice.builders.impl.GridRowBuilder getImpl() {
        return this.mImpl;
    }
}
