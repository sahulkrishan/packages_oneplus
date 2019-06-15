package com.android.settings.fuelgauge;

import android.app.AppGlobals;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatterySipper.DrainType;
import com.android.settings.R;
import com.android.settingslib.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class BatteryEntry {
    public static final int MSG_REPORT_FULLY_DRAWN = 2;
    public static final int MSG_UPDATE_NAME_ICON = 1;
    private static final String PACKAGE_SYSTEM = "android";
    private static final String TAG = "BatteryEntry";
    static final ArrayList<BatteryEntry> mRequestQueue = new ArrayList();
    private static NameAndIconLoader mRequestThread;
    static Locale sCurrentLocale = null;
    static Handler sHandler;
    static final HashMap<String, UidToDetail> sUidCache = new HashMap();
    public final Context context;
    public String defaultPackageName;
    public Drawable icon;
    public int iconId;
    public String name;
    public final BatterySipper sipper;

    /* renamed from: com.android.settings.fuelgauge.BatteryEntry$1 */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$os$BatterySipper$DrainType = new int[DrainType.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[DrainType.IDLE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[DrainType.CELL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[DrainType.PHONE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[DrainType.WIFI.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[DrainType.BLUETOOTH.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[DrainType.SCREEN.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[DrainType.FLASHLIGHT.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[DrainType.APP.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[DrainType.USER.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[DrainType.UNACCOUNTED.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[DrainType.OVERCOUNTED.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[DrainType.CAMERA.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[DrainType.AMBIENT_DISPLAY.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
        }
    }

    private static class NameAndIconLoader extends Thread {
        private boolean mAbort = false;

        public NameAndIconLoader() {
            super("BatteryUsage Icon Loader");
        }

        public void abort() {
            this.mAbort = true;
        }

        public void run() {
            while (true) {
                BatteryEntry be;
                synchronized (BatteryEntry.mRequestQueue) {
                    if (!BatteryEntry.mRequestQueue.isEmpty()) {
                        if (this.mAbort) {
                            break;
                        }
                        be = (BatteryEntry) BatteryEntry.mRequestQueue.remove(0);
                    }
                }
                be.loadNameAndIcon();
            }
            if (BatteryEntry.sHandler != null) {
                BatteryEntry.sHandler.sendEmptyMessage(2);
            }
            BatteryEntry.mRequestQueue.clear();
        }
    }

    static class UidToDetail {
        Drawable icon;
        String name;
        String packageName;

        UidToDetail() {
        }
    }

    public static void startRequestQueue() {
        if (sHandler != null) {
            synchronized (mRequestQueue) {
                if (!mRequestQueue.isEmpty()) {
                    if (mRequestThread != null) {
                        mRequestThread.abort();
                    }
                    mRequestThread = new NameAndIconLoader();
                    mRequestThread.setPriority(1);
                    mRequestThread.start();
                    mRequestQueue.notify();
                }
            }
        }
    }

    public static void stopRequestQueue() {
        synchronized (mRequestQueue) {
            if (mRequestThread != null) {
                mRequestThread.abort();
                mRequestThread = null;
                sHandler = null;
            }
        }
    }

    public static void clearUidCache() {
        sUidCache.clear();
    }

    public BatteryEntry(Context context, Handler handler, UserManager um, BatterySipper sipper) {
        sHandler = handler;
        this.context = context;
        this.sipper = sipper;
        switch (AnonymousClass1.$SwitchMap$com$android$internal$os$BatterySipper$DrainType[sipper.drainType.ordinal()]) {
            case 1:
                this.name = context.getResources().getString(R.string.power_idle);
                this.iconId = R.drawable.ic_settings_phone_idle;
                break;
            case 2:
                this.name = context.getResources().getString(R.string.power_cell);
                this.iconId = R.drawable.ic_settings_cell_standby;
                break;
            case 3:
                this.name = context.getResources().getString(R.string.power_phone);
                this.iconId = R.drawable.ic_settings_voice_calls;
                break;
            case 4:
                this.name = context.getResources().getString(R.string.power_wifi);
                this.iconId = R.drawable.ic_settings_wireless;
                break;
            case 5:
                this.name = context.getResources().getString(R.string.power_bluetooth);
                this.iconId = R.drawable.ic_settings_bluetooth;
                break;
            case 6:
                this.name = context.getResources().getString(R.string.power_screen);
                this.iconId = R.drawable.ic_settings_display;
                break;
            case 7:
                this.name = context.getResources().getString(R.string.power_flashlight);
                this.iconId = R.drawable.ic_settings_display;
                break;
            case 8:
                PackageManager pm = context.getPackageManager();
                sipper.mPackages = pm.getPackagesForUid(sipper.uidObj.getUid());
                if (sipper.mPackages != null && sipper.mPackages.length == 1) {
                    this.defaultPackageName = pm.getPackagesForUid(sipper.uidObj.getUid())[0];
                    try {
                        this.name = pm.getApplicationLabel(pm.getApplicationInfo(this.defaultPackageName, 0)).toString();
                        break;
                    } catch (NameNotFoundException e) {
                        String str = TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("PackageManager failed to retrieve ApplicationInfo for: ");
                        stringBuilder.append(this.defaultPackageName);
                        Log.d(str, stringBuilder.toString());
                        this.name = this.defaultPackageName;
                        break;
                    }
                }
                this.name = sipper.packageWithHighestDrain;
                break;
                break;
            case 9:
                UserInfo info = um.getUserInfo(sipper.userId);
                if (info == null) {
                    this.icon = null;
                    this.name = context.getResources().getString(R.string.running_process_item_removed_user_label);
                    break;
                }
                this.icon = Utils.getUserIcon(context, um, info);
                this.name = Utils.getUserLabel(context, info);
                break;
            case 10:
                this.name = context.getResources().getString(R.string.power_unaccounted);
                this.iconId = R.drawable.ic_power_system;
                break;
            case 11:
                this.name = context.getResources().getString(R.string.power_overcounted);
                this.iconId = R.drawable.ic_power_system;
                break;
            case 12:
                this.name = context.getResources().getString(R.string.power_camera);
                this.iconId = R.drawable.ic_settings_camera;
                break;
            case 13:
                this.name = context.getResources().getString(R.string.ambient_display_screen_title);
                this.iconId = R.drawable.ic_settings_aod;
                break;
        }
        if (this.iconId > 0) {
            this.icon = context.getDrawable(this.iconId);
            this.icon.setTint(context.getResources().getColor(R.color.oneplus_contorl_icon_color_active_default));
        }
        if ((this.name == null || this.iconId == 0) && this.sipper.uidObj != null) {
            getQuickNameIconForUid(this.sipper.uidObj.getUid());
        }
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public String getLabel() {
        return this.name;
    }

    /* Access modifiers changed, original: 0000 */
    public void getQuickNameIconForUid(int uid) {
        Locale locale = Locale.getDefault();
        if (sCurrentLocale != locale) {
            clearUidCache();
            sCurrentLocale = locale;
        }
        PackageManager pm = this.context.getPackageManager();
        String uidString = Integer.toString(uid);
        if (sUidCache.containsKey(uidString)) {
            UidToDetail utd = (UidToDetail) sUidCache.get(uidString);
            this.defaultPackageName = utd.packageName;
            this.name = utd.name;
            this.icon = utd.icon;
            if (pm.getPackagesForUid(uid) == null && this.icon != null) {
                this.icon.setTint(this.context.getColor(R.color.oneplus_contorl_icon_color_active_default));
            }
            return;
        }
        this.icon = pm.getDefaultActivityIcon();
        if (pm.getPackagesForUid(uid) == null) {
            if (uid == 0) {
                this.name = this.context.getResources().getString(R.string.process_kernel_label);
            } else if ("mediaserver".equals(this.name)) {
                this.name = this.context.getResources().getString(R.string.process_mediaserver_label);
            } else if ("dex2oat".equals(this.name)) {
                this.name = this.context.getResources().getString(R.string.process_dex2oat_label);
            }
            this.iconId = R.drawable.ic_power_system;
            this.icon = this.context.getDrawable(this.iconId);
            this.icon.setTint(this.context.getColor(R.color.oneplus_contorl_icon_color_active_default));
        }
        if (sHandler != null) {
            synchronized (mRequestQueue) {
                mRequestQueue.add(this);
            }
        }
    }

    public void loadNameAndIcon() {
        if (this.sipper.uidObj != null) {
            PackageManager pm = this.context.getPackageManager();
            int uid = this.sipper.uidObj.getUid();
            if (this.sipper.mPackages == null) {
                this.sipper.mPackages = pm.getPackagesForUid(uid);
            }
            String[] packages = extractPackagesFromSipper(this.sipper);
            if (packages != null) {
                int i;
                String str;
                String[] packageLabels = new String[packages.length];
                System.arraycopy(packages, 0, packageLabels, 0, packages.length);
                IPackageManager ipm = AppGlobals.getPackageManager();
                int userId = UserHandle.getUserId(uid);
                for (i = 0; i < packageLabels.length; i++) {
                    StringBuilder stringBuilder;
                    try {
                        ApplicationInfo ai = ipm.getApplicationInfo(packageLabels[i], 0, userId);
                        if (ai == null) {
                            str = TAG;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("Retrieving null app info for package ");
                            stringBuilder.append(packageLabels[i]);
                            stringBuilder.append(", user ");
                            stringBuilder.append(userId);
                            Log.d(str, stringBuilder.toString());
                        } else {
                            CharSequence label = ai.loadLabel(pm);
                            if (label != null) {
                                packageLabels[i] = label.toString();
                            }
                            if (ai.icon != 0) {
                                this.defaultPackageName = packages[i];
                                this.icon = ai.loadIcon(pm);
                                break;
                            }
                        }
                    } catch (RemoteException e) {
                        str = TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Error while retrieving app info for package ");
                        stringBuilder.append(packageLabels[i]);
                        stringBuilder.append(", user ");
                        stringBuilder.append(userId);
                        Log.d(str, stringBuilder.toString(), e);
                    }
                }
                if (packageLabels.length == 1) {
                    this.name = packageLabels[0];
                } else {
                    for (String str2 : packages) {
                        String str3;
                        StringBuilder stringBuilder2;
                        try {
                            PackageInfo pi = ipm.getPackageInfo(str2, 0, userId);
                            if (pi == null) {
                                str3 = TAG;
                                stringBuilder2 = new StringBuilder();
                                stringBuilder2.append("Retrieving null package info for package ");
                                stringBuilder2.append(str2);
                                stringBuilder2.append(", user ");
                                stringBuilder2.append(userId);
                                Log.d(str3, stringBuilder2.toString());
                            } else if (pi.sharedUserLabel != 0) {
                                CharSequence nm = pm.getText(str2, pi.sharedUserLabel, pi.applicationInfo);
                                if (nm != null) {
                                    this.name = nm.toString();
                                    if (pi.applicationInfo.icon != 0) {
                                        this.defaultPackageName = str2;
                                        this.icon = pi.applicationInfo.loadIcon(pm);
                                    }
                                }
                            } else {
                                continue;
                            }
                        } catch (RemoteException e2) {
                            str3 = TAG;
                            stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("Error while retrieving package info for package ");
                            stringBuilder2.append(str2);
                            stringBuilder2.append(", user ");
                            stringBuilder2.append(userId);
                            Log.d(str3, stringBuilder2.toString(), e2);
                        }
                    }
                }
            }
            String uidString = Integer.toString(uid);
            if (this.name == null) {
                this.name = uidString;
            }
            if (this.icon == null) {
                this.icon = pm.getDefaultActivityIcon();
            }
            UidToDetail utd = new UidToDetail();
            utd.name = this.name;
            utd.icon = this.icon;
            utd.packageName = this.defaultPackageName;
            sUidCache.put(uidString, utd);
            if (sHandler != null) {
                sHandler.sendMessage(sHandler.obtainMessage(1, this));
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public String[] extractPackagesFromSipper(BatterySipper sipper) {
        if (sipper.getUid() == 1000) {
            return new String[]{PACKAGE_SYSTEM};
        }
        return sipper.mPackages;
    }
}
