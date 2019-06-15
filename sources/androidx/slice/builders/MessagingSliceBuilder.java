package androidx.slice.builders;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Consumer;
import androidx.slice.SliceSpecs;
import androidx.slice.builders.impl.MessagingBasicImpl;
import androidx.slice.builders.impl.MessagingBuilder;
import androidx.slice.builders.impl.MessagingListV1Impl;
import androidx.slice.builders.impl.MessagingV1Impl;
import androidx.slice.builders.impl.TemplateBuilderImpl;

@RestrictTo({Scope.LIBRARY_GROUP})
public class MessagingSliceBuilder extends TemplateSliceBuilder {
    public static final int MAXIMUM_RETAINED_MESSAGES = 50;
    private MessagingBuilder mBuilder;

    public static final class MessageBuilder extends TemplateSliceBuilder {
        private androidx.slice.builders.impl.MessagingBuilder.MessageBuilder mImpl;

        public MessageBuilder(MessagingSliceBuilder parent) {
            super(parent.mBuilder.createMessageBuilder());
        }

        @RequiresApi(23)
        public MessageBuilder addSource(Icon source) {
            this.mImpl.addSource(source);
            return this;
        }

        public MessageBuilder addSource(IconCompat source) {
            if (VERSION.SDK_INT >= 23) {
                this.mImpl.addSource(source.toIcon());
            }
            return this;
        }

        public MessageBuilder addText(CharSequence text) {
            this.mImpl.addText(text);
            return this;
        }

        public MessageBuilder addTimestamp(long timestamp) {
            this.mImpl.addTimestamp(timestamp);
            return this;
        }

        /* Access modifiers changed, original: 0000 */
        public void setImpl(TemplateBuilderImpl impl) {
            this.mImpl = (androidx.slice.builders.impl.MessagingBuilder.MessageBuilder) impl;
        }
    }

    public MessagingSliceBuilder(@NonNull Context context, @NonNull Uri uri) {
        super(context, uri);
    }

    public MessagingSliceBuilder add(MessageBuilder builder) {
        this.mBuilder.add((TemplateBuilderImpl) builder.mImpl);
        return this;
    }

    public MessagingSliceBuilder add(Consumer<MessageBuilder> c) {
        MessageBuilder b = new MessageBuilder(this);
        c.accept(b);
        return add(b);
    }

    /* Access modifiers changed, original: 0000 */
    public void setImpl(TemplateBuilderImpl impl) {
        this.mBuilder = (MessagingBuilder) impl;
    }

    /* Access modifiers changed, original: protected */
    @RestrictTo({Scope.LIBRARY})
    public TemplateBuilderImpl selectImpl() {
        if (checkCompatible(SliceSpecs.MESSAGING)) {
            return new MessagingV1Impl(getBuilder(), SliceSpecs.MESSAGING);
        }
        if (checkCompatible(SliceSpecs.LIST)) {
            return new MessagingListV1Impl(getBuilder(), SliceSpecs.LIST);
        }
        if (checkCompatible(SliceSpecs.BASIC)) {
            return new MessagingBasicImpl(getBuilder(), SliceSpecs.BASIC);
        }
        return null;
    }
}
