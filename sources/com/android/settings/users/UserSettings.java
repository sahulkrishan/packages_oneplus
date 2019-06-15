package com.android.settings.users;

import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Profile;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.accounts.EmergencyInfoPreferenceController;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.users.EditUserInfoController.OnContentChangedCallback;
import com.android.settings.wifi.ConfigureWifiSettings;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.drawable.CircleFramedDrawable;
import com.android.settingslib.drawable.UserIcons;
import com.android.setupwizardlib.util.WizardManagerHelper;
import com.oneplus.settings.utils.OPUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class UserSettings extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnClickListener, OnDismissListener, OnContentChangedCallback, Indexable {
    private static final String ACTION_EDIT_EMERGENCY_INFO = "android.settings.EDIT_EMERGENGY_INFO";
    private static final int DIALOG_ADD_USER = 2;
    private static final int DIALOG_CHOOSE_USER_TYPE = 6;
    private static final int DIALOG_CONFIRM_EXIT_GUEST = 8;
    private static final int DIALOG_CONFIRM_REMOVE = 1;
    private static final int DIALOG_NEED_LOCKSCREEN = 7;
    private static final int DIALOG_SETUP_PROFILE = 4;
    private static final int DIALOG_SETUP_USER = 3;
    private static final int DIALOG_USER_CANNOT_MANAGE = 5;
    private static final int DIALOG_USER_PROFILE_EDITOR = 9;
    private static final String KEY_ADD_USER = "user_add";
    private static final String KEY_ADD_USER_LONG_MESSAGE_DISPLAYED = "key_add_user_long_message_displayed";
    private static final String KEY_ADD_USER_WHEN_LOCKED = "user_settings_add_users_when_locked";
    private static final String KEY_EMERGENCY_INFO = "emergency_info";
    private static final String KEY_SUMMARY = "summary";
    private static final String KEY_TITLE = "title";
    private static final String KEY_USER_LIST = "user_list";
    private static final String KEY_USER_ME = "user_me";
    private static final int MENU_REMOVE_USER = 1;
    private static final int MESSAGE_CONFIG_USER = 3;
    private static final int MESSAGE_SETUP_USER = 2;
    private static final int MESSAGE_UPDATE_LIST = 1;
    private static final int REQUEST_CHOOSE_LOCK = 10;
    private static final String SAVE_ADDING_USER = "adding_user";
    private static final String SAVE_REMOVING_USER = "removing_user";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        /* Access modifiers changed, original: protected */
        public boolean isPageSearchEnabled(Context context) {
            return UserCapabilities.create(context).mEnabled;
        }

        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> index = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.user_settings;
            index.add(sir);
            return index;
        }

        public List<String> getNonIndexableKeysFromXml(Context context, int xmlResId) {
            List<String> niks = super.getNonIndexableKeysFromXml(context, xmlResId);
            new AddUserWhenLockedPreferenceController(context, UserSettings.KEY_ADD_USER_WHEN_LOCKED, null).updateNonIndexableKeys(niks);
            new AutoSyncDataPreferenceController(context, null).updateNonIndexableKeys(niks);
            new AutoSyncPersonalDataPreferenceController(context, null).updateNonIndexableKeys(niks);
            new AutoSyncWorkDataPreferenceController(context, null).updateNonIndexableKeys(niks);
            return niks;
        }
    };
    private static final long STORAGE_SIZE_LIMIT = 209715200;
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = -$$Lambda$UserSettings$Eg6plZiaX7G7UUvF4Q46lU8PMRw.INSTANCE;
    private static final String TAG = "UserSettings";
    private static final int USER_TYPE_RESTRICTED_PROFILE = 2;
    private static final int USER_TYPE_USER = 1;
    private static SparseArray<Bitmap> sDarkDefaultUserBitmapCache = new SparseArray();
    private RestrictedPreference mAddUser;
    private AddUserWhenLockedPreferenceController mAddUserWhenLockedPreferenceController;
    private int mAddedUserId = 0;
    private boolean mAddingUser;
    private String mAddingUserName;
    private Drawable mDefaultIconDrawable;
    private EditUserInfoController mEditUserInfoController = new EditUserInfoController();
    private EmergencyInfoPreferenceController mEmergencyInfoPreferenceController;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    UserSettings.this.updateUserList();
                    return;
                case 2:
                    UserSettings.this.onUserCreated(msg.arg1);
                    return;
                case 3:
                    UserSettings.this.onManageUserClicked(msg.arg1, true);
                    return;
                default:
                    return;
            }
        }
    };
    private UserPreference mMePreference;
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            boolean canSwitchUsers = UserSettings.this.mUserManager.canSwitchUsers();
            boolean moreUsers = UserSettings.this.mUserManager.canAddMoreUsers();
            RestrictedPreference access$800 = UserSettings.this.mAddUser;
            boolean z = moreUsers && !UserSettings.this.mAddingUser && canSwitchUsers;
            access$800.setEnabled(z);
        }
    };
    private int mRemovingUserId = -1;
    private boolean mShouldUpdateUserList = true;
    private UserCapabilities mUserCaps;
    private BroadcastReceiver mUserChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.USER_REMOVED")) {
                UserSettings.this.mRemovingUserId = -1;
            } else if (intent.getAction().equals("android.intent.action.USER_INFO_CHANGED")) {
                int userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userHandle != -1) {
                    UserSettings.this.mUserIcons.remove(userHandle);
                }
            }
            UserSettings.this.mHandler.sendEmptyMessage(1);
        }
    };
    private SparseArray<Bitmap> mUserIcons = new SparseArray();
    private PreferenceGroup mUserListCategory;
    private final Object mUserLock = new Object();
    private UserManager mUserManager;
    private TelephonyManager tm;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                UserInfo info = ((UserManager) this.mContext.getSystemService(UserManager.class)).getUserInfo(UserHandle.myUserId());
                this.mSummaryLoader.setSummary(this, this.mContext.getString(R.string.users_summary, new Object[]{info.name}));
            }
        }
    }

    public int getMetricsCategory() {
        return 96;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.user_settings);
        if (Global.getInt(getContext().getContentResolver(), WizardManagerHelper.SETTINGS_GLOBAL_DEVICE_PROVISIONED, 0) == 0) {
            getActivity().finish();
            return;
        }
        Context context = getActivity();
        this.mAddUserWhenLockedPreferenceController = new AddUserWhenLockedPreferenceController(context, KEY_ADD_USER_WHEN_LOCKED, getLifecycle());
        PreferenceScreen screen = getPreferenceScreen();
        this.mAddUserWhenLockedPreferenceController.displayPreference(screen);
        screen.findPreference(this.mAddUserWhenLockedPreferenceController.getPreferenceKey()).setOnPreferenceChangeListener(this.mAddUserWhenLockedPreferenceController);
        this.mEmergencyInfoPreferenceController = new EmergencyInfoPreferenceController(context);
        this.mEmergencyInfoPreferenceController.displayPreference(screen);
        screen.findPreference(this.mEmergencyInfoPreferenceController.getPreferenceKey()).setOnPreferenceClickListener(this.mEmergencyInfoPreferenceController);
        if (icicle != null) {
            if (icicle.containsKey(SAVE_ADDING_USER)) {
                this.mAddedUserId = icicle.getInt(SAVE_ADDING_USER);
            }
            if (icicle.containsKey(SAVE_REMOVING_USER)) {
                this.mRemovingUserId = icicle.getInt(SAVE_REMOVING_USER);
            }
            this.mEditUserInfoController.onRestoreInstanceState(icicle);
        }
        this.mUserCaps = UserCapabilities.create(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
        if (this.mUserCaps.mEnabled) {
            int myUserId = UserHandle.myUserId();
            this.mUserListCategory = (PreferenceGroup) findPreference(KEY_USER_LIST);
            this.mMePreference = new UserPreference(getPrefContext(), null, myUserId, null, null);
            this.mMePreference.setKey(KEY_USER_ME);
            this.mMePreference.setOnPreferenceClickListener(this);
            if (this.mUserCaps.mIsAdmin) {
                this.mMePreference.setSummary((int) R.string.user_admin);
            }
            this.mAddUser = (RestrictedPreference) findPreference(KEY_ADD_USER);
            this.mAddUser.useAdminDisabledSummary(false);
            if (this.mUserCaps.mCanAddUser && Utils.isDeviceProvisioned(getActivity())) {
                this.mAddUser.setVisible(true);
                this.mAddUser.setOnPreferenceClickListener(this);
                if (!this.mUserCaps.mCanAddRestrictedProfile) {
                    this.mAddUser.setTitle((int) R.string.user_add_user_menu);
                }
            } else {
                this.mAddUser.setVisible(false);
            }
            IntentFilter filter = new IntentFilter("android.intent.action.USER_REMOVED");
            filter.addAction("android.intent.action.USER_INFO_CHANGED");
            context.registerReceiverAsUser(this.mUserChangeReceiver, UserHandle.ALL, filter, null, this.mHandler);
            loadProfile();
            updateUserList();
            this.mShouldUpdateUserList = false;
            this.tm = (TelephonyManager) context.getSystemService("phone");
            this.tm.listen(this.mPhoneStateListener, 32);
            if (OPUtils.isGuestMode()) {
                ((PreferenceGroup) findPreference("lock_screen_settings")).setVisible(false);
            }
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mUserCaps.mEnabled) {
            PreferenceScreen screen = getPreferenceScreen();
            if (this.mEmergencyInfoPreferenceController.isAvailable()) {
                this.mEmergencyInfoPreferenceController.updateState(screen.findPreference(this.mEmergencyInfoPreferenceController.getPreferenceKey()));
            }
            if (this.mAddUserWhenLockedPreferenceController.isAvailable()) {
                this.mAddUserWhenLockedPreferenceController.updateState(screen.findPreference(this.mAddUserWhenLockedPreferenceController.getPreferenceKey()));
            }
            if (this.mShouldUpdateUserList) {
                this.mUserCaps.updateAddUserCapabilities(getActivity());
                loadProfile();
                updateUserList();
            }
        }
    }

    public void onPause() {
        this.mShouldUpdateUserList = true;
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mUserCaps != null && this.mUserCaps.mEnabled) {
            getActivity().unregisterReceiver(this.mUserChangeReceiver);
            if (!(this.tm == null || this.mPhoneStateListener == null)) {
                this.tm.listen(this.mPhoneStateListener, 0);
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mEditUserInfoController.onSaveInstanceState(outState);
        outState.putInt(SAVE_ADDING_USER, this.mAddedUserId);
        outState.putInt(SAVE_REMOVING_USER, this.mRemovingUserId);
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        this.mEditUserInfoController.startingActivityForResult();
        super.startActivityForResult(intent, requestCode);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        UserManager um = (UserManager) getContext().getSystemService(UserManager.class);
        boolean allowRemoveUser = um.hasUserRestriction("no_remove_user") ^ true;
        boolean canSwitchUsers = um.canSwitchUsers();
        if (!this.mUserCaps.mIsAdmin && allowRemoveUser && canSwitchUsers) {
            String nickname = this.mUserManager.getUserName();
            int pos = 0 + 1;
            menu.add(0, 1, 0, getResources().getString(R.string.user_remove_user_menu, new Object[]{nickname})).setShowAsAction(0);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 1) {
            return super.onOptionsItemSelected(item);
        }
        onRemoveUserClicked(UserHandle.myUserId());
        return true;
    }

    private void loadProfile() {
        if (this.mUserCaps.mIsGuest) {
            this.mMePreference.setIcon(getEncircledDefaultIcon());
            this.mMePreference.setTitle((int) R.string.user_exit_guest_title);
            return;
        }
        new AsyncTask<Void, Void, String>() {
            /* Access modifiers changed, original: protected */
            public void onPostExecute(String result) {
                UserSettings.this.finishLoadProfile(result);
            }

            /* Access modifiers changed, original: protected|varargs */
            public String doInBackground(Void... values) {
                UserInfo user = UserSettings.this.mUserManager.getUserInfo(UserHandle.myUserId());
                if (user.iconPath == null || user.iconPath.equals("")) {
                    UserSettings.copyMeProfilePhoto(UserSettings.this.getActivity(), user);
                }
                return user.name;
            }
        }.execute(new Void[0]);
    }

    private void finishLoadProfile(String profileName) {
        if (getActivity() != null) {
            this.mMePreference.setTitle((CharSequence) getString(R.string.user_you, new Object[]{profileName}));
            int myUserId = UserHandle.myUserId();
            Bitmap b = this.mUserManager.getUserIcon(myUserId);
            if (b != null) {
                this.mMePreference.setIcon(encircle(b));
                this.mUserIcons.put(myUserId, b);
            }
        }
    }

    private boolean hasLockscreenSecurity() {
        return new LockPatternUtils(getActivity()).isSecure(UserHandle.myUserId());
    }

    private void launchChooseLockscreen() {
        Intent chooseLockIntent = new Intent("android.app.action.SET_NEW_PASSWORD");
        chooseLockIntent.putExtra(ChooseLockGenericFragment.MINIMUM_QUALITY_KEY, 65536);
        startActivityForResult(chooseLockIntent, 10);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 10) {
            this.mEditUserInfoController.onActivityResult(requestCode, resultCode, data);
        } else if (resultCode != 0 && hasLockscreenSecurity()) {
            addUserNow(2);
        }
    }

    private void onAddUserClicked(int userType) {
        synchronized (this.mUserLock) {
            if (this.mRemovingUserId == -1 && !this.mAddingUser) {
                switch (userType) {
                    case 1:
                        showDialog(2);
                        break;
                    case 2:
                        if (!hasLockscreenSecurity()) {
                            showDialog(7);
                            break;
                        } else {
                            addUserNow(2);
                            break;
                        }
                    default:
                        break;
                }
            }
        }
    }

    private void onRemoveUserClicked(int userId) {
        synchronized (this.mUserLock) {
            if (this.mRemovingUserId == -1 && !this.mAddingUser) {
                this.mRemovingUserId = userId;
                showDialog(1);
            }
        }
    }

    private UserInfo createRestrictedProfile() {
        UserInfo newUserInfo = this.mUserManager.createRestrictedProfile(this.mAddingUserName);
        if (newUserInfo == null || assignDefaultPhoto(getActivity(), newUserInfo.id)) {
            return newUserInfo;
        }
        return null;
    }

    private UserInfo createTrustedUser() {
        UserInfo newUserInfo = this.mUserManager.createUser(this.mAddingUserName, 0);
        if (newUserInfo == null || assignDefaultPhoto(getActivity(), newUserInfo.id)) {
            return newUserInfo;
        }
        return null;
    }

    private void onManageUserClicked(int userId, boolean newUser) {
        this.mAddingUser = false;
        if (userId == -11) {
            Bundle extras = new Bundle();
            extras.putBoolean("guest_user", true);
            new SubSettingLauncher(getContext()).setDestination(UserDetailsSettings.class.getName()).setArguments(extras).setTitle((int) R.string.user_guest).setSourceMetricsCategory(getMetricsCategory()).launch();
            return;
        }
        UserInfo info = this.mUserManager.getUserInfo(userId);
        Bundle extras2;
        if (info.isRestricted() && this.mUserCaps.mIsAdmin) {
            extras2 = new Bundle();
            extras2.putInt("user_id", userId);
            extras2.putBoolean(AppRestrictionsFragment.EXTRA_NEW_USER, newUser);
            new SubSettingLauncher(getContext()).setDestination(RestrictedProfileSettings.class.getName()).setArguments(extras2).setTitle((int) R.string.user_restrictions_title).setSourceMetricsCategory(getMetricsCategory()).launch();
        } else if (info.id == UserHandle.myUserId()) {
            OwnerInfoSettings.show(this);
        } else if (this.mUserCaps.mIsAdmin) {
            extras2 = new Bundle();
            extras2.putInt("user_id", userId);
            new SubSettingLauncher(getContext()).setDestination(UserDetailsSettings.class.getName()).setArguments(extras2).setTitle(info.name).setSourceMetricsCategory(getMetricsCategory()).launch();
        }
    }

    private void onUserCreated(int userId) {
        this.mAddedUserId = userId;
        this.mAddingUser = false;
        if (isResumed()) {
            if (this.mUserManager.getUserInfo(userId).isRestricted()) {
                showDialog(4);
            } else {
                showDialog(3);
            }
            return;
        }
        Log.w(TAG, "Cannot show dialog after onPause");
    }

    public void onDialogShowing() {
        super.onDialogShowing();
        setOnDismissListener(this);
    }

    public Dialog onCreateDialog(int dialogId) {
        Context context = getActivity();
        if (context == null) {
            return null;
        }
        int userType = 2;
        switch (dialogId) {
            case 1:
                return UserDialogs.createRemoveDialog(getActivity(), this.mRemovingUserId, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.removeUserNow();
                    }
                });
            case 2:
                int messageResId;
                final SharedPreferences preferences = getActivity().getPreferences(0);
                final boolean longMessageDisplayed = preferences.getBoolean(KEY_ADD_USER_LONG_MESSAGE_DISPLAYED, false);
                if (longMessageDisplayed) {
                    messageResId = R.string.user_add_user_message_short;
                } else {
                    messageResId = R.string.user_add_user_message_long;
                }
                if (dialogId == 2) {
                    userType = 1;
                }
                return new Builder(context).setTitle(R.string.user_add_user_title).setMessage(messageResId).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.addUserNow(userType);
                        if (!longMessageDisplayed) {
                            preferences.edit().putBoolean(UserSettings.KEY_ADD_USER_LONG_MESSAGE_DISPLAYED, true).apply();
                        }
                    }
                }).setNegativeButton(17039360, null).create();
            case 3:
                return new Builder(context).setTitle(R.string.user_setup_dialog_title).setMessage(R.string.user_setup_dialog_message).setPositiveButton(R.string.user_setup_button_setup_now, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.switchUserNow(UserSettings.this.mAddedUserId);
                    }
                }).setNegativeButton(R.string.user_setup_button_setup_later, null).create();
            case 4:
                return new Builder(context).setMessage(R.string.user_setup_profile_dialog_message).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.switchUserNow(UserSettings.this.mAddedUserId);
                    }
                }).setNegativeButton(17039360, null).create();
            case 5:
                return new Builder(context).setMessage(R.string.user_cannot_manage_message).setPositiveButton(17039370, null).create();
            case 6:
                List<HashMap<String, String>> data = new ArrayList();
                HashMap<String, String> addUserItem = new HashMap();
                addUserItem.put("title", getString(R.string.user_add_user_item_title));
                addUserItem.put("summary", getString(R.string.user_add_user_item_summary));
                HashMap<String, String> addProfileItem = new HashMap();
                addProfileItem.put("title", getString(R.string.user_add_profile_item_title));
                addProfileItem.put("summary", getString(R.string.user_add_profile_item_summary));
                data.add(addUserItem);
                data.add(addProfileItem);
                Builder builder = new Builder(context);
                SimpleAdapter adapter = new SimpleAdapter(builder.getContext(), data, R.layout.two_line_list_item, new String[]{"title", "summary"}, new int[]{R.id.title, R.id.summary});
                builder.setTitle(R.string.user_add_user_type_title);
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int i;
                        UserSettings userSettings = UserSettings.this;
                        if (which == 0) {
                            i = 1;
                        } else {
                            i = 2;
                        }
                        userSettings.onAddUserClicked(i);
                    }
                });
                return builder.create();
            case 7:
                return new Builder(context).setMessage(R.string.user_need_lock_message).setPositiveButton(R.string.user_set_lock_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.launchChooseLockscreen();
                    }
                }).setNegativeButton(17039360, null).create();
            case 8:
                return new Builder(context).setTitle(R.string.user_exit_guest_confirm_title).setMessage(R.string.user_exit_guest_confirm_message).setPositiveButton(R.string.user_exit_guest_dialog_remove, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserSettings.this.exitGuest();
                    }
                }).setNegativeButton(17039360, null).create();
            case 9:
                return this.mEditUserInfoController.createDialog(this, null, this.mMePreference.getTitle(), R.string.profile_info_settings_title, this, Process.myUserHandle());
            default:
                return null;
        }
    }

    public int getDialogMetricsCategory(int dialogId) {
        switch (dialogId) {
            case 1:
                return 591;
            case 2:
                return 595;
            case 3:
                return 596;
            case 4:
                return 597;
            case 5:
                return 594;
            case 6:
                return 598;
            case 7:
                return 599;
            case 8:
                return ConfigureWifiSettings.WIFI_WAKEUP_REQUEST_CODE;
            case 9:
                return 601;
            default:
                return 0;
        }
    }

    private void removeUserNow() {
        if (this.mRemovingUserId == UserHandle.myUserId()) {
            removeThisUser();
        } else {
            new Thread() {
                public void run() {
                    synchronized (UserSettings.this.mUserLock) {
                        UserSettings.this.mUserManager.removeUser(UserSettings.this.mRemovingUserId);
                        UserSettings.this.mHandler.sendEmptyMessage(1);
                    }
                }
            }.start();
        }
    }

    private void removeThisUser() {
        if (this.mUserManager.canSwitchUsers()) {
            try {
                ActivityManager.getService().switchUser(0);
                ((UserManager) getContext().getSystemService(UserManager.class)).removeUser(UserHandle.myUserId());
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to remove self user");
            }
            return;
        }
        Log.w(TAG, "Cannot remove current user when switching is disabled");
    }

    private void addUserNow(final int userType) {
        synchronized (this.mUserLock) {
            String string;
            this.mAddingUser = true;
            if (userType == 1) {
                string = getString(R.string.user_new_user_name);
            } else {
                string = getString(R.string.user_new_profile_name);
            }
            this.mAddingUserName = string;
            new Thread() {
                public void run() {
                    UserInfo user;
                    if (userType == 1) {
                        user = UserSettings.this.createTrustedUser();
                    } else {
                        user = UserSettings.this.createRestrictedProfile();
                    }
                    if (user == null) {
                        UserSettings.this.mAddingUser = false;
                        return;
                    }
                    synchronized (UserSettings.this.mUserLock) {
                        if (userType == 1) {
                            UserSettings.this.mHandler.sendEmptyMessage(1);
                            if (!UserSettings.this.mUserCaps.mDisallowSwitchUser) {
                                UserSettings.this.mHandler.sendMessage(UserSettings.this.mHandler.obtainMessage(2, user.id, user.serialNumber));
                            }
                        } else {
                            UserSettings.this.mHandler.sendMessage(UserSettings.this.mHandler.obtainMessage(3, user.id, user.serialNumber));
                        }
                    }
                }
            }.start();
        }
    }

    private void switchUserNow(int userId) {
        try {
            ActivityManager.getService().switchUser(userId);
        } catch (RemoteException e) {
        }
    }

    private void exitGuest() {
        if (this.mUserCaps.mIsGuest) {
            removeThisUser();
        }
    }

    private void updateUserList() {
        if (getActivity() != null) {
            EnforcedAdmin enforcedAdmin;
            boolean showDelete;
            UserPreference userPreference;
            List<UserInfo> users = this.mUserManager.getUsers(true);
            Context context = getActivity();
            boolean voiceCapable = Utils.isVoiceCapable(context);
            ArrayList<Integer> missingIcons = new ArrayList();
            ArrayList<UserPreference> userPreferences = new ArrayList();
            int guestId = -11;
            userPreferences.add(this.mMePreference);
            Iterator it = users.iterator();
            while (true) {
                enforcedAdmin = null;
                if (!it.hasNext()) {
                    break;
                }
                UserInfo user = (UserInfo) it.next();
                if (user.supportsSwitchToByUser()) {
                    UserPreference pref;
                    if (user.id == UserHandle.myUserId()) {
                        pref = this.mMePreference;
                    } else if (user.isGuest()) {
                        guestId = user.id;
                    } else {
                        boolean showSettings = this.mUserCaps.mIsAdmin && (voiceCapable || user.isRestricted());
                        showDelete = (!this.mUserCaps.mIsAdmin || voiceCapable || user.isRestricted() || user.isGuest()) ? false : true;
                        UserPreference pref2 = new UserPreference(getPrefContext(), null, user.id, showSettings ? this : null, showDelete ? this : null);
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("id=");
                        stringBuilder.append(user.id);
                        pref2.setKey(stringBuilder.toString());
                        userPreferences.add(pref2);
                        if (user.isAdmin()) {
                            pref2.setSummary((int) R.string.user_admin);
                        }
                        pref2.setTitle((CharSequence) user.name);
                        pref2.setSelectable(false);
                        pref = pref2;
                    }
                    if (pref != null) {
                        if (!isInitialized(user)) {
                            if (user.isRestricted()) {
                                pref.setSummary((int) R.string.user_summary_restricted_not_set_up);
                            } else {
                                pref.setSummary((int) R.string.user_summary_not_set_up);
                            }
                            if (!this.mUserCaps.mDisallowSwitchUser) {
                                pref.setOnPreferenceClickListener(this);
                                pref.setSelectable(true);
                            }
                        } else if (user.isRestricted()) {
                            pref.setSummary((int) R.string.user_summary_restricted_profile);
                        }
                        if (user.iconPath == null) {
                            pref.setIcon(getEncircledDefaultIcon());
                        } else if (this.mUserIcons.get(user.id) == null) {
                            missingIcons.add(Integer.valueOf(user.id));
                            pref.setIcon(getEncircledDefaultIcon());
                        } else {
                            setPhotoId(pref, user);
                        }
                    }
                }
            }
            if (this.mAddingUser) {
                userPreference = new UserPreference(getPrefContext(), null, -10, null, null);
                userPreference.setEnabled(false);
                userPreference.setTitle((CharSequence) this.mAddingUserName);
                userPreference.setIcon(getEncircledDefaultIcon());
                userPreferences.add(userPreference);
            }
            if (!this.mUserCaps.mIsGuest && (this.mUserCaps.mCanAddGuest || this.mUserCaps.mDisallowAddUserSetByAdmin)) {
                Context prefContext = getPrefContext();
                OnClickListener onClickListener = (this.mUserCaps.mIsAdmin && voiceCapable) ? this : null;
                userPreference = new UserPreference(prefContext, null, -11, onClickListener, null);
                userPreference.setTitle((int) R.string.user_guest);
                userPreference.setIcon(getEncircledDefaultIcon());
                userPreferences.add(userPreference);
                if (this.mUserCaps.mDisallowAddUser) {
                    userPreference.setDisabledByAdmin(this.mUserCaps.mEnforcedAdmin);
                } else if (this.mUserCaps.mDisallowSwitchUser) {
                    userPreference.setDisabledByAdmin(RestrictedLockUtils.getDeviceOwner(context));
                } else {
                    userPreference.setDisabledByAdmin(null);
                }
                userPreference.setOnPreferenceClickListener(new -$$Lambda$UserSettings$wMqzhBHYMbgNNY7TSuzlNB8n9UY(this, guestId));
            }
            Collections.sort(userPreferences, UserPreference.SERIAL_NUMBER_COMPARATOR);
            getActivity().invalidateOptionsMenu();
            if (missingIcons.size() > 0) {
                loadIconsAsync(missingIcons);
            }
            this.mUserListCategory.removeAll();
            if (this.mUserCaps.mCanAddRestrictedProfile) {
                this.mUserListCategory.setTitle((int) R.string.user_list_title);
            } else {
                this.mUserListCategory.setTitle(null);
            }
            it = userPreferences.iterator();
            while (it.hasNext()) {
                UserPreference userPreference2 = (UserPreference) it.next();
                userPreference2.setOrder(Integer.MAX_VALUE);
                this.mUserListCategory.addPreference(userPreference2);
            }
            if ((this.mUserCaps.mCanAddUser || this.mUserCaps.mDisallowAddUserSetByAdmin) && Utils.isDeviceProvisioned(getActivity())) {
                boolean moreUsers = this.mUserManager.canAddMoreUsers();
                boolean canSwitchUsers = this.mUserManager.canSwitchUsers();
                RestrictedPreference restrictedPreference = this.mAddUser;
                showDelete = moreUsers && !this.mAddingUser && canSwitchUsers;
                restrictedPreference.setEnabled(showDelete);
                if (moreUsers) {
                    this.mAddUser.setSummary(null);
                } else {
                    this.mAddUser.setSummary((CharSequence) getString(R.string.user_add_max_count, new Object[]{Integer.valueOf(getMaxRealUsers())}));
                }
                if (this.mAddUser.isEnabled()) {
                    RestrictedPreference restrictedPreference2 = this.mAddUser;
                    if (this.mUserCaps.mDisallowAddUser) {
                        enforcedAdmin = this.mUserCaps.mEnforcedAdmin;
                    }
                    restrictedPreference2.setDisabledByAdmin(enforcedAdmin);
                }
            }
        }
    }

    public static /* synthetic */ boolean lambda$updateUserList$0(UserSettings userSettings, int finalGuestId, Preference preference) {
        boolean canSwitchUsers = userSettings.mUserManager.canSwitchUsers();
        if (!userSettings.mUserManager.canAddMoreUsers() || userSettings.mAddingUser || !canSwitchUsers) {
            return false;
        }
        int id = finalGuestId;
        if (id == -11) {
            UserInfo guest = userSettings.mUserManager.createGuest(userSettings.getContext(), preference.getTitle().toString());
            if (guest != null) {
                id = guest.id;
            }
        }
        try {
            ActivityManager.getService().switchUser(id);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
        return true;
    }

    private int getMaxRealUsers() {
        int maxUsersAndGuest = UserManager.getMaxSupportedUsers() + 1;
        int managedProfiles = 0;
        for (UserInfo user : this.mUserManager.getUsers()) {
            if (user.isManagedProfile()) {
                managedProfiles++;
            }
        }
        return maxUsersAndGuest - managedProfiles;
    }

    private void loadIconsAsync(List<Integer> missingIcons) {
        new AsyncTask<List<Integer>, Void, Void>() {
            /* Access modifiers changed, original: protected */
            public void onPostExecute(Void result) {
                UserSettings.this.updateUserList();
            }

            /* Access modifiers changed, original: protected|varargs */
            public Void doInBackground(List<Integer>... values) {
                for (Integer userId : values[0]) {
                    int userId2 = userId.intValue();
                    Bitmap bitmap = UserSettings.this.mUserManager.getUserIcon(userId2);
                    if (bitmap == null) {
                        bitmap = UserSettings.getDefaultUserIconAsBitmap(UserSettings.this.getContext().getResources(), userId2);
                    }
                    UserSettings.this.mUserIcons.append(userId2, bitmap);
                }
                return null;
            }
        }.execute(new List[]{missingIcons});
    }

    private Drawable getEncircledDefaultIcon() {
        if (this.mDefaultIconDrawable == null) {
            this.mDefaultIconDrawable = encircle(getDefaultUserIconAsBitmap(getContext().getResources(), -10000));
        }
        return this.mDefaultIconDrawable;
    }

    private void setPhotoId(Preference pref, UserInfo user) {
        Bitmap bitmap = (Bitmap) this.mUserIcons.get(user.id);
        if (bitmap != null) {
            pref.setIcon(encircle(bitmap));
        }
    }

    public boolean onPreferenceClick(Preference pref) {
        boolean canSwitchUsers = this.mUserManager.canSwitchUsers();
        if (this.mAddingUser || !canSwitchUsers) {
            return false;
        }
        if (pref == this.mMePreference) {
            if (this.mUserCaps.mIsGuest) {
                showDialog(8);
                return true;
            } else if (this.mUserManager.isLinkedUser()) {
                onManageUserClicked(UserHandle.myUserId(), false);
            } else {
                showDialog(9);
            }
        } else if (pref instanceof UserPreference) {
            UserInfo user = this.mUserManager.getUserInfo(((UserPreference) pref).getUserId());
            if (!isInitialized(user)) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(2, user.id, user.serialNumber));
            }
        } else if (pref == this.mAddUser) {
            if (Long.valueOf(getAvailableInternalMemorySize()).longValue() < STORAGE_SIZE_LIMIT) {
                Toast.makeText(getContext(), R.string.settings_switch_user_storage_insufficient, 1).show();
            } else if (this.mUserCaps.mCanAddRestrictedProfile) {
                showDialog(6);
            } else {
                onAddUserClicked(1);
            }
        }
        return false;
    }

    public static long getAvailableInternalMemorySize() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        return ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize());
    }

    private boolean isInitialized(UserInfo user) {
        return (user.flags & 16) != 0;
    }

    private Drawable encircle(Bitmap icon) {
        return CircleFramedDrawable.getInstance(getActivity(), icon);
    }

    public void onClick(View v) {
        if (v.getTag() instanceof UserPreference) {
            int userId = ((UserPreference) v.getTag()).getUserId();
            int id = v.getId();
            if (id == R.id.manage_user) {
                onManageUserClicked(userId, false);
            } else if (id == R.id.trash_user) {
                EnforcedAdmin removeDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(getContext(), "no_remove_user", UserHandle.myUserId());
                if (removeDisallowedAdmin != null) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getContext(), removeDisallowedAdmin);
                } else {
                    onRemoveUserClicked(userId);
                }
            }
        }
    }

    public void onDismiss(DialogInterface dialog) {
        synchronized (this.mUserLock) {
            this.mRemovingUserId = -1;
            updateUserList();
        }
    }

    public int getHelpResource() {
        return R.string.help_url_users;
    }

    public void onPhotoChanged(Drawable photo) {
        this.mMePreference.setIcon(photo);
    }

    public void onLabelChanged(CharSequence label) {
        this.mMePreference.setTitle(label);
    }

    private static Bitmap getDefaultUserIconAsBitmap(Resources resources, int userId) {
        Bitmap bitmap = (Bitmap) sDarkDefaultUserBitmapCache.get(userId);
        if (bitmap != null) {
            return bitmap;
        }
        bitmap = UserIcons.convertToBitmap(UserIcons.getDefaultUserIcon(userId, false));
        sDarkDefaultUserBitmapCache.put(userId, bitmap);
        return bitmap;
    }

    @VisibleForTesting
    static boolean assignDefaultPhoto(Context context, int userId) {
        if (context == null) {
            return false;
        }
        ((UserManager) context.getSystemService("user")).setUserIcon(userId, getDefaultUserIconAsBitmap(context.getResources(), userId));
        return true;
    }

    @WorkerThread
    static void copyMeProfilePhoto(Context context, UserInfo user) {
        Uri contactUri = Profile.CONTENT_URI;
        int userId = user != null ? user.id : UserHandle.myUserId();
        InputStream avatarDataStream = Contacts.openContactPhotoInputStream(context.getContentResolver(), contactUri, true);
        if (avatarDataStream == null) {
            assignDefaultPhoto(context, userId);
            return;
        }
        ((UserManager) context.getSystemService("user")).setUserIcon(userId, BitmapFactory.decodeStream(avatarDataStream));
        try {
            avatarDataStream.close();
        } catch (IOException e) {
        }
    }
}
