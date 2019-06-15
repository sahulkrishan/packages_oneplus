package com.oneplus.lib.widget;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.oneplus.commonctrl.R;

public class OPToast extends Toast {
    public OPToast(Context context) {
        super(context);
    }

    public static OPToast makeText(Context context, CharSequence text, int duration) {
        OPToast result = new OPToast(context);
        LayoutInflater inflate = (LayoutInflater) context.getSystemService("layout_inflater");
        TypedArray a = context.obtainStyledAttributes(null, R.styleable.OPToast, R.attr.OPToastStyle, R.style.Oneplus_DeviceDefault_OPToast);
        int layoutResId = a.getResourceId(R.styleable.OPToast_android_layout, R.layout.op_transient_notification_light);
        a.recycle();
        View v = inflate.inflate(layoutResId, null);
        ((TextView) v.findViewById(16908299)).setText(text);
        setViewAndDuation(result, v, duration);
        return result;
    }

    public static OPToast makeText(Context context, int resId, int duration) throws NotFoundException {
        return makeText(context, context.getResources().getText(resId), duration);
    }

    private static void setViewAndDuation(OPToast toast, View v, int duration) {
        toast.setView(v);
        toast.setDuration(duration);
    }
}
