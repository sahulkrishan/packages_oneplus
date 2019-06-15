package com.android.settings.display;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import com.android.internal.app.ColorDisplayController;
import com.android.internal.app.ColorDisplayController.Callback;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class NightDisplaySettings extends DashboardFragment implements Callback, Indexable {
    private static final int DIALOG_END_TIME = 1;
    private static final int DIALOG_START_TIME = 0;
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.night_display_settings;
            result.add(sir);
            return result;
        }

        /* Access modifiers changed, original: protected */
        public boolean isPageSearchEnabled(Context context) {
            return ColorDisplayController.isAvailable(context);
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return NightDisplaySettings.buildPreferenceControllers(context);
        }
    };
    private static final String TAG = "NightDisplaySettings";
    private ColorDisplayController mController;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mController = new ColorDisplayController(getContext());
    }

    public void onStart() {
        super.onStart();
        this.mController.setListener(this);
    }

    public void onStop() {
        super.onStop();
        this.mController.setListener(null);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if ("night_display_end_time".equals(preference.getKey())) {
            showDialog(1);
            return true;
        } else if (!"night_display_start_time".equals(preference.getKey())) {
            return super.onPreferenceTreeClick(preference);
        } else {
            showDialog(0);
            return true;
        }
    }

    public Dialog onCreateDialog(int dialogId) {
        if (dialogId != 0 && dialogId != 1) {
            return super.onCreateDialog(dialogId);
        }
        LocalTime initialTime;
        if (dialogId == 0) {
            initialTime = this.mController.getCustomStartTime();
        } else {
            initialTime = this.mController.getCustomEndTime();
        }
        Context context = getContext();
        return new TimePickerDialog(context, new -$$Lambda$NightDisplaySettings$EHQrigX4B__bQ2Ww7B-DCA-KncQ(this, dialogId), initialTime.getHour(), initialTime.getMinute(), DateFormat.is24HourFormat(context));
    }

    public static /* synthetic */ void lambda$onCreateDialog$0(NightDisplaySettings nightDisplaySettings, int dialogId, TimePicker view, int hourOfDay, int minute) {
        LocalTime time = LocalTime.of(hourOfDay, minute);
        if (dialogId == 0) {
            nightDisplaySettings.mController.setCustomStartTime(time);
        } else {
            nightDisplaySettings.mController.setCustomEndTime(time);
        }
    }

    public int getDialogMetricsCategory(int dialogId) {
        switch (dialogId) {
            case 0:
                return 588;
            case 1:
                return 589;
            default:
                return 0;
        }
    }

    public void onActivated(boolean activated) {
        updatePreferenceStates();
    }

    public void onAutoModeChanged(int autoMode) {
        updatePreferenceStates();
    }

    public void onColorTemperatureChanged(int colorTemperature) {
        updatePreferenceStates();
    }

    public void onCustomStartTimeChanged(LocalTime startTime) {
        updatePreferenceStates();
    }

    public void onCustomEndTimeChanged(LocalTime endTime) {
        updatePreferenceStates();
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.night_display_settings;
    }

    public int getMetricsCategory() {
        return 488;
    }

    public int getHelpResource() {
        return R.string.help_url_night_display;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList(1);
        controllers.add(new NightDisplayFooterPreferenceController(context));
        return controllers;
    }
}
