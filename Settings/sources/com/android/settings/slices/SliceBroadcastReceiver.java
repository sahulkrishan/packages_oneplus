package com.android.settings.slices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.Uri.Builder;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.SliderPreferenceController;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.overlay.FeatureFactory;

public class SliceBroadcastReceiver extends BroadcastReceiver {
    private static String TAG = "SettSliceBroadcastRec";

    public void onReceive(android.content.Context r7, android.content.Intent r8) {
        /*
        r6 = this;
        r0 = r8.getAction();
        r1 = "com.android.settings.slice.extra.key";
        r1 = r8.getStringExtra(r1);
        r2 = "com.android.settings.slice.extra.platform";
        r3 = 0;
        r2 = r8.getBooleanExtra(r2, r3);
        r4 = r0.hashCode();
        r5 = -1;
        switch(r4) {
            case -2075790298: goto L_0x004c;
            case -932197342: goto L_0x0042;
            case -362341757: goto L_0x0038;
            case 17552563: goto L_0x002e;
            case 775016264: goto L_0x0024;
            case 1913359032: goto L_0x001a;
            default: goto L_0x0019;
        };
    L_0x0019:
        goto L_0x0056;
    L_0x001a:
        r4 = "com.android.settings.notification.ZEN_MODE_CHANGED";
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x0056;
    L_0x0022:
        r4 = 5;
        goto L_0x0057;
    L_0x0024:
        r4 = "com.android.settings.wifi.action.WIFI_CHANGED";
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x0056;
    L_0x002c:
        r4 = 3;
        goto L_0x0057;
    L_0x002e:
        r4 = "com.android.settings.slice.action.SLIDER_CHANGED";
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x0056;
    L_0x0036:
        r4 = 1;
        goto L_0x0057;
    L_0x0038:
        r4 = "com.android.settings.wifi.calling.action.WIFI_CALLING_CHANGED";
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x0056;
    L_0x0040:
        r4 = 4;
        goto L_0x0057;
    L_0x0042:
        r4 = "com.android.settings.bluetooth.action.BLUETOOTH_MODE_CHANGED";
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x0056;
    L_0x004a:
        r4 = 2;
        goto L_0x0057;
    L_0x004c:
        r4 = "com.android.settings.slice.action.TOGGLE_CHANGED";
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x0056;
    L_0x0054:
        r4 = r3;
        goto L_0x0057;
    L_0x0056:
        r4 = r5;
    L_0x0057:
        switch(r4) {
            case 0: goto L_0x0081;
            case 1: goto L_0x0077;
            case 2: goto L_0x0073;
            case 3: goto L_0x006f;
            case 4: goto L_0x005f;
            case 5: goto L_0x005b;
            default: goto L_0x005a;
        };
    L_0x005a:
        goto L_0x008b;
    L_0x005b:
        com.android.settings.notification.ZenModeSliceBuilder.handleUriChange(r7, r8);
        goto L_0x008b;
    L_0x005f:
        r3 = com.android.settings.overlay.FeatureFactory.getFactory(r7);
        r3 = r3.getSlicesFeatureProvider();
        r3 = r3.getNewWifiCallingSliceHelper(r7);
        r3.handleWifiCallingChanged(r8);
        goto L_0x008b;
    L_0x006f:
        com.android.settings.wifi.WifiSliceBuilder.handleUriChange(r7, r8);
        goto L_0x008b;
    L_0x0073:
        com.android.settings.bluetooth.BluetoothSliceBuilder.handleUriChange(r7, r8);
        goto L_0x008b;
    L_0x0077:
        r3 = "android.app.slice.extra.RANGE_VALUE";
        r3 = r8.getIntExtra(r3, r5);
        r6.handleSliderAction(r7, r1, r3, r2);
        goto L_0x008b;
    L_0x0081:
        r4 = "android.app.slice.extra.TOGGLE_STATE";
        r3 = r8.getBooleanExtra(r4, r3);
        r6.handleToggleAction(r7, r1, r3, r2);
    L_0x008b:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.slices.SliceBroadcastReceiver.onReceive(android.content.Context, android.content.Intent):void");
    }

    private void handleToggleAction(Context context, String key, boolean isChecked, boolean isPlatformSlice) {
        if (TextUtils.isEmpty(key)) {
            throw new IllegalStateException("No key passed to Intent for toggle controller");
        }
        BasePreferenceController controller = getPreferenceController(context, key);
        StringBuilder stringBuilder;
        if (!(controller instanceof TogglePreferenceController)) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Toggle action passed for a non-toggle key: ");
            stringBuilder.append(key);
            throw new IllegalStateException(stringBuilder.toString());
        } else if (controller.isAvailable()) {
            ((TogglePreferenceController) controller).setChecked(isChecked);
            logSliceValueChange(context, key, isChecked);
            if (!controller.hasAsyncUpdate()) {
                updateUri(context, key, isPlatformSlice);
            }
        } else {
            String str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Can't update ");
            stringBuilder.append(key);
            stringBuilder.append(" since the setting is unavailable");
            Log.w(str, stringBuilder.toString());
            if (!controller.hasAsyncUpdate()) {
                updateUri(context, key, isPlatformSlice);
            }
        }
    }

    private void handleSliderAction(Context context, String key, int newPosition, boolean isPlatformSlice) {
        if (TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException("No key passed to Intent for slider controller. Use extra: com.android.settings.slice.extra.key");
        } else if (newPosition != -1) {
            BasePreferenceController controller = getPreferenceController(context, key);
            StringBuilder stringBuilder;
            if (!(controller instanceof SliderPreferenceController)) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Slider action passed for a non-slider key: ");
                stringBuilder.append(key);
                throw new IllegalArgumentException(stringBuilder.toString());
            } else if (controller.isAvailable()) {
                SliderPreferenceController sliderController = (SliderPreferenceController) controller;
                int maxSteps = sliderController.getMaxSteps();
                if (newPosition < 0 || newPosition > maxSteps) {
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Invalid position passed to Slider controller. Expected between 0 and ");
                    stringBuilder2.append(maxSteps);
                    stringBuilder2.append(" but found ");
                    stringBuilder2.append(newPosition);
                    throw new IllegalArgumentException(stringBuilder2.toString());
                }
                sliderController.setSliderPosition(newPosition);
                logSliceValueChange(context, key, newPosition);
                updateUri(context, key, isPlatformSlice);
            } else {
                String str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Can't update ");
                stringBuilder.append(key);
                stringBuilder.append(" since the setting is unavailable");
                Log.w(str, stringBuilder.toString());
                updateUri(context, key, isPlatformSlice);
            }
        } else {
            throw new IllegalArgumentException("Invalid position passed to Slider controller");
        }
    }

    private void logSliceValueChange(Context context, String sliceKey, int newValue) {
        Pair<Integer, Object> namePair = Pair.create(Integer.valueOf(854), sliceKey);
        Pair<Integer, Object> valuePair = Pair.create(Integer.valueOf(1089), Integer.valueOf(newValue));
        FeatureFactory.getFactory(context).getMetricsFeatureProvider().action(context, 1372, namePair, valuePair);
    }

    private BasePreferenceController getPreferenceController(Context context, String key) {
        return SliceBuilderUtils.getPreferenceController(context, new SlicesDatabaseAccessor(context).getSliceDataFromKey(key));
    }

    private void updateUri(Context context, String key, boolean isPlatformDefined) {
        String authority;
        if (isPlatformDefined) {
            authority = "android.settings.slices";
        } else {
            authority = SettingsSliceProvider.SLICE_AUTHORITY;
        }
        context.getContentResolver().notifyChange(new Builder().scheme("content").authority(authority).appendPath("action").appendPath(key).build(), null);
    }
}
