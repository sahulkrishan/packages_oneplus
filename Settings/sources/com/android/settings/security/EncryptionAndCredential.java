package com.android.settings.security;

import android.content.Context;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.widget.PreferenceCategoryController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EncryptionAndCredential extends DashboardFragment {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new SecuritySearchIndexProvider();
    private static final String TAG = "EncryptionAndCredential";

    private static class SecuritySearchIndexProvider extends BaseSearchIndexProvider {
        private SecuritySearchIndexProvider() {
        }

        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.encryption_and_credential;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return EncryptionAndCredential.buildPreferenceControllers(context, null);
        }

        /* Access modifiers changed, original: protected */
        public boolean isPageSearchEnabled(Context context) {
            return ((UserManager) context.getSystemService("user")).isAdminUser();
        }
    }

    public int getMetricsCategory() {
        return 846;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle());
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.encryption_and_credential;
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Lifecycle lifecycle) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new EncryptionStatusPreferenceController(context, "encryption_and_credentials_encryption_status"));
        controllers.add(new PreferenceCategoryController(context, "encryption_and_credentials_status_category").setChildren(Arrays.asList(new AbstractPreferenceController[]{encryptStatusController})));
        controllers.add(new CredentialStoragePreferenceController(context));
        controllers.add(new UserCredentialsPreferenceController(context));
        controllers.add(new ResetCredentialsPreferenceController(context, lifecycle));
        controllers.add(new InstallCredentialsPreferenceController(context));
        return controllers;
    }

    public int getHelpResource() {
        return R.string.help_url_encryption;
    }
}
