package com.android.settingslib;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.icu.text.MeasureFormat;
import android.icu.text.MeasureFormat.FormatWidth;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build.VERSION;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableStringBuilder;
import android.text.format.Formatter;
import android.text.style.TtsSpan.MeasureBuilder;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.UserIcons;
import com.android.settingslib.drawable.UserIconDrawable;
import com.android.settingslib.wrapper.LocationManagerWrapper;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Utils {
    private static final String CURRENT_MODE_KEY = "CURRENT_MODE";
    private static final String NEW_MODE_KEY = "NEW_MODE";
    private static final int SECONDS_PER_DAY = 86400;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;
    @VisibleForTesting
    static final String STORAGE_MANAGER_SHOW_OPT_IN_PROPERTY = "ro.storage_manager.show_opt_in";
    public static final String[] UNIT_OF_STORAGE = new String[]{"%28?<%21[吉千兆太]%29比特", "%28?<%21[吉千兆太]%29字节", "吉比特", "吉字节", "千比特", "千字节", "兆比特", "兆字节", "太比特", "太字节"};
    public static final String[] UNIT_OF_STORAGE_REPLACE = new String[]{"b", "B", "Gb", "GB", "Kb", "KB", "Mb", "MB", "Tb", "TB"};
    static final int[] WIFI_PIE = new int[]{17302781, 17302782, 17302783, 17302784, 17302785};
    private static String sPermissionControllerPackageName;
    private static String sServicesSystemSharedLibPackageName;
    private static String sSharedSystemSharedLibPackageName;
    private static Signature[] sSystemSignature;

    public static void updateLocationEnabled(Context context, boolean enabled, int userId, int source) {
        Secure.putIntForUser(context.getContentResolver(), "location_changer", source, userId);
        Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
        int i = 0;
        int oldMode = Secure.getIntForUser(context.getContentResolver(), "location_mode", 0, userId);
        if (enabled) {
            i = 3;
        }
        int newMode = i;
        intent.putExtra(CURRENT_MODE_KEY, oldMode);
        intent.putExtra(NEW_MODE_KEY, newMode);
        context.sendBroadcastAsUser(intent, UserHandle.of(userId), "android.permission.WRITE_SECURE_SETTINGS");
        new LocationManagerWrapper((LocationManager) context.getSystemService("location")).setLocationEnabledForUser(enabled, UserHandle.of(userId));
    }

    public static boolean updateLocationMode(Context context, int oldMode, int newMode, int userId, int source) {
        Secure.putIntForUser(context.getContentResolver(), "location_changer", source, userId);
        Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
        intent.putExtra(CURRENT_MODE_KEY, oldMode);
        intent.putExtra(NEW_MODE_KEY, newMode);
        context.sendBroadcastAsUser(intent, UserHandle.of(userId), "android.permission.WRITE_SECURE_SETTINGS");
        return Secure.putIntForUser(context.getContentResolver(), "location_mode", newMode, userId);
    }

    public static int getTetheringLabel(ConnectivityManager cm) {
        String[] usbRegexs = cm.getTetherableUsbRegexs();
        String[] wifiRegexs = cm.getTetherableWifiRegexs();
        String[] bluetoothRegexs = cm.getTetherableBluetoothRegexs();
        boolean bluetoothAvailable = false;
        boolean usbAvailable = usbRegexs.length != 0;
        boolean wifiAvailable = wifiRegexs.length != 0;
        if (bluetoothRegexs.length != 0) {
            bluetoothAvailable = true;
        }
        if (wifiAvailable && usbAvailable && bluetoothAvailable) {
            return R.string.tether_settings_title_all;
        }
        if (wifiAvailable && usbAvailable) {
            return R.string.tether_settings_title_all;
        }
        if (wifiAvailable && bluetoothAvailable) {
            return R.string.tether_settings_title_all;
        }
        if (wifiAvailable) {
            return R.string.tether_settings_title_wifi;
        }
        if (usbAvailable && bluetoothAvailable) {
            return R.string.tether_settings_title_usb_bluetooth;
        }
        if (usbAvailable) {
            return R.string.tether_settings_title_usb;
        }
        return R.string.tether_settings_title_bluetooth;
    }

    public static String getUserLabel(Context context, UserInfo info) {
        String name = info != null ? info.name : null;
        if (info.isManagedProfile()) {
            return context.getString(R.string.managed_user_title);
        }
        if (info.isGuest()) {
            name = context.getString(R.string.user_guest);
        }
        if (name == null && info != null) {
            name = Integer.toString(info.id);
        } else if (info == null) {
            name = context.getString(R.string.unknown);
        }
        return context.getResources().getString(R.string.running_process_item_user_label, new Object[]{name});
    }

    public static Drawable getUserIcon(Context context, UserManager um, UserInfo user) {
        int iconSize = UserIconDrawable.getSizeForList(context);
        if (user.isManagedProfile()) {
            Drawable drawable = UserIconDrawable.getManagedUserDrawable(context);
            drawable.setBounds(0, 0, iconSize, iconSize);
            return drawable;
        }
        if (user.iconPath != null) {
            Bitmap icon = um.getUserIcon(user.id);
            if (icon != null) {
                return new UserIconDrawable(iconSize).setIcon(icon).bake();
            }
        }
        return new UserIconDrawable(iconSize).setIconDrawable(UserIcons.getDefaultUserIcon(context.getResources(), user.id, false)).bake();
    }

    public static String formatPercentage(double percentage, boolean round) {
        return formatPercentage(round ? Math.round((float) percentage) : (int) percentage);
    }

    public static String formatPercentage(long amount, long total) {
        return formatPercentage(((double) amount) / ((double) total));
    }

    public static String formatPercentage(int percentage) {
        return formatPercentage(((double) percentage) / 100.0d);
    }

    public static String formatPercentage(double percentage) {
        return NumberFormat.getPercentInstance().format(percentage);
    }

    public static int getBatteryLevel(Intent batteryChangedIntent) {
        return (batteryChangedIntent.getIntExtra("level", 0) * 100) / batteryChangedIntent.getIntExtra("scale", 100);
    }

    public static String getBatteryStatus(Resources res, Intent batteryChangedIntent) {
        int status = batteryChangedIntent.getIntExtra(NotificationCompat.CATEGORY_STATUS, 1);
        if (status == 2) {
            String statusString;
            if (batteryChangedIntent.getIntExtra("level", 0) >= 100) {
                statusString = res.getString(R.string.battery_info_status_full);
            } else {
                statusString = res.getString(R.string.battery_info_status_charging);
            }
            return statusString;
        } else if (status == 3) {
            return res.getString(R.string.battery_info_status_discharging);
        } else {
            if (status == 4) {
                return res.getString(R.string.battery_info_status_not_charging);
            }
            if (status == 5) {
                return res.getString(R.string.battery_info_status_full);
            }
            return res.getString(R.string.battery_info_status_unknown);
        }
    }

    public static int getColorAccent(Context context) {
        return getColorAttr(context, 16843829);
    }

    public static int getColorError(Context context) {
        return getColorAttr(context, 16844099);
    }

    public static int getDefaultColor(Context context, int resId) {
        return context.getResources().getColorStateList(resId, context.getTheme()).getDefaultColor();
    }

    public static int getDisabled(Context context, int inputColor) {
        return applyAlphaAttr(context, 16842803, inputColor);
    }

    public static int applyAlphaAttr(Context context, int attr, int inputColor) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        float alpha = ta.getFloat(0, 0.0f);
        ta.recycle();
        return applyAlpha(alpha, inputColor);
    }

    public static int applyAlpha(float alpha, int inputColor) {
        return Color.argb((int) (alpha * ((float) Color.alpha(inputColor))), Color.red(inputColor), Color.green(inputColor), Color.blue(inputColor));
    }

    public static int getColorAttr(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        int colorAccent = ta.getColor(0, 0);
        ta.recycle();
        return colorAccent;
    }

    public static int getThemeAttr(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        int theme = ta.getResourceId(0, 0);
        ta.recycle();
        return theme;
    }

    public static Drawable getDrawable(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        Drawable drawable = ta.getDrawable(0);
        ta.recycle();
        return drawable;
    }

    public static boolean isSystemPackage(Resources resources, PackageManager pm, PackageInfo pkg) {
        if (sSystemSignature == null) {
            sSystemSignature = new Signature[]{getSystemSignature(pm)};
        }
        if (sPermissionControllerPackageName == null) {
            sPermissionControllerPackageName = pm.getPermissionControllerPackageName();
        }
        if (sServicesSystemSharedLibPackageName == null) {
            sServicesSystemSharedLibPackageName = pm.getServicesSystemSharedLibraryPackageName();
        }
        if (sSharedSystemSharedLibPackageName == null) {
            sSharedSystemSharedLibPackageName = pm.getSharedSystemSharedLibraryPackageName();
        }
        if ((sSystemSignature[0] != null && sSystemSignature[0].equals(getFirstSignature(pkg))) || pkg.packageName.equals(sPermissionControllerPackageName) || pkg.packageName.equals(sServicesSystemSharedLibPackageName) || pkg.packageName.equals(sSharedSystemSharedLibPackageName) || pkg.packageName.equals("com.android.printspooler") || isDeviceProvisioningPackage(resources, pkg.packageName)) {
            return true;
        }
        return false;
    }

    private static Signature getFirstSignature(PackageInfo pkg) {
        if (pkg == null || pkg.signatures == null || pkg.signatures.length <= 0) {
            return null;
        }
        return pkg.signatures[0];
    }

    private static Signature getSystemSignature(PackageManager pm) {
        try {
            return getFirstSignature(pm.getPackageInfo("android", 64));
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public static boolean isDeviceProvisioningPackage(Resources resources, String packageName) {
        String deviceProvisioningPackage = resources.getString(17039686);
        return deviceProvisioningPackage != null && deviceProvisioningPackage.equals(packageName);
    }

    public static int getWifiIconResource(int level) {
        if (level >= 0 && level < WIFI_PIE.length) {
            return WIFI_PIE[level];
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("No Wifi icon found for level: ");
        stringBuilder.append(level);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public static int getDefaultStorageManagerDaysToRetain(Resources resources) {
        try {
            return resources.getInteger(17694881);
        } catch (NotFoundException e) {
            return 90;
        }
    }

    public static boolean isWifiOnly(Context context) {
        return ((ConnectivityManager) context.getSystemService(ConnectivityManager.class)).isNetworkSupported(0) ^ 1;
    }

    public static boolean isStorageManagerEnabled(Context context) {
        boolean isDefaultOn;
        try {
            isDefaultOn = SystemProperties.getBoolean(STORAGE_MANAGER_SHOW_OPT_IN_PROPERTY, true) ^ true;
        } catch (NotFoundException e) {
            isDefaultOn = false;
        }
        if (Secure.getInt(context.getContentResolver(), "automatic_storage_manager_enabled", isDefaultOn ? 1 : 0) != 0) {
            return true;
        }
        return false;
    }

    public static CharSequence formatElapsedTime(Context context, double millis, boolean withSeconds) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        int seconds = (int) Math.floor(millis / 1000.0d);
        if (!withSeconds) {
            seconds += 30;
        }
        int days = 0;
        int hours = 0;
        int minutes = 0;
        if (seconds >= 86400) {
            days = seconds / 86400;
            seconds -= 86400 * days;
        }
        if (seconds >= 3600) {
            hours = seconds / 3600;
            seconds -= hours * 3600;
        }
        if (seconds >= 60) {
            minutes = seconds / 60;
            seconds -= minutes * 60;
        }
        ArrayList<Measure> measureList = new ArrayList(4);
        if (days > 0) {
            measureList.add(new Measure(Integer.valueOf(days), MeasureUnit.DAY));
        }
        if (hours > 0) {
            measureList.add(new Measure(Integer.valueOf(hours), MeasureUnit.HOUR));
        }
        if (minutes > 0) {
            measureList.add(new Measure(Integer.valueOf(minutes), MeasureUnit.MINUTE));
        }
        if (withSeconds && seconds > 0) {
            measureList.add(new Measure(Integer.valueOf(seconds), MeasureUnit.SECOND));
        }
        if (measureList.size() == 0) {
            measureList.add(new Measure(Integer.valueOf(0), withSeconds ? MeasureUnit.SECOND : MeasureUnit.MINUTE));
        }
        Measure[] measureArray = (Measure[]) measureList.toArray(new Measure[measureList.size()]);
        sb.append(MeasureFormat.getInstance(context.getResources().getConfiguration().locale, FormatWidth.NARROW).formatMeasures(measureArray));
        if (measureArray.length == 1 && MeasureUnit.MINUTE.equals(measureArray[0].getUnit())) {
            sb.setSpan(new MeasureBuilder().setNumber((long) minutes).setUnit("minute").build(), 0, sb.length(), 33);
        }
        return sb;
    }

    public static String formatFileSize(Context ctx, long size) {
        String sizeString = Formatter.formatFileSize(ctx, size);
        if (VERSION.SDK_INT > 26) {
            Locale defaultLocale = Locale.getDefault();
            String language = defaultLocale.getLanguage();
            String country = defaultLocale.getCountry();
            if (language.equalsIgnoreCase("zh") && country.equalsIgnoreCase("CN")) {
                for (int i = 0; i < UNIT_OF_STORAGE.length; i++) {
                    sizeString = sizeString.replaceAll(UNIT_OF_STORAGE[i], UNIT_OF_STORAGE_REPLACE[i]);
                }
            }
        }
        return sizeString;
    }

    public static boolean isAudioModeOngoingCall(Context context) {
        int audioMode = ((AudioManager) context.getSystemService(AudioManager.class)).getMode();
        return audioMode == 1 || audioMode == 2 || audioMode == 3;
    }
}
