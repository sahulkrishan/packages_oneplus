package com.android.settings.bluetooth;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
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
import com.android.settings.connecteddevice.BluetoothDashboardFragment;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.slices.SliceBroadcastReceiver;
import com.android.settingslib.Utils;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class BluetoothSliceBuilder {
    public static final String ACTION_BLUETOOTH_SLICE_CHANGED = "com.android.settings.bluetooth.action.BLUETOOTH_MODE_CHANGED";
    public static final Uri BLUETOOTH_URI = new Builder().scheme("content").authority("android.settings.slices").appendPath("action").appendPath("bluetooth").build();
    public static final IntentFilter INTENT_FILTER = new IntentFilter();
    private static final String TAG = "BluetoothSliceBuilder";

    static {
        INTENT_FILTER.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        INTENT_FILTER.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
    }

    private BluetoothSliceBuilder() {
    }

    public static Slice getSlice(Context context) {
        boolean isBluetoothEnabled = isBluetoothEnabled();
        CharSequence title = context.getText(R.string.bluetooth_settings);
        IconCompat icon = IconCompat.createWithResource(context, (int) R.drawable.ic_settings_bluetooth);
        int color = Utils.getColorAccent(context);
        PendingIntent toggleAction = getBroadcastIntent(context);
        SliceAction primarySliceAction = new SliceAction(getPrimaryAction(context), icon, title);
        return new ListBuilder(context, BLUETOOTH_URI, -1).setAccentColor(color).addRow(new -$$Lambda$BluetoothSliceBuilder$t1meuX4HmFYfZCMXndFcRlc9eII(title, new SliceAction(toggleAction, null, isBluetoothEnabled), primarySliceAction)).build();
    }

    public static Intent getIntent(Context context) {
        String screenTitle = context.getText(R.string.bluetooth_settings_title).toString();
        return DatabaseIndexingUtils.buildSearchResultPageIntent(context, BluetoothDashboardFragment.class.getName(), null, screenTitle, 747).setClassName(context.getPackageName(), SubSettings.class.getName()).setData(new Builder().appendPath("bluetooth").build());
    }

    public static void handleUriChange(Context context, Intent intent) {
        LocalBluetoothManager.getInstance(context, null).getBluetoothAdapter().setBluetoothEnabled(intent.getBooleanExtra("android.app.slice.extra.TOGGLE_STATE", false));
    }

    private static boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    private static PendingIntent getPrimaryAction(Context context) {
        return PendingIntent.getActivity(context, 0, getIntent(context), 0);
    }

    private static PendingIntent getBroadcastIntent(Context context) {
        return PendingIntent.getBroadcast(context, 0, new Intent(ACTION_BLUETOOTH_SLICE_CHANGED).setClass(context, SliceBroadcastReceiver.class), 268435456);
    }
}
