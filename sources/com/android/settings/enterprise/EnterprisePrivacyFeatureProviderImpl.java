package com.android.settings.enterprise;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.view.View;
import com.android.settings.R;
import com.android.settings.vpn2.VpnUtils;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.util.Date;
import java.util.List;

public class EnterprisePrivacyFeatureProviderImpl implements EnterprisePrivacyFeatureProvider {
    private static final int MY_USER_ID = UserHandle.myUserId();
    private final ConnectivityManager mCm;
    private final Context mContext;
    private final DevicePolicyManager mDpm;
    private final PackageManagerWrapper mPm;
    private final Resources mResources;
    private final UserManager mUm;

    protected static class EnterprisePrivacySpan extends ClickableSpan {
        private final Context mContext;

        public EnterprisePrivacySpan(Context context) {
            this.mContext = context;
        }

        public void onClick(View widget) {
            this.mContext.startActivity(new Intent("android.settings.ENTERPRISE_PRIVACY_SETTINGS").addFlags(268435456));
        }

        public boolean equals(Object object) {
            return (object instanceof EnterprisePrivacySpan) && ((EnterprisePrivacySpan) object).mContext == this.mContext;
        }
    }

    public EnterprisePrivacyFeatureProviderImpl(Context context, DevicePolicyManager dpm, PackageManagerWrapper pm, UserManager um, ConnectivityManager cm, Resources resources) {
        this.mContext = context.getApplicationContext();
        this.mDpm = dpm;
        this.mPm = pm;
        this.mUm = um;
        this.mCm = cm;
        this.mResources = resources;
    }

    public boolean hasDeviceOwner() {
        boolean z = false;
        if (!this.mPm.hasSystemFeature("android.software.device_admin")) {
            return false;
        }
        if (this.mDpm.getDeviceOwnerComponentOnAnyUser() != null) {
            z = true;
        }
        return z;
    }

    private int getManagedProfileUserId() {
        for (UserInfo userInfo : this.mUm.getProfiles(MY_USER_ID)) {
            if (userInfo.isManagedProfile()) {
                return userInfo.id;
            }
        }
        return -10000;
    }

    public boolean isInCompMode() {
        return hasDeviceOwner() && getManagedProfileUserId() != -10000;
    }

    public String getDeviceOwnerOrganizationName() {
        CharSequence organizationName = this.mDpm.getDeviceOwnerOrganizationName();
        if (organizationName == null) {
            return null;
        }
        return organizationName.toString();
    }

    public CharSequence getDeviceOwnerDisclosure() {
        if (!hasDeviceOwner()) {
            return null;
        }
        SpannableStringBuilder disclosure = new SpannableStringBuilder();
        if (this.mDpm.getDeviceOwnerOrganizationName() != null) {
            disclosure.append(this.mResources.getString(R.string.do_disclosure_with_name, new Object[]{organizationName}));
        } else {
            disclosure.append(this.mResources.getString(R.string.do_disclosure_generic));
        }
        disclosure.append(this.mResources.getString(R.string.do_disclosure_learn_more_separator));
        disclosure.append(this.mResources.getString(R.string.learn_more), new EnterprisePrivacySpan(this.mContext), 0);
        return disclosure;
    }

    public Date getLastSecurityLogRetrievalTime() {
        long timestamp = this.mDpm.getLastSecurityLogRetrievalTime();
        return timestamp < 0 ? null : new Date(timestamp);
    }

    public Date getLastBugReportRequestTime() {
        long timestamp = this.mDpm.getLastBugReportRequestTime();
        return timestamp < 0 ? null : new Date(timestamp);
    }

    public Date getLastNetworkLogRetrievalTime() {
        long timestamp = this.mDpm.getLastNetworkLogRetrievalTime();
        return timestamp < 0 ? null : new Date(timestamp);
    }

    public boolean isSecurityLoggingEnabled() {
        return this.mDpm.isSecurityLoggingEnabled(null);
    }

    public boolean isNetworkLoggingEnabled() {
        return this.mDpm.isNetworkLoggingEnabled(null);
    }

    public boolean isAlwaysOnVpnSetInCurrentUser() {
        return VpnUtils.isAlwaysOnVpnSet(this.mCm, MY_USER_ID);
    }

    public boolean isAlwaysOnVpnSetInManagedProfile() {
        int managedProfileUserId = getManagedProfileUserId();
        return managedProfileUserId != -10000 && VpnUtils.isAlwaysOnVpnSet(this.mCm, managedProfileUserId);
    }

    public boolean isGlobalHttpProxySet() {
        return this.mCm.getGlobalProxy() != null;
    }

    public int getMaximumFailedPasswordsBeforeWipeInCurrentUser() {
        ComponentName owner = this.mDpm.getDeviceOwnerComponentOnCallingUser();
        if (owner == null) {
            owner = this.mDpm.getProfileOwnerAsUser(MY_USER_ID);
        }
        if (owner == null) {
            return 0;
        }
        return this.mDpm.getMaximumFailedPasswordsForWipe(owner, MY_USER_ID);
    }

    public int getMaximumFailedPasswordsBeforeWipeInManagedProfile() {
        int userId = getManagedProfileUserId();
        if (userId == -10000) {
            return 0;
        }
        ComponentName profileOwner = this.mDpm.getProfileOwnerAsUser(userId);
        if (profileOwner == null) {
            return 0;
        }
        return this.mDpm.getMaximumFailedPasswordsForWipe(profileOwner, userId);
    }

    public String getImeLabelIfOwnerSet() {
        if (!this.mDpm.isCurrentInputMethodSetByOwner()) {
            return null;
        }
        String packageName = Secure.getStringForUser(this.mContext.getContentResolver(), "default_input_method", MY_USER_ID);
        if (packageName == null) {
            return null;
        }
        try {
            return this.mPm.getApplicationInfoAsUser(packageName, 0, MY_USER_ID).loadLabel(this.mPm.getPackageManager()).toString();
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public int getNumberOfOwnerInstalledCaCertsForCurrentUser() {
        List<String> certs = this.mDpm.getOwnerInstalledCaCerts(new UserHandle(MY_USER_ID));
        if (certs == null) {
            return 0;
        }
        return certs.size();
    }

    public int getNumberOfOwnerInstalledCaCertsForManagedProfile() {
        int userId = getManagedProfileUserId();
        if (userId == -10000) {
            return 0;
        }
        List<String> certs = this.mDpm.getOwnerInstalledCaCerts(new UserHandle(userId));
        if (certs == null) {
            return 0;
        }
        return certs.size();
    }

    public int getNumberOfActiveDeviceAdminsForCurrentUserAndManagedProfile() {
        int activeAdmins = 0;
        for (UserInfo userInfo : this.mUm.getProfiles(MY_USER_ID)) {
            List<ComponentName> activeAdminsForUser = this.mDpm.getActiveAdminsAsUser(userInfo.id);
            if (!(activeAdminsForUser == null || userInfo.id == 999)) {
                activeAdmins += activeAdminsForUser.size();
            }
        }
        return activeAdmins;
    }

    public boolean areBackupsMandatory() {
        return this.mDpm.getMandatoryBackupTransport() != null;
    }
}
