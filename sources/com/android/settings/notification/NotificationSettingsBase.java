package com.android.settings.notification;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.arch.lifecycle.LifecycleObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.notification.NotificationBackend.AppRow;
import com.android.settings.widget.MasterCheckBoxPreference;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.oneplus.settings.utils.OPConstants;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class NotificationSettingsBase extends DashboardFragment {
    protected static final String ARG_FROM_SETTINGS = "fromSettings";
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "NotifiSettingsBase";
    protected AppRow mAppRow;
    protected Bundle mArgs;
    protected NotificationBackend mBackend = new NotificationBackend();
    protected NotificationChannel mChannel;
    protected Comparator<NotificationChannel> mChannelComparator = -$$Lambda$NotificationSettingsBase$-zFOM6q-03lCRFkOVmbrRVoBxkk.INSTANCE;
    protected NotificationChannelGroup mChannelGroup;
    protected Context mContext;
    protected List<NotificationPreferenceController> mControllers = new ArrayList();
    protected List<Preference> mDynamicPreferences = new ArrayList();
    protected ImportanceListener mImportanceListener = new ImportanceListener();
    protected Intent mIntent;
    protected boolean mListeningToPackageRemove;
    protected NotificationManager mNm;
    protected final BroadcastReceiver mPackageRemovedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getData().getSchemeSpecificPart();
            if (NotificationSettingsBase.this.mPkgInfo == null || TextUtils.equals(NotificationSettingsBase.this.mPkgInfo.packageName, packageName)) {
                if (NotificationSettingsBase.DEBUG) {
                    String str = NotificationSettingsBase.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Package (");
                    stringBuilder.append(packageName);
                    stringBuilder.append(") removed. RemovingNotificationSettingsBase.");
                    Log.d(str, stringBuilder.toString());
                }
                NotificationSettingsBase.this.onPackageRemoved();
            }
        }
    };
    protected String mPkg;
    protected PackageInfo mPkgInfo;
    protected PackageManager mPm;
    protected boolean mShowLegacyChannelConfig = false;
    protected EnforcedAdmin mSuspendedAppsAdmin;
    protected int mUid;
    protected int mUserId;

    protected class ImportanceListener {
        protected ImportanceListener() {
        }

        /* Access modifiers changed, original: protected */
        public void onImportanceChanged() {
            PreferenceScreen screen = NotificationSettingsBase.this.getPreferenceScreen();
            for (NotificationPreferenceController controller : NotificationSettingsBase.this.mControllers) {
                controller.displayPreference(screen);
            }
            NotificationSettingsBase.this.updatePreferenceStates();
            boolean hideDynamicFields = false;
            if (NotificationSettingsBase.this.mAppRow == null || NotificationSettingsBase.this.mAppRow.banned) {
                hideDynamicFields = true;
            } else if (NotificationSettingsBase.this.mChannel != null) {
                hideDynamicFields = NotificationSettingsBase.this.mChannel.getImportance() == 0;
            } else if (NotificationSettingsBase.this.mChannelGroup != null) {
                hideDynamicFields = NotificationSettingsBase.this.mChannelGroup.isBlocked();
            }
            for (Preference preference : NotificationSettingsBase.this.mDynamicPreferences) {
                NotificationSettingsBase.this.setVisible(NotificationSettingsBase.this.getPreferenceScreen(), preference, !hideDynamicFields);
            }
        }
    }

    public void onAttach(Context context) {
        String stringExtra;
        int intExtra;
        super.onAttach(context);
        this.mContext = getActivity();
        this.mIntent = getActivity().getIntent();
        this.mArgs = getArguments();
        this.mPm = getPackageManager();
        this.mNm = NotificationManager.from(this.mContext);
        if (this.mArgs == null || !this.mArgs.containsKey("package")) {
            stringExtra = this.mIntent.getStringExtra("android.provider.extra.APP_PACKAGE");
        } else {
            stringExtra = this.mArgs.getString("package");
        }
        this.mPkg = stringExtra;
        if (this.mArgs == null || !this.mArgs.containsKey("uid")) {
            intExtra = this.mIntent.getIntExtra("app_uid", -1);
        } else {
            intExtra = this.mArgs.getInt("uid");
        }
        this.mUid = intExtra;
        if (this.mUid < 0) {
            try {
                this.mUid = this.mPm.getPackageUid(this.mPkg, 0);
            } catch (NameNotFoundException e) {
            }
        }
        this.mPkgInfo = findPackageInfo(this.mPkg, this.mUid);
        this.mUserId = UserHandle.getUserId(this.mUid);
        this.mSuspendedAppsAdmin = RestrictedLockUtils.checkIfApplicationIsSuspended(this.mContext, this.mPkg, this.mUserId);
        loadChannel();
        loadAppRow();
        loadChannelGroup();
        collectConfigActivities();
        getLifecycle().addObserver((LifecycleObserver) use(HeaderPreferenceController.class));
        for (NotificationPreferenceController controller : this.mControllers) {
            controller.onResume(this.mAppRow, this.mChannel, this.mChannelGroup, this.mSuspendedAppsAdmin);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mIntent == null && this.mArgs == null) {
            Log.w(TAG, "No intent");
            toastAndFinish();
        } else if (this.mUid < 0 || TextUtils.isEmpty(this.mPkg) || this.mPkgInfo == null) {
            Log.w(TAG, "Missing package or uid or packageinfo");
            toastAndFinish();
        } else {
            startListeningToPackageRemove();
        }
    }

    public void onDestroy() {
        stopListeningToPackageRemove();
        super.onDestroy();
    }

    public void onResume() {
        super.onResume();
        if (this.mUid < 0 || TextUtils.isEmpty(this.mPkg) || this.mPkgInfo == null || this.mAppRow == null) {
            Log.w(TAG, "Missing package or uid or packageinfo");
            finish();
            return;
        }
        loadAppRow();
        if (this.mAppRow == null) {
            Log.w(TAG, "Can't load package");
            finish();
            return;
        }
        loadChannel();
        loadChannelGroup();
        collectConfigActivities();
    }

    private void loadChannel() {
        Intent intent = getActivity().getIntent();
        String str = null;
        String channelId = intent != null ? intent.getStringExtra("android.provider.extra.CHANNEL_ID") : null;
        if (channelId == null && intent != null) {
            Bundle args = intent.getBundleExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS);
            if (args != null) {
                str = args.getString("android.provider.extra.CHANNEL_ID");
            }
            channelId = str;
        }
        this.mChannel = this.mBackend.getChannel(this.mPkg, this.mUid, channelId);
    }

    private void loadAppRow() {
        if (this.mPkgInfo != null) {
            this.mAppRow = this.mBackend.loadAppRow(this.mContext, this.mPm, this.mPkgInfo);
        }
    }

    private void loadChannelGroup() {
        if (this.mAppRow != null) {
            boolean z = this.mBackend.onlyHasDefaultChannel(this.mAppRow.pkg, this.mAppRow.uid) || (this.mChannel != null && "miscellaneous".equals(this.mChannel.getId()));
            this.mShowLegacyChannelConfig = z;
            if (this.mShowLegacyChannelConfig) {
                this.mChannel = this.mBackend.getChannel(this.mAppRow.pkg, this.mAppRow.uid, "miscellaneous");
            }
            if (this.mChannel != null && !TextUtils.isEmpty(this.mChannel.getGroup())) {
                NotificationChannelGroup group = this.mBackend.getGroup(this.mPkg, this.mUid, this.mChannel.getGroup());
                if (group != null) {
                    this.mChannelGroup = group;
                }
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void toastAndFinish() {
        Toast.makeText(this.mContext, R.string.app_not_found_dlg_text, 0).show();
        getActivity().finish();
    }

    /* Access modifiers changed, original: protected */
    public void collectConfigActivities() {
        if (this.mAppRow != null) {
            Intent intent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.NOTIFICATION_PREFERENCES").setPackage(this.mAppRow.pkg);
            List<ResolveInfo> resolveInfos = this.mPm.queryIntentActivities(intent, 0);
            if (DEBUG) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Found ");
                stringBuilder.append(resolveInfos.size());
                stringBuilder.append(" preference activities");
                stringBuilder.append(resolveInfos.size() == 0 ? " ;_;" : "");
                Log.d(str, stringBuilder.toString());
            }
            for (ResolveInfo ri : resolveInfos) {
                ActivityInfo activityInfo = ri.activityInfo;
                if (this.mAppRow.settingsIntent == null) {
                    this.mAppRow.settingsIntent = intent.setPackage(null).setClassName(activityInfo.packageName, activityInfo.name);
                    if (this.mChannel != null) {
                        this.mAppRow.settingsIntent.putExtra("android.intent.extra.CHANNEL_ID", this.mChannel.getId());
                    }
                    if (this.mChannelGroup != null) {
                        this.mAppRow.settingsIntent.putExtra("android.intent.extra.CHANNEL_GROUP_ID", this.mChannelGroup.getId());
                    }
                } else if (DEBUG) {
                    String str2 = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Ignoring duplicate notification preference activity (");
                    stringBuilder2.append(activityInfo.name);
                    stringBuilder2.append(") for package ");
                    stringBuilder2.append(activityInfo.packageName);
                    Log.d(str2, stringBuilder2.toString());
                }
            }
        }
    }

    private PackageInfo findPackageInfo(String pkg, int uid) {
        if (pkg == null || uid < 0) {
            return null;
        }
        String[] packages = this.mPm.getPackagesForUid(uid);
        if (!(packages == null || pkg == null)) {
            int N = packages.length;
            int i = 0;
            while (i < N) {
                if (pkg.equals(packages[i])) {
                    try {
                        return this.mPm.getPackageInfo(pkg, 64);
                    } catch (NameNotFoundException e) {
                        String str = TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Failed to load package ");
                        stringBuilder.append(pkg);
                        Log.w(str, stringBuilder.toString(), e);
                    }
                } else {
                    i++;
                }
            }
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public Preference populateSingleChannelPrefs(PreferenceGroup parent, final NotificationChannel channel, boolean groupBlocked) {
        MasterCheckBoxPreference channelPref = new MasterCheckBoxPreference(getPrefContext());
        boolean z = false;
        if (OPConstants.PACKAGENAME_DESKCOLCK.equals(this.mPkg) || OPConstants.PACKAGENAME_INCALLUI.equals(this.mPkg) || "com.google.android.calendar".equals(this.mPkg) || "com.oneplus.calendar".equals(this.mPkg)) {
            channelPref.setCheckBoxEnabled(false);
        } else {
            boolean z2 = this.mSuspendedAppsAdmin == null && isChannelBlockable(channel) && isChannelConfigurable(channel) && !groupBlocked;
            channelPref.setCheckBoxEnabled(z2);
        }
        channelPref.setKey(channel.getId());
        channelPref.setTitle(channel.getName());
        if (channel.getImportance() != 0) {
            z = true;
        }
        channelPref.setChecked(z);
        Bundle channelArgs = new Bundle();
        channelArgs.putInt("uid", this.mUid);
        channelArgs.putString("package", this.mPkg);
        channelArgs.putString("android.provider.extra.CHANNEL_ID", channel.getId());
        channelArgs.putBoolean(ARG_FROM_SETTINGS, true);
        channelPref.setIntent(new SubSettingLauncher(getActivity()).setDestination(ChannelNotificationSettings.class.getName()).setArguments(channelArgs).setTitle((int) R.string.notification_channel_title).setSourceMetricsCategory(getMetricsCategory()).toIntent());
        channelPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                channel.setImportance(((Boolean) o).booleanValue() ? 2 : 0);
                channel.lockFields(4);
                NotificationSettingsBase.this.mBackend.updateChannel(NotificationSettingsBase.this.mPkg, NotificationSettingsBase.this.mUid, channel);
                return true;
            }
        });
        parent.addPreference(channelPref);
        return channelPref;
    }

    /* Access modifiers changed, original: protected */
    public boolean isChannelConfigurable(NotificationChannel channel) {
        if (channel == null || this.mAppRow == null) {
            return false;
        }
        return channel.getId().equals(this.mAppRow.lockedChannelId) ^ 1;
    }

    /* Access modifiers changed, original: protected */
    public boolean isChannelBlockable(NotificationChannel channel) {
        boolean z = false;
        if (channel == null || this.mAppRow == null) {
            return false;
        }
        if (!this.mAppRow.systemApp) {
            return true;
        }
        if (channel.isBlockableSystem() || channel.getImportance() == 0) {
            z = true;
        }
        return z;
    }

    /* Access modifiers changed, original: protected */
    public boolean isChannelGroupBlockable(NotificationChannelGroup group) {
        if (group == null || this.mAppRow == null) {
            return false;
        }
        if (this.mAppRow.systemApp) {
            return group.isBlocked();
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void setVisible(Preference p, boolean visible) {
        setVisible(getPreferenceScreen(), p, visible);
    }

    /* Access modifiers changed, original: protected */
    public void setVisible(PreferenceGroup parent, Preference p, boolean visible) {
        if ((parent.findPreference(p.getKey()) != null) != visible) {
            if (visible) {
                parent.addPreference(p);
            } else {
                parent.removePreference(p);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void startListeningToPackageRemove() {
        if (!this.mListeningToPackageRemove) {
            this.mListeningToPackageRemove = true;
            IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_REMOVED");
            filter.addDataScheme("package");
            getContext().registerReceiver(this.mPackageRemovedReceiver, filter);
        }
    }

    /* Access modifiers changed, original: protected */
    public void stopListeningToPackageRemove() {
        if (this.mListeningToPackageRemove) {
            this.mListeningToPackageRemove = false;
            getContext().unregisterReceiver(this.mPackageRemovedReceiver);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onPackageRemoved() {
        getActivity().finishAndRemoveTask();
    }

    static /* synthetic */ int lambda$new$0(NotificationChannel left, NotificationChannel right) {
        if (left.isDeleted() != right.isDeleted()) {
            return Boolean.compare(left.isDeleted(), right.isDeleted());
        }
        if (left.getId().equals("miscellaneous")) {
            return 1;
        }
        if (right.getId().equals("miscellaneous")) {
            return -1;
        }
        return left.getId().compareTo(right.getId());
    }
}
