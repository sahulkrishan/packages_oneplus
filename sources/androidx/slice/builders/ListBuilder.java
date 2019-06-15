package androidx.slice.builders;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Consumer;
import androidx.slice.SliceSpecs;
import androidx.slice.builders.impl.ListBuilderBasicImpl;
import androidx.slice.builders.impl.ListBuilderV1Impl;
import androidx.slice.builders.impl.TemplateBuilderImpl;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class ListBuilder extends TemplateSliceBuilder {
    public static final int ICON_IMAGE = 0;
    public static final long INFINITY = -1;
    public static final int LARGE_IMAGE = 2;
    public static final int SMALL_IMAGE = 1;
    public static final int UNKNOWN_IMAGE = 3;
    private boolean mHasSeeMore;
    private androidx.slice.builders.impl.ListBuilder mImpl;

    @RestrictTo({Scope.LIBRARY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ImageMode {
    }

    @RestrictTo({Scope.LIBRARY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LayoutDirection {
    }

    public static class HeaderBuilder extends TemplateSliceBuilder {
        private androidx.slice.builders.impl.ListBuilder.HeaderBuilder mImpl;

        public HeaderBuilder(@NonNull ListBuilder parent) {
            super(parent.mImpl.createHeaderBuilder());
        }

        @RestrictTo({Scope.LIBRARY_GROUP})
        public HeaderBuilder(@NonNull ListBuilder parent, @NonNull Uri uri) {
            super(parent.mImpl.createHeaderBuilder(uri));
        }

        @NonNull
        public HeaderBuilder setTitle(@NonNull CharSequence title) {
            return setTitle(title, false);
        }

        @NonNull
        public HeaderBuilder setTitle(@NonNull CharSequence title, boolean isLoading) {
            this.mImpl.setTitle(title, isLoading);
            return this;
        }

        @NonNull
        public HeaderBuilder setSubtitle(@NonNull CharSequence subtitle) {
            return setSubtitle(subtitle, false);
        }

        @NonNull
        public HeaderBuilder setSubtitle(@NonNull CharSequence subtitle, boolean isLoading) {
            this.mImpl.setSubtitle(subtitle, isLoading);
            return this;
        }

        @NonNull
        public HeaderBuilder setSummarySubtitle(@NonNull CharSequence summarySubtitle) {
            return setSummary(summarySubtitle, false);
        }

        @NonNull
        public HeaderBuilder setSummarySubtitle(@NonNull CharSequence summarySubtitle, boolean isLoading) {
            return setSummary(summarySubtitle, isLoading);
        }

        @NonNull
        public HeaderBuilder setSummary(@NonNull CharSequence summary) {
            return setSummary(summary, false);
        }

        @NonNull
        public HeaderBuilder setSummary(@NonNull CharSequence summary, boolean isLoading) {
            this.mImpl.setSummary(summary, isLoading);
            return this;
        }

        @NonNull
        public HeaderBuilder setPrimaryAction(@NonNull SliceAction action) {
            this.mImpl.setPrimaryAction(action);
            return this;
        }

        @NonNull
        public HeaderBuilder setContentDescription(@NonNull CharSequence description) {
            this.mImpl.setContentDescription(description);
            return this;
        }

        @NonNull
        public HeaderBuilder setLayoutDirection(int layoutDirection) {
            this.mImpl.setLayoutDirection(layoutDirection);
            return this;
        }

        /* Access modifiers changed, original: 0000 */
        public void setImpl(TemplateBuilderImpl impl) {
            this.mImpl = (androidx.slice.builders.impl.ListBuilder.HeaderBuilder) impl;
        }
    }

    public static class InputRangeBuilder extends TemplateSliceBuilder {
        private androidx.slice.builders.impl.ListBuilder.InputRangeBuilder mImpl;

        public InputRangeBuilder(@NonNull ListBuilder parent) {
            super(parent.mImpl.createInputRangeBuilder());
        }

        @NonNull
        public InputRangeBuilder setMin(int min) {
            this.mImpl.setMin(min);
            return this;
        }

        @NonNull
        public InputRangeBuilder setMax(int max) {
            this.mImpl.setMax(max);
            return this;
        }

        @NonNull
        public InputRangeBuilder setValue(int value) {
            this.mImpl.setValue(value);
            return this;
        }

        @NonNull
        public InputRangeBuilder setTitle(@NonNull CharSequence title) {
            this.mImpl.setTitle(title);
            return this;
        }

        @NonNull
        public InputRangeBuilder setSubtitle(@NonNull CharSequence title) {
            this.mImpl.setSubtitle(title);
            return this;
        }

        @Deprecated
        @NonNull
        public InputRangeBuilder setAction(@NonNull PendingIntent action) {
            this.mImpl.setInputAction(action);
            return this;
        }

        @NonNull
        public InputRangeBuilder setInputAction(@NonNull PendingIntent action) {
            this.mImpl.setInputAction(action);
            return this;
        }

        @RequiresApi(23)
        @NonNull
        public InputRangeBuilder setThumb(@NonNull Icon thumb) {
            return setThumb(IconCompat.createFromIcon(thumb));
        }

        @NonNull
        public InputRangeBuilder setThumb(@NonNull IconCompat thumb) {
            this.mImpl.setThumb(thumb);
            return this;
        }

        @NonNull
        public InputRangeBuilder setPrimaryAction(@NonNull SliceAction action) {
            this.mImpl.setPrimaryAction(action);
            return this;
        }

        @NonNull
        public InputRangeBuilder setContentDescription(@NonNull CharSequence description) {
            this.mImpl.setContentDescription(description);
            return this;
        }

        @NonNull
        public InputRangeBuilder setLayoutDirection(int layoutDirection) {
            this.mImpl.setLayoutDirection(layoutDirection);
            return this;
        }

        /* Access modifiers changed, original: 0000 */
        public void setImpl(TemplateBuilderImpl impl) {
            this.mImpl = (androidx.slice.builders.impl.ListBuilder.InputRangeBuilder) impl;
        }
    }

    public static class RangeBuilder extends TemplateSliceBuilder {
        private androidx.slice.builders.impl.ListBuilder.RangeBuilder mImpl;

        public RangeBuilder(@NonNull ListBuilder parent) {
            super(parent.mImpl.createRangeBuilder());
        }

        @NonNull
        public RangeBuilder setMax(int max) {
            this.mImpl.setMax(max);
            return this;
        }

        @NonNull
        public RangeBuilder setValue(int value) {
            this.mImpl.setValue(value);
            return this;
        }

        @NonNull
        public RangeBuilder setTitle(@NonNull CharSequence title) {
            this.mImpl.setTitle(title);
            return this;
        }

        @NonNull
        public RangeBuilder setSubtitle(@NonNull CharSequence title) {
            this.mImpl.setSubtitle(title);
            return this;
        }

        @NonNull
        public RangeBuilder setPrimaryAction(@NonNull SliceAction action) {
            this.mImpl.setPrimaryAction(action);
            return this;
        }

        @NonNull
        public RangeBuilder setContentDescription(@NonNull CharSequence description) {
            this.mImpl.setContentDescription(description);
            return this;
        }

        @NonNull
        public RangeBuilder setLayoutDirection(int layoutDirection) {
            this.mImpl.setLayoutDirection(layoutDirection);
            return this;
        }

        /* Access modifiers changed, original: 0000 */
        public void setImpl(TemplateBuilderImpl impl) {
            this.mImpl = (androidx.slice.builders.impl.ListBuilder.RangeBuilder) impl;
        }
    }

    public static class RowBuilder extends TemplateSliceBuilder {
        private boolean mHasDefaultToggle;
        private boolean mHasEndActionOrToggle;
        private boolean mHasEndImage;
        private boolean mHasTimestamp;
        private androidx.slice.builders.impl.ListBuilder.RowBuilder mImpl;

        public RowBuilder(@NonNull ListBuilder parent) {
            super(parent.mImpl.createRowBuilder());
        }

        public RowBuilder(@NonNull ListBuilder parent, @NonNull Uri uri) {
            super(parent.mImpl.createRowBuilder(uri));
        }

        public RowBuilder(@NonNull Context context, @NonNull Uri uri) {
            super(new ListBuilder(context, uri).mImpl.createRowBuilder(uri));
        }

        @NonNull
        public RowBuilder setTitleItem(long timeStamp) {
            if (this.mHasTimestamp) {
                throw new IllegalArgumentException("Trying to add a timestamp when one has already been added");
            }
            this.mImpl.setTitleItem(timeStamp);
            this.mHasTimestamp = true;
            return this;
        }

        @RequiresApi(23)
        @Deprecated
        @NonNull
        public RowBuilder setTitleItem(@NonNull Icon icon) {
            return setTitleItem(icon, 0);
        }

        @RequiresApi(23)
        @Deprecated
        @NonNull
        public RowBuilder setTitleItem(@Nullable Icon icon, boolean isLoading) {
            return setTitleItem(icon, 0, isLoading);
        }

        @RequiresApi(23)
        @Deprecated
        public RowBuilder setTitleItem(@NonNull Icon icon, int imageMode) {
            this.mImpl.setTitleItem(IconCompat.createFromIcon(icon), imageMode, false);
            return this;
        }

        @RequiresApi(23)
        @Deprecated
        @NonNull
        public RowBuilder setTitleItem(@Nullable Icon icon, int imageMode, boolean isLoading) {
            this.mImpl.setTitleItem(IconCompat.createFromIcon(icon), imageMode, isLoading);
            return this;
        }

        @Deprecated
        @NonNull
        public RowBuilder setTitleItem(@NonNull IconCompat icon) {
            return setTitleItem(icon, 0);
        }

        @Deprecated
        @NonNull
        public RowBuilder setTitleItem(@Nullable IconCompat icon, boolean isLoading) {
            return setTitleItem(icon, 0, isLoading);
        }

        public RowBuilder setTitleItem(@NonNull IconCompat icon, int imageMode) {
            this.mImpl.setTitleItem(icon, imageMode, false);
            return this;
        }

        @NonNull
        public RowBuilder setTitleItem(@Nullable IconCompat icon, int imageMode, boolean isLoading) {
            this.mImpl.setTitleItem(icon, imageMode, isLoading);
            return this;
        }

        @NonNull
        public RowBuilder setTitleItem(@NonNull SliceAction action) {
            return setTitleItem(action, false);
        }

        @NonNull
        public RowBuilder setTitleItem(@NonNull SliceAction action, boolean isLoading) {
            this.mImpl.setTitleItem(action, isLoading);
            return this;
        }

        @NonNull
        public RowBuilder setPrimaryAction(@NonNull SliceAction action) {
            this.mImpl.setPrimaryAction(action);
            return this;
        }

        @NonNull
        public RowBuilder setTitle(@NonNull CharSequence title) {
            this.mImpl.setTitle(title);
            return this;
        }

        @NonNull
        public RowBuilder setTitle(@Nullable CharSequence title, boolean isLoading) {
            this.mImpl.setTitle(title, isLoading);
            return this;
        }

        @NonNull
        public RowBuilder setSubtitle(@NonNull CharSequence subtitle) {
            return setSubtitle(subtitle, false);
        }

        @NonNull
        public RowBuilder setSubtitle(@Nullable CharSequence subtitle, boolean isLoading) {
            this.mImpl.setSubtitle(subtitle, isLoading);
            return this;
        }

        @NonNull
        public RowBuilder addEndItem(long timeStamp) {
            if (this.mHasTimestamp) {
                throw new IllegalArgumentException("Trying to add a timestamp when one has already been added");
            }
            this.mImpl.addEndItem(timeStamp);
            this.mHasTimestamp = true;
            return this;
        }

        @RequiresApi(23)
        @Deprecated
        @NonNull
        public RowBuilder addEndItem(@NonNull Icon icon) {
            return addEndItem(icon, 0, false);
        }

        @RequiresApi(23)
        @Deprecated
        @NonNull
        public RowBuilder addEndItem(@NonNull Icon icon, boolean isLoading) {
            return addEndItem(icon, 0, isLoading);
        }

        @RequiresApi(23)
        @Deprecated
        @NonNull
        public RowBuilder addEndItem(@NonNull Icon icon, int imageMode) {
            return addEndItem(icon, imageMode, false);
        }

        @RequiresApi(23)
        @Deprecated
        @NonNull
        public RowBuilder addEndItem(@Nullable Icon icon, int imageMode, boolean isLoading) {
            if (this.mHasEndActionOrToggle) {
                throw new IllegalArgumentException("Trying to add an icon to end items when anaction has already been added. End items cannot have a mixture of actions and icons.");
            }
            this.mImpl.addEndItem(IconCompat.createFromIcon(icon), imageMode, isLoading);
            this.mHasEndImage = true;
            return this;
        }

        @Deprecated
        @NonNull
        public RowBuilder addEndItem(@NonNull IconCompat icon) {
            return addEndItem(icon, 0, false);
        }

        @Deprecated
        @NonNull
        public RowBuilder addEndItem(@NonNull IconCompat icon, boolean isLoading) {
            return addEndItem(icon, 0, isLoading);
        }

        @NonNull
        public RowBuilder addEndItem(@NonNull IconCompat icon, int imageMode) {
            return addEndItem(icon, imageMode, false);
        }

        @NonNull
        public RowBuilder addEndItem(@Nullable IconCompat icon, int imageMode, boolean isLoading) {
            if (this.mHasEndActionOrToggle) {
                throw new IllegalArgumentException("Trying to add an icon to end items when anaction has already been added. End items cannot have a mixture of actions and icons.");
            }
            this.mImpl.addEndItem(icon, imageMode, isLoading);
            this.mHasEndImage = true;
            return this;
        }

        @NonNull
        public RowBuilder addEndItem(@NonNull SliceAction action) {
            return addEndItem(action, false);
        }

        @NonNull
        public RowBuilder addEndItem(@NonNull SliceAction action, boolean isLoading) {
            if (this.mHasEndImage) {
                throw new IllegalArgumentException("Trying to add an action to end items when anicon has already been added. End items cannot have a mixture of actions and icons.");
            } else if (this.mHasDefaultToggle) {
                throw new IllegalStateException("Only one non-custom toggle can be added in a single row. If you would like to include multiple toggles in a row, set a custom icon for each toggle.");
            } else {
                this.mImpl.addEndItem(action, isLoading);
                this.mHasDefaultToggle = action.getImpl().isDefaultToggle();
                this.mHasEndActionOrToggle = true;
                return this;
            }
        }

        @NonNull
        public RowBuilder setContentDescription(@NonNull CharSequence description) {
            this.mImpl.setContentDescription(description);
            return this;
        }

        @NonNull
        public RowBuilder setLayoutDirection(int layoutDirection) {
            this.mImpl.setLayoutDirection(layoutDirection);
            return this;
        }

        /* Access modifiers changed, original: 0000 */
        public void setImpl(TemplateBuilderImpl impl) {
            this.mImpl = (androidx.slice.builders.impl.ListBuilder.RowBuilder) impl;
        }

        @RestrictTo({Scope.LIBRARY})
        public androidx.slice.builders.impl.ListBuilder.RowBuilder getImpl() {
            return this.mImpl;
        }
    }

    @Deprecated
    public ListBuilder(@NonNull Context context, @NonNull Uri uri) {
        super(context, uri);
    }

    public ListBuilder(@NonNull Context context, @NonNull Uri uri, long ttl) {
        super(context, uri);
        this.mImpl.setTtl(ttl);
    }

    /* Access modifiers changed, original: 0000 */
    public void setImpl(TemplateBuilderImpl impl) {
        this.mImpl = (androidx.slice.builders.impl.ListBuilder) impl;
    }

    @NonNull
    public ListBuilder addRow(@NonNull RowBuilder builder) {
        this.mImpl.addRow((TemplateBuilderImpl) builder.mImpl);
        return this;
    }

    @NonNull
    public ListBuilder addRow(@NonNull Consumer<RowBuilder> c) {
        RowBuilder b = new RowBuilder(this);
        c.accept(b);
        return addRow(b);
    }

    @Deprecated
    @NonNull
    public ListBuilder addGrid(@NonNull GridBuilder builder) {
        this.mImpl.addGridRow((TemplateBuilderImpl) builder.getImpl());
        return this;
    }

    @Deprecated
    @NonNull
    public ListBuilder addGrid(@NonNull Consumer<GridBuilder> c) {
        GridBuilder b = new GridBuilder(this);
        c.accept(b);
        return addGrid(b);
    }

    @NonNull
    public ListBuilder addGridRow(@NonNull GridRowBuilder builder) {
        this.mImpl.addGridRow((TemplateBuilderImpl) builder.getImpl());
        return this;
    }

    @NonNull
    public ListBuilder addGridRow(@NonNull Consumer<GridRowBuilder> c) {
        GridRowBuilder b = new GridRowBuilder(this);
        c.accept(b);
        return addGridRow(b);
    }

    @NonNull
    public ListBuilder setHeader(@NonNull HeaderBuilder builder) {
        this.mImpl.setHeader((TemplateBuilderImpl) builder.mImpl);
        return this;
    }

    @NonNull
    public ListBuilder setHeader(@NonNull Consumer<HeaderBuilder> c) {
        HeaderBuilder b = new HeaderBuilder(this);
        c.accept(b);
        return setHeader(b);
    }

    @NonNull
    public ListBuilder addAction(@NonNull SliceAction action) {
        this.mImpl.addAction(action);
        return this;
    }

    @Deprecated
    @NonNull
    public ListBuilder setColor(@ColorInt int color) {
        return setAccentColor(color);
    }

    @NonNull
    public ListBuilder setAccentColor(@ColorInt int color) {
        this.mImpl.setColor(color);
        return this;
    }

    @NonNull
    public ListBuilder setKeywords(List<String> keywords) {
        this.mImpl.setKeywords(keywords);
        return this;
    }

    @NonNull
    public ListBuilder setLayoutDirection(int layoutDirection) {
        this.mImpl.setLayoutDirection(layoutDirection);
        return this;
    }

    @NonNull
    public ListBuilder setSeeMoreRow(@NonNull RowBuilder builder) {
        if (this.mHasSeeMore) {
            throw new IllegalArgumentException("Trying to add see more row when one has already been added");
        }
        this.mImpl.setSeeMoreRow((TemplateBuilderImpl) builder.mImpl);
        this.mHasSeeMore = true;
        return this;
    }

    @NonNull
    public ListBuilder setSeeMoreRow(@NonNull Consumer<RowBuilder> c) {
        RowBuilder b = new RowBuilder(this);
        c.accept(b);
        return addSeeMoreRow(b);
    }

    @NonNull
    public ListBuilder setSeeMoreAction(@NonNull PendingIntent intent) {
        if (this.mHasSeeMore) {
            throw new IllegalArgumentException("Trying to add see more action when one has already been added");
        }
        this.mImpl.setSeeMoreAction(intent);
        this.mHasSeeMore = true;
        return this;
    }

    @Deprecated
    @NonNull
    public ListBuilder addSeeMoreRow(@NonNull RowBuilder builder) {
        if (this.mHasSeeMore) {
            throw new IllegalArgumentException("Trying to add see more row when one has already been added");
        }
        this.mImpl.setSeeMoreRow((TemplateBuilderImpl) builder.mImpl);
        this.mHasSeeMore = true;
        return this;
    }

    @Deprecated
    @NonNull
    public ListBuilder addSeeMoreRow(@NonNull Consumer<RowBuilder> c) {
        RowBuilder b = new RowBuilder(this);
        c.accept(b);
        return setSeeMoreRow(b);
    }

    @Deprecated
    @NonNull
    public ListBuilder addSeeMoreAction(@NonNull PendingIntent intent) {
        if (this.mHasSeeMore) {
            throw new IllegalArgumentException("Trying to add see more action when one has already been added");
        }
        this.mImpl.setSeeMoreAction(intent);
        this.mHasSeeMore = true;
        return this;
    }

    @NonNull
    public ListBuilder setIsError(boolean isError) {
        this.mImpl.setIsError(isError);
        return this;
    }

    /* Access modifiers changed, original: protected */
    @RestrictTo({Scope.LIBRARY})
    public TemplateBuilderImpl selectImpl() {
        if (checkCompatible(SliceSpecs.LIST)) {
            return new ListBuilderV1Impl(getBuilder(), SliceSpecs.LIST, getClock());
        }
        if (checkCompatible(SliceSpecs.BASIC)) {
            return new ListBuilderBasicImpl(getBuilder(), SliceSpecs.BASIC);
        }
        return null;
    }

    @RestrictTo({Scope.LIBRARY})
    public androidx.slice.builders.impl.ListBuilder getImpl() {
        return this.mImpl;
    }

    @NonNull
    public ListBuilder addInputRange(@NonNull InputRangeBuilder b) {
        this.mImpl.addInputRange((TemplateBuilderImpl) b.mImpl);
        return this;
    }

    @NonNull
    public ListBuilder addInputRange(@NonNull Consumer<InputRangeBuilder> c) {
        InputRangeBuilder inputRangeBuilder = new InputRangeBuilder(this);
        c.accept(inputRangeBuilder);
        return addInputRange(inputRangeBuilder);
    }

    @NonNull
    public ListBuilder addRange(@NonNull RangeBuilder rangeBuilder) {
        this.mImpl.addRange((TemplateBuilderImpl) rangeBuilder.mImpl);
        return this;
    }

    @NonNull
    public ListBuilder addRange(@NonNull Consumer<RangeBuilder> c) {
        RangeBuilder rangeBuilder = new RangeBuilder(this);
        c.accept(rangeBuilder);
        return addRange(rangeBuilder);
    }
}
