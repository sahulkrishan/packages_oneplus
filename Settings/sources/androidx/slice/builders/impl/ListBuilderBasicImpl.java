package androidx.slice.builders.impl;

import android.app.PendingIntent;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import androidx.slice.Slice.Builder;
import androidx.slice.SliceSpec;
import androidx.slice.builders.SliceAction;
import androidx.slice.builders.impl.ListBuilder.HeaderBuilder;
import androidx.slice.builders.impl.ListBuilder.RowBuilder;
import androidx.slice.builders.impl.ListBuilderV1Impl.InputRangeBuilderImpl;
import androidx.slice.builders.impl.ListBuilderV1Impl.RangeBuilderImpl;
import java.util.List;

@RestrictTo({Scope.LIBRARY})
public class ListBuilderBasicImpl extends TemplateBuilderImpl implements ListBuilder {
    boolean mIsError;

    public static class HeaderBuilderImpl extends TemplateBuilderImpl implements HeaderBuilder {
        public HeaderBuilderImpl(@NonNull ListBuilderBasicImpl parent) {
            super(parent.createChildBuilder(), null);
        }

        public HeaderBuilderImpl(@NonNull Uri uri) {
            super(new Builder(uri), null);
        }

        public void apply(Builder builder) {
        }

        public void setTitle(CharSequence title, boolean isLoading) {
        }

        public void setSubtitle(CharSequence subtitle, boolean isLoading) {
        }

        public void setSummary(CharSequence summarySubtitle, boolean isLoading) {
        }

        public void setPrimaryAction(SliceAction action) {
        }

        public void setContentDescription(CharSequence description) {
        }

        public void setLayoutDirection(int layoutDirection) {
        }
    }

    public static class RowBuilderImpl extends TemplateBuilderImpl implements RowBuilder {
        public RowBuilderImpl(@NonNull ListBuilderBasicImpl parent) {
            super(parent.createChildBuilder(), null);
        }

        public RowBuilderImpl(@NonNull Uri uri) {
            super(new Builder(uri), null);
        }

        public void addEndItem(SliceAction action) {
        }

        public void addEndItem(SliceAction action, boolean isLoading) {
        }

        public void setContentDescription(CharSequence description) {
        }

        public void setLayoutDirection(int layoutDirection) {
        }

        public void setTitleItem(long timeStamp) {
        }

        public void setTitleItem(IconCompat icon, int imageMode) {
        }

        public void setTitleItem(IconCompat icon, int imageMode, boolean isLoading) {
        }

        public void setTitleItem(SliceAction action) {
        }

        public void setTitleItem(SliceAction action, boolean isLoading) {
        }

        public void setPrimaryAction(SliceAction action) {
        }

        public void setTitle(CharSequence title) {
        }

        public void setTitle(CharSequence title, boolean isLoading) {
        }

        public void setSubtitle(CharSequence subtitle) {
        }

        public void setSubtitle(CharSequence subtitle, boolean isLoading) {
        }

        public void addEndItem(long timeStamp) {
        }

        public void addEndItem(IconCompat icon, int imageMode) {
        }

        public void addEndItem(IconCompat icon, int imageMode, boolean isLoading) {
        }

        public void apply(Builder builder) {
        }
    }

    public ListBuilderBasicImpl(Builder b, SliceSpec spec) {
        super(b, spec);
    }

    public void addRow(TemplateBuilderImpl impl) {
    }

    public void addGridRow(TemplateBuilderImpl impl) {
    }

    public void addAction(SliceAction impl) {
    }

    public void setHeader(TemplateBuilderImpl impl) {
    }

    public void addInputRange(TemplateBuilderImpl builder) {
    }

    public void addRange(TemplateBuilderImpl builder) {
    }

    public void setSeeMoreRow(TemplateBuilderImpl builder) {
    }

    public void setSeeMoreAction(PendingIntent intent) {
    }

    public void setColor(@ColorInt int color) {
    }

    public void setKeywords(List<String> keywords) {
        Builder sb = new Builder(getBuilder());
        for (int i = 0; i < keywords.size(); i++) {
            sb.addText((CharSequence) keywords.get(i), null, new String[0]);
        }
        getBuilder().addSubSlice(sb.addHints("keywords").build());
    }

    public void setTtl(long ttl) {
    }

    public void setIsError(boolean isError) {
        this.mIsError = isError;
    }

    public void setLayoutDirection(int layoutDirection) {
    }

    public TemplateBuilderImpl createRowBuilder() {
        return new RowBuilderImpl(this);
    }

    public TemplateBuilderImpl createRowBuilder(Uri uri) {
        return new RowBuilderImpl(uri);
    }

    public TemplateBuilderImpl createGridBuilder() {
        return new GridRowBuilderBasicImpl(this);
    }

    public TemplateBuilderImpl createHeaderBuilder() {
        return new HeaderBuilderImpl(this);
    }

    public TemplateBuilderImpl createHeaderBuilder(Uri uri) {
        return new HeaderBuilderImpl(uri);
    }

    public TemplateBuilderImpl createInputRangeBuilder() {
        return new InputRangeBuilderImpl(getBuilder());
    }

    public TemplateBuilderImpl createRangeBuilder() {
        return new RangeBuilderImpl(getBuilder());
    }

    public void apply(Builder builder) {
        if (this.mIsError) {
            builder.addHints("error");
        }
    }
}
