package com.android.settings.slices;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.android.settings.bluetooth.BluetoothSliceBuilder;
import com.android.settings.location.LocationSliceBuilder;
import com.android.settings.notification.ZenModeSliceBuilder;
import com.android.settings.wifi.WifiSliceBuilder;
import java.net.URISyntaxException;

public class SliceDeepLinkSpringBoard extends Activity {
    public static final String ACTION_VIEW_SLICE = "com.android.settings.action.VIEW_SLICE";
    public static final String EXTRA_SLICE = "slice";
    public static final String INTENT = "intent";
    public static final String SETTINGS = "settings";
    private static final String TAG = "DeeplinkSpringboard";

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getIntent().getData();
        if (uri == null) {
            Log.e(TAG, "No data found");
            finish();
            return;
        }
        try {
            Intent intent = parse(uri, getPackageName());
            if (ACTION_VIEW_SLICE.equals(intent.getAction())) {
                Intent launchIntent;
                Uri slice = Uri.parse(intent.getStringExtra("slice"));
                if (WifiSliceBuilder.WIFI_URI.equals(slice)) {
                    launchIntent = WifiSliceBuilder.getIntent(this);
                } else if (ZenModeSliceBuilder.ZEN_MODE_URI.equals(slice)) {
                    launchIntent = ZenModeSliceBuilder.getIntent(this);
                } else if (BluetoothSliceBuilder.BLUETOOTH_URI.equals(slice)) {
                    launchIntent = BluetoothSliceBuilder.getIntent(this);
                } else if (LocationSliceBuilder.LOCATION_URI.equals(slice)) {
                    launchIntent = LocationSliceBuilder.getIntent(this);
                } else {
                    launchIntent = SliceBuilderUtils.getContentIntent(this, new SlicesDatabaseAccessor(this).getSliceDataFromUri(slice));
                    startActivity(launchIntent);
                }
                startActivity(launchIntent);
            } else {
                startActivity(intent);
            }
            finish();
        } catch (URISyntaxException e) {
            Log.e(TAG, "Error decoding uri", e);
            finish();
        } catch (IllegalStateException e2) {
            Log.w(TAG, "Couldn't launch Slice intent", e2);
            startActivity(new Intent("android.settings.SETTINGS"));
            finish();
        }
    }

    public static Intent parse(Uri uri, String pkg) throws URISyntaxException {
        Intent intent = Intent.parseUri(uri.getQueryParameter(INTENT), 2);
        intent.setComponent(null);
        if (intent.getExtras() != null) {
            intent.getExtras().clear();
        }
        intent.setPackage(pkg);
        return intent;
    }
}
