package com.android.settings;

import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.app.AppGlobals;
import android.app.Dialog;
import android.app.Fragment;
import android.app.IActivityManager;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.INetworkManagementService.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.preference.PreferenceFrameLayout;
import android.preference.PreferenceFrameLayout.LayoutParams;
import android.provider.ContactsContract.Profile;
import android.provider.Settings.Global;
import android.provider.Telephony.Carriers;
import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.TtsSpan.TextBuilder;
import android.util.ArraySet;
import android.util.IconDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import com.android.internal.R;
import com.android.internal.app.UnlaunchableAppActivity;
import com.android.internal.util.ArrayUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.setupwizardlib.util.WizardManagerHelper;
import com.oneplus.settings.utils.OPFirewallUtils;
import java.net.InetAddress;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.codeaurora.internal.IExtTelephony;

public final class Utils extends com.android.settingslib.Utils {
    public static final int[] BADNESS_COLORS = new int[]{0, -3917784, -1750760, -754944, -344276, -9986505, -16089278};
    public static final String OS_PKG = "os";
    public static final String PERSISTENT = "persistent";
    public static final String READ_ONLY = "read_only";
    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final String TAG = "Settings";
    public static final int UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY = 1;
    private static final StringBuilder sBuilder = new StringBuilder(50);
    private static final Formatter sFormatter = new Formatter(sBuilder, Locale.getDefault());

    public static boolean updatePreferenceToSpecificActivityOrRemove(Context context, PreferenceGroup parentPreferenceGroup, String preferenceKey, int flags) {
        Preference preference = parentPreferenceGroup.findPreference(preferenceKey);
        if (preference == null) {
            return false;
        }
        Intent intent = preference.getIntent();
        if (intent != null) {
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                ResolveInfo resolveInfo = (ResolveInfo) list.get(i);
                if ((resolveInfo.activityInfo.applicationInfo.flags & 1) != 0) {
                    preference.setIntent(new Intent().setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
                    if ((flags & 1) != 0) {
                        preference.setTitle(resolveInfo.loadLabel(pm));
                    }
                    return true;
                }
            }
        }
        parentPreferenceGroup.removePreference(preference);
        return false;
    }

    public static UserManager getUserManager(Context context) {
        UserManager um = UserManager.get(context);
        if (um != null) {
            return um;
        }
        throw new IllegalStateException("Unable to load UserManager");
    }

    public static boolean isMonkeyRunning() {
        return ActivityManager.isUserAMonkey();
    }

    public static boolean isVoiceCapable(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService("phone");
        return telephony != null && telephony.isVoiceCapable();
    }

    public static String getWifiIpAddresses(Context context) {
        Network currentNetwork = ((WifiManager) context.getSystemService(WifiManager.class)).getCurrentNetwork();
        if (currentNetwork != null) {
            return formatIpAddresses(((ConnectivityManager) context.getSystemService("connectivity")).getLinkProperties(currentNetwork));
        }
        return null;
    }

    private static String formatIpAddresses(LinkProperties prop) {
        if (prop == null) {
            return null;
        }
        Iterator<InetAddress> iter = prop.getAllAddresses().iterator();
        if (!iter.hasNext()) {
            return null;
        }
        String addresses = "";
        while (iter.hasNext()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(addresses);
            stringBuilder.append(((InetAddress) iter.next()).getHostAddress());
            addresses = stringBuilder.toString();
            if (iter.hasNext()) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(addresses);
                stringBuilder.append("\n");
                addresses = stringBuilder.toString();
            }
        }
        return addresses;
    }

    public static Locale createLocaleFromString(String localeStr) {
        if (localeStr == null) {
            return Locale.getDefault();
        }
        String[] brokenDownLocale = localeStr.split("_", 3);
        if (1 == brokenDownLocale.length) {
            return new Locale(brokenDownLocale[0]);
        }
        if (2 == brokenDownLocale.length) {
            return new Locale(brokenDownLocale[0], brokenDownLocale[1]);
        }
        return new Locale(brokenDownLocale[0], brokenDownLocale[1], brokenDownLocale[2]);
    }

    public static boolean isBatteryPresent(Intent batteryChangedIntent) {
        return batteryChangedIntent.getBooleanExtra("present", true);
    }

    public static String getBatteryPercentage(Intent batteryChangedIntent) {
        return com.android.settingslib.Utils.formatPercentage(com.android.settingslib.Utils.getBatteryLevel(batteryChangedIntent));
    }

    public static Dialog buildGlobalChangeWarningDialog(Context context, int titleResId, final Runnable positiveAction) {
        Builder builder = new Builder(context);
        builder.setTitle(titleResId);
        builder.setMessage(R.string.locale_not_translated);
        builder.setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                positiveAction.run();
            }
        });
        builder.setNegativeButton(17039360, null);
        return builder.create();
    }

    public static void forcePrepareCustomPreferencesList(ViewGroup parent, View child, ListView list, boolean ignoreSidePadding) {
        list.setScrollBarStyle(33554432);
        list.setClipToPadding(false);
        prepareCustomPreferencesList(parent, child, list, ignoreSidePadding);
    }

    public static void prepareCustomPreferencesList(ViewGroup parent, View child, View list, boolean ignoreSidePadding) {
        if (list.getScrollBarStyle() == 33554432) {
            Resources res = list.getResources();
            int paddingSide = res.getDimensionPixelSize(R.dimen.settings_side_margin);
            int paddingBottom = res.getDimensionPixelSize(17105260);
            if (parent instanceof PreferenceFrameLayout) {
                ((LayoutParams) child.getLayoutParams()).removeBorders = true;
                int effectivePaddingSide = ignoreSidePadding ? 0 : paddingSide;
                list.setPaddingRelative(effectivePaddingSide, 0, effectivePaddingSide, paddingBottom);
                return;
            }
            list.setPaddingRelative(paddingSide, 0, paddingSide, paddingBottom);
        }
    }

    public static void forceCustomPadding(View view, boolean additive) {
        Resources res = view.getResources();
        int paddingSide = res.getDimensionPixelSize(R.dimen.settings_side_margin);
        view.setPaddingRelative((additive ? view.getPaddingStart() : 0) + paddingSide, 0, (additive ? view.getPaddingEnd() : 0) + paddingSide, res.getDimensionPixelSize(17105260));
    }

    public static String getMeProfileName(Context context, boolean full) {
        if (full) {
            return getProfileDisplayName(context);
        }
        return getShorterNameIfPossible(context);
    }

    private static String getShorterNameIfPossible(Context context) {
        String given = getLocalProfileGivenName(context);
        return !TextUtils.isEmpty(given) ? given : getProfileDisplayName(context);
    }

    private static String getLocalProfileGivenName(Context context) {
        ContentResolver cr = context.getContentResolver();
        Cursor localRawProfile = cr.query(Profile.CONTENT_RAW_CONTACTS_URI, new String[]{OPFirewallUtils._ID}, "account_type IS NULL AND account_name IS NULL", null, null);
        if (localRawProfile == null) {
            return null;
        }
        try {
            if (!localRawProfile.moveToFirst()) {
                return null;
            }
            long localRowProfileId = localRawProfile.getLong(0);
            localRawProfile.close();
            Uri build = Profile.CONTENT_URI.buildUpon().appendPath("data").build();
            String[] strArr = new String[]{"data2", "data3"};
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("raw_contact_id=");
            stringBuilder.append(localRowProfileId);
            Cursor structuredName = cr.query(build, strArr, stringBuilder.toString(), null, null);
            if (structuredName == null) {
                return null;
            }
            try {
                if (!structuredName.moveToFirst()) {
                    return null;
                }
                String partialName = structuredName.getString(0);
                if (TextUtils.isEmpty(partialName)) {
                    partialName = structuredName.getString(1);
                }
                structuredName.close();
                return partialName;
            } finally {
                structuredName.close();
            }
        } finally {
            localRawProfile.close();
        }
    }

    private static final String getProfileDisplayName(Context context) {
        ContentResolver profile = context.getContentResolver();
        Cursor profile2 = profile.query(Profile.CONTENT_URI, new String[]{"display_name"}, null, null, null);
        if (profile2 == null) {
            return null;
        }
        try {
            if (!profile2.moveToFirst()) {
                return null;
            }
            String string = profile2.getString(0);
            profile2.close();
            return string;
        } finally {
            profile2.close();
        }
    }

    public static boolean hasMultipleUsers(Context context) {
        return ((UserManager) context.getSystemService("user")).getUsers().size() > 1;
    }

    public static UserHandle getManagedProfile(UserManager userManager) {
        List<UserHandle> userProfiles = userManager.getUserProfiles();
        int count = userProfiles.size();
        for (int i = 0; i < count; i++) {
            UserHandle profile = (UserHandle) userProfiles.get(i);
            if (profile.getIdentifier() != userManager.getUserHandle()) {
                UserInfo userInfo = userManager.getUserInfo(profile.getIdentifier());
                if (userInfo.isManagedProfile() && userInfo.id != 999) {
                    return profile;
                }
            }
        }
        return null;
    }

    public static UserHandle getManagedProfileWithDisabled(UserManager userManager) {
        int myUserId = UserHandle.myUserId();
        List<UserInfo> profiles = userManager.getProfiles(myUserId);
        int count = profiles.size();
        for (int i = 0; i < count; i++) {
            UserInfo profile = (UserInfo) profiles.get(i);
            if (profile.isManagedProfile() && profile.getUserHandle().getIdentifier() != myUserId) {
                return profile.getUserHandle();
            }
        }
        return null;
    }

    public static int getManagedProfileId(UserManager um, int parentUserId) {
        for (int profileId : um.getProfileIdsWithDisabled(parentUserId)) {
            if (profileId != parentUserId) {
                return profileId;
            }
        }
        return -10000;
    }

    public static UserHandle getSecureTargetUser(IBinder activityToken, UserManager um, Bundle arguments, Bundle intentExtras) {
        UserHandle currentUser = new UserHandle(UserHandle.myUserId());
        IActivityManager am = ActivityManager.getService();
        try {
            boolean launchedFromSettingsApp = "com.android.settings".equals(am.getLaunchedFromPackage(activityToken));
            UserHandle launchedFromUser = new UserHandle(UserHandle.getUserId(am.getLaunchedFromUid(activityToken)));
            if (!launchedFromUser.equals(currentUser) && isProfileOf(um, launchedFromUser)) {
                return launchedFromUser;
            }
            UserHandle extrasUser = getUserHandleFromBundle(intentExtras);
            if (extrasUser != null && !extrasUser.equals(currentUser) && launchedFromSettingsApp && isProfileOf(um, extrasUser)) {
                return extrasUser;
            }
            UserHandle argumentsUser = getUserHandleFromBundle(arguments);
            if (argumentsUser == null || argumentsUser.equals(currentUser) || !launchedFromSettingsApp || !isProfileOf(um, argumentsUser)) {
                return currentUser;
            }
            return argumentsUser;
        } catch (RemoteException e) {
            Log.v(TAG, "Could not talk to activity manager.", e);
        }
    }

    private static UserHandle getUserHandleFromBundle(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        UserHandle user = (UserHandle) bundle.getParcelable("android.intent.extra.USER");
        if (user != null) {
            return user;
        }
        int userId = bundle.getInt("android.intent.extra.USER_ID", -1);
        if (userId != -1) {
            return UserHandle.of(userId);
        }
        return null;
    }

    private static boolean isProfileOf(UserManager um, UserHandle otherUser) {
        boolean z = false;
        if (um == null || otherUser == null) {
            return false;
        }
        if (UserHandle.myUserId() == otherUser.getIdentifier() || um.getUserProfiles().contains(otherUser)) {
            z = true;
        }
        return z;
    }

    public static boolean showSimCardTile(Context context) {
        return ((TelephonyManager) context.getSystemService("phone")).getSimCount() > 1;
    }

    public static UserInfo getExistingUser(UserManager userManager, UserHandle checkUser) {
        List<UserInfo> users = userManager.getUsers(true);
        int checkUserId = checkUser.getIdentifier();
        for (UserInfo user : users) {
            if (user.id == checkUserId) {
                return user;
            }
        }
        return null;
    }

    public static View inflateCategoryHeader(LayoutInflater inflater, ViewGroup parent) {
        TypedArray a = inflater.getContext().obtainStyledAttributes(null, R.styleable.Preference, 16842892, 0);
        int resId = a.getResourceId(3, 0);
        a.recycle();
        return inflater.inflate(resId, parent, false);
    }

    public static ArraySet<String> getHandledDomains(PackageManager pm, String packageName) {
        List<IntentFilterVerificationInfo> iviList = pm.getIntentFilterVerifications(packageName);
        List<IntentFilter> filters = pm.getAllIntentFilters(packageName);
        ArraySet<String> result = new ArraySet();
        if (iviList != null && iviList.size() > 0) {
            for (IntentFilterVerificationInfo ivi : iviList) {
                for (String host : ivi.getDomains()) {
                    result.add(host);
                }
            }
        }
        if (filters != null && filters.size() > 0) {
            for (IntentFilter filter : filters) {
                if (filter.hasCategory("android.intent.category.BROWSABLE") && (filter.hasDataScheme("http") || filter.hasDataScheme("https"))) {
                    result.addAll(filter.getHostsList());
                }
            }
        }
        return result;
    }

    public static ApplicationInfo getAdminApplicationInfo(Context context, int profileId) {
        ComponentName mdmPackage = ((DevicePolicyManager) context.getSystemService("device_policy")).getProfileOwnerAsUser(profileId);
        if (mdmPackage == null) {
            return null;
        }
        String mdmPackageName = mdmPackage.getPackageName();
        try {
            return AppGlobals.getPackageManager().getApplicationInfo(mdmPackageName, 0, profileId);
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error while retrieving application info for package ");
            stringBuilder.append(mdmPackageName);
            stringBuilder.append(", userId ");
            stringBuilder.append(profileId);
            Log.e(str, stringBuilder.toString(), e);
            return null;
        }
    }

    public static boolean isBandwidthControlEnabled() {
        try {
            return Stub.asInterface(ServiceManager.getService("network_management")).isBandwidthControlEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public static SpannableString createAccessibleSequence(CharSequence displayText, String accessibileText) {
        SpannableString str = new SpannableString(displayText);
        str.setSpan(new TextBuilder(accessibileText).build(), 0, displayText.length(), 18);
        return str;
    }

    public static int getUserIdFromBundle(Context context, Bundle bundle) {
        return getUserIdFromBundle(context, bundle, false);
    }

    public static int getUserIdFromBundle(Context context, Bundle bundle, boolean isInternal) {
        if (bundle == null) {
            return getCredentialOwnerUserId(context);
        }
        boolean allowAnyUser = false;
        if (isInternal && bundle.getBoolean(ChooseLockSettingsHelper.EXTRA_ALLOW_ANY_USER, false)) {
            allowAnyUser = true;
        }
        int userId = bundle.getInt("android.intent.extra.USER_ID", UserHandle.myUserId());
        if (userId == -9999) {
            return allowAnyUser ? userId : enforceSystemUser(context, userId);
        }
        return allowAnyUser ? userId : enforceSameOwner(context, userId);
    }

    public static int enforceSystemUser(Context context, int userId) {
        if (UserHandle.myUserId() == 0) {
            return userId;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Given user id ");
        stringBuilder.append(userId);
        stringBuilder.append(" must only be used from USER_SYSTEM, but current user is ");
        stringBuilder.append(UserHandle.myUserId());
        throw new SecurityException(stringBuilder.toString());
    }

    public static int enforceSameOwner(Context context, int userId) {
        if (ArrayUtils.contains(getUserManager(context).getProfileIdsWithDisabled(UserHandle.myUserId()), userId)) {
            return userId;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Given user id ");
        stringBuilder.append(userId);
        stringBuilder.append(" does not belong to user ");
        stringBuilder.append(UserHandle.myUserId());
        throw new SecurityException(stringBuilder.toString());
    }

    public static int getCredentialOwnerUserId(Context context) {
        return getCredentialOwnerUserId(context, UserHandle.myUserId());
    }

    public static int getCredentialOwnerUserId(Context context, int userId) {
        return getUserManager(context).getCredentialOwnerProfile(userId);
    }

    public static String formatDateRange(Context context, long start, long end) {
        String formatter;
        synchronized (sBuilder) {
            sBuilder.setLength(0);
            formatter = DateUtils.formatDateRange(context, sFormatter, start, end, 65552, null).toString();
        }
        return formatter;
    }

    public static boolean isDeviceProvisioned(Context context) {
        return Global.getInt(context.getContentResolver(), WizardManagerHelper.SETTINGS_GLOBAL_DEVICE_PROVISIONED, 0) != 0;
    }

    public static boolean startQuietModeDialogIfNecessary(Context context, UserManager um, int userId) {
        if (!um.isQuietModeEnabled(UserHandle.of(userId))) {
            return false;
        }
        context.startActivity(UnlaunchableAppActivity.createInQuietModeDialogIntent(userId));
        return true;
    }

    public static boolean unlockWorkProfileIfNecessary(Context context, int userId) {
        try {
            if (ActivityManager.getService().isUserRunning(userId, 2) && new LockPatternUtils(context).isSecure(userId)) {
                return confirmWorkProfileCredentials(context, userId);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    private static boolean confirmWorkProfileCredentials(Context context, int userId) {
        Intent unlockIntent = ((KeyguardManager) context.getSystemService("keyguard")).createConfirmDeviceCredentialIntent(null, null, userId);
        if (unlockIntent == null) {
            return false;
        }
        context.startActivity(unlockIntent);
        return true;
    }

    public static CharSequence getApplicationLabel(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 4194816).loadLabel(context.getPackageManager());
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unable to find info for package: ");
            stringBuilder.append(packageName);
            Log.w(str, stringBuilder.toString());
            return null;
        }
    }

    public static boolean isPackageDirectBootAware(Context context, String packageName) {
        boolean z = false;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(packageName, 0);
            if (ai.isDirectBootAware() || ai.isPartiallyDirectBootAware()) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static Context createPackageContextAsUser(Context context, int userId) {
        try {
            return context.createPackageContextAsUser(context.getPackageName(), 0, UserHandle.of(userId));
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Failed to create user context", e);
            return null;
        }
    }

    public static FingerprintManager getFingerprintManagerOrNull(Context context) {
        if (context.getPackageManager().hasSystemFeature("android.hardware.fingerprint")) {
            return (FingerprintManager) context.getSystemService("fingerprint");
        }
        return null;
    }

    public static boolean hasFingerprintHardware(Context context) {
        FingerprintManager fingerprintManager = getFingerprintManagerOrNull(context);
        return fingerprintManager != null && fingerprintManager.isHardwareDetected();
    }

    public static void launchIntent(Fragment fragment, Intent intent) {
        try {
            int userId = intent.getIntExtra("android.intent.extra.USER_ID", -1);
            if (userId == -1) {
                fragment.startActivity(intent);
            } else {
                fragment.getActivity().startActivityAsUser(intent, new UserHandle(userId));
            }
        } catch (ActivityNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("No activity found for ");
            stringBuilder.append(intent);
            Log.w(str, stringBuilder.toString());
        }
    }

    public static boolean isDemoUser(Context context) {
        return UserManager.isDeviceInDemoMode(context) && getUserManager(context).isDemoUser();
    }

    public static ComponentName getDeviceOwnerComponent(Context context) {
        return ((DevicePolicyManager) context.getSystemService("device_policy")).getDeviceOwnerComponentOnAnyUser();
    }

    public static boolean isProfileOf(UserInfo user, UserInfo profile) {
        return user.id == profile.id || (user.profileGroupId != -10000 && user.profileGroupId == profile.profileGroupId);
    }

    public static VolumeInfo maybeInitializeVolume(StorageManager sm, Bundle bundle) {
        VolumeInfo volume = sm.findVolumeById(bundle.getString("android.os.storage.extra.VOLUME_ID", "private"));
        return isVolumeValid(volume) ? volume : null;
    }

    public static boolean isProfileOrDeviceOwner(UserManager userManager, DevicePolicyManager devicePolicyManager, String packageName) {
        List<UserInfo> userInfos = userManager.getUsers();
        if (devicePolicyManager.isDeviceOwnerAppOnAnyUser(packageName)) {
            return true;
        }
        int size = userInfos.size();
        for (int i = 0; i < size; i++) {
            ComponentName cn = devicePolicyManager.getProfileOwnerAsUser(((UserInfo) userInfos.get(i)).id);
            if (cn != null && cn.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    @StringRes
    public static int getInstallationStatus(ApplicationInfo info) {
        if ((info.flags & 8388608) == 0) {
            return R.string.not_installed;
        }
        return info.enabled ? R.string.installed : R.string.disabled;
    }

    private static boolean isVolumeValid(VolumeInfo volume) {
        if (volume != null && volume.getType() == 1 && volume.isMountedReadable()) {
            return true;
        }
        return false;
    }

    public static void setEditTextCursorPosition(EditText editText) {
        editText.setSelection(editText.getText().length());
    }

    public static void setSafeIcon(Preference pref, Drawable icon) {
        Drawable safeIcon = icon;
        if (!(icon == null || (icon instanceof VectorDrawable))) {
            safeIcon = getSafeDrawable(icon, 500, 500);
        }
        pref.setIcon(safeIcon);
    }

    public static Drawable getSafeDrawable(Drawable original, int maxWidth, int maxHeight) {
        int actualWidth = original.getMinimumWidth();
        int actualHeight = original.getMinimumHeight();
        if (actualWidth <= maxWidth && actualHeight <= maxHeight) {
            return original;
        }
        Bitmap bitmap;
        float scale = Math.min(((float) maxWidth) / ((float) actualWidth), ((float) maxHeight) / ((float) actualHeight));
        int width = (int) (((float) actualWidth) * scale);
        int height = (int) (((float) actualHeight) * scale);
        if (original instanceof BitmapDrawable) {
            bitmap = Bitmap.createScaledBitmap(((BitmapDrawable) original).getBitmap(), width, height, false);
        } else {
            bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            original.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            original.draw(canvas);
        }
        return new BitmapDrawable(null, bitmap);
    }

    public static Drawable getBadgedIcon(IconDrawableFactory iconDrawableFactory, PackageManager packageManager, String packageName, int userId) {
        try {
            return iconDrawableFactory.getBadgedIcon(packageManager.getApplicationInfoAsUser(packageName, 128, userId), userId);
        } catch (NameNotFoundException e) {
            return packageManager.getDefaultActivityIcon();
        }
    }

    public static String getLocalizedName(Context context, String resName) {
        if (context == null) {
            return null;
        }
        String localizedName = null;
        if (!(resName == null || resName.isEmpty())) {
            int resId = context.getResources().getIdentifier(resName, "string", context.getPackageName());
            if (resId > 0) {
                try {
                    localizedName = context.getResources().getString(resId);
                    Log.d(TAG, "Replaced apn name with localized name");
                } catch (NotFoundException e) {
                    Log.e(TAG, "Got execption while getting the localized apn name.", e);
                }
            }
        }
        return localizedName;
    }

    public static boolean carrierTableFieldValidate(String field) {
        if (field == null) {
            return false;
        }
        if (READ_ONLY.equalsIgnoreCase(field) || PERSISTENT.equalsIgnoreCase(field) || "authtype".equalsIgnoreCase(field) || "sub_id".equalsIgnoreCase(field)) {
            return true;
        }
        field = field.toUpperCase();
        try {
            Carriers.class.getDeclaredField(field);
            return true;
        } catch (NoSuchFieldException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(field);
            stringBuilder.append("is not a valid field in class Telephony.Carriers");
            Log.w(str, stringBuilder.toString());
            return false;
        }
    }

    public static boolean isNetworkSettingsApkAvailable() {
        IExtTelephony extTelephony = IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone"));
        if (extTelephony != null) {
            try {
                if (extTelephony.isVendorApkAvailable("com.qualcomm.qti.networksetting")) {
                    return true;
                }
            } catch (RemoteException e) {
            }
        }
        return false;
    }

    public static void startWithFragment(Context context, String fragmentName, Bundle args, Fragment resultTo, int resultRequestCode, int titleResId, CharSequence title) {
        startWithFragment(context, fragmentName, args, resultTo, resultRequestCode, null, titleResId, title, false);
    }

    public static void startWithFragment(Context context, String fragmentName, Bundle args, Fragment resultTo, int resultRequestCode, String titleResPackageName, int titleResId, CharSequence title) {
        startWithFragment(context, fragmentName, args, resultTo, resultRequestCode, titleResPackageName, titleResId, title, false);
    }

    public static void startWithFragment(Context context, String fragmentName, Bundle args, Fragment resultTo, int resultRequestCode, int titleResId, CharSequence title, boolean isShortcut) {
        Intent intent = onBuildStartFragmentIntent(context, fragmentName, args, null, titleResId, title, isShortcut);
        if (resultTo == null) {
            context.startActivity(intent);
        } else {
            resultTo.startActivityForResult(intent, resultRequestCode);
        }
    }

    public static void startWithFragment(Context context, String fragmentName, Bundle args, Fragment resultTo, int resultRequestCode, String titleResPackageName, int titleResId, CharSequence title, boolean isShortcut) {
        Intent intent = onBuildStartFragmentIntent(context, fragmentName, args, titleResPackageName, titleResId, title, isShortcut);
        if (resultTo == null) {
            context.startActivity(intent);
        } else {
            resultTo.startActivityForResult(intent, resultRequestCode);
        }
    }

    public static Intent onBuildStartFragmentIntent(Context context, String fragmentName, Bundle args, String titleResPackageName, int titleResId, CharSequence title, boolean isShortcut) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClass(context, SubSettings.class);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, fragmentName);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RES_PACKAGE_NAME, titleResPackageName);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RESID, titleResId);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE, title);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_AS_SHORTCUT, isShortcut);
        return intent;
    }

    public static Intent onBuildStartFragmentIntent(Context context, String fragmentName, Bundle args, String titleResPackageName, int titleResId, CharSequence title, boolean isShortcut, int sourceMetricsCategory) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClass(context, SubSettings.class);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, fragmentName);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RES_PACKAGE_NAME, titleResPackageName);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RESID, titleResId);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE, title);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_AS_SHORTCUT, isShortcut);
        return intent;
    }

    public static boolean isManagedProfile(UserManager userManager) {
        return isManagedProfile(userManager, UserHandle.myUserId());
    }

    public static boolean isManagedProfile(UserManager userManager, int userId) {
        if (userManager != null) {
            UserInfo userInfo = userManager.getUserInfo(userId);
            boolean z = false;
            if (userId == 999) {
                return false;
            }
            if (userInfo != null) {
                z = userInfo.isManagedProfile();
            }
            return z;
        }
        throw new IllegalArgumentException("userManager must not be null");
    }

    public static boolean isMultiAppEnable(List<UserInfo> profiles) {
        for (UserInfo profile : profiles) {
            if (profile.id == 999) {
                return true;
            }
        }
        return false;
    }
}
