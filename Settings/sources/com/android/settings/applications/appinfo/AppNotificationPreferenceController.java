package com.android.settings.applications.appinfo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.notification.AppNotificationSettings;
import com.android.settings.notification.NotificationBackend;
import com.android.settings.notification.NotificationBackend.AppRow;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

public class AppNotificationPreferenceController extends AppInfoPreferenceControllerBase {
    private final NotificationBackend mBackend = new NotificationBackend();
    private String mChannelId = null;

    public AppNotificationPreferenceController(Context context, String key) {
        super(context, key);
    }

    public void setParentFragment(AppInfoDashboardFragment parent) {
        super.setParentFragment(parent);
        if (parent != null && parent.getActivity() != null && parent.getActivity().getIntent() != null) {
            this.mChannelId = parent.getActivity().getIntent().getStringExtra(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY);
        }
    }

    public void updateState(Preference preference) {
        preference.setSummary(getNotificationSummary(this.mParent.getAppEntry(), this.mContext, this.mBackend));
    }

    /* Access modifiers changed, original: protected */
    public Class<? extends SettingsPreferenceFragment> getDetailFragmentClass() {
        return AppNotificationSettings.class;
    }

    /* Access modifiers changed, original: protected */
    public Bundle getArguments() {
        if (this.mChannelId == null) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putString(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY, this.mChannelId);
        return bundle;
    }

    private CharSequence getNotificationSummary(AppEntry appEntry, Context context, NotificationBackend backend) {
        return getNotificationSummary(backend.loadAppRow(context, context.getPackageManager(), appEntry.info), context);
    }

    public static CharSequence getNotificationSummary(AppRow appRow, Context context) {
        if (appRow == null) {
            return "";
        }
        if (appRow.banned) {
            return context.getText(R.string.notifications_disabled);
        }
        if (appRow.channelCount == 0) {
            return context.getText(R.string.notifications_enabled);
        }
        if (appRow.channelCount == appRow.blockedChannelCount) {
            return context.getText(R.string.notifications_disabled);
        }
        if (appRow.blockedChannelCount == 0) {
            return context.getText(R.string.notifications_enabled);
        }
        Object[] objArr = new Object[1];
        objArr[0] = context.getResources().getQuantityString(R.plurals.notifications_categories_off, appRow.blockedChannelCount, new Object[]{Integer.valueOf(appRow.blockedChannelCount)});
        return context.getString(R.string.notifications_enabled_with_info, objArr);
    }
}
