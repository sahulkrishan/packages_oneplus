package com.android.setupwizardlib.items;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.android.setupwizardlib.R;
import com.android.setupwizardlib.view.CheckableLinearLayout;

public class ExpandableSwitchItem extends SwitchItem implements OnCheckedChangeListener, OnClickListener {
    private CharSequence mCollapsedSummary;
    private CharSequence mExpandedSummary;
    private boolean mIsExpanded = false;

    public ExpandableSwitchItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SuwExpandableSwitchItem);
        this.mCollapsedSummary = a.getText(R.styleable.SuwExpandableSwitchItem_suwCollapsedSummary);
        this.mExpandedSummary = a.getText(R.styleable.SuwExpandableSwitchItem_suwExpandedSummary);
        a.recycle();
    }

    /* Access modifiers changed, original: protected */
    public int getDefaultLayoutResource() {
        return R.layout.suw_items_expandable_switch;
    }

    public CharSequence getSummary() {
        return this.mIsExpanded ? getExpandedSummary() : getCollapsedSummary();
    }

    public boolean isExpanded() {
        return this.mIsExpanded;
    }

    public void setExpanded(boolean expanded) {
        if (this.mIsExpanded != expanded) {
            this.mIsExpanded = expanded;
            notifyItemChanged();
        }
    }

    public CharSequence getCollapsedSummary() {
        return this.mCollapsedSummary;
    }

    public void setCollapsedSummary(CharSequence collapsedSummary) {
        this.mCollapsedSummary = collapsedSummary;
        if (!isExpanded()) {
            notifyChanged();
        }
    }

    public CharSequence getExpandedSummary() {
        return this.mExpandedSummary;
    }

    public void setExpandedSummary(CharSequence expandedSummary) {
        this.mExpandedSummary = expandedSummary;
        if (isExpanded()) {
            notifyChanged();
        }
    }

    public void onBindView(View view) {
        super.onBindView(view);
        View content = view.findViewById(R.id.suw_items_expandable_switch_content);
        content.setOnClickListener(this);
        if (content instanceof CheckableLinearLayout) {
            ((CheckableLinearLayout) content).setChecked(isExpanded());
        }
        tintCompoundDrawables(view);
        view.setFocusable(false);
    }

    public void onClick(View v) {
        setExpanded(isExpanded() ^ 1);
    }

    private void tintCompoundDrawables(View view) {
        TypedArray a = view.getContext();
        int[] iArr = new int[1];
        int i = 0;
        iArr[0] = 16842806;
        a = a.obtainStyledAttributes(iArr);
        ColorStateList tintColor = a.getColorStateList(0);
        a.recycle();
        if (tintColor != null) {
            int length;
            TextView titleView = (TextView) view.findViewById(R.id.suw_items_title);
            for (Drawable drawable : titleView.getCompoundDrawables()) {
                if (drawable != null) {
                    drawable.setColorFilter(tintColor.getDefaultColor(), Mode.SRC_IN);
                }
            }
            if (VERSION.SDK_INT >= 17) {
                Drawable[] compoundDrawablesRelative = titleView.getCompoundDrawablesRelative();
                length = compoundDrawablesRelative.length;
                while (i < length) {
                    Drawable drawable2 = compoundDrawablesRelative[i];
                    if (drawable2 != null) {
                        drawable2.setColorFilter(tintColor.getDefaultColor(), Mode.SRC_IN);
                    }
                    i++;
                }
            }
        }
    }
}
