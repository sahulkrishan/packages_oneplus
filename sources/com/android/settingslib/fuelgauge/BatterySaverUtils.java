package com.android.settingslib.fuelgauge;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.util.KeyValueListParser;
import android.util.Slog;

public class BatterySaverUtils {
    public static final String ACTION_SHOW_AUTO_SAVER_SUGGESTION = "PNW.autoSaverSuggestion";
    public static final String ACTION_SHOW_START_SAVER_CONFIRMATION = "PNW.startSaverConfirmation";
    private static final boolean DEBUG = false;
    private static final String SYSUI_PACKAGE = "com.android.systemui";
    private static final String TAG = "BatterySaverUtils";

    private static class Parameters {
        private static final int AUTO_SAVER_SUGGESTION_END_NTH = 8;
        private static final int AUTO_SAVER_SUGGESTION_START_NTH = 4;
        public final int endNth;
        private final Context mContext;
        public final int startNth;

        public Parameters(Context context) {
            this.mContext = context;
            String newValue = Global.getString(this.mContext.getContentResolver(), "low_power_mode_suggestion_params");
            KeyValueListParser parser = new KeyValueListParser(',');
            try {
                parser.setString(newValue);
            } catch (IllegalArgumentException e) {
                String str = BatterySaverUtils.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Bad constants: ");
                stringBuilder.append(newValue);
                Slog.wtf(str, stringBuilder.toString());
            }
            this.startNth = parser.getInt("start_nth", 4);
            this.endNth = parser.getInt("end_nth", 8);
        }
    }

    private BatterySaverUtils() {
    }

    /* JADX WARNING: Missing block: B:28:0x0059, code skipped:
            return true;
     */
    public static synchronized boolean setPowerSaveMode(android.content.Context r7, boolean r8, boolean r9) {
        /*
        r0 = com.android.settingslib.fuelgauge.BatterySaverUtils.class;
        monitor-enter(r0);
        r1 = r7.getContentResolver();	 Catch:{ all -> 0x005c }
        r2 = 0;
        if (r8 == 0) goto L_0x0014;
    L_0x000a:
        if (r9 == 0) goto L_0x0014;
    L_0x000c:
        r3 = maybeShowBatterySaverConfirmation(r7);	 Catch:{ all -> 0x005c }
        if (r3 == 0) goto L_0x0014;
    L_0x0012:
        monitor-exit(r0);
        return r2;
    L_0x0014:
        if (r8 == 0) goto L_0x001b;
    L_0x0016:
        if (r9 != 0) goto L_0x001b;
    L_0x0018:
        setBatterySaverConfirmationAcknowledged(r7);	 Catch:{ all -> 0x005c }
    L_0x001b:
        r3 = android.os.PowerManager.class;
        r3 = r7.getSystemService(r3);	 Catch:{ all -> 0x005c }
        r3 = (android.os.PowerManager) r3;	 Catch:{ all -> 0x005c }
        r3 = r3.setPowerSaveMode(r8);	 Catch:{ all -> 0x005c }
        if (r3 == 0) goto L_0x005a;
    L_0x0029:
        r3 = 1;
        if (r8 == 0) goto L_0x0058;
    L_0x002c:
        r4 = "low_power_manual_activation_count";
        r4 = android.provider.Settings.Secure.getInt(r1, r4, r2);	 Catch:{ all -> 0x005c }
        r4 = r4 + r3;
        r5 = "low_power_manual_activation_count";
        android.provider.Settings.Secure.putInt(r1, r5, r4);	 Catch:{ all -> 0x005c }
        r5 = new com.android.settingslib.fuelgauge.BatterySaverUtils$Parameters;	 Catch:{ all -> 0x005c }
        r5.<init>(r7);	 Catch:{ all -> 0x005c }
        r6 = r5.startNth;	 Catch:{ all -> 0x005c }
        if (r4 < r6) goto L_0x0058;
    L_0x0041:
        r6 = r5.endNth;	 Catch:{ all -> 0x005c }
        if (r4 > r6) goto L_0x0058;
    L_0x0045:
        r6 = "low_power_trigger_level";
        r6 = android.provider.Settings.Global.getInt(r1, r6, r2);	 Catch:{ all -> 0x005c }
        if (r6 != 0) goto L_0x0058;
    L_0x004d:
        r6 = "suppress_auto_battery_saver_suggestion";
        r2 = android.provider.Settings.Secure.getInt(r1, r6, r2);	 Catch:{ all -> 0x005c }
        if (r2 != 0) goto L_0x0058;
    L_0x0055:
        showAutoBatterySaverSuggestion(r7);	 Catch:{ all -> 0x005c }
    L_0x0058:
        monitor-exit(r0);
        return r3;
    L_0x005a:
        monitor-exit(r0);
        return r2;
    L_0x005c:
        r7 = move-exception;
        monitor-exit(r0);
        throw r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.fuelgauge.BatterySaverUtils.setPowerSaveMode(android.content.Context, boolean, boolean):boolean");
    }

    private static boolean maybeShowBatterySaverConfirmation(Context context) {
        if (Secure.getInt(context.getContentResolver(), "low_power_warning_acknowledged", 0) != 0) {
            return false;
        }
        context.sendBroadcast(getSystemUiBroadcast(ACTION_SHOW_START_SAVER_CONFIRMATION));
        return true;
    }

    private static void showAutoBatterySaverSuggestion(Context context) {
        context.sendBroadcast(getSystemUiBroadcast(ACTION_SHOW_AUTO_SAVER_SUGGESTION));
    }

    private static Intent getSystemUiBroadcast(String action) {
        Intent i = new Intent(action);
        i.setFlags(268435456);
        i.setPackage("com.android.systemui");
        return i;
    }

    private static void setBatterySaverConfirmationAcknowledged(Context context) {
        Secure.putInt(context.getContentResolver(), "low_power_warning_acknowledged", 1);
    }

    public static void suppressAutoBatterySaver(Context context) {
        Secure.putInt(context.getContentResolver(), "suppress_auto_battery_saver_suggestion", 1);
    }

    public static void setAutoBatterySaverTriggerLevel(Context context, int level) {
        if (level > 0) {
            suppressAutoBatterySaver(context);
        }
        Global.putInt(context.getContentResolver(), "low_power_trigger_level", level);
    }

    public static void ensureAutoBatterySaver(Context context, int level) {
        if (Global.getInt(context.getContentResolver(), "low_power_trigger_level", 0) == 0) {
            setAutoBatterySaverTriggerLevel(context, level);
        }
    }
}
