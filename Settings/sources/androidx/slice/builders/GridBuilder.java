package androidx.slice.builders;

import android.app.PendingIntent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Consumer;
import androidx.slice.builders.impl.GridRowBuilder;
import androidx.slice.builders.impl.TemplateBuilderImpl;

@Deprecated
public class GridBuilder extends TemplateSliceBuilder {
    @Deprecated
    public static final int ICON_IMAGE = 0;
    @Deprecated
    public static final int LARGE_IMAGE = 2;
    @Deprecated
    public static final int SMALL_IMAGE = 1;
    private boolean mHasSeeMore;
    private GridRowBuilder mImpl;

    @Deprecated
    public static final class CellBuilder extends TemplateSliceBuilder {
        private androidx.slice.builders.impl.GridRowBuilder.CellBuilder mImpl;

        public CellBuilder(@NonNull GridBuilder parent) {
            super(parent.mImpl.createGridRowBuilder());
        }

        public CellBuilder(@NonNull GridBuilder parent, @NonNull Uri uri) {
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

        @RequiresApi(23)
        @Deprecated
        @NonNull
        public CellBuilder addLargeImage(@NonNull Icon image) {
            return addImage(image, 2, false);
        }

        @RequiresApi(23)
        @Deprecated
        @NonNull
        public CellBuilder addLargeImage(@Nullable Icon image, boolean isLoading) {
            return addImage(image, 2, isLoading);
        }

        @RequiresApi(23)
        @Deprecated
        @NonNull
        public CellBuilder addImage(@NonNull Icon image) {
            return addImage(image, 1, false);
        }

        @RequiresApi(23)
        @Deprecated
        @NonNull
        public CellBuilder addImage(@Nullable Icon image, boolean isLoading) {
            return addImage(image, 1, isLoading);
        }

        @RequiresApi(23)
        @Deprecated
        @NonNull
        public CellBuilder addImage(@NonNull Icon image, int imageMode) {
            return addImage(image, imageMode, false);
        }

        @RequiresApi(23)
        @Deprecated
        @NonNull
        public CellBuilder addImage(@Nullable Icon image, int imageMode, boolean isLoading) {
            this.mImpl.addImage(IconCompat.createFromIcon(image), imageMode, isLoading);
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

    public GridBuilder(@NonNull ListBuilder parent) {
        super(parent.getImpl().createGridBuilder());
    }

    /* Access modifiers changed, original: 0000 */
    public void setImpl(TemplateBuilderImpl impl) {
        this.mImpl = (GridRowBuilder) impl;
    }

    @NonNull
    public GridBuilder addCell(@NonNull CellBuilder builder) {
        this.mImpl.addCell((TemplateBuilderImpl) builder.mImpl);
        return this;
    }

    @NonNull
    public GridBuilder addCell(@NonNull Consumer<CellBuilder> c) {
        CellBuilder b = new CellBuilder(this);
        c.accept(b);
        return addCell(b);
    }

    @NonNull
    public GridBuilder addSeeMoreCell(@NonNull CellBuilder builder) {
        if (this.mHasSeeMore) {
            throw new IllegalStateException("Trying to add see more cell when one has already been added");
        }
        this.mImpl.setSeeMoreCell((TemplateBuilderImpl) builder.mImpl);
        this.mHasSeeMore = true;
        return this;
    }

    @NonNull
    public GridBuilder addSeeMoreCell(@NonNull Consumer<CellBuilder> c) {
        CellBuilder b = new CellBuilder(this);
        c.accept(b);
        return addSeeMoreCell(b);
    }

    @NonNull
    public GridBuilder addSeeMoreAction(@NonNull PendingIntent intent) {
        if (this.mHasSeeMore) {
            throw new IllegalStateException("Trying to add see more action when one has already been added");
        }
        this.mImpl.setSeeMoreAction(intent);
        this.mHasSeeMore = true;
        return this;
    }

    @NonNull
    public GridBuilder setPrimaryAction(@NonNull SliceAction action) {
        this.mImpl.setPrimaryAction(action);
        return this;
    }

    @NonNull
    public GridBuilder setContentDescription(@NonNull CharSequence description) {
        this.mImpl.setContentDescription(description);
        return this;
    }

    @RestrictTo({Scope.LIBRARY})
    public GridRowBuilder getImpl() {
        return this.mImpl;
    }
}
