package com.android.settings.notification;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;

public class DeletedChannelsPreferenceController extends NotificationPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_DELETED = "deleted";

    public DeletedChannelsPreferenceController(Context context, NotificationBackend backend) {
        super(context, backend);
    }

    public String getPreferenceKey() {
        return KEY_DELETED;
    }

    public boolean isAvailable() {
        boolean z = false;
        if (!super.isAvailable() || this.mChannel != null || hasValidGroup()) {
            return false;
        }
        if (this.mBackend.getDeletedChannelCount(this.mAppRow.pkg, this.mAppRow.uid) > 0) {
            z = true;
        }
        return z;
    }

    public void updateState(Preference preference) {
        if (this.mAppRow != null) {
            int deletedChannelCount = this.mBackend.getDeletedChannelCount(this.mAppRow.pkg, this.mAppRow.uid);
            preference.setTitle(this.mContext.getResources().getQuantityString(R.plurals.deleted_channels, deletedChannelCount, new Object[]{Integer.valueOf(deletedChannelCount)}));
        }
        preference.setSelectable(false);
    }
}
