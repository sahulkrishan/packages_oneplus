package com.oneplus.lib.widget.button;

import android.content.Context;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.widget.util.utils;

public class OPCheckBox extends OPCompoundButton {
    public static String TAG = OPCheckBox.class.getSimpleName();

    public OPCheckBox(Context context) {
        this(context, null);
    }

    public OPCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 16842860);
    }

    public OPCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Oneplus_DeviceDefault_Widget_Material_CompoundButton_CheckBox);
    }

    public OPCheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, utils.resolveDefStyleAttr(context, defStyleAttr), defStyleRes);
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(OPCheckBox.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(OPCheckBox.class.getName());
    }

    public void setChecked(boolean checked) {
        setChecked(Boolean.valueOf(checked));
    }

    public void setChecked(Boolean checked) {
        setTriStateChecked(checked);
    }
}
