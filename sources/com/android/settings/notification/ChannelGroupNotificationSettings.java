package com.android.settings.notification;

import android.app.NotificationChannel;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChannelGroupNotificationSettings extends NotificationSettingsBase {
    private static final String TAG = "ChannelGroupSettings";

    public int getMetricsCategory() {
        return 1218;
    }

    public void onResume() {
        super.onResume();
        if (this.mAppRow == null || this.mChannelGroup == null) {
            Log.w(TAG, "Missing package or uid or packageinfo or group");
            finish();
            return;
        }
        populateChannelList();
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
        return R.xml.notification_group_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        this.mControllers = new ArrayList();
        this.mControllers.add(new HeaderPreferenceController(context, this));
        this.mControllers.add(new BlockPreferenceController(context, this.mImportanceListener, this.mBackend));
        this.mControllers.add(new AppLinkPreferenceController(context));
        this.mControllers.add(new NotificationsOffPreferenceController(context));
        this.mControllers.add(new DescriptionPreferenceController(context));
        return new ArrayList(this.mControllers);
    }

    private void populateChannelList() {
        if (!this.mDynamicPreferences.isEmpty()) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Notification channel group posted twice to settings - old size ");
            stringBuilder.append(this.mDynamicPreferences.size());
            stringBuilder.append(", new size ");
            stringBuilder.append(this.mDynamicPreferences.size());
            Log.w(str, stringBuilder.toString());
            for (Preference p : this.mDynamicPreferences) {
                getPreferenceScreen().removePreference(p);
            }
        }
        if (this.mChannelGroup.getChannels().isEmpty()) {
            Preference empty = new Preference(getPrefContext());
            empty.setTitle((int) R.string.no_channels);
            empty.setEnabled(false);
            getPreferenceScreen().addPreference(empty);
            this.mDynamicPreferences.add(empty);
            return;
        }
        List<NotificationChannel> channels = this.mChannelGroup.getChannels();
        Collections.sort(channels, this.mChannelComparator);
        for (NotificationChannel channel : channels) {
            this.mDynamicPreferences.add(populateSingleChannelPrefs(getPreferenceScreen(), channel, this.mChannelGroup.isBlocked()));
        }
    }
}
