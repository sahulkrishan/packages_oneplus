package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;

public class TwoStateButtonPreference extends LayoutPreference implements OnClickListener {
    private final Button mButtonOff;
    private final Button mButtonOn;
    private boolean mIsChecked;

    public TwoStateButtonPreference(Context context, AttributeSet attrs) {
        super(context, attrs, TypedArrayUtils.getAttr(context, R.attr.twoStateButtonPreferenceStyle, 16842894));
        if (attrs == null) {
            this.mButtonOn = null;
            this.mButtonOff = null;
            return;
        }
        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.TwoStateButtonPreference);
        int textOnId = styledAttrs.getResourceId(1, R.string.summary_placeholder);
        int textOffId = styledAttrs.getResourceId(0, R.string.summary_placeholder);
        styledAttrs.recycle();
        this.mButtonOn = (Button) findViewById(R.id.state_on_button);
        this.mButtonOn.setText(textOnId);
        this.mButtonOn.setOnClickListener(this);
        this.mButtonOff = (Button) findViewById(R.id.state_off_button);
        this.mButtonOff.setText(textOffId);
        this.mButtonOff.setOnClickListener(this);
        setChecked(isChecked());
    }

    public void onClick(View v) {
        boolean stateOn = v.getId() == R.id.state_on_button;
        setChecked(stateOn);
        callChangeListener(Boolean.valueOf(stateOn));
    }

    public void setChecked(boolean checked) {
        this.mIsChecked = checked;
        if (checked) {
            this.mButtonOn.setVisibility(8);
            this.mButtonOff.setVisibility(0);
            return;
        }
        this.mButtonOn.setVisibility(0);
        this.mButtonOff.setVisibility(8);
    }

    public boolean isChecked() {
        return this.mIsChecked;
    }

    public void setButtonEnabled(boolean enabled) {
        this.mButtonOn.setEnabled(enabled);
        this.mButtonOff.setEnabled(enabled);
    }

    @VisibleForTesting
    public Button getStateOnButton() {
        return this.mButtonOn;
    }

    @VisibleForTesting
    public Button getStateOffButton() {
        return this.mButtonOff;
    }
}
