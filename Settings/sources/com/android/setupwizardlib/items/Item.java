package com.android.setupwizardlib.items;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.setupwizardlib.R;

public class Item extends AbstractItem {
    private boolean mEnabled;
    private Drawable mIcon;
    private int mLayoutRes;
    private CharSequence mSummary;
    private CharSequence mTitle;
    private boolean mVisible;

    public Item() {
        this.mEnabled = true;
        this.mVisible = true;
        this.mLayoutRes = getDefaultLayoutResource();
    }

    public Item(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mEnabled = true;
        this.mVisible = true;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SuwItem);
        this.mEnabled = a.getBoolean(R.styleable.SuwItem_android_enabled, true);
        this.mIcon = a.getDrawable(R.styleable.SuwItem_android_icon);
        this.mTitle = a.getText(R.styleable.SuwItem_android_title);
        this.mSummary = a.getText(R.styleable.SuwItem_android_summary);
        this.mLayoutRes = a.getResourceId(R.styleable.SuwItem_android_layout, getDefaultLayoutResource());
        this.mVisible = a.getBoolean(R.styleable.SuwItem_android_visible, true);
        a.recycle();
    }

    /* Access modifiers changed, original: protected */
    public int getDefaultLayoutResource() {
        return R.layout.suw_items_default;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
        notifyItemChanged();
    }

    public int getCount() {
        return isVisible();
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
        notifyItemChanged();
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public void setLayoutResource(int layoutResource) {
        this.mLayoutRes = layoutResource;
        notifyItemChanged();
    }

    public int getLayoutResource() {
        return this.mLayoutRes;
    }

    public void setSummary(CharSequence summary) {
        this.mSummary = summary;
        notifyItemChanged();
    }

    public CharSequence getSummary() {
        return this.mSummary;
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
        notifyItemChanged();
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public void setVisible(boolean visible) {
        if (this.mVisible != visible) {
            this.mVisible = visible;
            if (visible) {
                notifyItemRangeInserted(0, 1);
            } else {
                notifyItemRangeRemoved(0, 1);
            }
        }
    }

    public boolean isVisible() {
        return this.mVisible;
    }

    public int getViewId() {
        return getId();
    }

    public void onBindView(View view) {
        ((TextView) view.findViewById(R.id.suw_items_title)).setText(getTitle());
        TextView summaryView = (TextView) view.findViewById(R.id.suw_items_summary);
        CharSequence summary = getSummary();
        if (summary == null || summary.length() <= 0) {
            summaryView.setVisibility(8);
        } else {
            summaryView.setText(summary);
            summaryView.setVisibility(0);
        }
        View iconContainer = view.findViewById(R.id.suw_items_icon_container);
        Drawable icon = getIcon();
        if (icon != null) {
            ImageView iconView = (ImageView) view.findViewById(R.id.suw_items_icon);
            iconView.setImageDrawable(null);
            onMergeIconStateAndLevels(iconView, icon);
            iconView.setImageDrawable(icon);
            iconContainer.setVisibility(0);
        } else {
            iconContainer.setVisibility(8);
        }
        view.setId(getViewId());
    }

    /* Access modifiers changed, original: protected */
    public void onMergeIconStateAndLevels(ImageView iconView, Drawable icon) {
        iconView.setImageState(icon.getState(), false);
        iconView.setImageLevel(icon.getLevel());
    }
}
