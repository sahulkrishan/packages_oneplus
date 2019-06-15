package androidx.slice.builders.impl;

import android.graphics.drawable.Icon;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import androidx.slice.Slice.Builder;
import androidx.slice.SliceSpec;
import androidx.slice.builders.impl.ListBuilderV1Impl.RowBuilderImpl;

@RestrictTo({Scope.LIBRARY})
public class MessagingListV1Impl extends TemplateBuilderImpl implements MessagingBuilder {
    private final ListBuilderV1Impl mListBuilder;

    public static final class MessageBuilder extends TemplateBuilderImpl implements androidx.slice.builders.impl.MessagingBuilder.MessageBuilder {
        private final RowBuilderImpl mListBuilder;

        public MessageBuilder(MessagingListV1Impl parent) {
            this(parent.createChildBuilder());
        }

        private MessageBuilder(Builder builder) {
            super(builder, null);
            this.mListBuilder = new RowBuilderImpl(builder);
        }

        @RequiresApi(23)
        public void addSource(Icon source) {
            this.mListBuilder.setTitleItem(IconCompat.createFromIcon(source), 1);
        }

        public void addText(CharSequence text) {
            this.mListBuilder.setSubtitle(text);
        }

        public void addTimestamp(long timestamp) {
            this.mListBuilder.addEndItem(timestamp);
        }

        public void apply(Builder builder) {
            this.mListBuilder.apply(builder);
        }
    }

    public MessagingListV1Impl(Builder b, SliceSpec spec) {
        super(b, spec);
        this.mListBuilder = new ListBuilderV1Impl(b, spec);
        this.mListBuilder.setTtl(-1);
    }

    public void add(TemplateBuilderImpl builder) {
        this.mListBuilder.addRow(((MessageBuilder) builder).mListBuilder);
    }

    public TemplateBuilderImpl createMessageBuilder() {
        return new MessageBuilder(this);
    }

    public void apply(Builder builder) {
        this.mListBuilder.apply(builder);
    }
}
