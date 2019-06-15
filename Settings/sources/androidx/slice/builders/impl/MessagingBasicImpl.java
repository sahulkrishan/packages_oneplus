package androidx.slice.builders.impl;

import android.graphics.drawable.Icon;
import android.os.Build.VERSION;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.IconCompat;
import androidx.slice.Slice.Builder;
import androidx.slice.SliceSpec;

@RestrictTo({Scope.LIBRARY})
public class MessagingBasicImpl extends TemplateBuilderImpl implements MessagingBuilder {
    private MessageBuilder mLastMessage;

    public static final class MessageBuilder extends TemplateBuilderImpl implements androidx.slice.builders.impl.MessagingBuilder.MessageBuilder {
        @RequiresApi(23)
        private Icon mIcon;
        private CharSequence mText;
        private long mTimestamp;

        public MessageBuilder(MessagingBasicImpl parent) {
            this(parent.createChildBuilder());
        }

        private MessageBuilder(Builder builder) {
            super(builder, null);
        }

        @RequiresApi(23)
        public void addSource(Icon source) {
            this.mIcon = source;
        }

        public void addText(CharSequence text) {
            this.mText = text;
        }

        public void addTimestamp(long timestamp) {
            this.mTimestamp = timestamp;
        }

        public void apply(Builder builder) {
        }
    }

    public MessagingBasicImpl(Builder builder, SliceSpec spec) {
        super(builder, spec);
    }

    public void apply(Builder builder) {
        if (this.mLastMessage != null) {
            if (VERSION.SDK_INT >= 23 && this.mLastMessage.mIcon != null) {
                builder.addIcon(IconCompat.createFromIcon(this.mLastMessage.mIcon), null, new String[0]);
            }
            if (this.mLastMessage.mText != null) {
                builder.addText(this.mLastMessage.mText, null, new String[0]);
            }
        }
    }

    public void add(TemplateBuilderImpl builder) {
        MessageBuilder b = (MessageBuilder) builder;
        if (this.mLastMessage == null || this.mLastMessage.mTimestamp < b.mTimestamp) {
            this.mLastMessage = b;
        }
    }

    public TemplateBuilderImpl createMessageBuilder() {
        return new MessageBuilder(this);
    }
}
