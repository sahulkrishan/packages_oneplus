package com.android.settings.security.screenlock;

import android.app.Fragment;
import android.content.Context;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.security.OwnerInfoPreferenceController;
import com.android.settings.security.OwnerInfoPreferenceController.OwnerInfoCallback;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.List;

public class ScreenLockSettings extends DashboardFragment implements OwnerInfoCallback {
    private static final String KEY_LOCK_SCREEN_TITLE = "security_settings_password_sub_screen";
    private static final int MY_USER_ID = UserHandle.myUserId();
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.screen_lock_settings;
            result.add(sir);
            return result;
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return ScreenLockSettings.buildPreferenceControllers(context, null, null, new LockPatternUtils(context));
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add(ScreenLockSettings.KEY_LOCK_SCREEN_TITLE);
            return keys;
        }
    };
    private static final String TAG = "ScreenLockSettings";
    private LockPatternUtils mLockPatternUtils;

    public int getMetricsCategory() {
        return 1265;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.screen_lock_settings;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        this.mLockPatternUtils = new LockPatternUtils(context);
        return buildPreferenceControllers(context, this, getLifecycle(), this.mLockPatternUtils);
    }

    public void onOwnerInfoUpdated() {
        ((OwnerInfoPreferenceController) use(OwnerInfoPreferenceController.class)).updateSummary();
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Fragment parent, Lifecycle lifecycle, LockPatternUtils lockPatternUtils) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new PatternVisiblePreferenceController(context, MY_USER_ID, lockPatternUtils));
        controllers.add(new PowerButtonInstantLockPreferenceController(context, MY_USER_ID, lockPatternUtils));
        controllers.add(new LockAfterTimeoutPreferenceController(context, MY_USER_ID, lockPatternUtils));
        controllers.add(new OwnerInfoPreferenceController(context, parent, lifecycle));
        return controllers;
    }
}
