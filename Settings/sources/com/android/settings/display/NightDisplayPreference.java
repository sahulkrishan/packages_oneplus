package com.android.settings.display;

import android.content.Context;
import android.support.v14.preference.SwitchPreference;
import android.util.AttributeSet;
import com.android.internal.app.ColorDisplayController;
import com.android.internal.app.ColorDisplayController.Callback;
import java.time.LocalTime;

public class NightDisplayPreference extends SwitchPreference implements Callback {
    private ColorDisplayController mController;
    private NightDisplayTimeFormatter mTimeFormatter;

    public NightDisplayPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mController = new ColorDisplayController(context);
        this.mTimeFormatter = new NightDisplayTimeFormatter(context);
    }

    public void onAttached() {
        super.onAttached();
        this.mController.setListener(this);
        updateSummary();
    }

    public void onDetached() {
        super.onDetached();
        this.mController.setListener(null);
    }

    public void onActivated(boolean activated) {
        updateSummary();
    }

    public void onAutoModeChanged(int autoMode) {
        updateSummary();
    }

    public void onCustomStartTimeChanged(LocalTime startTime) {
        updateSummary();
    }

    public void onCustomEndTimeChanged(LocalTime endTime) {
        updateSummary();
    }

    private void updateSummary() {
        setSummary((CharSequence) this.mTimeFormatter.getAutoModeTimeSummary(getContext(), this.mController));
    }
}
