package com.android.settings.widget;

import android.content.Context;
import android.support.v7.preference.PreferenceScreen;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public abstract class TwoStateButtonPreferenceController extends BasePreferenceController implements OnClickListener {
    private Button mButtonOff;
    private Button mButtonOn;

    public abstract void onButtonClicked(boolean z);

    public TwoStateButtonPreferenceController(Context context, String key) {
        super(context, key);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        TwoStateButtonPreference preference = (TwoStateButtonPreference) screen.findPreference(getPreferenceKey());
        this.mButtonOn = preference.getStateOnButton();
        this.mButtonOn.setOnClickListener(this);
        this.mButtonOff = preference.getStateOffButton();
        this.mButtonOff.setOnClickListener(this);
    }

    /* Access modifiers changed, original: protected */
    public void setButtonVisibility(boolean stateOn) {
        if (stateOn) {
            this.mButtonOff.setVisibility(8);
            this.mButtonOn.setVisibility(0);
            return;
        }
        this.mButtonOff.setVisibility(0);
        this.mButtonOn.setVisibility(8);
    }

    /* Access modifiers changed, original: protected */
    public void setButtonEnabled(boolean enabled) {
        this.mButtonOn.setEnabled(enabled);
        this.mButtonOff.setEnabled(enabled);
    }

    public void onClick(View v) {
        onButtonClicked(v.getId() == R.id.state_on_button);
    }
}
