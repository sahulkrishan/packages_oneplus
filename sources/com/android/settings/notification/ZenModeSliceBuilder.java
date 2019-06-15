package com.android.settings.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.Uri.Builder;
import android.support.v4.graphics.drawable.IconCompat;
import androidx.slice.Slice;
import androidx.slice.builders.ListBuilder;
import androidx.slice.builders.SliceAction;
import com.android.settings.R;
import com.android.settings.SubSettings;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.slices.SettingsSliceProvider;
import com.android.settings.slices.SliceBroadcastReceiver;
import com.android.settingslib.Utils;

public class ZenModeSliceBuilder {
    public static final String ACTION_ZEN_MODE_SLICE_CHANGED = "com.android.settings.notification.ZEN_MODE_CHANGED";
    public static final IntentFilter INTENT_FILTER = new IntentFilter();
    private static final String TAG = "ZenModeSliceBuilder";
    private static final String ZEN_MODE_KEY = "zen_mode";
    public static final Uri ZEN_MODE_URI = new Builder().scheme("content").authority(SettingsSliceProvider.SLICE_AUTHORITY).appendPath("action").appendPath(ZEN_MODE_KEY).build();

    static {
        INTENT_FILTER.addAction("android.app.action.NOTIFICATION_POLICY_CHANGED");
        INTENT_FILTER.addAction("android.app.action.INTERRUPTION_FILTER_CHANGED");
        INTENT_FILTER.addAction("android.app.action.INTERRUPTION_FILTER_CHANGED_INTERNAL");
    }

    private ZenModeSliceBuilder() {
    }

    public static Slice getSlice(Context context) {
        boolean isZenModeEnabled = isZenModeEnabled(context);
        CharSequence title = context.getText(R.string.zen_mode_settings_title);
        int color = Utils.getColorAccent(context);
        PendingIntent toggleAction = getBroadcastIntent(context);
        SliceAction primarySliceAction = new SliceAction(getPrimaryAction(context), (IconCompat) null, title);
        return new ListBuilder(context, ZEN_MODE_URI, -1).setAccentColor(color).addRow(new -$$Lambda$ZenModeSliceBuilder$sz-ZmwW0wKJApaEBVBuTr2mkXrg(title, new SliceAction(toggleAction, null, isZenModeEnabled), primarySliceAction)).build();
    }

    public static void handleUriChange(Context context, Intent intent) {
        int zenMode = 0;
        if (intent.getBooleanExtra("android.app.slice.extra.TOGGLE_STATE", false)) {
            zenMode = 1;
        }
        NotificationManager.from(context).setZenMode(zenMode, null, TAG);
    }

    public static Intent getIntent(Context context) {
        return DatabaseIndexingUtils.buildSearchResultPageIntent(context, ZenModeSettings.class.getName(), ZEN_MODE_KEY, context.getText(R.string.zen_mode_settings_title).toString(), 76).setClassName(context.getPackageName(), SubSettings.class.getName()).setData(new Builder().appendPath(ZEN_MODE_KEY).build());
    }

    private static boolean isZenModeEnabled(Context context) {
        switch (((NotificationManager) context.getSystemService(NotificationManager.class)).getZenMode()) {
            case 1:
            case 2:
            case 3:
                return true;
            default:
                return false;
        }
    }

    private static PendingIntent getPrimaryAction(Context context) {
        return PendingIntent.getActivity(context, 0, getIntent(context), 0);
    }

    private static PendingIntent getBroadcastIntent(Context context) {
        return PendingIntent.getBroadcast(context, 0, new Intent(ACTION_ZEN_MODE_SLICE_CHANGED).setClass(context, SliceBroadcastReceiver.class), 268435456);
    }
}
