package com.oneplus.lib.widget.button;

import android.content.Context;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.widget.util.utils;

public class OPRadioButton extends OPCompoundButton {
    public OPRadioButton(Context context) {
        this(context, null);
    }

    public OPRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, 16842878);
    }

    public OPRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Oneplus_DeviceDefault_Widget_Material_CompoundButton_RadioButton);
    }

    public OPRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, utils.resolveDefStyleAttr(context, defStyleAttr), defStyleRes);
    }

    public void toggle() {
        if (!isChecked()) {
            super.toggle();
        }
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(OPRadioButton.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(OPRadioButton.class.getName());
    }
}
