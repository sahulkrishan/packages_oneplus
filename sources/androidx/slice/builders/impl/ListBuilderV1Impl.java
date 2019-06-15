package androidx.slice.builders.impl;

import android.app.PendingIntent;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import androidx.slice.Clock;
import androidx.slice.Slice;
import androidx.slice.Slice.Builder;
import androidx.slice.SliceItem;
import androidx.slice.SliceSpec;
import androidx.slice.SystemClock;
import androidx.slice.builders.SliceAction;
import androidx.slice.builders.impl.ListBuilder.HeaderBuilder;
import androidx.slice.builders.impl.ListBuilder.InputRangeBuilder;
import androidx.slice.builders.impl.ListBuilder.RangeBuilder;
import androidx.slice.builders.impl.ListBuilder.RowBuilder;
import androidx.slice.core.SliceHints;
import com.android.settings.inputmethod.UserDictionaryAddWordContents;
import java.util.ArrayList;
import java.util.List;

@RestrictTo({Scope.LIBRARY})
public class ListBuilderV1Impl extends TemplateBuilderImpl implements ListBuilder {
    private boolean mIsError;
    private List<Slice> mSliceActions;
    private Slice mSliceHeader;

    public static class HeaderBuilderImpl extends TemplateBuilderImpl implements HeaderBuilder {
        private CharSequence mContentDescr;
        private SliceAction mPrimaryAction;
        private SliceItem mSubtitleItem;
        private SliceItem mSummaryItem;
        private SliceItem mTitleItem;

        public HeaderBuilderImpl(@NonNull ListBuilderV1Impl parent) {
            super(parent.createChildBuilder(), null);
        }

        public HeaderBuilderImpl(@NonNull Uri uri) {
            super(new Builder(uri), null);
        }

        public void apply(Builder b) {
            if (this.mTitleItem != null) {
                b.addItem(this.mTitleItem);
            }
            if (this.mSubtitleItem != null) {
                b.addItem(this.mSubtitleItem);
            }
            if (this.mSummaryItem != null) {
                b.addItem(this.mSummaryItem);
            }
            if (this.mContentDescr != null) {
                b.addText(this.mContentDescr, "content_description", new String[0]);
            }
            if (this.mPrimaryAction != null) {
                b.addSubSlice(this.mPrimaryAction.buildSlice(new Builder(getBuilder()).addHints("title", UserDictionaryAddWordContents.EXTRA_SHORTCUT)), null);
            }
        }

        public void setTitle(CharSequence title, boolean isLoading) {
            this.mTitleItem = new SliceItem((Object) title, "text", null, new String[]{"title"});
            if (isLoading) {
                this.mTitleItem.addHint("partial");
            }
        }

        public void setSubtitle(CharSequence subtitle, boolean isLoading) {
            this.mSubtitleItem = new SliceItem((Object) subtitle, "text", null, new String[0]);
            if (isLoading) {
                this.mSubtitleItem.addHint("partial");
            }
        }

        public void setSummary(CharSequence summarySubtitle, boolean isLoading) {
            this.mSummaryItem = new SliceItem((Object) summarySubtitle, "text", null, new String[]{"summary"});
            if (isLoading) {
                this.mSummaryItem.addHint("partial");
            }
        }

        public void setPrimaryAction(SliceAction action) {
            this.mPrimaryAction = action;
        }

        public void setContentDescription(CharSequence description) {
            this.mContentDescr = description;
        }

        public void setLayoutDirection(int layoutDirection) {
            getBuilder().addInt(layoutDirection, "layout_direction", new String[0]);
        }
    }

    public static class RangeBuilderImpl extends TemplateBuilderImpl implements RangeBuilder {
        private CharSequence mContentDescr;
        private int mLayoutDir = -1;
        private int mMax = 100;
        private int mMin = 0;
        private SliceAction mPrimaryAction;
        private CharSequence mSubtitle;
        private CharSequence mTitle;
        private int mValue = 0;
        private boolean mValueSet = false;

        public RangeBuilderImpl(Builder sb) {
            super(sb, null);
        }

        public void setMin(int min) {
            this.mMin = min;
        }

        public void setMax(int max) {
            this.mMax = max;
        }

        public void setValue(int value) {
            this.mValue = value;
            this.mValueSet = true;
        }

        public void setTitle(@NonNull CharSequence title) {
            this.mTitle = title;
        }

        public void setSubtitle(@NonNull CharSequence title) {
            this.mSubtitle = title;
        }

        public void setPrimaryAction(@NonNull SliceAction action) {
            this.mPrimaryAction = action;
        }

        public void setContentDescription(@NonNull CharSequence description) {
            this.mContentDescr = description;
        }

        public void setLayoutDirection(int layoutDirection) {
            this.mLayoutDir = layoutDirection;
        }

        public void apply(Builder builder) {
            if (!this.mValueSet) {
                this.mValue = this.mMin;
            }
            if (this.mMin > this.mValue || this.mValue > this.mMax || this.mMin >= this.mMax) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Invalid range values, min=");
                stringBuilder.append(this.mMin);
                stringBuilder.append(", value=");
                stringBuilder.append(this.mValue);
                stringBuilder.append(", max=");
                stringBuilder.append(this.mMax);
                stringBuilder.append(" ensure value falls within (min, max) and min < max.");
                throw new IllegalArgumentException(stringBuilder.toString());
            }
            if (this.mTitle != null) {
                builder.addText(this.mTitle, null, "title");
            }
            if (this.mSubtitle != null) {
                builder.addText(this.mSubtitle, null, new String[0]);
            }
            if (this.mContentDescr != null) {
                builder.addText(this.mContentDescr, "content_description", new String[0]);
            }
            if (this.mPrimaryAction != null) {
                builder.addSubSlice(this.mPrimaryAction.buildSlice(new Builder(getBuilder()).addHints("title", UserDictionaryAddWordContents.EXTRA_SHORTCUT)), null);
            }
            if (this.mLayoutDir != -1) {
                builder.addInt(this.mLayoutDir, "layout_direction", new String[0]);
            }
            builder.addHints("list_item").addInt(this.mMin, SliceHints.SUBTYPE_MIN, new String[0]).addInt(this.mMax, "max", new String[0]).addInt(this.mValue, "value", new String[0]);
        }
    }

    public static class RowBuilderImpl extends TemplateBuilderImpl implements RowBuilder {
        private CharSequence mContentDescr;
        private ArrayList<Slice> mEndItems = new ArrayList();
        private SliceAction mPrimaryAction;
        private Slice mStartItem;
        private SliceItem mSubtitleItem;
        private SliceItem mTitleItem;

        public RowBuilderImpl(@NonNull ListBuilderV1Impl parent) {
            super(parent.createChildBuilder(), null);
        }

        public RowBuilderImpl(@NonNull Uri uri) {
            super(new Builder(uri), null);
        }

        public RowBuilderImpl(Builder builder) {
            super(builder, null);
        }

        @NonNull
        public void setTitleItem(long timeStamp) {
            this.mStartItem = new Builder(getBuilder()).addTimestamp(timeStamp, null, new String[0]).addHints("title").build();
        }

        @NonNull
        public void setTitleItem(IconCompat icon, int imageMode) {
            setTitleItem(icon, imageMode, false);
        }

        @NonNull
        public void setTitleItem(IconCompat icon, int imageMode, boolean isLoading) {
            List hints = new ArrayList();
            if (imageMode != 0) {
                hints.add("no_tint");
            }
            if (imageMode == 2) {
                hints.add("large");
            }
            if (isLoading) {
                hints.add("partial");
            }
            Builder sb = new Builder(getBuilder()).addIcon(icon, null, hints);
            if (isLoading) {
                sb.addHints("partial");
            }
            this.mStartItem = sb.addHints("title").build();
        }

        @NonNull
        public void setTitleItem(@NonNull SliceAction action) {
            setTitleItem(action, false);
        }

        public void setTitleItem(SliceAction action, boolean isLoading) {
            Builder sb = new Builder(getBuilder()).addHints("title");
            if (isLoading) {
                sb.addHints("partial");
            }
            this.mStartItem = action.buildSlice(sb);
        }

        @NonNull
        public void setPrimaryAction(@NonNull SliceAction action) {
            this.mPrimaryAction = action;
        }

        @NonNull
        public void setTitle(CharSequence title) {
            setTitle(title, false);
        }

        public void setTitle(CharSequence title, boolean isLoading) {
            this.mTitleItem = new SliceItem((Object) title, "text", null, new String[]{"title"});
            if (isLoading) {
                this.mTitleItem.addHint("partial");
            }
        }

        @NonNull
        public void setSubtitle(CharSequence subtitle) {
            setSubtitle(subtitle, false);
        }

        public void setSubtitle(CharSequence subtitle, boolean isLoading) {
            this.mSubtitleItem = new SliceItem((Object) subtitle, "text", null, new String[0]);
            if (isLoading) {
                this.mSubtitleItem.addHint("partial");
            }
        }

        @NonNull
        public void addEndItem(long timeStamp) {
            this.mEndItems.add(new Builder(getBuilder()).addTimestamp(timeStamp, null, new String[0]).build());
        }

        @NonNull
        public void addEndItem(IconCompat icon, int imageMode) {
            addEndItem(icon, imageMode, false);
        }

        @NonNull
        public void addEndItem(IconCompat icon, int imageMode, boolean isLoading) {
            List hints = new ArrayList();
            if (imageMode != 0) {
                hints.add("no_tint");
            }
            if (imageMode == 2) {
                hints.add("large");
            }
            if (isLoading) {
                hints.add("partial");
            }
            Builder sb = new Builder(getBuilder()).addIcon(icon, null, hints);
            if (isLoading) {
                sb.addHints("partial");
            }
            this.mEndItems.add(sb.build());
        }

        @NonNull
        public void addEndItem(@NonNull SliceAction action) {
            addEndItem(action, false);
        }

        public void addEndItem(@NonNull SliceAction action, boolean isLoading) {
            Builder sb = new Builder(getBuilder());
            if (isLoading) {
                sb.addHints("partial");
            }
            this.mEndItems.add(action.buildSlice(sb));
        }

        public void setContentDescription(CharSequence description) {
            this.mContentDescr = description;
        }

        public void setLayoutDirection(int layoutDirection) {
            getBuilder().addInt(layoutDirection, "layout_direction", new String[0]);
        }

        public void apply(Builder b) {
            if (this.mStartItem != null) {
                b.addSubSlice(this.mStartItem);
            }
            if (this.mTitleItem != null) {
                b.addItem(this.mTitleItem);
            }
            if (this.mSubtitleItem != null) {
                b.addItem(this.mSubtitleItem);
            }
            for (int i = 0; i < this.mEndItems.size(); i++) {
                b.addSubSlice((Slice) this.mEndItems.get(i));
            }
            if (this.mContentDescr != null) {
                b.addText(this.mContentDescr, "content_description", new String[0]);
            }
            if (this.mPrimaryAction != null) {
                b.addSubSlice(this.mPrimaryAction.buildSlice(new Builder(getBuilder()).addHints("title", UserDictionaryAddWordContents.EXTRA_SHORTCUT)), null);
            }
        }
    }

    public static class InputRangeBuilderImpl extends RangeBuilderImpl implements InputRangeBuilder {
        private PendingIntent mAction;
        private IconCompat mThumb;

        public InputRangeBuilderImpl(Builder sb) {
            super(sb);
        }

        public void setInputAction(@NonNull PendingIntent action) {
            this.mAction = action;
        }

        public void setThumb(@NonNull IconCompat thumb) {
            this.mThumb = thumb;
        }

        public void apply(Builder builder) {
            if (this.mAction != null) {
                Builder sb = new Builder(builder);
                super.apply(sb);
                if (this.mThumb != null) {
                    sb.addIcon(this.mThumb, null, new String[0]);
                }
                builder.addAction(this.mAction, sb.build(), "range").addHints("list_item");
                return;
            }
            throw new IllegalStateException("Input ranges must have an associated action.");
        }
    }

    public ListBuilderV1Impl(Builder b, SliceSpec spec) {
        this(b, spec, new SystemClock());
    }

    public ListBuilderV1Impl(Builder b, SliceSpec spec, Clock clock) {
        super(b, spec, clock);
    }

    public void apply(Builder builder) {
        builder.addLong(getClock().currentTimeMillis(), SliceHints.SUBTYPE_MILLIS, SliceHints.HINT_LAST_UPDATED);
        if (this.mSliceHeader != null) {
            builder.addSubSlice(this.mSliceHeader);
        }
        if (this.mSliceActions != null) {
            Builder sb = new Builder(builder);
            for (int i = 0; i < this.mSliceActions.size(); i++) {
                sb.addSubSlice((Slice) this.mSliceActions.get(i));
            }
            builder.addSubSlice(sb.addHints("actions").build());
        }
        if (this.mIsError) {
            builder.addHints("error");
        }
    }

    @NonNull
    public void addRow(@NonNull TemplateBuilderImpl builder) {
        builder.getBuilder().addHints("list_item");
        getBuilder().addSubSlice(builder.build());
    }

    @NonNull
    public void addGridRow(@NonNull TemplateBuilderImpl builder) {
        builder.getBuilder().addHints("list_item");
        getBuilder().addSubSlice(builder.build());
    }

    public void setHeader(@NonNull TemplateBuilderImpl builder) {
        this.mSliceHeader = builder.build();
    }

    public void addAction(@NonNull SliceAction action) {
        if (this.mSliceActions == null) {
            this.mSliceActions = new ArrayList();
        }
        this.mSliceActions.add(action.buildSlice(new Builder(getBuilder()).addHints("actions")));
    }

    public void addInputRange(TemplateBuilderImpl builder) {
        getBuilder().addSubSlice(builder.build(), "range");
    }

    public void addRange(TemplateBuilderImpl builder) {
        getBuilder().addSubSlice(builder.build(), "range");
    }

    public void setSeeMoreRow(TemplateBuilderImpl builder) {
        builder.getBuilder().addHints("see_more");
        getBuilder().addSubSlice(builder.build());
    }

    public void setSeeMoreAction(PendingIntent intent) {
        getBuilder().addSubSlice(new Builder(getBuilder()).addHints("see_more").addAction(intent, new Builder(getBuilder()).addHints("see_more").build(), null).build());
    }

    @NonNull
    public void setColor(@ColorInt int color) {
        getBuilder().addInt(color, "color", new String[0]);
    }

    public void setKeywords(@NonNull List<String> keywords) {
        Builder sb = new Builder(getBuilder());
        for (int i = 0; i < keywords.size(); i++) {
            sb.addText((CharSequence) keywords.get(i), null, new String[0]);
        }
        getBuilder().addSubSlice(sb.addHints("keywords").build());
    }

    public void setTtl(long ttl) {
        long expiry = -1;
        if (ttl != -1) {
            expiry = getClock().currentTimeMillis() + ttl;
        }
        getBuilder().addTimestamp(expiry, SliceHints.SUBTYPE_MILLIS, SliceHints.HINT_TTL);
    }

    public void setIsError(boolean isError) {
        this.mIsError = isError;
    }

    public void setLayoutDirection(int layoutDirection) {
        getBuilder().addInt(layoutDirection, "layout_direction", new String[0]);
    }

    public TemplateBuilderImpl createRowBuilder() {
        return new RowBuilderImpl(this);
    }

    public TemplateBuilderImpl createRowBuilder(Uri uri) {
        return new RowBuilderImpl(uri);
    }

    public TemplateBuilderImpl createInputRangeBuilder() {
        return new InputRangeBuilderImpl(createChildBuilder());
    }

    public TemplateBuilderImpl createRangeBuilder() {
        return new RangeBuilderImpl(createChildBuilder());
    }

    public TemplateBuilderImpl createGridBuilder() {
        return new GridRowBuilderListV1Impl(this);
    }

    public TemplateBuilderImpl createHeaderBuilder() {
        return new HeaderBuilderImpl(this);
    }

    public TemplateBuilderImpl createHeaderBuilder(Uri uri) {
        return new HeaderBuilderImpl(uri);
    }
}
