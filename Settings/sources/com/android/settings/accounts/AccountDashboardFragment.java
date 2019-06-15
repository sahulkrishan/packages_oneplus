package com.android.settings.accounts;

import android.content.Context;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.users.AutoSyncDataPreferenceController;
import com.android.settings.users.AutoSyncPersonalDataPreferenceController;
import com.android.settings.users.AutoSyncWorkDataPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AccountDashboardFragment extends DashboardFragment {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.accounts_dashboard_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList();
            context.getSystemService("user");
            if (Utils.getManagedProfile(UserManager.get(context)) == null) {
                result.add(AutoSyncWorkDataPreferenceController.KEY_AUTO_SYNC_WORK_ACCOUNT);
            }
            return result;
        }
    };
    private static final String TAG = "AccountDashboardFrag";

    public int getMetricsCategory() {
        return 8;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.accounts_dashboard_settings;
    }

    public int getHelpResource() {
        return R.string.help_url_user_and_account_dashboard;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        AccountPreferenceController accountPrefController = new AccountPreferenceController(context, this, getIntent().getStringArrayExtra(AccountPreferenceBase.AUTHORITIES_FILTER_KEY));
        getLifecycle().addObserver(accountPrefController);
        controllers.add(accountPrefController);
        controllers.add(new AutoSyncDataPreferenceController(context, this));
        controllers.add(new AutoSyncPersonalDataPreferenceController(context, this));
        controllers.add(new AutoSyncWorkDataPreferenceController(context, this));
        return controllers;
    }
}
