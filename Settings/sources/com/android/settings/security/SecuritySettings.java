package com.android.settings.security;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.PreferenceGroup;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.enterprise.EnterprisePrivacyPreferenceController;
import com.android.settings.enterprise.ManageDeviceAdminPreferenceController;
import com.android.settings.fingerprint.FingerprintProfileStatusPreferenceController;
import com.android.settings.fingerprint.FingerprintStatusPreferenceController;
import com.android.settings.location.LocationPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.security.screenlock.LockScreenPreferenceController;
import com.android.settings.security.trustagent.ManageTrustAgentsPreferenceController;
import com.android.settings.security.trustagent.TrustAgentListPreferenceController;
import com.android.settings.system.OPCollectDiagnosticsPreferenceController;
import com.android.settings.widget.PreferenceCategoryController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.oneplus.settings.OPSecurityDetectionSwitchPreferenceController;
import com.oneplus.settings.controllers.OPFaceUnlockPreferenceController;
import com.oneplus.settings.others.OPEmergencyRescueSettingsPreferenceController;
import com.oneplus.settings.utils.OPPreferenceDividerLine;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class SecuritySettings extends DashboardFragment {
    public static final int CHANGE_TRUST_AGENT_SETTINGS = 126;
    private static final String PRIVACY_CATAGORY_FACE_UNLOCK_CATEGORY = "privacy_catagory_face_unlock";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> index = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.security_dashboard_settings;
            index.add(sir);
            return index;
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return SecuritySettings.buildPreferenceControllers(context, null, null);
        }
    };
    private static final String SECURITY_CATEGORY = "security_category";
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private static final String TAG = "SecuritySettings";
    public static final int UNIFY_LOCK_CONFIRM_DEVICE_REQUEST = 128;
    public static final int UNIFY_LOCK_CONFIRM_PROFILE_REQUEST = 129;
    public static final int UNUNIFY_LOCK_CONFIRM_DEVICE_REQUEST = 130;
    private static final String WORK_PROFILE_SECURITY_CATEGORY = "security_category_profile";

    static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                FingerprintManager fpm = Utils.getFingerprintManagerOrNull(this.mContext);
                if (fpm == null || !fpm.isHardwareDetected()) {
                    this.mSummaryLoader.setSummary(this, this.mContext.getString(R.string.security_dashboard_summary_no_fingerprint));
                } else if (OPUtils.isGuestMode() || !OPUtils.isSurportFaceUnlock(this.mContext)) {
                    this.mSummaryLoader.setSummary(this, this.mContext.getString(R.string.oneplus_security_dashboard_summary_no_faceunlock));
                } else {
                    this.mSummaryLoader.setSummary(this, this.mContext.getString(R.string.oneplus_security_dashboard_summary));
                }
            }
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        PreferenceGroup mSecurityStatusnPreferenceGroup = (PreferenceGroup) findPreference("security_status");
        if (mSecurityStatusnPreferenceGroup != null && !OPUtils.isO2()) {
            mSecurityStatusnPreferenceGroup.setVisible(false);
        }
    }

    public int getMetricsCategory() {
        return 87;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.security_dashboard_settings;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public int getHelpResource() {
        return R.string.help_url_security;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle(), this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!((TrustAgentListPreferenceController) use(TrustAgentListPreferenceController.class)).handleActivityResult(requestCode, resultCode) && !((LockUnificationPreferenceController) use(LockUnificationPreferenceController.class)).handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void launchConfirmDeviceLockForUnification() {
        ((LockUnificationPreferenceController) use(LockUnificationPreferenceController.class)).launchConfirmDeviceLockForUnification();
    }

    /* Access modifiers changed, original: 0000 */
    public void unifyUncompliantLocks() {
        ((LockUnificationPreferenceController) use(LockUnificationPreferenceController.class)).unifyUncompliantLocks();
    }

    /* Access modifiers changed, original: 0000 */
    public void updateUnificationPreference() {
        ((LockUnificationPreferenceController) use(LockUnificationPreferenceController.class)).updateState(null);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Lifecycle lifecycle, SecuritySettings host) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new OPPreferenceDividerLine(context));
        controllers.add(new LocationPreferenceController(context, lifecycle));
        controllers.add(new ManageDeviceAdminPreferenceController(context));
        controllers.add(new EnterprisePrivacyPreferenceController(context));
        controllers.add(new ManageTrustAgentsPreferenceController(context));
        controllers.add(new ScreenPinningPreferenceController(context));
        controllers.add(new OPCollectDiagnosticsPreferenceController(context));
        controllers.add(new OPEmergencyRescueSettingsPreferenceController(context));
        controllers.add(new OPSecurityDetectionSwitchPreferenceController(context, lifecycle));
        controllers.add(new SimLockPreferenceController(context));
        controllers.add(new ShowPasswordPreferenceController(context));
        controllers.add(new EncryptionStatusPreferenceController(context, "encryption_and_credential"));
        controllers.add(new TrustAgentListPreferenceController(context, host, lifecycle));
        List<AbstractPreferenceController> securityPreferenceControllers = new ArrayList();
        securityPreferenceControllers.add(new FingerprintStatusPreferenceController(context));
        securityPreferenceControllers.add(new OPFaceUnlockPreferenceController(context));
        securityPreferenceControllers.add(new LockScreenPreferenceController(context, lifecycle));
        securityPreferenceControllers.add(new ChangeScreenLockPreferenceController(context, host));
        controllers.add(new PreferenceCategoryController(context, SECURITY_CATEGORY).setChildren(securityPreferenceControllers));
        controllers.addAll(securityPreferenceControllers);
        List<AbstractPreferenceController> profileSecurityControllers = new ArrayList();
        profileSecurityControllers.add(new ChangeProfileScreenLockPreferenceController(context, host));
        profileSecurityControllers.add(new LockUnificationPreferenceController(context, host));
        profileSecurityControllers.add(new VisiblePatternProfilePreferenceController(context, lifecycle));
        profileSecurityControllers.add(new FingerprintProfileStatusPreferenceController(context));
        controllers.add(new PreferenceCategoryController(context, WORK_PROFILE_SECURITY_CATEGORY).setChildren(profileSecurityControllers));
        controllers.addAll(profileSecurityControllers);
        return controllers;
    }
}
