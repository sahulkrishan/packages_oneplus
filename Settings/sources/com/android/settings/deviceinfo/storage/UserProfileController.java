package com.android.settings.deviceinfo.storage;

import android.content.Context;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.SparseArray;
import com.android.internal.util.Preconditions;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.deviceinfo.StorageItemPreference;
import com.android.settings.deviceinfo.StorageProfileFragment;
import com.android.settings.deviceinfo.storage.StorageAsyncLoader.AppsStorageResult;
import com.android.settings.deviceinfo.storage.StorageAsyncLoader.ResultHandler;
import com.android.settings.deviceinfo.storage.UserIconLoader.UserIconHandler;
import com.android.settingslib.core.AbstractPreferenceController;

public class UserProfileController extends AbstractPreferenceController implements PreferenceControllerMixin, ResultHandler, UserIconHandler {
    private static final String PREFERENCE_KEY_BASE = "pref_profile_";
    private final int mPreferenceOrder;
    private StorageItemPreference mStoragePreference;
    private long mTotalSizeBytes;
    private UserInfo mUser;

    public UserProfileController(Context context, UserInfo info, int preferenceOrder) {
        super(context);
        this.mUser = (UserInfo) Preconditions.checkNotNull(info);
        this.mPreferenceOrder = preferenceOrder;
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(PREFERENCE_KEY_BASE);
        stringBuilder.append(this.mUser.id);
        return stringBuilder.toString();
    }

    public void displayPreference(PreferenceScreen screen) {
        this.mStoragePreference = new StorageItemPreference(screen.getContext());
        this.mStoragePreference.setOrder(this.mPreferenceOrder);
        StorageItemPreference storageItemPreference = this.mStoragePreference;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(PREFERENCE_KEY_BASE);
        stringBuilder.append(this.mUser.id);
        storageItemPreference.setKey(stringBuilder.toString());
        this.mStoragePreference.setTitle((CharSequence) this.mUser.name);
        screen.addPreference(this.mStoragePreference);
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (preference == null || this.mStoragePreference != preference) {
            return false;
        }
        Bundle args = new Bundle();
        args.putInt(StorageProfileFragment.USER_ID_EXTRA, this.mUser.id);
        args.putString("android.os.storage.extra.VOLUME_ID", "private");
        new SubSettingLauncher(this.mContext).setDestination(StorageProfileFragment.class.getName()).setArguments(args).setTitle(this.mUser.name).setSourceMetricsCategory(42).launch();
        return true;
    }

    public void handleResult(SparseArray<AppsStorageResult> stats) {
        Preconditions.checkNotNull(stats);
        AppsStorageResult result = (AppsStorageResult) stats.get(this.mUser.id);
        if (result != null) {
            setSize((((result.externalStats.totalBytes + result.otherAppsSize) + result.videoAppsSize) + result.musicAppsSize) + result.gamesSize, this.mTotalSizeBytes);
        }
    }

    public void setSize(long size, long totalSize) {
        if (this.mStoragePreference != null) {
            this.mStoragePreference.setStorageSize(size, totalSize);
        }
    }

    public void setTotalSize(long totalSize) {
        this.mTotalSizeBytes = totalSize;
    }

    public void handleUserIcons(SparseArray<Drawable> fetchedIcons) {
        Drawable userIcon = (Drawable) fetchedIcons.get(this.mUser.id);
        if (this.mUser.id == 999) {
            userIcon = this.mContext.getDrawable(R.drawable.op_parallel_apps);
            userIcon.setTint(this.mContext.getColor(R.color.oneplus_contorl_icon_color_active_default));
        }
        if (userIcon != null) {
            this.mStoragePreference.setIcon(applyTint(this.mContext, userIcon));
        }
    }

    private static Drawable applyTint(Context context, Drawable icon) {
        icon = icon.mutate();
        icon.setTint(context.getColor(R.color.oneplus_contorl_icon_color_active_default));
        return icon;
    }
}
