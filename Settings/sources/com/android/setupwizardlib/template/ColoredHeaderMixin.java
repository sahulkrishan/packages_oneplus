package com.android.setupwizardlib.template;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.setupwizardlib.R;
import com.android.setupwizardlib.TemplateLayout;

public class ColoredHeaderMixin extends HeaderMixin {
    public ColoredHeaderMixin(TemplateLayout layout, AttributeSet attrs, int defStyleAttr) {
        super(layout, attrs, defStyleAttr);
        TypedArray a = layout.getContext().obtainStyledAttributes(attrs, R.styleable.SuwColoredHeaderMixin, defStyleAttr, 0);
        ColorStateList headerColor = a.getColorStateList(R.styleable.SuwColoredHeaderMixin_suwHeaderColor);
        if (headerColor != null) {
            setColor(headerColor);
        }
        a.recycle();
    }

    public void setColor(ColorStateList color) {
        TextView titleView = getTextView();
        if (titleView != null) {
            titleView.setTextColor(color);
        }
    }

    public ColorStateList getColor() {
        TextView titleView = getTextView();
        return titleView != null ? titleView.getTextColors() : null;
    }
}
