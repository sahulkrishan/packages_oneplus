package com.oneplus.settings.ui;

import android.content.Context;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import com.android.settings.R;
import java.lang.reflect.Field;

public class OPRadioButtonPreferenceV7 extends CheckBoxPreference {
    public OPRadioButtonPreferenceV7(Context context) {
        super(context);
        initViews();
    }

    public OPRadioButtonPreferenceV7(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public OPRadioButtonPreferenceV7(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        setLayoutResource(R.layout.op_preference_material);
        setWidgetLayoutResource(R.layout.preference_widget_radiobutton);
        setCanRecycleLayout(true);
    }

    /* Access modifiers changed, original: protected */
    public void onClick() {
    }

    private void setCanRecycleLayout(boolean bCanRecycle) {
        try {
            Field canRecycleLayoutField = Preference.class.getDeclaredField("mCanRecycleLayout");
            canRecycleLayoutField.setAccessible(true);
            canRecycleLayoutField.setBoolean(this, bCanRecycle);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        }
    }
}
