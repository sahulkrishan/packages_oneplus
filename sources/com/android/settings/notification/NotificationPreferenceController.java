package com.android.settings.notification;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;
import com.android.settings.notification.NotificationBackend.AppRow;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.Objects;

public abstract class NotificationPreferenceController extends AbstractPreferenceController {
    private static final String TAG = "ChannelPrefContr";
    protected EnforcedAdmin mAdmin;
    protected AppRow mAppRow;
    protected final NotificationBackend mBackend;
    protected NotificationChannel mChannel;
    protected NotificationChannelGroup mChannelGroup;
    protected final Context mContext;
    protected final NotificationManager mNm = ((NotificationManager) this.mContext.getSystemService("notification"));
    protected final PackageManager mPm;
    protected Preference mPreference;
    protected final UserManager mUm;

    public NotificationPreferenceController(Context context, NotificationBackend backend) {
        super(context);
        this.mContext = context;
        this.mBackend = backend;
        this.mUm = (UserManager) this.mContext.getSystemService("user");
        this.mPm = this.mContext.getPackageManager();
    }

    public boolean isAvailable() {
        boolean z = false;
        if (this.mAppRow == null || this.mAppRow.banned) {
            return false;
        }
        if (this.mChannel != null) {
            if (this.mChannel.getImportance() != 0) {
                z = true;
            }
            return z;
        } else if (this.mChannelGroup != null) {
            return this.mChannelGroup.isBlocked() ^ 1;
        } else {
            return true;
        }
    }

    private void findAndRemovePreference(PreferenceGroup prefGroup, String key) {
        for (int i = prefGroup.getPreferenceCount() - 1; i >= 0; i--) {
            Preference preference = prefGroup.getPreference(i);
            String curKey = preference.getKey();
            if (curKey != null && curKey.equals(key)) {
                this.mPreference = preference;
                prefGroup.removePreference(preference);
            }
            if (preference instanceof PreferenceGroup) {
                findAndRemovePreference((PreferenceGroup) preference, key);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onResume(AppRow appRow, NotificationChannel channel, NotificationChannelGroup group, EnforcedAdmin admin) {
        this.mAppRow = appRow;
        this.mChannel = channel;
        this.mChannelGroup = group;
        this.mAdmin = admin;
    }

    /* Access modifiers changed, original: protected */
    public boolean checkCanBeVisible(int minImportanceVisible) {
        boolean z = false;
        if (this.mChannel == null) {
            Log.w(TAG, "No channel");
            return false;
        }
        int importance = this.mChannel.getImportance();
        if (importance == NotificationManagerCompat.IMPORTANCE_UNSPECIFIED) {
            return true;
        }
        if (importance >= minImportanceVisible) {
            z = true;
        }
        return z;
    }

    /* Access modifiers changed, original: protected */
    public void saveChannel() {
        if (this.mChannel != null && this.mAppRow != null) {
            this.mBackend.updateChannel(this.mAppRow.pkg, this.mAppRow.uid, this.mChannel);
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean isChannelConfigurable() {
        if (this.mChannel == null || this.mAppRow == null) {
            return false;
        }
        return Objects.equals(this.mChannel.getId(), this.mAppRow.lockedChannelId) ^ 1;
    }

    /* Access modifiers changed, original: protected */
    public boolean isChannelBlockable() {
        boolean z = false;
        if (this.mChannel == null || this.mAppRow == null) {
            return false;
        }
        if (!this.mAppRow.systemApp) {
            return true;
        }
        if (this.mChannel.isBlockableSystem() || this.mChannel.getImportance() == 0) {
            z = true;
        }
        return z;
    }

    /* Access modifiers changed, original: protected */
    public boolean isChannelGroupBlockable() {
        if (this.mChannelGroup == null || this.mAppRow == null) {
            return false;
        }
        if (this.mAppRow.systemApp) {
            return this.mChannelGroup.isBlocked();
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public boolean hasValidGroup() {
        return this.mChannelGroup != null;
    }

    /* Access modifiers changed, original: protected|final */
    public final boolean isDefaultChannel() {
        if (this.mChannel == null) {
            return false;
        }
        return Objects.equals("miscellaneous", this.mChannel.getId());
    }
}
