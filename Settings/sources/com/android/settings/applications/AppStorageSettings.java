package com.android.settings.applications;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppGlobals;
import android.app.GrantedUriPermission;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver.Stub;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.util.Log;
import android.util.MutableInt;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.StorageWizardMoveConfirm;
import com.android.settings.widget.ActionButtonPreference;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.android.settingslib.applications.StorageStatsSource;
import com.android.settingslib.applications.StorageStatsSource.AppStorageStats;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

public class AppStorageSettings extends AppInfoWithHeader implements OnClickListener, Callbacks, DialogInterface.OnClickListener, LoaderCallbacks<AppStorageStats> {
    private static final int DLG_CANNOT_CLEAR_DATA = 2;
    private static final int DLG_CLEAR_DATA = 1;
    private static final String KEY_APP_SIZE = "app_size";
    private static final String KEY_CACHE_CLEARED = "cache_cleared";
    private static final String KEY_CACHE_SIZE = "cache_size";
    private static final String KEY_CHANGE_STORAGE = "change_storage_button";
    private static final String KEY_CLEAR_URI = "clear_uri_button";
    private static final String KEY_DATA_CLEARED = "data_cleared";
    private static final String KEY_DATA_SIZE = "data_size";
    private static final String KEY_HEADER_BUTTONS = "header_view";
    private static final String KEY_STORAGE_CATEGORY = "storage_category";
    private static final String KEY_STORAGE_SPACE = "storage_space";
    private static final String KEY_STORAGE_USED = "storage_used";
    private static final String KEY_TOTAL_SIZE = "total_size";
    private static final String KEY_URI_CATEGORY = "uri_category";
    private static final int MSG_CLEAR_CACHE = 3;
    private static final int MSG_CLEAR_USER_DATA = 1;
    private static final int OP_FAILED = 2;
    private static final int OP_SUCCESSFUL = 1;
    public static final int REQUEST_MANAGE_SPACE = 2;
    private static final int SIZE_INVALID = -1;
    private static final String TAG = AppStorageSettings.class.getSimpleName();
    @VisibleForTesting
    ActionButtonPreference mButtonsPref;
    private boolean mCacheCleared;
    private boolean mCanClearData = true;
    private VolumeInfo[] mCandidates;
    private Button mChangeStorageButton;
    private ClearCacheObserver mClearCacheObserver;
    private ClearUserDataObserver mClearDataObserver;
    private LayoutPreference mClearUri;
    private Button mClearUriButton;
    private boolean mDataCleared;
    private Builder mDialogBuilder;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (AppStorageSettings.this.getView() != null) {
                int i = msg.what;
                if (i == 1) {
                    AppStorageSettings.this.mDataCleared = true;
                    AppStorageSettings.this.mCacheCleared = true;
                    AppStorageSettings.this.processClearMsg(msg);
                } else if (i == 3) {
                    AppStorageSettings.this.mCacheCleared = true;
                    AppStorageSettings.this.updateSize();
                }
            }
        }
    };
    private ApplicationInfo mInfo;
    @VisibleForTesting
    AppStorageSizesController mSizeController;
    private Preference mStorageUsed;
    private PreferenceCategory mUri;

    class ClearCacheObserver extends Stub {
        ClearCacheObserver() {
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) {
            Message msg = AppStorageSettings.this.mHandler.obtainMessage(3);
            msg.arg1 = succeeded ? 1 : 2;
            AppStorageSettings.this.mHandler.sendMessage(msg);
        }
    }

    class ClearUserDataObserver extends Stub {
        ClearUserDataObserver() {
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) {
            int i = 1;
            Message msg = AppStorageSettings.this.mHandler.obtainMessage(1);
            if (!succeeded) {
                i = 2;
            }
            msg.arg1 = i;
            AppStorageSettings.this.mHandler.sendMessage(msg);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            boolean z = false;
            this.mCacheCleared = savedInstanceState.getBoolean(KEY_CACHE_CLEARED, false);
            this.mDataCleared = savedInstanceState.getBoolean(KEY_DATA_CLEARED, false);
            if (this.mCacheCleared || this.mDataCleared) {
                z = true;
            }
            this.mCacheCleared = z;
        }
        addPreferencesFromResource(R.xml.app_storage_settings);
        setupViews();
        initMoveDialog();
    }

    public void onResume() {
        super.onResume();
        updateSize();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_CACHE_CLEARED, this.mCacheCleared);
        outState.putBoolean(KEY_DATA_CLEARED, this.mDataCleared);
    }

    private void setupViews() {
        this.mSizeController = new AppStorageSizesController.Builder().setTotalSizePreference(findPreference(KEY_TOTAL_SIZE)).setAppSizePreference(findPreference(KEY_APP_SIZE)).setDataSizePreference(findPreference(KEY_DATA_SIZE)).setCacheSizePreference(findPreference(KEY_CACHE_SIZE)).setComputingString(R.string.computing_size).setErrorString(R.string.invalid_size_value).build();
        this.mButtonsPref = ((ActionButtonPreference) findPreference(KEY_HEADER_BUTTONS)).setButton1Positive(false).setButton2Positive(false);
        this.mStorageUsed = findPreference(KEY_STORAGE_USED);
        this.mChangeStorageButton = (Button) ((LayoutPreference) findPreference(KEY_CHANGE_STORAGE)).findViewById(R.id.button);
        this.mChangeStorageButton.setText(R.string.change);
        this.mChangeStorageButton.setOnClickListener(this);
        this.mButtonsPref.setButton2Text(R.string.clear_cache_btn_text);
        this.mUri = (PreferenceCategory) findPreference(KEY_URI_CATEGORY);
        this.mClearUri = (LayoutPreference) this.mUri.findPreference(KEY_CLEAR_URI);
        this.mClearUriButton = (Button) this.mClearUri.findViewById(R.id.button);
        this.mClearUriButton.setText(R.string.clear_uri_btn_text);
        this.mClearUriButton.setOnClickListener(this);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void handleClearCacheClick() {
        if (this.mAppsControlDisallowedAdmin == null || this.mAppsControlDisallowedBySystem) {
            if (this.mClearCacheObserver == null) {
                this.mClearCacheObserver = new ClearCacheObserver();
            }
            this.mMetricsFeatureProvider.action(getContext(), 877, new Pair[0]);
            this.mPm.deleteApplicationCacheFiles(this.mPackageName, this.mClearCacheObserver);
            return;
        }
        RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(), this.mAppsControlDisallowedAdmin);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void handleClearDataClick() {
        if (this.mAppsControlDisallowedAdmin != null && !this.mAppsControlDisallowedBySystem) {
            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(), this.mAppsControlDisallowedAdmin);
        } else if (this.mAppEntry.info.manageSpaceActivityName == null) {
            showDialogInner(1, 0);
        } else if (!Utils.isMonkeyRunning()) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setClassName(this.mAppEntry.info.packageName, this.mAppEntry.info.manageSpaceActivityName);
            startActivityForResult(intent, 2);
        }
    }

    public void onClick(View v) {
        if (v == this.mChangeStorageButton && this.mDialogBuilder != null && !isMoveInProgress()) {
            this.mDialogBuilder.show();
        } else if (v != this.mClearUriButton) {
        } else {
            if (this.mAppsControlDisallowedAdmin == null || this.mAppsControlDisallowedBySystem) {
                clearUriPermissions();
            } else {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(), this.mAppsControlDisallowedAdmin);
            }
        }
    }

    private boolean isMoveInProgress() {
        try {
            AppGlobals.getPackageManager().checkPackageStartable(this.mPackageName, UserHandle.myUserId());
            return false;
        } catch (RemoteException | SecurityException e) {
            return true;
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        Context context = getActivity();
        VolumeInfo targetVol = this.mCandidates[which];
        if (!Objects.equals(targetVol, context.getPackageManager().getPackageCurrentVolume(this.mAppEntry.info))) {
            Intent intent = new Intent(context, StorageWizardMoveConfirm.class);
            intent.putExtra("android.os.storage.extra.VOLUME_ID", targetVol.getId());
            intent.putExtra("android.intent.extra.PACKAGE_NAME", this.mAppEntry.info.packageName);
            startActivity(intent);
        }
        dialog.dismiss();
    }

    /* Access modifiers changed, original: protected */
    public boolean refreshUi() {
        retrieveAppEntry();
        if (this.mAppEntry == null) {
            return false;
        }
        updateUiWithSize(this.mSizeController.getLastResult());
        refreshGrantedUriPermissions();
        StorageManager storage = (StorageManager) getContext().getSystemService(StorageManager.class);
        this.mStorageUsed.setSummary(storage.getBestVolumeDescription(getActivity().getPackageManager().getPackageCurrentVolume(this.mAppEntry.info)));
        refreshButtons();
        return true;
    }

    private void refreshButtons() {
        initMoveDialog();
        initDataButtons();
    }

    private void initDataButtons() {
        boolean isManageSpaceActivityAvailable = true;
        boolean appHasSpaceManagementUI = this.mAppEntry.info.manageSpaceActivityName != null;
        boolean appRestrictsClearingData = ((this.mAppEntry.info.flags & 65) == 1) || this.mDpm.packageHasActiveAdmins(this.mPackageName);
        Intent intent = new Intent("android.intent.action.VIEW");
        if (appHasSpaceManagementUI) {
            intent.setClassName(this.mAppEntry.info.packageName, this.mAppEntry.info.manageSpaceActivityName);
        }
        if (getPackageManager().resolveActivity(intent, 0) == null) {
            isManageSpaceActivityAvailable = false;
        }
        if ((appHasSpaceManagementUI || !appRestrictsClearingData) && isManageSpaceActivityAvailable) {
            if (appHasSpaceManagementUI) {
                this.mButtonsPref.setButton1Text(R.string.manage_space_text);
            } else {
                this.mButtonsPref.setButton1Text(R.string.clear_user_data_text);
            }
            this.mButtonsPref.setButton1Text(R.string.clear_user_data_text).setButton1OnClickListener(new -$$Lambda$AppStorageSettings$uXyfUeZFqT2Ct1euRP3fPo2Es3o(this));
        } else {
            this.mButtonsPref.setButton1Text(R.string.clear_user_data_text).setButton1Enabled(false);
            this.mCanClearData = false;
        }
        if (this.mAppsControlDisallowedBySystem) {
            this.mButtonsPref.setButton1Enabled(false);
        }
    }

    private void initMoveDialog() {
        Context context = getActivity();
        StorageManager storage = (StorageManager) context.getSystemService(StorageManager.class);
        List<VolumeInfo> candidates = context.getPackageManager().getPackageCandidateVolumes(this.mAppEntry.info);
        if (candidates.size() > 1) {
            Collections.sort(candidates, VolumeInfo.getDescriptionComparator());
            CharSequence[] labels = new CharSequence[candidates.size()];
            int current = -1;
            for (int i = 0; i < candidates.size(); i++) {
                String volDescrip = storage.getBestVolumeDescription((VolumeInfo) candidates.get(i));
                if (Objects.equals(volDescrip, this.mStorageUsed.getSummary())) {
                    current = i;
                }
                labels[i] = volDescrip;
            }
            this.mCandidates = (VolumeInfo[]) candidates.toArray(new VolumeInfo[candidates.size()]);
            this.mDialogBuilder = new Builder(getContext()).setTitle(R.string.change_storage).setSingleChoiceItems(labels, current, this).setNegativeButton(R.string.cancel, null);
            return;
        }
        removePreference(KEY_STORAGE_USED);
        removePreference(KEY_CHANGE_STORAGE);
        removePreference(KEY_STORAGE_SPACE);
    }

    private void initiateClearUserData() {
        this.mMetricsFeatureProvider.action(getContext(), 876, new Pair[0]);
        this.mButtonsPref.setButton1Enabled(false);
        String packageName = this.mAppEntry.info.packageName;
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Clearing user data for package : ");
        stringBuilder.append(packageName);
        Log.i(str, stringBuilder.toString());
        if (this.mClearDataObserver == null) {
            this.mClearDataObserver = new ClearUserDataObserver();
        }
        if (((ActivityManager) getActivity().getSystemService("activity")).clearApplicationUserData(packageName, this.mClearDataObserver)) {
            this.mButtonsPref.setButton1Text(R.string.recompute_size);
            return;
        }
        String str2 = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("Couldn't clear application user data for package:");
        stringBuilder2.append(packageName);
        Log.i(str2, stringBuilder2.toString());
        showDialogInner(2, 0);
    }

    private void processClearMsg(Message msg) {
        int result = msg.arg1;
        String packageName = this.mAppEntry.info.packageName;
        this.mButtonsPref.setButton1Text(R.string.clear_user_data_text);
        if (result == 1) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cleared user data for package : ");
            stringBuilder.append(packageName);
            Log.i(str, stringBuilder.toString());
            updateSize();
            return;
        }
        this.mButtonsPref.setButton1Enabled(true);
    }

    private void refreshGrantedUriPermissions() {
        removeUriPermissionsFromUi();
        List<GrantedUriPermission> perms = ((ActivityManager) getActivity().getSystemService("activity")).getGrantedUriPermissions(this.mAppEntry.info.packageName).getList();
        if (perms.isEmpty()) {
            this.mClearUriButton.setVisibility(8);
            return;
        }
        PackageManager pm = getActivity().getPackageManager();
        Map<CharSequence, MutableInt> uriCounters = new TreeMap();
        for (GrantedUriPermission perm : perms) {
            CharSequence app = pm.resolveContentProvider(perm.uri.getAuthority(), 0).applicationInfo.loadLabel(pm);
            MutableInt count = (MutableInt) uriCounters.get(app);
            if (count == null) {
                uriCounters.put(app, new MutableInt(1));
            } else {
                count.value++;
            }
        }
        for (Entry<CharSequence, MutableInt> entry : uriCounters.entrySet()) {
            int numberResources = ((MutableInt) entry.getValue()).value;
            Preference pref = new Preference(getPrefContext());
            pref.setTitle((CharSequence) entry.getKey());
            pref.setSummary(getPrefContext().getResources().getQuantityString(R.plurals.uri_permissions_text, numberResources, new Object[]{Integer.valueOf(numberResources)}));
            pref.setSelectable(false);
            pref.setLayoutResource(R.layout.horizontal_preference);
            pref.setOrder(0);
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Adding preference '");
            stringBuilder.append(pref);
            stringBuilder.append("' at order ");
            stringBuilder.append(0);
            Log.v(str, stringBuilder.toString());
            this.mUri.addPreference(pref);
        }
        if (this.mAppsControlDisallowedBySystem) {
            this.mClearUriButton.setEnabled(false);
        }
        this.mClearUri.setOrder(0);
        this.mClearUriButton.setVisibility(0);
    }

    private void clearUriPermissions() {
        Context context = getActivity();
        String packageName = this.mAppEntry.info.packageName;
        ((ActivityManager) context.getSystemService("activity")).clearGrantedUriPermissions(packageName);
        Uri providerUri = new Uri.Builder().scheme("content").authority("com.android.documentsui.scopedAccess").appendPath("permissions").appendPath("*").build();
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Asking ");
        stringBuilder.append(providerUri);
        stringBuilder.append(" to delete permissions for ");
        stringBuilder.append(packageName);
        Log.v(str, stringBuilder.toString());
        int deleted = context.getContentResolver().delete(providerUri, null, new String[]{packageName});
        String str2 = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("Deleted ");
        stringBuilder2.append(deleted);
        stringBuilder2.append(" entries for package ");
        stringBuilder2.append(packageName);
        Log.d(str2, stringBuilder2.toString());
        refreshGrantedUriPermissions();
    }

    private void removeUriPermissionsFromUi() {
        for (int i = this.mUri.getPreferenceCount() - 1; i >= 0; i--) {
            Preference pref = this.mUri.getPreference(i);
            if (pref != this.mClearUri) {
                this.mUri.removePreference(pref);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public AlertDialog createDialog(int id, int errorCode) {
        switch (id) {
            case 1:
                return new Builder(getActivity()).setTitle(getActivity().getText(R.string.clear_data_dlg_title)).setMessage(getActivity().getText(R.string.clear_data_dlg_text)).setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AppStorageSettings.this.initiateClearUserData();
                    }
                }).setNegativeButton(R.string.dlg_cancel, null).create();
            case 2:
                return new Builder(getActivity()).setTitle(getActivity().getText(R.string.clear_user_data_text)).setMessage(getActivity().getText(R.string.clear_failed_dlg_text)).setNeutralButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AppStorageSettings.this.mButtonsPref.setButton1Enabled(false);
                        AppStorageSettings.this.setIntentAndFinish(false, false);
                    }
                }).create();
            default:
                return null;
        }
    }

    public void onPackageSizeChanged(String packageName) {
    }

    public Loader<AppStorageStats> onCreateLoader(int id, Bundle args) {
        Context context = getContext();
        return new FetchPackageStorageAsyncLoader(context, new StorageStatsSource(context), this.mInfo, UserHandle.of(this.mUserId));
    }

    public void onLoadFinished(Loader<AppStorageStats> loader, AppStorageStats result) {
        this.mSizeController.setResult(result);
        updateUiWithSize(result);
    }

    public void onLoaderReset(Loader<AppStorageStats> loader) {
    }

    private void updateSize() {
        try {
            this.mInfo = getPackageManager().getApplicationInfo(this.mPackageName, 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Could not find package", e);
        }
        if (this.mInfo != null) {
            getLoaderManager().restartLoader(1, Bundle.EMPTY, this);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateUiWithSize(AppStorageStats result) {
        if (this.mCacheCleared) {
            this.mSizeController.setCacheCleared(true);
        }
        if (this.mDataCleared) {
            this.mSizeController.setDataCleared(true);
        }
        this.mSizeController.updateUi(getContext());
        if (result == null) {
            this.mButtonsPref.setButton1Enabled(false).setButton2Enabled(false);
        } else {
            long cacheSize = result.getCacheBytes();
            if (result.getDataBytes() - cacheSize <= 0 || !this.mCanClearData || this.mDataCleared) {
                this.mButtonsPref.setButton1Enabled(false);
            } else {
                this.mButtonsPref.setButton1Enabled(true).setButton1OnClickListener(new -$$Lambda$AppStorageSettings$n1EpAla7gNI7Nnl-O3UD0UWSgTo(this));
            }
            if (cacheSize <= 0 || this.mCacheCleared) {
                this.mButtonsPref.setButton2Enabled(false);
            } else {
                this.mButtonsPref.setButton2Enabled(true).setButton2OnClickListener(new -$$Lambda$AppStorageSettings$DjRyx_XFfzsxe3o1nZS2usao_fc(this));
            }
        }
        if (this.mAppsControlDisallowedBySystem) {
            this.mButtonsPref.setButton1Enabled(false).setButton2Enabled(false);
        }
    }

    public int getMetricsCategory() {
        return 19;
    }
}
