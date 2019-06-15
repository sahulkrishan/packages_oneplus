package com.android.settings.sim;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.support.v4.app.NotificationCompat.Builder;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.Settings.SimSettingsActivity;
import com.android.settings.Utils;
import java.util.List;
import org.codeaurora.internal.IExtTelephony;
import org.codeaurora.internal.IExtTelephony.Stub;

public class SimSelectNotification extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 1;
    private static final String SIM_SELECT_NOTIFICATION_CHANNEL = "sim_select_notification_channel";
    private static final String TAG = "SimSelectNotification";

    public void onReceive(Context context, Intent intent) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        int numSlots = telephonyManager.getSimCount();
        boolean skipUserSelection = SystemProperties.getBoolean("persist.vendor.radio.aosp_usr_pref_sel", false) ^ true;
        IExtTelephony extTelephony = Stub.asInterface(ServiceManager.getService("extphone"));
        if (extTelephony != null) {
            try {
                if (!extTelephony.isVendorApkAvailable("com.qualcomm.qti.simsettings")) {
                    skipUserSelection = false;
                }
            } catch (RemoteException e) {
                skipUserSelection = false;
            }
        }
        if (numSlots < 2 || !Utils.isDeviceProvisioned(context) || skipUserSelection) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(" no of slots ");
            stringBuilder.append(numSlots);
            stringBuilder.append(" provision = ");
            stringBuilder.append(Utils.isDeviceProvisioned(context));
            Log.d(str, stringBuilder.toString());
            return;
        }
        cancelNotification(context);
        String simStatus = intent.getStringExtra("ss");
        if ("ABSENT".equals(simStatus) || "LOADED".equals(simStatus)) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("simstatus = ");
            stringBuilder2.append(simStatus);
            Log.d(str2, stringBuilder2.toString());
            int i = 0;
            while (i < numSlots) {
                int state = telephonyManager.getSimState(i);
                if (state == 1 || state == 5 || state == 0) {
                    i++;
                } else {
                    Log.d(TAG, "All sims not in valid state yet");
                    return;
                }
            }
            List<SubscriptionInfo> sil = subscriptionManager.getActiveSubscriptionInfoList();
            if (sil == null || sil.size() < 1) {
                Log.d(TAG, "Subscription list is empty");
                return;
            }
            subscriptionManager.clearDefaultsForInactiveSubIds();
            boolean dataSelected = SubscriptionManager.isUsableSubIdValue(SubscriptionManager.getDefaultDataSubscriptionId());
            boolean smsSelected = SubscriptionManager.isUsableSubIdValue(SubscriptionManager.getDefaultSmsSubscriptionId());
            if (dataSelected && smsSelected) {
                Log.d(TAG, "Data & SMS default sims are selected. No notification");
                return;
            }
            createNotification(context);
            Intent newIntent;
            if (sil.size() == 1) {
                newIntent = new Intent(context, SimDialogActivity.class);
                newIntent.addFlags(268435456);
                newIntent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, 3);
                newIntent.putExtra(SimDialogActivity.PREFERRED_SIM, ((SubscriptionInfo) sil.get(0)).getSimSlotIndex());
                context.startActivity(newIntent);
            } else if (!dataSelected) {
                newIntent = new Intent(context, SimDialogActivity.class);
                newIntent.addFlags(268435456);
                newIntent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, 0);
                context.startActivity(newIntent);
            }
            return;
        }
        Log.d(TAG, "sim state is not Absent or Loaded");
    }

    private void createNotification(Context context) {
        Resources resources = context.getResources();
        NotificationChannel notificationChannel = new NotificationChannel(SIM_SELECT_NOTIFICATION_CHANNEL, resources.getString(R.string.sim_selection_channel_title), 2);
        Builder builder = new Builder(context, SIM_SELECT_NOTIFICATION_CHANNEL).setSmallIcon(R.drawable.ic_sim_card_alert_white_48dp).setColor(context.getColor(R.color.sim_noitification)).setContentTitle(resources.getString(R.string.sim_notification_title)).setContentText(resources.getString(R.string.sim_notification_summary));
        Intent resultIntent = new Intent(context, SimSettingsActivity.class);
        resultIntent.addFlags(268435456);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, resultIntent, 268435456));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(1, builder.build());
    }

    public static void cancelNotification(Context context) {
        ((NotificationManager) context.getSystemService("notification")).cancel(1);
    }
}
