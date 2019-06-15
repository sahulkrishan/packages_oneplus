package com.android.settingslib;

import android.app.AppGlobals;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManager.EnforcingUser;
import android.support.annotation.VisibleForTesting;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import java.util.List;
import java.util.Objects;

public class RestrictedLockUtils {
    @VisibleForTesting
    static Proxy sProxy = new Proxy();

    public static class EnforcedAdmin {
        public static final EnforcedAdmin MULTIPLE_ENFORCED_ADMIN = new EnforcedAdmin();
        public ComponentName component = null;
        public String enforcedRestriction = null;
        public int userId = -10000;

        public static EnforcedAdmin createDefaultEnforcedAdminWithRestriction(String enforcedRestriction) {
            EnforcedAdmin enforcedAdmin = new EnforcedAdmin();
            enforcedAdmin.enforcedRestriction = enforcedRestriction;
            return enforcedAdmin;
        }

        public EnforcedAdmin(ComponentName component, int userId) {
            this.component = component;
            this.userId = userId;
        }

        public EnforcedAdmin(ComponentName component, String enforcedRestriction, int userId) {
            this.component = component;
            this.enforcedRestriction = enforcedRestriction;
            this.userId = userId;
        }

        public EnforcedAdmin(EnforcedAdmin other) {
            if (other != null) {
                this.component = other.component;
                this.enforcedRestriction = other.enforcedRestriction;
                this.userId = other.userId;
                return;
            }
            throw new IllegalArgumentException();
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            EnforcedAdmin that = (EnforcedAdmin) o;
            if (!(this.userId == that.userId && Objects.equals(this.component, that.component) && Objects.equals(this.enforcedRestriction, that.enforcedRestriction))) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.component, this.enforcedRestriction, Integer.valueOf(this.userId)});
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("EnforcedAdmin{component=");
            stringBuilder.append(this.component);
            stringBuilder.append(", enforcedRestriction='");
            stringBuilder.append(this.enforcedRestriction);
            stringBuilder.append(", userId=");
            stringBuilder.append(this.userId);
            stringBuilder.append('}');
            return stringBuilder.toString();
        }
    }

    private interface LockSettingCheck {
        boolean isEnforcing(DevicePolicyManager devicePolicyManager, ComponentName componentName, int i);
    }

    @VisibleForTesting
    static class Proxy {
        Proxy() {
        }

        public boolean isSeparateProfileChallengeEnabled(LockPatternUtils utils, int userHandle) {
            return utils.isSeparateProfileChallengeEnabled(userHandle);
        }

        public DevicePolicyManager getParentProfileInstance(DevicePolicyManager dpm, UserInfo ui) {
            return dpm.getParentProfileInstance(ui);
        }
    }

    public static Drawable getRestrictedPadlock(Context context) {
        Drawable restrictedPadlock = context.getDrawable(R.drawable.ic_info);
        int iconSize = context.getResources().getDimensionPixelSize(R.dimen.restricted_icon_size);
        restrictedPadlock.setBounds(0, 0, iconSize, iconSize);
        return restrictedPadlock;
    }

    public static EnforcedAdmin checkIfRestrictionEnforced(Context context, String userRestriction, int userId) {
        if (((DevicePolicyManager) context.getSystemService("device_policy")) == null) {
            return null;
        }
        UserManager um = UserManager.get(context);
        List<EnforcingUser> enforcingUsers = um.getUserRestrictionSources(userRestriction, UserHandle.of(userId));
        if (enforcingUsers.isEmpty()) {
            return null;
        }
        if (enforcingUsers.size() > 1) {
            return EnforcedAdmin.createDefaultEnforcedAdminWithRestriction(userRestriction);
        }
        int restrictionSource = ((EnforcingUser) enforcingUsers.get(0)).getUserRestrictionSource();
        int adminUserId = ((EnforcingUser) enforcingUsers.get(0)).getUserHandle().getIdentifier();
        if (restrictionSource == 4) {
            if (adminUserId == userId) {
                return getProfileOwner(context, userRestriction, adminUserId);
            }
            EnforcedAdmin createDefaultEnforcedAdminWithRestriction;
            UserInfo parentUser = um.getProfileParent(adminUserId);
            if (parentUser == null || parentUser.id != userId) {
                createDefaultEnforcedAdminWithRestriction = EnforcedAdmin.createDefaultEnforcedAdminWithRestriction(userRestriction);
            } else {
                createDefaultEnforcedAdminWithRestriction = getProfileOwner(context, userRestriction, adminUserId);
            }
            return createDefaultEnforcedAdminWithRestriction;
        } else if (restrictionSource != 2) {
            return null;
        } else {
            EnforcedAdmin deviceOwner;
            if (adminUserId == userId) {
                deviceOwner = getDeviceOwner(context, userRestriction);
            } else {
                deviceOwner = EnforcedAdmin.createDefaultEnforcedAdminWithRestriction(userRestriction);
            }
            return deviceOwner;
        }
    }

    public static boolean hasBaseUserRestriction(Context context, String userRestriction, int userId) {
        return ((UserManager) context.getSystemService("user")).hasBaseUserRestriction(userRestriction, UserHandle.of(userId));
    }

    public static EnforcedAdmin checkIfKeyguardFeaturesDisabled(Context context, int keyguardFeatures, int userId) {
        LockSettingCheck check = new -$$Lambda$RestrictedLockUtils$r6A4RGcQdg8eQ1PGvcQ0UANBzNk(userId, keyguardFeatures);
        if (!UserManager.get(context).getUserInfo(userId).isManagedProfile()) {
            return checkForLockSetting(context, userId, check);
        }
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        return findEnforcedAdmin(dpm.getActiveAdminsAsUser(userId), dpm, userId, check);
    }

    static /* synthetic */ boolean lambda$checkIfKeyguardFeaturesDisabled$0(int userId, int keyguardFeatures, DevicePolicyManager dpm, ComponentName admin, int checkUser) {
        int effectiveFeatures = dpm.getKeyguardDisabledFeatures(admin, checkUser);
        if (checkUser != userId) {
            effectiveFeatures &= 432;
        }
        return (effectiveFeatures & keyguardFeatures) != 0;
    }

    private static EnforcedAdmin findEnforcedAdmin(List<ComponentName> admins, DevicePolicyManager dpm, int userId, LockSettingCheck check) {
        if (admins == null) {
            return null;
        }
        EnforcedAdmin enforcedAdmin = null;
        for (ComponentName admin : admins) {
            if (check.isEnforcing(dpm, admin, userId)) {
                if (enforcedAdmin != null) {
                    return EnforcedAdmin.MULTIPLE_ENFORCED_ADMIN;
                }
                enforcedAdmin = new EnforcedAdmin(admin, userId);
            }
        }
        return enforcedAdmin;
    }

    public static EnforcedAdmin checkIfUninstallBlocked(Context context, String packageName, int userId) {
        EnforcedAdmin allAppsControlDisallowedAdmin = checkIfRestrictionEnforced(context, "no_control_apps", userId);
        if (allAppsControlDisallowedAdmin != null) {
            return allAppsControlDisallowedAdmin;
        }
        EnforcedAdmin allAppsUninstallDisallowedAdmin = checkIfRestrictionEnforced(context, "no_uninstall_apps", userId);
        if (allAppsUninstallDisallowedAdmin != null) {
            return allAppsUninstallDisallowedAdmin;
        }
        try {
            if (AppGlobals.getPackageManager().getBlockUninstallForUser(packageName, userId)) {
                return getProfileOrDeviceOwner(context, userId);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public static EnforcedAdmin checkIfApplicationIsSuspended(Context context, String packageName, int userId) {
        try {
            if (AppGlobals.getPackageManager().isPackageSuspendedForUser(packageName, userId)) {
                return getProfileOrDeviceOwner(context, userId);
            }
        } catch (RemoteException | IllegalArgumentException e) {
        }
        return null;
    }

    public static EnforcedAdmin checkIfInputMethodDisallowed(Context context, String packageName, int userId) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        if (dpm == null) {
            return null;
        }
        EnforcedAdmin admin = getProfileOrDeviceOwner(context, userId);
        boolean permitted = true;
        if (admin != null) {
            permitted = dpm.isInputMethodPermittedByAdmin(admin.component, packageName, userId);
        }
        int managedProfileId = getManagedProfileId(context, userId);
        EnforcedAdmin profileAdmin = getProfileOrDeviceOwner(context, managedProfileId);
        boolean permittedByProfileAdmin = true;
        if (profileAdmin != null) {
            permittedByProfileAdmin = dpm.isInputMethodPermittedByAdmin(profileAdmin.component, packageName, managedProfileId);
        }
        if (!permitted && !permittedByProfileAdmin) {
            return EnforcedAdmin.MULTIPLE_ENFORCED_ADMIN;
        }
        if (!permitted) {
            return admin;
        }
        if (permittedByProfileAdmin) {
            return null;
        }
        return profileAdmin;
    }

    public static EnforcedAdmin checkIfRemoteContactSearchDisallowed(Context context, int userId) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        if (dpm == null) {
            return null;
        }
        EnforcedAdmin admin = getProfileOwner(context, userId);
        if (admin == null) {
            return null;
        }
        UserHandle userHandle = UserHandle.of(userId);
        if (dpm.getCrossProfileContactsSearchDisabled(userHandle) && dpm.getCrossProfileCallerIdDisabled(userHandle)) {
            return admin;
        }
        return null;
    }

    public static EnforcedAdmin checkIfAccessibilityServiceDisallowed(Context context, String packageName, int userId) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        if (dpm == null) {
            return null;
        }
        EnforcedAdmin admin = getProfileOrDeviceOwner(context, userId);
        boolean permitted = true;
        if (admin != null) {
            permitted = dpm.isAccessibilityServicePermittedByAdmin(admin.component, packageName, userId);
        }
        int managedProfileId = getManagedProfileId(context, userId);
        EnforcedAdmin profileAdmin = getProfileOrDeviceOwner(context, managedProfileId);
        boolean permittedByProfileAdmin = true;
        if (profileAdmin != null) {
            permittedByProfileAdmin = dpm.isAccessibilityServicePermittedByAdmin(profileAdmin.component, packageName, managedProfileId);
        }
        if (!permitted && !permittedByProfileAdmin) {
            return EnforcedAdmin.MULTIPLE_ENFORCED_ADMIN;
        }
        if (!permitted) {
            return admin;
        }
        if (permittedByProfileAdmin) {
            return null;
        }
        return profileAdmin;
    }

    private static int getManagedProfileId(Context context, int userId) {
        for (UserInfo uInfo : ((UserManager) context.getSystemService("user")).getProfiles(userId)) {
            if (uInfo.id != userId) {
                if (uInfo.isManagedProfile()) {
                    return uInfo.id;
                }
            }
        }
        return -10000;
    }

    public static EnforcedAdmin checkIfAccountManagementDisabled(Context context, String accountType, int userId) {
        if (accountType == null) {
            return null;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        if (!context.getPackageManager().hasSystemFeature("android.software.device_admin") || dpm == null) {
            return null;
        }
        boolean isAccountTypeDisabled = false;
        for (String type : dpm.getAccountTypesWithManagementDisabledAsUser(userId)) {
            if (accountType.equals(type)) {
                isAccountTypeDisabled = true;
                break;
            }
        }
        if (isAccountTypeDisabled) {
            return getProfileOrDeviceOwner(context, userId);
        }
        return null;
    }

    public static EnforcedAdmin checkIfMeteredDataRestricted(Context context, String packageName, int userId) {
        EnforcedAdmin enforcedAdmin = getProfileOrDeviceOwner(context, userId);
        EnforcedAdmin enforcedAdmin2 = null;
        if (enforcedAdmin == null) {
            return null;
        }
        if (((DevicePolicyManager) context.getSystemService("device_policy")).isMeteredDataDisabledPackageForUser(enforcedAdmin.component, packageName, userId)) {
            enforcedAdmin2 = enforcedAdmin;
        }
        return enforcedAdmin2;
    }

    public static EnforcedAdmin checkIfAutoTimeRequired(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        if (dpm == null || !dpm.getAutoTimeRequired()) {
            return null;
        }
        return new EnforcedAdmin(dpm.getDeviceOwnerComponentOnCallingUser(), UserHandle.myUserId());
    }

    public static EnforcedAdmin checkIfPasswordQualityIsSet(Context context, int userId) {
        LockSettingCheck check = -$$Lambda$RestrictedLockUtils$ZGpdJ-Goya42TrXyPazgpDXw5os.INSTANCE;
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        if (dpm == null) {
            return null;
        }
        if (!sProxy.isSeparateProfileChallengeEnabled(new LockPatternUtils(context), userId)) {
            return checkForLockSetting(context, userId, check);
        }
        List<ComponentName> admins = dpm.getActiveAdminsAsUser(userId);
        if (admins == null) {
            return null;
        }
        EnforcedAdmin enforcedAdmin = null;
        for (ComponentName admin : admins) {
            if (check.isEnforcing(dpm, admin, userId)) {
                if (enforcedAdmin != null) {
                    return EnforcedAdmin.MULTIPLE_ENFORCED_ADMIN;
                }
                enforcedAdmin = new EnforcedAdmin(admin, userId);
            }
        }
        return enforcedAdmin;
    }

    static /* synthetic */ boolean lambda$checkIfPasswordQualityIsSet$1(DevicePolicyManager dpm, ComponentName admin, int checkUser) {
        return dpm.getPasswordQuality(admin, checkUser) > 0;
    }

    public static EnforcedAdmin checkIfMaximumTimeToLockIsSet(Context context) {
        return checkForLockSetting(context, UserHandle.myUserId(), -$$Lambda$RestrictedLockUtils$sbYwAwFLTMW969YNG1W7ojc-r04.INSTANCE);
    }

    static /* synthetic */ boolean lambda$checkIfMaximumTimeToLockIsSet$2(DevicePolicyManager dpm, ComponentName admin, int userId) {
        return dpm.getMaximumTimeToLock(admin, userId) > 0;
    }

    private static EnforcedAdmin checkForLockSetting(Context context, int userId, LockSettingCheck check) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        if (dpm == null) {
            return null;
        }
        LockPatternUtils lockPatternUtils = new LockPatternUtils(context);
        EnforcedAdmin enforcedAdmin = null;
        for (UserInfo userInfo : UserManager.get(context).getProfiles(userId)) {
            List<ComponentName> admins = dpm.getActiveAdminsAsUser(userInfo.id);
            if (admins != null) {
                boolean isSeparateProfileChallengeEnabled = sProxy.isSeparateProfileChallengeEnabled(lockPatternUtils, userInfo.id);
                for (ComponentName admin : admins) {
                    if (isSeparateProfileChallengeEnabled || !check.isEnforcing(dpm, admin, userInfo.id)) {
                        if (userInfo.isManagedProfile() && check.isEnforcing(sProxy.getParentProfileInstance(dpm, userInfo), admin, userInfo.id)) {
                            if (enforcedAdmin != null) {
                                return EnforcedAdmin.MULTIPLE_ENFORCED_ADMIN;
                            }
                            enforcedAdmin = new EnforcedAdmin(admin, userInfo.id);
                        }
                    } else if (enforcedAdmin != null) {
                        return EnforcedAdmin.MULTIPLE_ENFORCED_ADMIN;
                    } else {
                        enforcedAdmin = new EnforcedAdmin(admin, userInfo.id);
                    }
                }
            }
        }
        return enforcedAdmin;
    }

    public static EnforcedAdmin getProfileOrDeviceOwner(Context context, int userId) {
        return getProfileOrDeviceOwner(context, null, userId);
    }

    public static EnforcedAdmin getProfileOrDeviceOwner(Context context, String enforcedRestriction, int userId) {
        if (userId == -10000) {
            return null;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        if (dpm == null) {
            return null;
        }
        ComponentName adminComponent = dpm.getProfileOwnerAsUser(userId);
        if (adminComponent != null) {
            return new EnforcedAdmin(adminComponent, enforcedRestriction, userId);
        }
        if (dpm.getDeviceOwnerUserId() == userId) {
            adminComponent = dpm.getDeviceOwnerComponentOnAnyUser();
            if (adminComponent != null) {
                return new EnforcedAdmin(adminComponent, enforcedRestriction, userId);
            }
        }
        return null;
    }

    public static EnforcedAdmin getDeviceOwner(Context context) {
        return getDeviceOwner(context, null);
    }

    private static EnforcedAdmin getDeviceOwner(Context context, String enforcedRestriction) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        if (dpm == null) {
            return null;
        }
        ComponentName adminComponent = dpm.getDeviceOwnerComponentOnAnyUser();
        if (adminComponent != null) {
            return new EnforcedAdmin(adminComponent, enforcedRestriction, dpm.getDeviceOwnerUserId());
        }
        return null;
    }

    private static EnforcedAdmin getProfileOwner(Context context, int userId) {
        return getProfileOwner(context, null, userId);
    }

    private static EnforcedAdmin getProfileOwner(Context context, String enforcedRestriction, int userId) {
        if (userId == -10000) {
            return null;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        if (dpm == null) {
            return null;
        }
        ComponentName adminComponent = dpm.getProfileOwnerAsUser(userId);
        if (adminComponent != null) {
            return new EnforcedAdmin(adminComponent, enforcedRestriction, userId);
        }
        return null;
    }

    public static void setMenuItemAsDisabledByAdmin(final Context context, MenuItem item, final EnforcedAdmin admin) {
        SpannableStringBuilder sb = new SpannableStringBuilder(item.getTitle());
        removeExistingRestrictedSpans(sb);
        if (admin != null) {
            sb.setSpan(new ForegroundColorSpan(context.getColor(R.color.disabled_text_color)), 0, sb.length(), 33);
            sb.append(" ", new RestrictedLockImageSpan(context), 33);
            item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(context, admin);
                    return true;
                }
            });
        } else {
            item.setOnMenuItemClickListener(null);
        }
        item.setTitle(sb);
    }

    private static void removeExistingRestrictedSpans(SpannableStringBuilder sb) {
        int length = sb.length();
        int i = 0;
        for (ImageSpan span : (RestrictedLockImageSpan[]) sb.getSpans(length - 1, length, RestrictedLockImageSpan.class)) {
            int start = sb.getSpanStart(span);
            int end = sb.getSpanEnd(span);
            sb.removeSpan(span);
            sb.delete(start, end);
        }
        ForegroundColorSpan[] colorSpans = (ForegroundColorSpan[]) sb.getSpans(0, length, ForegroundColorSpan.class);
        int length2 = colorSpans.length;
        while (i < length2) {
            sb.removeSpan(colorSpans[i]);
            i++;
        }
    }

    public static void sendShowAdminSupportDetailsIntent(Context context, EnforcedAdmin admin) {
        Intent intent = getShowAdminSupportDetailsIntent(context, admin);
        int targetUserId = UserHandle.myUserId();
        if (!(admin == null || admin.userId == -10000 || !isCurrentUserOrProfile(context, admin.userId))) {
            targetUserId = admin.userId;
        }
        intent.putExtra("android.app.extra.RESTRICTION", admin.enforcedRestriction);
        context.startActivityAsUser(intent, new UserHandle(targetUserId));
    }

    public static Intent getShowAdminSupportDetailsIntent(Context context, EnforcedAdmin admin) {
        Intent intent = new Intent("android.settings.SHOW_ADMIN_SUPPORT_DETAILS");
        if (admin != null) {
            if (admin.component != null) {
                intent.putExtra("android.app.extra.DEVICE_ADMIN", admin.component);
            }
            int adminUserId = UserHandle.myUserId();
            if (admin.userId != -10000) {
                adminUserId = admin.userId;
            }
            intent.putExtra("android.intent.extra.USER_ID", adminUserId);
        }
        return intent;
    }

    public static boolean isCurrentUserOrProfile(Context context, int userId) {
        for (UserInfo userInfo : UserManager.get(context).getProfiles(UserHandle.myUserId())) {
            if (userInfo.id == userId) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAdminInCurrentUserOrProfile(Context context, ComponentName admin) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        for (UserInfo userInfo : UserManager.get(context).getProfiles(UserHandle.myUserId())) {
            if (dpm.isAdminActiveAsUser(admin, userInfo.id)) {
                return true;
            }
        }
        return false;
    }

    public static void setTextViewPadlock(Context context, TextView textView, boolean showPadlock) {
        SpannableStringBuilder sb = new SpannableStringBuilder(textView.getText());
        removeExistingRestrictedSpans(sb);
        if (showPadlock) {
            sb.append(" ", new RestrictedLockImageSpan(context), 33);
        }
        textView.setText(sb);
    }

    public static void setTextViewAsDisabledByAdmin(Context context, TextView textView, boolean disabled) {
        SpannableStringBuilder sb = new SpannableStringBuilder(textView.getText());
        removeExistingRestrictedSpans(sb);
        if (disabled) {
            sb.setSpan(new ForegroundColorSpan(context.getColor(R.color.disabled_text_color)), 0, sb.length(), 33);
            textView.setCompoundDrawables(null, null, getRestrictedPadlock(context), null);
            textView.setCompoundDrawablePadding(context.getResources().getDimensionPixelSize(R.dimen.restricted_icon_padding));
        } else {
            textView.setCompoundDrawables(null, null, null, null);
        }
        textView.setText(sb);
    }
}
