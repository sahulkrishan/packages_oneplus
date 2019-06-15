package com.android.settings.notification;

import android.content.Context;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.settings.R;

public class NotificationFooterPreference extends Preference {
    public NotificationFooterPreference(Context context, AttributeSet attrs) {
        super(context, attrs, TypedArrayUtils.getAttr(context, R.attr.footerPreferenceStyle, 16842894));
        init();
    }

    public NotificationFooterPreference(Context context) {
        this(context, null);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView title = (TextView) holder.itemView.findViewById(16908310);
        title.setMovementMethod(new LinkMovementMethod());
        title.setClickable(false);
        title.setLongClickable(false);
    }

    private void init() {
        setIcon((int) R.drawable.ic_info_outline_24dp);
        setSelectable(false);
    }
}
