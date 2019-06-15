package com.android.settings.datetime.timezone;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.PreferenceCategory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.datetime.timezone.TimeZoneInfo.Formatter;
import com.android.settings.datetime.timezone.model.FilteredCountryTimeZones;
import com.android.settings.datetime.timezone.model.TimeZoneData;
import com.android.settings.datetime.timezone.model.TimeZoneDataLoader.LoaderCreator;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class TimeZoneSettings extends DashboardFragment {
    private static final int MENU_BY_OFFSET = 2;
    private static final int MENU_BY_REGION = 1;
    private static final String PREF_KEY_FIXED_OFFSET_CATEGORY = "time_zone_fixed_offset_preference_category";
    private static final String PREF_KEY_REGION = "time_zone_region";
    private static final String PREF_KEY_REGION_CATEGORY = "time_zone_region_preference_category";
    private static final int REQUEST_CODE_FIXED_OFFSET_ZONE_PICKER = 3;
    private static final int REQUEST_CODE_REGION_PICKER = 1;
    private static final int REQUEST_CODE_ZONE_PICKER = 2;
    private static final String TAG = "TimeZoneSettings";
    private Locale mLocale;
    private boolean mSelectByRegion;
    private String mSelectedTimeZoneId;
    private TimeZoneData mTimeZoneData;
    private Formatter mTimeZoneInfoFormatter;

    public int getMetricsCategory() {
        return 515;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.time_zone_prefs;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    @VisibleForTesting
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        this.mLocale = context.getResources().getConfiguration().getLocales().get(0);
        this.mTimeZoneInfoFormatter = new Formatter(this.mLocale, new Date());
        List<AbstractPreferenceController> controllers = new ArrayList();
        RegionPreferenceController regionPreferenceController = new RegionPreferenceController(context);
        regionPreferenceController.setOnClickListener(new -$$Lambda$TimeZoneSettings$vqMeoCUXFQsF8oLE4z3Gn5iFYMM(this));
        RegionZonePreferenceController regionZonePreferenceController = new RegionZonePreferenceController(context);
        regionZonePreferenceController.setOnClickListener(new -$$Lambda$TimeZoneSettings$fBefFKEAVxzXT5oriz7X9NJj6a0(this));
        FixedOffsetPreferenceController fixedOffsetPreferenceController = new FixedOffsetPreferenceController(context);
        fixedOffsetPreferenceController.setOnClickListener(new -$$Lambda$TimeZoneSettings$Ah3tL-2LTanl7tTAw64r8xCK07o(this));
        controllers.add(regionPreferenceController);
        controllers.add(regionZonePreferenceController);
        controllers.add(fixedOffsetPreferenceController);
        return controllers;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setPreferenceCategoryVisible((PreferenceCategory) findPreference(PREF_KEY_REGION_CATEGORY), false);
        setPreferenceCategoryVisible((PreferenceCategory) findPreference(PREF_KEY_FIXED_OFFSET_CATEGORY), false);
        getLoaderManager().initLoader(0, null, new LoaderCreator(getContext(), new -$$Lambda$TimeZoneSettings$CFHMJtb3KFCwNTuhyOFedUZcT20(this)));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 && data != null) {
            String regionId;
            switch (requestCode) {
                case 1:
                case 2:
                    regionId = data.getStringExtra(BaseTimeZonePicker.EXTRA_RESULT_REGION_ID);
                    String tzId = data.getStringExtra(BaseTimeZonePicker.EXTRA_RESULT_TIME_ZONE_ID);
                    if (!(Objects.equals(regionId, ((RegionPreferenceController) use(RegionPreferenceController.class)).getRegionId()) && Objects.equals(tzId, this.mSelectedTimeZoneId))) {
                        onRegionZoneChanged(regionId, tzId);
                        break;
                    }
                case 3:
                    regionId = data.getStringExtra(BaseTimeZonePicker.EXTRA_RESULT_TIME_ZONE_ID);
                    if (!(regionId == null || regionId.equals(this.mSelectedTimeZoneId))) {
                        onFixedOffsetZoneChanged(regionId);
                        break;
                    }
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setTimeZoneData(TimeZoneData timeZoneData) {
        this.mTimeZoneData = timeZoneData;
    }

    private void onTimeZoneDataReady(TimeZoneData timeZoneData) {
        if (this.mTimeZoneData == null && timeZoneData != null) {
            this.mTimeZoneData = timeZoneData;
            setupForCurrentTimeZone();
            getActivity().invalidateOptionsMenu();
        }
    }

    private void startRegionPicker() {
        startPickerFragment(RegionSearchPicker.class, new Bundle(), 1);
    }

    private void onRegionZonePreferenceClicked() {
        Bundle args = new Bundle();
        args.putString(RegionZonePicker.EXTRA_REGION_ID, ((RegionPreferenceController) use(RegionPreferenceController.class)).getRegionId());
        startPickerFragment(RegionZonePicker.class, args, 2);
    }

    private void startFixedOffsetPicker() {
        startPickerFragment(FixedOffsetPicker.class, new Bundle(), 3);
    }

    private void startPickerFragment(Class<? extends BaseTimeZonePicker> fragmentClass, Bundle args, int resultRequestCode) {
        new SubSettingLauncher(getContext()).setDestination(fragmentClass.getCanonicalName()).setArguments(args).setSourceMetricsCategory(getMetricsCategory()).setResultListener(this, resultRequestCode).launch();
    }

    private void setDisplayedRegion(String regionId) {
        ((RegionPreferenceController) use(RegionPreferenceController.class)).setRegionId(regionId);
        updatePreferenceStates();
    }

    private void setDisplayedTimeZoneInfo(String regionId, String tzId) {
        TimeZoneInfo tzInfo = tzId == null ? null : this.mTimeZoneInfoFormatter.format(tzId);
        FilteredCountryTimeZones countryTimeZones = this.mTimeZoneData.lookupCountryTimeZones(regionId);
        ((RegionZonePreferenceController) use(RegionZonePreferenceController.class)).setTimeZoneInfo(tzInfo);
        RegionZonePreferenceController regionZonePreferenceController = (RegionZonePreferenceController) use(RegionZonePreferenceController.class);
        boolean z = true;
        if (tzInfo != null && (countryTimeZones == null || countryTimeZones.getTimeZoneIds().size() <= 1)) {
            z = false;
        }
        regionZonePreferenceController.setClickable(z);
        ((TimeZoneInfoPreferenceController) use(TimeZoneInfoPreferenceController.class)).setTimeZoneInfo(tzInfo);
        updatePreferenceStates();
    }

    private void setDisplayedFixedOffsetTimeZoneInfo(String tzId) {
        if (isFixedOffset(tzId)) {
            ((FixedOffsetPreferenceController) use(FixedOffsetPreferenceController.class)).setTimeZoneInfo(this.mTimeZoneInfoFormatter.format(tzId));
        } else {
            ((FixedOffsetPreferenceController) use(FixedOffsetPreferenceController.class)).setTimeZoneInfo(null);
        }
        updatePreferenceStates();
    }

    private void onRegionZoneChanged(String regionId, String tzId) {
        FilteredCountryTimeZones countryTimeZones = this.mTimeZoneData.lookupCountryTimeZones(regionId);
        if (countryTimeZones == null || !countryTimeZones.getTimeZoneIds().contains(tzId)) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unknown time zone id is selected: ");
            stringBuilder.append(tzId);
            Log.e(str, stringBuilder.toString());
            return;
        }
        this.mSelectedTimeZoneId = tzId;
        setDisplayedRegion(regionId);
        setDisplayedTimeZoneInfo(regionId, this.mSelectedTimeZoneId);
        saveTimeZone(regionId, this.mSelectedTimeZoneId);
        setSelectByRegion(true);
    }

    private void onFixedOffsetZoneChanged(String tzId) {
        this.mSelectedTimeZoneId = tzId;
        setDisplayedFixedOffsetTimeZoneInfo(tzId);
        saveTimeZone(null, this.mSelectedTimeZoneId);
        setSelectByRegion(false);
    }

    private void saveTimeZone(String regionId, String tzId) {
        Editor editor = getPreferenceManager().getSharedPreferences().edit();
        if (regionId == null) {
            editor.remove(PREF_KEY_REGION);
        } else {
            editor.putString(PREF_KEY_REGION, regionId);
        }
        editor.apply();
        ((AlarmManager) getActivity().getSystemService(AlarmManager.class)).setTimeZone(tzId);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 1, 0, R.string.zone_menu_by_region);
        menu.add(0, 2, 0, R.string.zone_menu_by_offset);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        boolean z = true;
        MenuItem findItem = menu.findItem(1);
        boolean z2 = (this.mTimeZoneData == null || this.mSelectByRegion) ? false : true;
        findItem.setVisible(z2);
        findItem = menu.findItem(2);
        if (this.mTimeZoneData == null || !this.mSelectByRegion) {
            z = false;
        }
        findItem.setVisible(z);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                startRegionPicker();
                return true;
            case 2:
                startFixedOffsetPicker();
                return true;
            default:
                return false;
        }
    }

    private void setupForCurrentTimeZone() {
        this.mSelectedTimeZoneId = TimeZone.getDefault().getID();
        setSelectByRegion(isFixedOffset(this.mSelectedTimeZoneId) ^ 1);
    }

    private static boolean isFixedOffset(String tzId) {
        return tzId.startsWith("Etc/GMT") || tzId.equals("Etc/UTC");
    }

    private void setSelectByRegion(boolean selectByRegion) {
        this.mSelectByRegion = selectByRegion;
        setPreferenceCategoryVisible((PreferenceCategory) findPreference(PREF_KEY_REGION_CATEGORY), selectByRegion);
        setPreferenceCategoryVisible((PreferenceCategory) findPreference(PREF_KEY_FIXED_OFFSET_CATEGORY), selectByRegion ^ 1);
        String localeRegionId = getLocaleRegionId();
        String displayRegion = this.mTimeZoneData.getRegionIds().contains(localeRegionId) ? localeRegionId : null;
        setDisplayedRegion(displayRegion);
        setDisplayedTimeZoneInfo(displayRegion, null);
        if (this.mSelectByRegion) {
            String regionId = findRegionIdForTzId(this.mSelectedTimeZoneId);
            if (regionId != null) {
                setDisplayedRegion(regionId);
                setDisplayedTimeZoneInfo(regionId, this.mSelectedTimeZoneId);
            }
            return;
        }
        setDisplayedFixedOffsetTimeZoneInfo(this.mSelectedTimeZoneId);
    }

    private String findRegionIdForTzId(String tzId) {
        return findRegionIdForTzId(tzId, getPreferenceManager().getSharedPreferences().getString(PREF_KEY_REGION, null), getLocaleRegionId());
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String findRegionIdForTzId(String tzId, String sharePrefRegionId, String localeRegionId) {
        Set<String> matchedRegions = this.mTimeZoneData.lookupCountryCodesForZoneId(tzId);
        if (matchedRegions.size() == 0) {
            return null;
        }
        if (sharePrefRegionId != null && matchedRegions.contains(sharePrefRegionId)) {
            return sharePrefRegionId;
        }
        if (localeRegionId == null || !matchedRegions.contains(localeRegionId)) {
            return ((String[]) matchedRegions.toArray(new String[matchedRegions.size()]))[0];
        }
        return localeRegionId;
    }

    private void setPreferenceCategoryVisible(PreferenceCategory category, boolean isVisible) {
        category.setVisible(isVisible);
        for (int i = 0; i < category.getPreferenceCount(); i++) {
            category.getPreference(i).setVisible(isVisible);
        }
    }

    private String getLocaleRegionId() {
        return this.mLocale.getCountry().toUpperCase(Locale.US);
    }
}
