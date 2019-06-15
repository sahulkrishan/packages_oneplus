package com.android.setupwizardlib.template;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.setupwizardlib.R;
import com.android.setupwizardlib.TemplateLayout;

public class IconMixin implements Mixin {
    private TemplateLayout mTemplateLayout;

    public IconMixin(TemplateLayout layout, AttributeSet attrs, int defStyleAttr) {
        this.mTemplateLayout = layout;
        TypedArray a = layout.getContext().obtainStyledAttributes(attrs, R.styleable.SuwIconMixin, defStyleAttr, 0);
        int icon = a.getResourceId(R.styleable.SuwIconMixin_android_icon, 0);
        if (icon != 0) {
            setIcon(icon);
        }
        a.recycle();
    }

    public void setIcon(Drawable icon) {
        ImageView iconView = getView();
        if (iconView != null) {
            iconView.setImageDrawable(icon);
            iconView.setVisibility(icon != null ? 0 : 8);
        }
    }

    public void setIcon(@DrawableRes int icon) {
        ImageView iconView = getView();
        if (iconView != null) {
            iconView.setImageResource(icon);
            iconView.setVisibility(icon != 0 ? 0 : 8);
        }
    }

    public Drawable getIcon() {
        ImageView iconView = getView();
        return iconView != null ? iconView.getDrawable() : null;
    }

    public void setContentDescription(CharSequence description) {
        ImageView iconView = getView();
        if (iconView != null) {
            iconView.setContentDescription(description);
        }
    }

    public CharSequence getContentDescription() {
        ImageView iconView = getView();
        return iconView != null ? iconView.getContentDescription() : null;
    }

    /* Access modifiers changed, original: protected */
    public ImageView getView() {
        return (ImageView) this.mTemplateLayout.findManagedViewById(R.id.suw_layout_icon);
    }
}
