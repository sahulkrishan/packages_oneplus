package com.oneplus.lib.widget.button;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.widget.util.utils;

@RemoteView
public class OPButton extends TextView {
    public OPButton(Context context) {
        this(context, null);
    }

    public OPButton(Context context, AttributeSet attrs) {
        this(context, attrs, 16842824);
    }

    public OPButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.OnePlus_DeviceDefault_Widget_Material_Button);
    }

    public OPButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, utils.resolveDefStyleAttr(context, defStyleAttr), defStyleRes);
    }

    public CharSequence getAccessibilityClassName() {
        return OPButton.class.getName();
    }
}
