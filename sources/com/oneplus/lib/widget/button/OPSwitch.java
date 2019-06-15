package com.oneplus.lib.widget.button;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Switch;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.util.OPFeaturesUtils;
import com.oneplus.lib.util.VibratorSceneUtils;
import com.oneplus.lib.widget.util.utils;

public class OPSwitch extends Switch {
    public static String TAG = OPSwitch.class.getSimpleName();
    private long[] mVibratePattern;
    private Vibrator mVibrator;
    private int mVibratorSceneId;

    public OPSwitch(Context context) {
        this(context, null);
    }

    public OPSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 16843839);
    }

    public OPSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Oneplus_DeviceDefault_Widget_Material_CompoundButton_Switch);
    }

    public OPSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, utils.resolveDefStyleAttr(context, defStyleAttr), defStyleRes);
        this.mVibratorSceneId = 1003;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OPSwitch, defStyleAttr, defStyleRes);
        setRadius(a.getDimensionPixelSize(R.styleable.OPSwitch_android_radius, -1));
        a.recycle();
        if (OPFeaturesUtils.isSupportXVibrate()) {
            this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        }
    }

    public void setVibrateSceneId(int id) {
        this.mVibratorSceneId = id;
    }

    public boolean performClick() {
        if (VibratorSceneUtils.systemVibrateEnabled(getContext())) {
            this.mVibratePattern = VibratorSceneUtils.getVibratorScenePattern(getContext(), this.mVibrator, this.mVibratorSceneId);
            VibratorSceneUtils.vibrateIfNeeded(this.mVibratePattern, this.mVibrator);
        }
        return super.performClick();
    }

    public void setChecked(boolean checked) {
        super.setChecked(checked);
    }

    private void setRadius(int nRadius) {
        if (nRadius != -1) {
            Drawable background = getBackground();
            if (background == null || !(background instanceof RippleDrawable)) {
                Log.i(TAG, "setRaidus fail , background not a rippleDrawable");
            } else {
                background.mutate();
                ((RippleDrawable) background).setRadius(nRadius);
            }
        }
    }
}
