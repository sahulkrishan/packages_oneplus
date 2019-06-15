package com.android.settings.wifi;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.settings.LinkifyUtils;
import com.android.settings.LinkifyUtils.OnClickListener;

public class LinkablePreference extends Preference {
    private OnClickListener mClickListener;
    private CharSequence mContentDescription;
    private CharSequence mContentTitle;

    public LinkablePreference(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
        setSelectable(false);
    }

    public LinkablePreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        setSelectable(false);
    }

    public LinkablePreference(Context ctx) {
        super(ctx);
        setSelectable(false);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        TextView textView = (TextView) view.findViewById(16908310);
        if (textView != null) {
            textView.setSingleLine(false);
            if (this.mContentTitle != null && this.mClickListener != null) {
                StringBuilder contentBuilder = new StringBuilder().append(this.mContentTitle);
                if (this.mContentDescription != null) {
                    contentBuilder.append("\n\n");
                    contentBuilder.append(this.mContentDescription);
                }
                if (LinkifyUtils.linkify(textView, contentBuilder, this.mClickListener) && this.mContentTitle != null) {
                    Spannable boldSpan = (Spannable) textView.getText();
                    boldSpan.setSpan(new TextAppearanceSpan(getContext(), 16973892), 0, this.mContentTitle.length(), 17);
                    textView.setText(boldSpan);
                    textView.setMovementMethod(new LinkMovementMethod());
                }
            }
        }
    }

    public void setText(CharSequence contentTitle, @Nullable CharSequence contentDescription, OnClickListener clickListener) {
        this.mContentTitle = contentTitle;
        this.mContentDescription = contentDescription;
        this.mClickListener = clickListener;
        super.setTitle(contentTitle);
    }

    public void setTitle(int titleResId) {
        this.mContentTitle = null;
        this.mContentDescription = null;
        super.setTitle(titleResId);
    }

    public void setTitle(CharSequence title) {
        this.mContentTitle = null;
        this.mContentDescription = null;
        super.setTitle(title);
    }
}
