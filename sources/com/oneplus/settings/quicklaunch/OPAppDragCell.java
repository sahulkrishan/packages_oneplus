package com.oneplus.settings.quicklaunch;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.settings.R;

class OPAppDragCell extends RelativeLayout {
    private ImageView mAppIcon;
    private CheckBox mCheckbox;
    private ImageView mDeleteButton;
    private ImageView mDragHandle;
    private TextView mLabel;
    private ImageView mSmallIcon;

    public OPAppDragCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* Access modifiers changed, original: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mLabel = (TextView) findViewById(R.id.label);
        this.mAppIcon = (ImageView) findViewById(R.id.quick_launch_app_icon);
        this.mSmallIcon = (ImageView) findViewById(R.id.small_icon);
        this.mCheckbox = (CheckBox) findViewById(R.id.checkbox);
        this.mDragHandle = (ImageView) findViewById(R.id.dragHandle);
        this.mDeleteButton = (ImageView) findViewById(R.id.delete_button);
    }

    public void setShowHandle(boolean showHandle) {
        this.mDragHandle.setVisibility(showHandle ? 0 : 4);
        invalidate();
        requestLayout();
    }

    public void setShowCheckbox(boolean showCheckbox) {
        if (showCheckbox) {
            this.mCheckbox.setVisibility(0);
            this.mLabel.setVisibility(4);
        } else {
            this.mCheckbox.setVisibility(4);
            this.mLabel.setVisibility(0);
        }
        invalidate();
        requestLayout();
    }

    public void setChecked(boolean checked) {
        this.mCheckbox.setChecked(checked);
    }

    public void setShowAppIcon(boolean showAppIcon) {
        this.mAppIcon.setVisibility(showAppIcon ? 0 : 8);
        invalidate();
        requestLayout();
    }

    public void setAppIcon(Drawable icon) {
        this.mAppIcon.setImageDrawable(icon);
        invalidate();
    }

    public void setSmallIcon(Drawable icon) {
        this.mSmallIcon.setImageDrawable(icon);
        invalidate();
    }

    public void setLabelAndDescription(String labelText, String description) {
        this.mLabel.setText(labelText);
        this.mCheckbox.setText(labelText);
        this.mLabel.setContentDescription(description);
        this.mCheckbox.setContentDescription(description);
        invalidate();
    }

    public ImageView getDragHandle() {
        return this.mDragHandle;
    }

    public CheckBox getCheckbox() {
        return this.mCheckbox;
    }

    public ImageView getDeleteButton() {
        return this.mDeleteButton;
    }
}
