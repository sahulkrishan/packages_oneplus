package com.oneplus.settings.notification;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.oneplus.settings.ui.OPLedColorPickerPreference;
import com.oneplus.settings.utils.OPUtils;

public class OPLEDSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    public static final String BATTERY_LIGHT_FULL_COLOR = "battery_light_full_color";
    public static final String BATTERY_LIGHT_LOW_COLOR = "battery_light_low_color";
    public static final String BATTERY_LIGHT_MEDIUM_COLOR = "battery_light_medium_color";
    private static final String COLOR_BLUE = "#FF0000FF";
    private static final String COLOR_BLUE_DRIVER = "#FF0000FF";
    private static final String COLOR_CYAN = "#FF40FFFF";
    private static final String COLOR_CYAN_DRIVER = "#FF40FFFF";
    private static final String COLOR_GREEN = "#FF40FF00";
    private static final String COLOR_GREEN_DRIVER = "#FF40FF00";
    private static final String COLOR_ORANGE = "#FFFFAE00";
    private static final String COLOR_ORANGE_DRIVER = "#FFFF4000";
    private static final String COLOR_PINK = "#FFEC407A";
    private static final String COLOR_PINK_DRIVER = "#FFFF0040";
    private static final String COLOR_PURPLE = "#FF9E00F9";
    private static final String COLOR_PURPLE_DRIVER = "#FFFF00FF";
    private static final String COLOR_RED = "#FFFF0000";
    private static final String COLOR_RED_DRIVER = "#FFFF0000";
    private static final String COLOR_YELLOW = "#FFFFFF00";
    private static final String COLOR_YELLOW_DRIVER = "#FFFFFF00";
    private static final String DEFAULT_COLOR_BATTERY_FULL = "#FF00FF00";
    private static final String DEFAULT_COLOR_BATTERY_LOW = "#FEFF0000";
    private static final String DEFAULT_COLOR_BATTERY_MEDIUM = "#FEFF0000";
    private static final String DEFAULT_COLOR_NOTIFICATION = "#FF00FF00";
    private static final String KEY_BATTERY_CHARGING = "led_settings_battery_charging";
    private static final String KEY_BATTERY_FULL = "led_settings_battery_full";
    private static final String KEY_BATTERY_LOW = "led_settings_battery_low";
    private static final String KEY_GLOABL_NOTIFICATION = "led_settings_global_notification";
    public static final String NOTIFICATION_LIGHT_PULSE_COLOR = "notification_light_pulse_color";
    private static final String TAG = "LEDSettings";
    private OPLedColorPickerPreference mBatteryChargingPreference;
    private OPLedColorPickerPreference mBatteryFullPreference;
    private OPLedColorPickerPreference mBatteryLowPreference;
    private String[] mDialogColorPalette = new String[]{"#FF0000FF", "#FF40FFFF", COLOR_ORANGE, "#FF40FF00", "#FFFF0000", "#FFFFFF00", COLOR_PURPLE, COLOR_PINK};
    private OPLedColorPickerPreference mGlobalNotificationPreference;

    private java.lang.String getDriverCode(java.lang.String r3) {
        /*
        r2 = this;
        r0 = "";
        r1 = r3.hashCode();
        switch(r1) {
            case -1995913790: goto L_0x0050;
            case -1654092313: goto L_0x0046;
            case -1622811997: goto L_0x003c;
            case -1622285369: goto L_0x0032;
            case -1622135453: goto L_0x0028;
            case 2021997219: goto L_0x001e;
            case 2137189663: goto L_0x0014;
            case 2137190367: goto L_0x000a;
            default: goto L_0x0009;
        };
    L_0x0009:
        goto L_0x005a;
    L_0x000a:
        r1 = "#FF40FFFF";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x0012:
        r1 = 1;
        goto L_0x005b;
    L_0x0014:
        r1 = "#FF40FF00";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x001c:
        r1 = 3;
        goto L_0x005b;
    L_0x001e:
        r1 = "#FF0000FF";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x0026:
        r1 = 0;
        goto L_0x005b;
    L_0x0028:
        r1 = "#FFFFFF00";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x0030:
        r1 = 5;
        goto L_0x005b;
    L_0x0032:
        r1 = "#FFFFAE00";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x003a:
        r1 = 2;
        goto L_0x005b;
    L_0x003c:
        r1 = "#FFFF0000";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x0044:
        r1 = 4;
        goto L_0x005b;
    L_0x0046:
        r1 = "#FFEC407A";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x004e:
        r1 = 7;
        goto L_0x005b;
    L_0x0050:
        r1 = "#FF9E00F9";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x0058:
        r1 = 6;
        goto L_0x005b;
    L_0x005a:
        r1 = -1;
    L_0x005b:
        switch(r1) {
            case 0: goto L_0x0074;
            case 1: goto L_0x0071;
            case 2: goto L_0x006e;
            case 3: goto L_0x006b;
            case 4: goto L_0x0068;
            case 5: goto L_0x0065;
            case 6: goto L_0x0062;
            case 7: goto L_0x005f;
            default: goto L_0x005e;
        };
    L_0x005e:
        goto L_0x0077;
    L_0x005f:
        r0 = "#FFFF0040";
        goto L_0x0077;
    L_0x0062:
        r0 = "#FFFF00FF";
        goto L_0x0077;
    L_0x0065:
        r0 = "#FFFFFF00";
        goto L_0x0077;
    L_0x0068:
        r0 = "#FFFF0000";
        goto L_0x0077;
    L_0x006b:
        r0 = "#FF40FF00";
        goto L_0x0077;
    L_0x006e:
        r0 = "#FFFF4000";
        goto L_0x0077;
    L_0x0071:
        r0 = "#FF40FFFF";
        goto L_0x0077;
    L_0x0074:
        r0 = "#FF0000FF";
    L_0x0077:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.notification.OPLEDSettings.getDriverCode(java.lang.String):java.lang.String");
    }

    private java.lang.String getDialogCode(java.lang.String r3) {
        /*
        r2 = this;
        r0 = "";
        r1 = r3.hashCode();
        switch(r1) {
            case -1622811997: goto L_0x0050;
            case -1622811873: goto L_0x0046;
            case -1622811293: goto L_0x003c;
            case -1622692833: goto L_0x0032;
            case -1622135453: goto L_0x0028;
            case 2021997219: goto L_0x001e;
            case 2137189663: goto L_0x0014;
            case 2137190367: goto L_0x000a;
            default: goto L_0x0009;
        };
    L_0x0009:
        goto L_0x005a;
    L_0x000a:
        r1 = "#FF40FFFF";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x0012:
        r1 = 1;
        goto L_0x005b;
    L_0x0014:
        r1 = "#FF40FF00";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x001c:
        r1 = 3;
        goto L_0x005b;
    L_0x001e:
        r1 = "#FF0000FF";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x0026:
        r1 = 0;
        goto L_0x005b;
    L_0x0028:
        r1 = "#FFFFFF00";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x0030:
        r1 = 5;
        goto L_0x005b;
    L_0x0032:
        r1 = "#FFFF4000";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x003a:
        r1 = 2;
        goto L_0x005b;
    L_0x003c:
        r1 = "#FFFF00FF";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x0044:
        r1 = 6;
        goto L_0x005b;
    L_0x0046:
        r1 = "#FFFF0040";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x004e:
        r1 = 7;
        goto L_0x005b;
    L_0x0050:
        r1 = "#FFFF0000";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x005a;
    L_0x0058:
        r1 = 4;
        goto L_0x005b;
    L_0x005a:
        r1 = -1;
    L_0x005b:
        switch(r1) {
            case 0: goto L_0x0074;
            case 1: goto L_0x0071;
            case 2: goto L_0x006e;
            case 3: goto L_0x006b;
            case 4: goto L_0x0068;
            case 5: goto L_0x0065;
            case 6: goto L_0x0062;
            case 7: goto L_0x005f;
            default: goto L_0x005e;
        };
    L_0x005e:
        goto L_0x0077;
    L_0x005f:
        r0 = "#FFEC407A";
        goto L_0x0077;
    L_0x0062:
        r0 = "#FF9E00F9";
        goto L_0x0077;
    L_0x0065:
        r0 = "#FFFFFF00";
        goto L_0x0077;
    L_0x0068:
        r0 = "#FFFF0000";
        goto L_0x0077;
    L_0x006b:
        r0 = "#FF40FF00";
        goto L_0x0077;
    L_0x006e:
        r0 = "#FFFFAE00";
        goto L_0x0077;
    L_0x0071:
        r0 = "#FF40FFFF";
        goto L_0x0077;
    L_0x0074:
        r0 = "#FF0000FF";
    L_0x0077:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.notification.OPLEDSettings.getDialogCode(java.lang.String):java.lang.String");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.op_led_settings);
        this.mGlobalNotificationPreference = (OPLedColorPickerPreference) findPreference(KEY_GLOABL_NOTIFICATION);
        this.mGlobalNotificationPreference.setColorPalette(this.mDialogColorPalette);
        this.mGlobalNotificationPreference.setDefaultColor("#FF00FF00");
        int notificationColorInt = System.getInt(getActivity().getContentResolver(), NOTIFICATION_LIGHT_PULSE_COLOR, Color.parseColor("#FF00FF00"));
        String notificationColorString = String.format("#%06X", new Object[]{Integer.valueOf(notificationColorInt)});
        if (!TextUtils.isEmpty(notificationColorString)) {
            this.mGlobalNotificationPreference.setColor(getDialogCode(notificationColorString));
        }
        this.mGlobalNotificationPreference.setMessageText((int) R.string.color_picker_led_color_message);
        this.mGlobalNotificationPreference.setImageViewVisibility();
        this.mGlobalNotificationPreference.setOnPreferenceChangeListener(this);
        this.mBatteryFullPreference = (OPLedColorPickerPreference) findPreference(KEY_BATTERY_FULL);
        this.mBatteryFullPreference.setColorPalette(this.mDialogColorPalette);
        this.mBatteryFullPreference.setDefaultColor("#FF00FF00");
        int batteryFullColorInt = System.getInt(getActivity().getContentResolver(), BATTERY_LIGHT_FULL_COLOR, Color.parseColor("#FF00FF00"));
        String batteryFullColorString = String.format("#%06X", new Object[]{Integer.valueOf(batteryFullColorInt)});
        if (!TextUtils.isEmpty(batteryFullColorString)) {
            this.mBatteryFullPreference.setColor(getDialogCode(batteryFullColorString));
        }
        this.mBatteryFullPreference.setMessageText((int) R.string.color_picker_led_color_message);
        this.mBatteryFullPreference.setImageViewVisibility();
        this.mBatteryFullPreference.setOnPreferenceChangeListener(this);
        this.mBatteryChargingPreference = (OPLedColorPickerPreference) findPreference(KEY_BATTERY_CHARGING);
        this.mBatteryChargingPreference.setColorPalette(this.mDialogColorPalette);
        this.mBatteryChargingPreference.setDefaultColor("#FEFF0000");
        int batteryChargingColorInt = System.getInt(getActivity().getContentResolver(), BATTERY_LIGHT_MEDIUM_COLOR, Color.parseColor("#FEFF0000"));
        String batteryChargingColorString = String.format("#%06X", new Object[]{Integer.valueOf(batteryChargingColorInt)});
        if (!TextUtils.isEmpty(batteryChargingColorString)) {
            this.mBatteryChargingPreference.setColor(getDialogCode(batteryChargingColorString));
        }
        this.mBatteryChargingPreference.setMessageText((int) R.string.color_picker_led_color_message);
        this.mBatteryChargingPreference.setImageViewVisibility();
        this.mBatteryChargingPreference.setOnPreferenceChangeListener(this);
        this.mBatteryLowPreference = (OPLedColorPickerPreference) findPreference(KEY_BATTERY_LOW);
        this.mBatteryLowPreference.setColorPalette(this.mDialogColorPalette);
        this.mBatteryLowPreference.setDefaultColor("#FEFF0000");
        int batteryLowColorInt = System.getInt(getActivity().getContentResolver(), BATTERY_LIGHT_LOW_COLOR, Color.parseColor("#FEFF0000"));
        String batteryLowColorString = String.format("#%06X", new Object[]{Integer.valueOf(batteryLowColorInt)});
        if (!TextUtils.isEmpty(batteryLowColorString)) {
            this.mBatteryLowPreference.setColor(getDialogCode(batteryLowColorString));
        }
        this.mBatteryLowPreference.setMessageText((int) R.string.color_picker_led_color_message);
        this.mBatteryLowPreference.setImageViewVisibility();
        this.mBatteryLowPreference.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        String key = preference.getKey();
        String color = getDriverCode((String) objValue);
        boolean isColor = (color == null || TextUtils.isEmpty(color)) ? false : true;
        if (KEY_GLOABL_NOTIFICATION.equals(key)) {
            System.putInt(getActivity().getContentResolver(), NOTIFICATION_LIGHT_PULSE_COLOR, Color.parseColor(isColor ? color : "#FF00FF00"));
        }
        if (KEY_BATTERY_FULL.equals(key)) {
            System.putInt(getActivity().getContentResolver(), BATTERY_LIGHT_FULL_COLOR, Color.parseColor(isColor ? color : "#FF00FF00"));
        }
        if (KEY_BATTERY_CHARGING.equals(key)) {
            System.putInt(getActivity().getContentResolver(), BATTERY_LIGHT_MEDIUM_COLOR, Color.parseColor(isColor ? color : "#FEFF0000"));
        }
        if (KEY_BATTERY_LOW.equals(key)) {
            System.putInt(getActivity().getContentResolver(), BATTERY_LIGHT_LOW_COLOR, Color.parseColor(isColor ? color : "#FEFF0000"));
        }
        return true;
    }

    public void onResume() {
        super.onResume();
        if (this.mBatteryLowPreference != null) {
            this.mBatteryLowPreference.setSummary((CharSequence) getResources().getString(R.string.led_settings_battery_low_summary).replace(" 5%", " 15%"));
        }
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
