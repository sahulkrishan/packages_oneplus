package com.android.settings.enterprise;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.ApplicationFeatureProvider;
import com.android.settings.applications.EnterpriseDefaultApps;
import com.android.settings.applications.UserAppInfo;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.users.UserFeatureProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.utils.ThreadUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public class EnterpriseSetDefaultAppsListPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private final ApplicationFeatureProvider mApplicationFeatureProvider;
    private List<EnumMap<EnterpriseDefaultApps, List<ApplicationInfo>>> mApps = Collections.emptyList();
    private final EnterprisePrivacyFeatureProvider mEnterprisePrivacyFeatureProvider;
    private final SettingsPreferenceFragment mParent;
    private final PackageManager mPm;
    private final UserFeatureProvider mUserFeatureProvider;
    private List<UserInfo> mUsers = Collections.emptyList();

    public EnterpriseSetDefaultAppsListPreferenceController(Context context, SettingsPreferenceFragment parent, PackageManager packageManager) {
        super(context);
        this.mPm = packageManager;
        this.mParent = parent;
        FeatureFactory factory = FeatureFactory.getFactory(context);
        this.mApplicationFeatureProvider = factory.getApplicationFeatureProvider(context);
        this.mEnterprisePrivacyFeatureProvider = factory.getEnterprisePrivacyFeatureProvider(context);
        this.mUserFeatureProvider = factory.getUserFeatureProvider(context);
        buildAppList();
    }

    private void buildAppList() {
        this.mUsers = new ArrayList();
        this.mApps = new ArrayList();
        for (UserHandle user : this.mUserFeatureProvider.getUserProfiles()) {
            EnumMap<EnterpriseDefaultApps, List<ApplicationInfo>> userMap = null;
            boolean hasDefaultsForUser = false;
            for (EnterpriseDefaultApps typeOfDefault : EnterpriseDefaultApps.values()) {
                List<UserAppInfo> apps = this.mApplicationFeatureProvider.findPersistentPreferredActivities(user.getIdentifier(), typeOfDefault.getIntents());
                if (!apps.isEmpty()) {
                    if (!hasDefaultsForUser) {
                        hasDefaultsForUser = true;
                        this.mUsers.add(((UserAppInfo) apps.get(0)).userInfo);
                        userMap = new EnumMap(EnterpriseDefaultApps.class);
                        this.mApps.add(userMap);
                    }
                    ArrayList<ApplicationInfo> applicationInfos = new ArrayList();
                    for (UserAppInfo userAppInfo : apps) {
                        applicationInfos.add(userAppInfo.appInfo);
                    }
                    userMap.put(typeOfDefault, applicationInfos);
                }
            }
        }
        ThreadUtils.postOnMainThread(new -$$Lambda$EnterpriseSetDefaultAppsListPreferenceController$iIsgYxioer_lSG0lJzt4UtTCm2Y(this));
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return null;
    }

    private void updateUi() {
        Context prefContext = this.mParent.getPreferenceManager().getContext();
        PreferenceScreen screen = this.mParent.getPreferenceScreen();
        if (screen != null) {
            int i = 0;
            if (this.mEnterprisePrivacyFeatureProvider.isInCompMode() || this.mUsers.size() != 1) {
                while (true) {
                    int i2 = i;
                    if (i2 >= this.mUsers.size()) {
                        break;
                    }
                    UserInfo userInfo = (UserInfo) this.mUsers.get(i2);
                    PreferenceCategory category = new PreferenceCategory(prefContext);
                    screen.addPreference(category);
                    if (userInfo.isManagedProfile()) {
                        category.setTitle((int) R.string.managed_device_admin_title);
                    } else {
                        category.setTitle((int) R.string.personal_device_admin_title);
                    }
                    category.setOrder(i2);
                    createPreferences(prefContext, category, (EnumMap) this.mApps.get(i2));
                    i = i2 + 1;
                }
            } else {
                createPreferences(prefContext, screen, (EnumMap) this.mApps.get(0));
            }
        }
    }

    private void createPreferences(Context prefContext, PreferenceGroup group, EnumMap<EnterpriseDefaultApps, List<ApplicationInfo>> apps) {
        if (group != null) {
            for (EnterpriseDefaultApps typeOfDefault : EnterpriseDefaultApps.values()) {
                List<ApplicationInfo> appsForCategory = (List) apps.get(typeOfDefault);
                if (!(appsForCategory == null || appsForCategory.isEmpty())) {
                    Preference preference = new Preference(prefContext);
                    preference.setTitle(getTitle(prefContext, typeOfDefault, appsForCategory.size()));
                    preference.setSummary(buildSummaryString(prefContext, appsForCategory));
                    preference.setOrder(typeOfDefault.ordinal());
                    preference.setSelectable(false);
                    group.addPreference(preference);
                }
            }
        }
    }

    private CharSequence buildSummaryString(Context context, List<ApplicationInfo> apps) {
        CharSequence[] appNames = new String[apps.size()];
        for (int i = 0; i < apps.size(); i++) {
            appNames[i] = ((ApplicationInfo) apps.get(i)).loadLabel(this.mPm);
        }
        if (apps.size() == 1) {
            return appNames[0];
        }
        if (apps.size() == 2) {
            return context.getString(R.string.app_names_concatenation_template_2, new Object[]{appNames[0], appNames[1]});
        }
        return context.getString(R.string.app_names_concatenation_template_3, new Object[]{appNames[0], appNames[1], appNames[2]});
    }

    private String getTitle(Context context, EnterpriseDefaultApps typeOfDefault, int appCount) {
        switch (typeOfDefault) {
            case BROWSER:
                return context.getString(R.string.default_browser_title);
            case CALENDAR:
                return context.getString(R.string.default_calendar_app_title);
            case CONTACTS:
                return context.getString(R.string.default_contacts_app_title);
            case PHONE:
                return context.getResources().getQuantityString(R.plurals.default_phone_app_title, appCount);
            case MAP:
                return context.getString(R.string.default_map_app_title);
            case EMAIL:
                return context.getResources().getQuantityString(R.plurals.default_email_app_title, appCount);
            case CAMERA:
                return context.getResources().getQuantityString(R.plurals.default_camera_app_title, appCount);
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unknown type of default ");
                stringBuilder.append(typeOfDefault);
                throw new IllegalStateException(stringBuilder.toString());
        }
    }
}
