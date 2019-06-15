package com.android.settings.notification;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.widget.MasterCheckBoxPreference;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppNotificationSettings extends NotificationSettingsBase {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static String KEY_ADVANCED_CATEGORY = "app_advanced";
    private static String KEY_APP_LINK = "app_link";
    private static String KEY_BADGE = "badge";
    private static String KEY_GENERAL_CATEGORY = "categories";
    private static final String TAG = "AppNotificationSettings";
    private Comparator<NotificationChannelGroup> mChannelGroupComparator = new Comparator<NotificationChannelGroup>() {
        public int compare(NotificationChannelGroup left, NotificationChannelGroup right) {
            if (left.getId() == null && right.getId() != null) {
                return 1;
            }
            if (right.getId() != null || left.getId() == null) {
                return left.getId().compareTo(right.getId());
            }
            return -1;
        }
    };
    private List<NotificationChannelGroup> mChannelGroupList;

    public int getMetricsCategory() {
        return 72;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceScreen screen = getPreferenceScreen();
        if (this.mShowLegacyChannelConfig && screen != null) {
            Preference badge = findPreference(KEY_BADGE);
            Preference appLink = findPreference(KEY_APP_LINK);
            removePreference(KEY_ADVANCED_CATEGORY);
            if (badge != null) {
                screen.addPreference(badge);
            }
            if (appLink != null) {
                screen.addPreference(appLink);
            }
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mUid < 0 || TextUtils.isEmpty(this.mPkg) || this.mPkgInfo == null) {
            Log.w(TAG, "Missing package or uid or packageinfo");
            finish();
            return;
        }
        if (!this.mShowLegacyChannelConfig) {
            new AsyncTask<Void, Void, Void>() {
                /* Access modifiers changed, original: protected|varargs */
                public Void doInBackground(Void... unused) {
                    AppNotificationSettings.this.mChannelGroupList = AppNotificationSettings.this.mBackend.getGroups(AppNotificationSettings.this.mPkg, AppNotificationSettings.this.mUid).getList();
                    Collections.sort(AppNotificationSettings.this.mChannelGroupList, AppNotificationSettings.this.mChannelGroupComparator);
                    return null;
                }

                /* Access modifiers changed, original: protected */
                public void onPostExecute(Void unused) {
                    if (AppNotificationSettings.this.getHost() != null) {
                        AppNotificationSettings.this.populateList();
                    }
                }
            }.execute(new Void[0]);
        }
        for (NotificationPreferenceController controller : this.mControllers) {
            controller.onResume(this.mAppRow, this.mChannel, this.mChannelGroup, this.mSuspendedAppsAdmin);
            controller.displayPreference(getPreferenceScreen());
        }
        updatePreferenceStates();
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.app_notification_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        this.mControllers = new ArrayList();
        this.mControllers.add(new HeaderPreferenceController(context, this));
        this.mControllers.add(new BlockPreferenceController(context, this.mImportanceListener, this.mBackend));
        this.mControllers.add(new BadgePreferenceController(context, this.mBackend));
        this.mControllers.add(new AllowSoundPreferenceController(context, this.mImportanceListener, this.mBackend));
        this.mControllers.add(new ImportancePreferenceController(context, this.mImportanceListener, this.mBackend));
        this.mControllers.add(new SoundPreferenceController(context, this, this.mImportanceListener, this.mBackend));
        this.mControllers.add(new LightsPreferenceController(context, this.mBackend));
        this.mControllers.add(new VibrationPreferenceController(context, this.mBackend));
        this.mControllers.add(new VisibilityPreferenceController(context, new LockPatternUtils(context), this.mBackend));
        this.mControllers.add(new DndPreferenceController(context, this.mBackend));
        this.mControllers.add(new AppLinkPreferenceController(context));
        this.mControllers.add(new DescriptionPreferenceController(context));
        this.mControllers.add(new NotificationsOffPreferenceController(context));
        this.mControllers.add(new DeletedChannelsPreferenceController(context, this.mBackend));
        return new ArrayList(this.mControllers);
    }

    private void populateList() {
        Preference p;
        if (!this.mDynamicPreferences.isEmpty()) {
            for (Preference p2 : this.mDynamicPreferences) {
                getPreferenceScreen().removePreference(p2);
            }
            this.mDynamicPreferences.clear();
        }
        if (this.mChannelGroupList.isEmpty()) {
            PreferenceCategory groupCategory = new PreferenceCategory(getPrefContext());
            groupCategory.setTitle((int) R.string.notification_channels);
            groupCategory.setKey(KEY_GENERAL_CATEGORY);
            getPreferenceScreen().addPreference(groupCategory);
            this.mDynamicPreferences.add(groupCategory);
            p2 = new Preference(getPrefContext());
            p2.setTitle((int) R.string.no_channels);
            p2.setEnabled(false);
            groupCategory.addPreference(p2);
            return;
        }
        populateGroupList();
        this.mImportanceListener.onImportanceChanged();
    }

    private void populateGroupList() {
        for (NotificationChannelGroup group : this.mChannelGroupList) {
            PreferenceCategory groupCategory = new PreferenceCategory(getPrefContext());
            groupCategory.setOrderingAsAdded(true);
            getPreferenceScreen().addPreference(groupCategory);
            this.mDynamicPreferences.add(groupCategory);
            if (group.getId() == null) {
                if (this.mChannelGroupList.size() > 1) {
                    groupCategory.setTitle((int) R.string.notification_channels_other);
                }
                groupCategory.setKey(KEY_GENERAL_CATEGORY);
            } else {
                groupCategory.setTitle(group.getName());
                groupCategory.setKey(group.getId());
                populateGroupToggle(groupCategory, group);
            }
            if (!group.isBlocked()) {
                List<NotificationChannel> channels = group.getChannels();
                Collections.sort(channels, this.mChannelComparator);
                int N = channels.size();
                for (int i = 0; i < N; i++) {
                    populateSingleChannelPrefs(groupCategory, (NotificationChannel) channels.get(i), group.isBlocked());
                }
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void populateGroupToggle(PreferenceGroup parent, NotificationChannelGroup group) {
        boolean z;
        RestrictedSwitchPreference preference = new RestrictedSwitchPreference(getPrefContext());
        preference.setTitle((int) R.string.notification_switch_label);
        if (this.mSuspendedAppsAdmin == null && isChannelGroupBlockable(group)) {
            z = true;
        } else {
            z = false;
        }
        preference.setEnabled(z);
        preference.setChecked(group.isBlocked() ^ 1);
        preference.setOnPreferenceClickListener(new -$$Lambda$AppNotificationSettings$KKPiatF9s2jsC7BTjM3YfK_E8S4(this, group));
        parent.addPreference(preference);
    }

    public static /* synthetic */ boolean lambda$populateGroupToggle$0(AppNotificationSettings appNotificationSettings, NotificationChannelGroup group, Preference preference1) {
        group.setBlocked(((SwitchPreference) preference1).isChecked() ^ 1);
        appNotificationSettings.mBackend.updateChannelGroup(appNotificationSettings.mAppRow.pkg, appNotificationSettings.mAppRow.uid, group);
        appNotificationSettings.onGroupBlockStateChanged(group);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void onGroupBlockStateChanged(NotificationChannelGroup group) {
        if (group != null) {
            PreferenceGroup groupGroup = (PreferenceGroup) getPreferenceScreen().findPreference(group.getId());
            if (groupGroup != null) {
                int i = 0;
                int childCount;
                if (group.isBlocked()) {
                    Preference pref;
                    List<Preference> toRemove = new ArrayList();
                    childCount = groupGroup.getPreferenceCount();
                    while (i < childCount) {
                        pref = groupGroup.getPreference(i);
                        if (pref instanceof MasterCheckBoxPreference) {
                            toRemove.add(pref);
                        }
                        i++;
                    }
                    for (Preference pref2 : toRemove) {
                        groupGroup.removePreference(pref2);
                    }
                } else {
                    List<NotificationChannel> channels = group.getChannels();
                    Collections.sort(channels, this.mChannelComparator);
                    childCount = channels.size();
                    while (i < childCount) {
                        populateSingleChannelPrefs(groupGroup, (NotificationChannel) channels.get(i), group.isBlocked());
                        i++;
                    }
                }
            }
        }
    }
}
