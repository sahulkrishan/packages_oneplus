package com.oneplus.lib.widget.button;

import android.content.Context;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.widget.util.utils;

public class OPImageCheckBox extends OPCompoundButton {
    public static String TAG = OPImageCheckBox.class.getSimpleName();

    public OPImageCheckBox(Context context) {
        this(context, null);
    }

    public OPImageCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.OPImageCheckboxStyle);
    }

    public OPImageCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Oneplus_DeviceDefault_Widget_Material_ImageCompoundButton_CheckBox);
    }

    public OPImageCheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, utils.resolveDefStyleAttr(context, defStyleAttr), defStyleRes);
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(OPImageCheckBox.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(OPImageCheckBox.class.getName());
    }
}
