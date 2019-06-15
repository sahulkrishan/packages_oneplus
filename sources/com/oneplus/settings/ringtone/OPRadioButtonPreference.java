package com.oneplus.settings.ringtone;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import com.android.settings.R;
import java.lang.reflect.Field;

public class OPRadioButtonPreference extends CheckBoxPreference {
    private RingData mData;

    public static class RingData {
        String mData;
        String mimetype;
        String title;

        public RingData(String t1, String t2, String t3) {
            this.mData = t1;
            this.title = t2;
            this.mimetype = t3;
        }
    }

    public OPRadioButtonPreference(Context context) {
        super(context);
        initViews();
    }

    public OPRadioButtonPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public OPRadioButtonPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        setWidgetLayoutResource(R.layout.preference_widget_radiobutton);
    }

    /* Access modifiers changed, original: protected */
    public void onClick() {
    }

    public void setData(RingData data) {
        this.mData = data;
    }

    public RingData getData() {
        return this.mData;
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
