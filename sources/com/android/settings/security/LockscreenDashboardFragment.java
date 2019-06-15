package com.android.settings.security;

import android.content.Context;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.notification.LockScreenNotificationPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.security.OwnerInfoPreferenceController.OwnerInfoCallback;
import com.android.settings.users.AddUserWhenLockedPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LockscreenDashboardFragment extends DashboardFragment implements OwnerInfoCallback {
    @VisibleForTesting
    static final String KEY_ADD_USER_FROM_LOCK_SCREEN = "security_lockscreen_add_users_when_locked";
    @VisibleForTesting
    static final String KEY_LOCK_SCREEN_NOTIFICATON = "security_setting_lock_screen_notif";
    @VisibleForTesting
    static final String KEY_LOCK_SCREEN_NOTIFICATON_WORK_PROFILE = "security_setting_lock_screen_notif_work";
    @VisibleForTesting
    static final String KEY_LOCK_SCREEN_NOTIFICATON_WORK_PROFILE_HEADER = "security_setting_lock_screen_notif_work_header";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.security_lockscreen_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            List<AbstractPreferenceController> controllers = new ArrayList();
            controllers.add(new LockScreenNotificationPreferenceController(context));
            controllers.add(new AddUserWhenLockedPreferenceController(context, LockscreenDashboardFragment.KEY_ADD_USER_FROM_LOCK_SCREEN, null));
            controllers.add(new OwnerInfoPreferenceController(context, null, null));
            controllers.add(new LockdownButtonPreferenceController(context));
            return controllers;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> niks = super.getNonIndexableKeys(context);
            niks.add(LockscreenDashboardFragment.KEY_ADD_USER_FROM_LOCK_SCREEN);
            niks.add(LockscreenDashboardFragment.KEY_LOCK_SCREEN_NOTIFICATON_WORK_PROFILE);
            if (Utils.getManagedProfile(UserManager.get(context)) == null) {
                niks.add(LockscreenDashboardFragment.KEY_LOCK_SCREEN_NOTIFICATON_WORK_PROFILE_HEADER);
            }
            return niks;
        }
    };
    private static final String TAG = "LockscreenDashboardFragment";
    private OwnerInfoPreferenceController mOwnerInfoPreferenceController;

    public int getMetricsCategory() {
        return 882;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.security_lockscreen_settings;
    }

    public int getHelpResource() {
        return R.string.help_url_lockscreen;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        Lifecycle lifecycle = getLifecycle();
        LockScreenNotificationPreferenceController notificationController = new LockScreenNotificationPreferenceController(context, KEY_LOCK_SCREEN_NOTIFICATON, KEY_LOCK_SCREEN_NOTIFICATON_WORK_PROFILE_HEADER, KEY_LOCK_SCREEN_NOTIFICATON_WORK_PROFILE);
        lifecycle.addObserver(notificationController);
        controllers.add(notificationController);
        controllers.add(new AddUserWhenLockedPreferenceController(context, KEY_ADD_USER_FROM_LOCK_SCREEN, lifecycle));
        this.mOwnerInfoPreferenceController = new OwnerInfoPreferenceController(context, this, lifecycle);
        controllers.add(this.mOwnerInfoPreferenceController);
        controllers.add(new LockdownButtonPreferenceController(context));
        return controllers;
    }

    public void onOwnerInfoUpdated() {
        if (this.mOwnerInfoPreferenceController != null) {
            this.mOwnerInfoPreferenceController.updateSummary();
        }
    }
}
