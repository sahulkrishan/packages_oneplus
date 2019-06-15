package com.android.settingslib.users;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.util.UserIcons;
import java.util.Iterator;
import java.util.List;

@Deprecated
public final class UserManagerHelper {
    private static final String HEADLESS_SYSTEM_USER = "android.car.systemuser.headless";
    private static final String TAG = "UserManagerHelper";
    private final ActivityManager mActivityManager;
    private final Context mContext;
    private OnUsersUpdateListener mUpdateListener;
    private final BroadcastReceiver mUserChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            UserManagerHelper.this.mUpdateListener.onUsersUpdate();
        }
    };
    private final UserManager mUserManager;

    public interface OnUsersUpdateListener {
        void onUsersUpdate();
    }

    public UserManagerHelper(Context context) {
        this.mContext = context;
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
    }

    public void registerOnUsersUpdateListener(OnUsersUpdateListener listener) {
        this.mUpdateListener = listener;
        registerReceiver();
    }

    public void unregisterOnUsersUpdateListener() {
        unregisterReceiver();
    }

    public boolean isHeadlessSystemUser() {
        return SystemProperties.getBoolean(HEADLESS_SYSTEM_USER, false);
    }

    public UserInfo getForegroundUserInfo() {
        return this.mUserManager.getUserInfo(getForegroundUserId());
    }

    public int getForegroundUserId() {
        ActivityManager activityManager = this.mActivityManager;
        return ActivityManager.getCurrentUser();
    }

    public UserInfo getCurrentProcessUserInfo() {
        return this.mUserManager.getUserInfo(getCurrentProcessUserId());
    }

    public int getCurrentProcessUserId() {
        return UserHandle.myUserId();
    }

    public List<UserInfo> getAllUsersExcludesCurrentProcessUser() {
        return getAllUsersExceptUser(getCurrentProcessUserId());
    }

    public List<UserInfo> getAllUsersExcludesForegroundUser() {
        return getAllUsersExceptUser(getForegroundUserId());
    }

    public List<UserInfo> getAllUsersExcludesSystemUser() {
        return getAllUsersExceptUser(0);
    }

    public List<UserInfo> getAllUsersExceptUser(int userId) {
        List<UserInfo> others = this.mUserManager.getUsers(true);
        Iterator<UserInfo> iterator = others.iterator();
        while (iterator.hasNext()) {
            if (((UserInfo) iterator.next()).id == userId) {
                iterator.remove();
            }
        }
        return others;
    }

    public List<UserInfo> getAllUsers() {
        if (isHeadlessSystemUser()) {
            return getAllUsersExcludesSystemUser();
        }
        return this.mUserManager.getUsers(true);
    }

    public boolean userIsSystemUser(UserInfo userInfo) {
        return userInfo.id == 0;
    }

    public boolean userCanBeRemoved(UserInfo userInfo) {
        return userIsSystemUser(userInfo) ^ 1;
    }

    public boolean userIsForegroundUser(UserInfo userInfo) {
        return getForegroundUserId() == userInfo.id;
    }

    public boolean userIsRunningCurrentProcess(UserInfo userInfo) {
        return getCurrentProcessUserId() == userInfo.id;
    }

    public boolean foregroundUserIsGuestUser() {
        return getForegroundUserInfo().isGuest();
    }

    public boolean foregroundUserHasUserRestriction(String restriction) {
        return this.mUserManager.hasUserRestriction(restriction, getForegroundUserInfo().getUserHandle());
    }

    public boolean foregroundUserCanAddUsers() {
        return foregroundUserHasUserRestriction("no_add_user") ^ 1;
    }

    public boolean currentProcessRunningAsDemoUser() {
        return this.mUserManager.isDemoUser();
    }

    public boolean currentProcessRunningAsGuestUser() {
        return this.mUserManager.isGuestUser();
    }

    public boolean currentProcessRunningAsSystemUser() {
        return this.mUserManager.isSystemUser();
    }

    public boolean currentProcessHasUserRestriction(String restriction) {
        return this.mUserManager.hasUserRestriction(restriction);
    }

    public boolean currentProcessCanAddUsers() {
        return currentProcessHasUserRestriction("no_add_user") ^ 1;
    }

    public boolean currentProcessCanRemoveUsers() {
        return currentProcessHasUserRestriction("no_remove_user") ^ 1;
    }

    public boolean currentProcessCanSwitchUsers() {
        return currentProcessHasUserRestriction("no_user_switch") ^ 1;
    }

    public boolean currentProcessCanModifyAccounts() {
        return (currentProcessHasUserRestriction("no_modify_accounts") || currentProcessRunningAsDemoUser() || currentProcessRunningAsGuestUser()) ? false : true;
    }

    public UserInfo createNewUser(String userName) {
        UserInfo user = this.mUserManager.createUser(userName, 0);
        if (user == null) {
            Log.w(TAG, "can't create user.");
            return null;
        }
        assignDefaultIcon(user);
        return user;
    }

    public boolean removeUser(UserInfo userInfo) {
        if (userIsSystemUser(userInfo)) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("User ");
            stringBuilder.append(userInfo.id);
            stringBuilder.append(" is system user, could not be removed.");
            Log.w(str, stringBuilder.toString());
            return false;
        }
        if (userInfo.id == getCurrentProcessUserId()) {
            switchToUserId(0);
        }
        return this.mUserManager.removeUser(userInfo.id);
    }

    public void switchToUser(UserInfo userInfo) {
        if (userInfo.id != getForegroundUserId()) {
            switchToUserId(userInfo.id);
        }
    }

    public void startNewGuestSession(String guestName) {
        UserInfo guest = this.mUserManager.createGuest(this.mContext, guestName);
        if (guest == null) {
            Log.w(TAG, "can't create user.");
            return;
        }
        assignDefaultIcon(guest);
        switchToUserId(guest.id);
    }

    public Bitmap getUserIcon(UserInfo userInfo) {
        Bitmap picture = this.mUserManager.getUserIcon(userInfo.id);
        if (picture == null) {
            return assignDefaultIcon(userInfo);
        }
        return picture;
    }

    public Drawable scaleUserIcon(Bitmap icon, int desiredSize) {
        return new BitmapDrawable(this.mContext.getResources(), Bitmap.createScaledBitmap(icon, desiredSize, desiredSize, true));
    }

    public void setUserName(UserInfo user, String name) {
        this.mUserManager.setUserName(user.id, name);
    }

    public Bitmap getUserDefaultIcon(UserInfo userInfo) {
        return UserIcons.convertToBitmap(UserIcons.getDefaultUserIcon(this.mContext.getResources(), userInfo.id, false));
    }

    public Bitmap getGuestDefaultIcon() {
        return UserIcons.convertToBitmap(UserIcons.getDefaultUserIcon(this.mContext.getResources(), -10000, false));
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.USER_ADDED");
        filter.addAction("android.intent.action.USER_INFO_CHANGED");
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction("android.intent.action.USER_STOPPED");
        filter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiverAsUser(this.mUserChangeReceiver, UserHandle.ALL, filter, null, null);
    }

    private Bitmap assignDefaultIcon(UserInfo userInfo) {
        Bitmap bitmap = userInfo.isGuest() ? getGuestDefaultIcon() : getUserDefaultIcon(userInfo);
        this.mUserManager.setUserIcon(userInfo.id, bitmap);
        return bitmap;
    }

    private void switchToUserId(int id) {
        try {
            this.mActivityManager.switchUser(id);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't switch user.", e);
        }
    }

    private void unregisterReceiver() {
        this.mContext.unregisterReceiver(this.mUserChangeReceiver);
    }
}
