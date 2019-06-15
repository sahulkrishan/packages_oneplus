package com.android.setupwizardlib.template;

import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.setupwizardlib.R;
import com.android.setupwizardlib.TemplateLayout;

public class HeaderMixin implements Mixin {
    private TemplateLayout mTemplateLayout;

    public HeaderMixin(@NonNull TemplateLayout layout, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this.mTemplateLayout = layout;
        TypedArray a = layout.getContext().obtainStyledAttributes(attrs, R.styleable.SuwHeaderMixin, defStyleAttr, 0);
        CharSequence headerText = a.getText(R.styleable.SuwHeaderMixin_suwHeaderText);
        if (headerText != null) {
            setText(headerText);
        }
        a.recycle();
    }

    public TextView getTextView() {
        return (TextView) this.mTemplateLayout.findManagedViewById(R.id.suw_layout_title);
    }

    public void setText(int title) {
        TextView titleView = getTextView();
        if (titleView != null) {
            titleView.setText(title);
        }
    }

    public void setText(CharSequence title) {
        TextView titleView = getTextView();
        if (titleView != null) {
            titleView.setText(title);
        }
    }

    public CharSequence getText() {
        TextView titleView = getTextView();
        return titleView != null ? titleView.getText() : null;
    }
}
