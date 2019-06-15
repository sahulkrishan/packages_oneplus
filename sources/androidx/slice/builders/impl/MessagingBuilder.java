package androidx.slice.builders.impl;

import android.graphics.drawable.Icon;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;

@RestrictTo({Scope.LIBRARY})
public interface MessagingBuilder {

    public interface MessageBuilder {
        @RequiresApi(23)
        void addSource(Icon icon);

        void addText(CharSequence charSequence);

        void addTimestamp(long j);
    }

    void add(TemplateBuilderImpl templateBuilderImpl);

    TemplateBuilderImpl createMessageBuilder();
}
