package com.android.settings.connecteddevice;

import android.content.Context;
import android.provider.Settings.System;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.applications.defaultapps.DefaultPaymentSettingsPreferenceController;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.nfc.NfcPreferenceController;

public class AdvancedConnectedDeviceController extends BasePreferenceController {
    private static final String DRIVING_MODE_SETTINGS_ENABLED = "gearhead:driving_mode_settings_enabled";

    public AdvancedConnectedDeviceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public CharSequence getSummary() {
        return getConnectedDevicesSummaryString(this.mContext);
    }

    public static int getConnectedDevicesSummaryResourceId(Context context) {
        return getConnectedDevicesSummaryResourceId(new NfcPreferenceController(context, NfcPreferenceController.KEY_TOGGLE_NFC), isDrivingModeAvailable(context));
    }

    @VisibleForTesting
    static boolean isDrivingModeAvailable(Context context) {
        if (System.getInt(context.getContentResolver(), DRIVING_MODE_SETTINGS_ENABLED, 0) == 1) {
            return true;
        }
        return false;
    }

    @VisibleForTesting
    static int getConnectedDevicesSummaryResourceId(NfcPreferenceController nfcPreferenceController, boolean isDrivingModeAvailable) {
        if (nfcPreferenceController.isAvailable()) {
            if (isDrivingModeAvailable) {
                return R.string.connected_devices_dashboard_summary;
            }
            return R.string.connected_devices_dashboard_no_driving_mode_summary;
        } else if (isDrivingModeAvailable) {
            return R.string.connected_devices_dashboard_no_nfc_summary;
        } else {
            return R.string.connected_devices_dashboard_no_driving_mode_no_nfc_summary;
        }
    }

    public static CharSequence getConnectedDevicesSummaryString(Context context) {
        return getConnectedDevicesSummaryString(context, new NfcPreferenceController(context, NfcPreferenceController.KEY_TOGGLE_NFC), new DefaultPaymentSettingsPreferenceController(context), isDrivingModeAvailable(context));
    }

    static CharSequence getConnectedDevicesSummaryString(Context context, NfcPreferenceController nfcPreferenceController, DefaultPaymentSettingsPreferenceController paymentPreferenceController, boolean isDrivingModeAvailable) {
        String summary = "";
        if (nfcPreferenceController.isAvailable()) {
            summary = context.getString(R.string.restriction_nfc_enable_title);
        } else {
            summary = context.getString(R.string.op_wifi_display_summary);
        }
        String castSettingSummary = context.getString(R.string.op_wifi_display_summary).toLowerCase();
        summary = context.getString(R.string.join_many_items_middle, new Object[]{summary, castSettingSummary});
        if (!paymentPreferenceController.isAvailable()) {
            return summary;
        }
        String tapPaySettingSummary = context.getString(R.string.oneplus_nfc_payment_settings_title).toLowerCase();
        return context.getString(R.string.join_many_items_middle, new Object[]{summary, tapPaySettingSummary});
    }

    public static CharSequence getDeviceConnetionSummaryString(Context context) {
        return getDeviceConnetionSummaryString(context, new NfcPreferenceController(context, NfcPreferenceController.KEY_TOGGLE_NFC), isDrivingModeAvailable(context));
    }

    static CharSequence getDeviceConnetionSummaryString(Context context, NfcPreferenceController nfcPreferenceController, boolean isDrivingModeAvailable) {
        String summary = context.getString(R.string.bluetooth_settings_title);
        String nfcSettingSummary;
        if (nfcPreferenceController.isAvailable()) {
            nfcSettingSummary = context.getString(R.string.restriction_nfc_enable_title);
            summary = context.getString(R.string.join_many_items_middle, new Object[]{summary, nfcSettingSummary});
            String castSettingSummary = context.getString(R.string.op_wifi_display_summary).toLowerCase();
            return context.getString(R.string.join_many_items_middle, new Object[]{summary, castSettingSummary});
        }
        nfcSettingSummary = context.getString(R.string.op_wifi_display_summary);
        return context.getString(R.string.join_many_items_middle, new Object[]{summary, nfcSettingSummary});
    }
}
