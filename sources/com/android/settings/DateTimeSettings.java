package com.android.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.datetime.AutoTimeFormatPreferenceController;
import com.android.settings.datetime.AutoTimePreferenceController;
import com.android.settings.datetime.AutoTimeZonePreferenceController;
import com.android.settings.datetime.DatePreferenceController;
import com.android.settings.datetime.DatePreferenceController.DatePreferenceHost;
import com.android.settings.datetime.TimeChangeListenerMixin;
import com.android.settings.datetime.TimeFormatPreferenceController;
import com.android.settings.datetime.TimePreferenceController;
import com.android.settings.datetime.TimePreferenceController.TimePreferenceHost;
import com.android.settings.datetime.TimeZonePreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.datetime.ZoneGetter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DateTimeSettings extends DashboardFragment implements TimePreferenceHost, DatePreferenceHost {
    protected static final String EXTRA_IS_FROM_SUW = "firstRun";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new DateTimeSearchIndexProvider();
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private static final String TAG = "DateTimeSettings";

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                Calendar now = Calendar.getInstance();
                this.mSummaryLoader.setSummary(this, ZoneGetter.getTimeZoneOffsetAndName(this.mContext, now.getTimeZone(), now.getTime()));
            }
        }
    }

    private static class DateTimeSearchIndexProvider extends BaseSearchIndexProvider {
        private DateTimeSearchIndexProvider() {
        }

        /* synthetic */ DateTimeSearchIndexProvider(AnonymousClass1 x0) {
            this();
        }

        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> result = new ArrayList();
            if (UserManager.isDeviceInDemoMode(context)) {
                return result;
            }
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.date_time_prefs;
            result.add(sir);
            return result;
        }
    }

    public int getMetricsCategory() {
        return 38;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.date_time_prefs;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        getLifecycle().addObserver(new TimeChangeListenerMixin(context, this));
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        Activity activity = getActivity();
        boolean isFromSUW = activity.getIntent().getBooleanExtra(EXTRA_IS_FROM_SUW, false);
        AutoTimeZonePreferenceController autoTimeZonePreferenceController = new AutoTimeZonePreferenceController(activity, this, isFromSUW);
        AutoTimePreferenceController autoTimePreferenceController = new AutoTimePreferenceController(activity, this);
        AutoTimeFormatPreferenceController autoTimeFormatPreferenceController = new AutoTimeFormatPreferenceController(activity, this);
        controllers.add(autoTimeZonePreferenceController);
        controllers.add(autoTimePreferenceController);
        controllers.add(autoTimeFormatPreferenceController);
        controllers.add(new TimeFormatPreferenceController(activity, this, isFromSUW));
        controllers.add(new TimeZonePreferenceController(activity, autoTimeZonePreferenceController));
        controllers.add(new TimePreferenceController(activity, this, autoTimePreferenceController));
        controllers.add(new DatePreferenceController(activity, this, autoTimePreferenceController));
        return controllers;
    }

    public void updateTimeAndDateDisplay(Context context) {
        updatePreferenceStates();
    }

    public Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                return ((DatePreferenceController) use(DatePreferenceController.class)).buildDatePicker(getActivity());
            case 1:
                return ((TimePreferenceController) use(TimePreferenceController.class)).buildTimePicker(getActivity());
            default:
                throw new IllegalArgumentException();
        }
    }

    public int getDialogMetricsCategory(int dialogId) {
        switch (dialogId) {
            case 0:
                return 607;
            case 1:
                return 608;
            default:
                return 0;
        }
    }

    public void showTimePicker() {
        removeDialog(1);
        showDialog(1);
    }

    public void showDatePicker() {
        showDialog(0);
    }
}
