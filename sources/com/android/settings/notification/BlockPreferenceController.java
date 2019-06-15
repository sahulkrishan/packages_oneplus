package com.android.settings.notification;

import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.preference.Preference;
import android.widget.Switch;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;
import com.oneplus.settings.utils.OPConstants;

public class BlockPreferenceController extends NotificationPreferenceController implements PreferenceControllerMixin, OnSwitchChangeListener {
    private static final String KEY_BLOCK = "block";
    private ImportanceListener mImportanceListener;

    public BlockPreferenceController(Context context, ImportanceListener importanceListener, NotificationBackend backend) {
        super(context, backend);
        this.mImportanceListener = importanceListener;
    }

    public String getPreferenceKey() {
        return KEY_BLOCK;
    }

    public boolean isAvailable() {
        boolean z = false;
        if (this.mAppRow == null) {
            return false;
        }
        if (this.mChannel != null) {
            return isChannelBlockable();
        }
        if (this.mChannelGroup != null) {
            return isChannelGroupBlockable();
        }
        if (!this.mAppRow.systemApp || (this.mAppRow.systemApp && this.mAppRow.banned)) {
            z = true;
        }
        return z;
    }

    public void updateState(Preference preference) {
        SwitchBar bar = (SwitchBar) ((LayoutPreference) preference).findViewById(R.id.switch_bar);
        if (bar != null && this.mAppRow != null) {
            bar.setSwitchBarText(R.string.notification_switch_label, R.string.notification_switch_label);
            if (OPConstants.PACKAGENAME_DESKCOLCK.equals(this.mAppRow.pkg) || OPConstants.PACKAGENAME_INCALLUI.equals(this.mAppRow.pkg) || "com.google.android.calendar".equals(this.mAppRow.pkg) || "com.oneplus.calendar".equals(this.mAppRow.pkg) || OPConstants.PACKAGENAME_DIALER.equals(this.mAppRow.pkg)) {
                bar.hide();
            } else {
                bar.show();
            }
            try {
                bar.addOnSwitchChangeListener(this);
            } catch (IllegalStateException e) {
            }
            bar.setDisabledByAdmin(this.mAdmin);
            boolean z = false;
            if (this.mChannel != null) {
                if (!(this.mAppRow.banned || this.mChannel.getImportance() == 0)) {
                    z = true;
                }
                bar.setChecked(z);
            } else if (this.mChannelGroup != null) {
                if (!(this.mAppRow.banned || this.mChannelGroup.isBlocked())) {
                    z = true;
                }
                bar.setChecked(z);
            } else {
                bar.setChecked(this.mAppRow.banned ^ 1);
            }
        }
    }

    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        boolean blocked = isChecked ^ 1;
        boolean z = false;
        if (this.mChannel != null) {
            int originalImportance = this.mChannel.getImportance();
            if (blocked || originalImportance == 0) {
                int importance = blocked ? 0 : isDefaultChannel() ? NotificationManagerCompat.IMPORTANCE_UNSPECIFIED : 3;
                this.mChannel.setImportance(importance);
                saveChannel();
            }
            if (this.mBackend.onlyHasDefaultChannel(this.mAppRow.pkg, this.mAppRow.uid) && this.mAppRow.banned != blocked) {
                this.mAppRow.banned = blocked;
                NotificationBackend notificationBackend = this.mBackend;
                String str = this.mAppRow.pkg;
                int i = this.mAppRow.uid;
                if (!blocked) {
                    z = true;
                }
                notificationBackend.setNotificationsEnabledForPackage(str, i, z);
            }
        } else if (this.mChannelGroup != null) {
            this.mChannelGroup.setBlocked(blocked);
            this.mBackend.updateChannelGroup(this.mAppRow.pkg, this.mAppRow.uid, this.mChannelGroup);
        } else if (this.mAppRow != null) {
            this.mAppRow.banned = blocked;
            NotificationBackend notificationBackend2 = this.mBackend;
            String str2 = this.mAppRow.pkg;
            int i2 = this.mAppRow.uid;
            if (!blocked) {
                z = true;
            }
            notificationBackend2.setNotificationsEnabledForPackage(str2, i2, z);
        }
        this.mImportanceListener.onImportanceChanged();
    }
}
