package com.android.settings.deviceinfo.storage;

import android.content.Context;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.UserManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.util.SparseArray;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.deviceinfo.StorageItemPreference;
import com.android.settings.deviceinfo.storage.StorageAsyncLoader.AppsStorageResult;
import com.android.settings.deviceinfo.storage.StorageAsyncLoader.ResultHandler;
import com.android.settings.deviceinfo.storage.UserIconLoader.UserIconHandler;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.List;

public class SecondaryUserController extends AbstractPreferenceController implements PreferenceControllerMixin, ResultHandler, UserIconHandler {
    private static final String PREFERENCE_KEY_BASE = "pref_user_";
    private static final int SIZE_NOT_SET = -1;
    private static final String TARGET_PREFERENCE_GROUP_KEY = "pref_secondary_users";
    private static final int USER_PROFILE_INSERTION_LOCATION = 6;
    private long mSize = -1;
    @Nullable
    private StorageItemPreference mStoragePreference;
    private long mTotalSizeBytes;
    @NonNull
    private UserInfo mUser;
    private Drawable mUserIcon;

    private static class NoSecondaryUserController extends AbstractPreferenceController implements PreferenceControllerMixin {
        public NoSecondaryUserController(Context context) {
            super(context);
        }

        public void displayPreference(PreferenceScreen screen) {
            PreferenceGroup group = (PreferenceGroup) screen.findPreference(SecondaryUserController.TARGET_PREFERENCE_GROUP_KEY);
            if (group != null) {
                screen.removePreference(group);
            }
        }

        public boolean isAvailable() {
            return true;
        }

        public String getPreferenceKey() {
            return null;
        }
    }

    public static List<AbstractPreferenceController> getSecondaryUserControllers(Context context, UserManager userManager) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        UserInfo primaryUser = userManager.getPrimaryUser();
        boolean addedUser = false;
        List<UserInfo> infos = userManager.getUsers();
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            UserInfo info = (UserInfo) infos.get(i);
            if (!info.isPrimary()) {
                if (info == null || Utils.isProfileOf(primaryUser, info)) {
                    controllers.add(new UserProfileController(context, info, 6));
                } else {
                    controllers.add(new SecondaryUserController(context, info));
                    addedUser = true;
                }
            }
        }
        if (!addedUser) {
            controllers.add(new NoSecondaryUserController(context));
        }
        return controllers;
    }

    @VisibleForTesting
    SecondaryUserController(Context context, @NonNull UserInfo info) {
        super(context);
        this.mUser = info;
    }

    public void displayPreference(PreferenceScreen screen) {
        if (this.mStoragePreference == null) {
            this.mStoragePreference = new StorageItemPreference(screen.getContext());
            PreferenceGroup group = (PreferenceGroup) screen.findPreference(TARGET_PREFERENCE_GROUP_KEY);
            this.mStoragePreference.setTitle((CharSequence) this.mUser.name);
            StorageItemPreference storageItemPreference = this.mStoragePreference;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(PREFERENCE_KEY_BASE);
            stringBuilder.append(this.mUser.id);
            storageItemPreference.setKey(stringBuilder.toString());
            if (this.mSize != -1) {
                this.mStoragePreference.setStorageSize(this.mSize, this.mTotalSizeBytes);
            }
            group.setVisible(true);
            group.addPreference(this.mStoragePreference);
            maybeSetIcon();
        }
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return this.mStoragePreference != null ? this.mStoragePreference.getKey() : null;
    }

    @NonNull
    public UserInfo getUser() {
        return this.mUser;
    }

    public void setSize(long size) {
        this.mSize = size;
        if (this.mStoragePreference != null) {
            this.mStoragePreference.setStorageSize(this.mSize, this.mTotalSizeBytes);
        }
    }

    public void setTotalSize(long totalSizeBytes) {
        this.mTotalSizeBytes = totalSizeBytes;
    }

    public void handleResult(SparseArray<AppsStorageResult> stats) {
        AppsStorageResult result = (AppsStorageResult) stats.get(getUser().id);
        if (result != null) {
            setSize(result.externalStats.totalBytes);
        }
    }

    public void handleUserIcons(SparseArray<Drawable> fetchedIcons) {
        this.mUserIcon = (Drawable) fetchedIcons.get(this.mUser.id);
        maybeSetIcon();
    }

    private void maybeSetIcon() {
        if (this.mUserIcon != null && this.mStoragePreference != null) {
            this.mStoragePreference.setIcon(this.mUserIcon);
        }
    }
}
