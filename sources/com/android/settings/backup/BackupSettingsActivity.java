package com.android.settings.backup;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import java.util.ArrayList;
import java.util.List;

public class BackupSettingsActivity extends Activity implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        private static final String BACKUP_SEARCH_INDEX_KEY = "backup";

        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = context.getString(R.string.privacy_settings_title);
            data.screenTitle = context.getString(R.string.settings_label);
            data.keywords = context.getString(R.string.keywords_backup);
            data.intentTargetPackage = context.getPackageName();
            data.intentTargetClass = BackupSettingsActivity.class.getName();
            data.intentAction = "android.intent.action.MAIN";
            data.key = BACKUP_SEARCH_INDEX_KEY;
            result.add(data);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            if (UserHandle.myUserId() != 0) {
                if (Log.isLoggable(BackupSettingsActivity.TAG, 3)) {
                    Log.d(BackupSettingsActivity.TAG, "Not a system user, not indexing the screen");
                }
                keys.add(BACKUP_SEARCH_INDEX_KEY);
            }
            return keys;
        }
    };
    private static final String TAG = "BackupSettingsActivity";
    private FragmentManager mFragmentManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BackupSettingsHelper backupHelper = new BackupSettingsHelper(this);
        if (backupHelper.isBackupProvidedByManufacturer()) {
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "Manufacturer provided backup settings, showing the preference screen");
            }
            if (this.mFragmentManager == null) {
                this.mFragmentManager = getFragmentManager();
            }
            this.mFragmentManager.beginTransaction().replace(16908290, new BackupSettingsFragment()).commit();
            return;
        }
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "No manufacturer settings found, launching the backup settings directly");
        }
        Intent intent = backupHelper.getIntentForBackupSettings();
        try {
            getPackageManager().setComponentEnabledSetting(intent.getComponent(), 1, 1);
        } catch (SecurityException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Trying to enable activity ");
            stringBuilder.append(intent.getComponent());
            stringBuilder.append(" but couldn't: ");
            stringBuilder.append(e.getMessage());
            Log.w(str, stringBuilder.toString());
        }
        startActivityForResult(intent, 1);
        finish();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setFragmentManager(FragmentManager fragmentManager) {
        this.mFragmentManager = fragmentManager;
    }
}
