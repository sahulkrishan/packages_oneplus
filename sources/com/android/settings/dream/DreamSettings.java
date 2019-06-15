package com.android.settings.dream;

import android.content.Context;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.dream.DreamBackend;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DreamSettings extends DashboardFragment {
    static final String EITHER_CHARGING_OR_DOCKED = "either_charging_or_docked";
    static final String NEVER_DREAM = "never";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.dream_fragment_overview;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return DreamSettings.buildPreferenceControllers(context);
        }
    };
    private static final String TAG = "DreamSettings";
    static final String WHILE_CHARGING_ONLY = "while_charging_only";
    static final String WHILE_DOCKED_ONLY = "while_docked_only";

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0049 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004c A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004b A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004a A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0049 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004c A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004b A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004a A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0049 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004c A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004b A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004a A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0049 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004c A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004b A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004a A:{RETURN} */
    static int getSettingFromPrefKey(java.lang.String r6) {
        /*
        r0 = r6.hashCode();
        r1 = -1592701525; // 0xffffffffa1114dab float:-4.9230704E-19 double:NaN;
        r2 = 1;
        r3 = 2;
        r4 = 3;
        r5 = 0;
        if (r0 == r1) goto L_0x003b;
    L_0x000d:
        r1 = -294641318; // 0xffffffffee70215a float:-1.857918E28 double:NaN;
        if (r0 == r1) goto L_0x0031;
    L_0x0012:
        r1 = 104712844; // 0x63dca8c float:3.5695757E-35 double:5.1735019E-316;
        if (r0 == r1) goto L_0x0027;
    L_0x0017:
        r1 = 1019349036; // 0x3cc2082c float:0.023685537 double:5.0362534E-315;
        if (r0 == r1) goto L_0x001d;
    L_0x001c:
        goto L_0x0045;
    L_0x001d:
        r0 = "while_charging_only";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x0045;
    L_0x0025:
        r0 = r5;
        goto L_0x0046;
    L_0x0027:
        r0 = "never";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x0045;
    L_0x002f:
        r0 = r4;
        goto L_0x0046;
    L_0x0031:
        r0 = "either_charging_or_docked";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x0045;
    L_0x0039:
        r0 = r3;
        goto L_0x0046;
    L_0x003b:
        r0 = "while_docked_only";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x0045;
    L_0x0043:
        r0 = r2;
        goto L_0x0046;
    L_0x0045:
        r0 = -1;
    L_0x0046:
        switch(r0) {
            case 0: goto L_0x004c;
            case 1: goto L_0x004b;
            case 2: goto L_0x004a;
            default: goto L_0x0049;
        };
    L_0x0049:
        return r4;
    L_0x004a:
        return r3;
    L_0x004b:
        return r2;
    L_0x004c:
        return r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.dream.DreamSettings.getSettingFromPrefKey(java.lang.String):int");
    }

    static String getKeyFromSetting(int dreamSetting) {
        switch (dreamSetting) {
            case 0:
                return WHILE_CHARGING_ONLY;
            case 1:
                return WHILE_DOCKED_ONLY;
            case 2:
                return EITHER_CHARGING_OR_DOCKED;
            default:
                return NEVER_DREAM;
        }
    }

    static int getDreamSettingDescriptionResId(int dreamSetting) {
        switch (dreamSetting) {
            case 0:
                return R.string.screensaver_settings_summary_sleep;
            case 1:
                return R.string.screensaver_settings_summary_dock;
            case 2:
                return R.string.screensaver_settings_summary_either_long;
            default:
                return R.string.screensaver_settings_summary_never;
        }
    }

    public int getMetricsCategory() {
        return 47;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.dream_fragment_overview;
    }

    public int getHelpResource() {
        return R.string.help_url_screen_saver;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context);
    }

    public static CharSequence getSummaryTextWithDreamName(Context context) {
        return getSummaryTextFromBackend(DreamBackend.getInstance(context), context);
    }

    @VisibleForTesting
    static CharSequence getSummaryTextFromBackend(DreamBackend backend, Context context) {
        if (backend.isEnabled()) {
            return backend.getActiveDreamName();
        }
        return context.getString(R.string.screensaver_settings_summary_off);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new CurrentDreamPreferenceController(context));
        controllers.add(new WhenToDreamPreferenceController(context));
        controllers.add(new StartNowPreferenceController(context));
        return controllers;
    }
}
