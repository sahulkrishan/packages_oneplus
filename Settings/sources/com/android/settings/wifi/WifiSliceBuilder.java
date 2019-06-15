package com.android.settings.wifi;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.Uri.Builder;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.graphics.drawable.IconCompat;
import android.text.TextUtils;
import androidx.slice.Slice;
import androidx.slice.builders.ListBuilder;
import androidx.slice.builders.SliceAction;
import com.android.settings.R;
import com.android.settings.SubSettings;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.slices.SliceBroadcastReceiver;
import com.android.settingslib.Utils;

public class WifiSliceBuilder {
    public static final String ACTION_WIFI_SLICE_CHANGED = "com.android.settings.wifi.action.WIFI_CHANGED";
    public static final IntentFilter INTENT_FILTER = new IntentFilter();
    public static final Uri WIFI_URI = new Builder().scheme("content").authority("android.settings.slices").appendPath("action").appendPath("wifi").build();

    static {
        INTENT_FILTER.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        INTENT_FILTER.addAction("android.net.wifi.STATE_CHANGE");
    }

    private WifiSliceBuilder() {
    }

    public static Slice getSlice(Context context) {
        boolean isWifiEnabled = isWifiEnabled(context);
        IconCompat icon = IconCompat.createWithResource(context, (int) R.drawable.ic_settings_wireless);
        CharSequence title = context.getString(R.string.wifi_settings);
        CharSequence summary = getSummary(context);
        int color = Utils.getColorAccent(context);
        PendingIntent toggleAction = getBroadcastIntent(context);
        SliceAction primarySliceAction = new SliceAction(getPrimaryAction(context), icon, title);
        return new ListBuilder(context, WIFI_URI, -1).setAccentColor(color).addRow(new -$$Lambda$WifiSliceBuilder$zGyWboi-khe6O7kGcUmHExYnEzU(title, summary, new SliceAction(toggleAction, null, isWifiEnabled), primarySliceAction)).build();
    }

    public static void handleUriChange(Context context, Intent intent) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WifiManager.class);
        wifiManager.setWifiEnabled(intent.getBooleanExtra("android.app.slice.extra.TOGGLE_STATE", wifiManager.isWifiEnabled()));
    }

    public static Intent getIntent(Context context) {
        String screenTitle = context.getText(R.string.wifi_settings).toString();
        return DatabaseIndexingUtils.buildSearchResultPageIntent(context, WifiSettings.class.getName(), "wifi", screenTitle, 603).setClassName(context.getPackageName(), SubSettings.class.getName()).setData(new Builder().appendPath("wifi").build());
    }

    private static boolean isWifiEnabled(Context context) {
        switch (((WifiManager) context.getSystemService(WifiManager.class)).getWifiState()) {
            case 2:
            case 3:
                return true;
            default:
                return false;
        }
    }

    private static CharSequence getSummary(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WifiManager.class);
        switch (wifiManager.getWifiState()) {
            case 0:
            case 1:
                return context.getText(R.string.switch_off_text);
            case 2:
                return context.getText(R.string.disconnected);
            case 3:
                String ssid = WifiInfo.removeDoubleQuotes(wifiManager.getConnectionInfo().getSSID());
                if (TextUtils.equals(ssid, "<unknown ssid>")) {
                    return context.getText(R.string.disconnected);
                }
                return ssid;
            default:
                return "";
        }
    }

    private static PendingIntent getPrimaryAction(Context context) {
        return PendingIntent.getActivity(context, 0, getIntent(context), 0);
    }

    private static PendingIntent getBroadcastIntent(Context context) {
        Intent intent = new Intent(ACTION_WIFI_SLICE_CHANGED);
        intent.setClass(context, SliceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, 268435456);
    }
}
