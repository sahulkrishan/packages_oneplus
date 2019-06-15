package com.android.settings.display;

import android.app.TimePickerDialog.OnTimeSetListener;
import android.widget.TimePicker;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$NightDisplaySettings$EHQrigX4B__bQ2Ww7B-DCA-KncQ implements OnTimeSetListener {
    private final /* synthetic */ NightDisplaySettings f$0;
    private final /* synthetic */ int f$1;

    public /* synthetic */ -$$Lambda$NightDisplaySettings$EHQrigX4B__bQ2Ww7B-DCA-KncQ(NightDisplaySettings nightDisplaySettings, int i) {
        this.f$0 = nightDisplaySettings;
        this.f$1 = i;
    }

    public final void onTimeSet(TimePicker timePicker, int i, int i2) {
        NightDisplaySettings.lambda$onCreateDialog$0(this.f$0, this.f$1, timePicker, i, i2);
    }
}
