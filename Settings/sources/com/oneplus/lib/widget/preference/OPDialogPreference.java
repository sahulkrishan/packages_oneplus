package com.oneplus.lib.widget.preference;

import android.content.Context;
import android.util.AttributeSet;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.preference.DialogPreference;
import com.oneplus.lib.widget.util.utils;

public class OPDialogPreference extends DialogPreference {
    public OPDialogPreference(Context context) {
        this(context, null);
    }

    public OPDialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.op_dialogPreferenceStyle);
    }

    public OPDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.OnePlus_DeviceDefault_Preference_Material_DialogPreference);
    }

    public OPDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, utils.resolveDefStyleAttr(context, defStyleAttr), defStyleRes);
    }
}
