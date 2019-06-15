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
import java.util.ArrayList;
import java.util.List;

@RestrictTo({Scope.LIBRARY})
public class GridRowBuilderListV1Impl extends TemplateBuilderImpl implements GridRowBuilder {
    private SliceAction mPrimaryAction;

    public static final class CellBuilder extends TemplateBuilderImpl implements androidx.slice.builders.impl.GridRowBuilder.CellBuilder {
        private PendingIntent mContentIntent;

        public CellBuilder(@NonNull GridRowBuilderListV1Impl parent) {
            super(parent.createChildBuilder(), null);
        }

        public CellBuilder(@NonNull Uri uri) {
            super(new Builder(uri), null);
        }

        @NonNull
        public void addText(@NonNull CharSequence text) {
            addText(text, false);
        }

        public void addText(@Nullable CharSequence text, boolean isLoading) {
            getBuilder().addText(text, null, isLoading ? new String[]{"partial"} : new String[null]);
        }

        @NonNull
        public void addTitleText(@NonNull CharSequence text) {
            addTitleText(text, false);
        }

        @NonNull
        public void addTitleText(@Nullable CharSequence text, boolean isLoading) {
            getBuilder().addText(text, null, isLoading ? new String[]{"partial", "title"} : new String[]{"title"});
        }

        @NonNull
        public void addImage(@NonNull IconCompat image, int imageMode) {
            addImage(image, imageMode, false);
        }

        @NonNull
        public void addImage(@Nullable IconCompat image, int imageMode, boolean isLoading) {
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
            getBuilder().addIcon(image, null, hints);
        }

        @NonNull
        public void setContentIntent(@NonNull PendingIntent intent) {
            this.mContentIntent = intent;
        }

        public void setContentDescription(CharSequence description) {
            getBuilder().addText(description, "content_description", new String[0]);
        }

        @RestrictTo({Scope.LIBRARY})
        public void apply(Builder b) {
            getBuilder().addHints("horizontal");
            if (this.mContentIntent != null) {
                b.addAction(this.mContentIntent, getBuilder().build(), null);
            } else {
                b.addSubSlice(getBuilder().build());
            }
        }
    }

    public GridRowBuilderListV1Impl(@NonNull ListBuilderV1Impl parent) {
        super(parent.createChildBuilder(), null);
    }

    public void apply(Builder builder) {
        builder.addHints("horizontal");
        if (this.mPrimaryAction != null) {
            builder.addSubSlice(this.mPrimaryAction.buildSlice(new Builder(getBuilder()).addHints("title")));
        }
    }

    public TemplateBuilderImpl createGridRowBuilder() {
        return new CellBuilder(this);
    }

    public TemplateBuilderImpl createGridRowBuilder(Uri uri) {
        return new CellBuilder(uri);
    }

    public void addCell(TemplateBuilderImpl builder) {
        builder.apply(getBuilder());
    }

    public void setSeeMoreCell(@NonNull TemplateBuilderImpl builder) {
        builder.getBuilder().addHints("see_more");
        builder.apply(getBuilder());
    }

    public void setSeeMoreAction(PendingIntent intent) {
        getBuilder().addSubSlice(new Builder(getBuilder()).addHints("see_more").addAction(intent, new Builder(getBuilder()).build(), null).build());
    }

    public void setPrimaryAction(SliceAction action) {
        this.mPrimaryAction = action;
    }

    public void setContentDescription(CharSequence description) {
        getBuilder().addText(description, "content_description", new String[0]);
    }

    public void setLayoutDirection(int layoutDirection) {
        getBuilder().addInt(layoutDirection, "layout_direction", new String[0]);
    }
}
