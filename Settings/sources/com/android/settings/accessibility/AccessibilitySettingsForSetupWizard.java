package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class AccessibilitySettingsForSetupWizard extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private static final String DISPLAY_MAGNIFICATION_PREFERENCE = "screen_magnification_preference";
    private static final String FONT_SIZE_PREFERENCE = "font_size_preference";
    private static final String SCREEN_READER_PACKAGE_NAME = "com.google.android.marvin.talkback";
    private static final String SCREEN_READER_PREFERENCE = "screen_reader_preference";
    private static final String SCREEN_READER_SERVICE_NAME = "com.google.android.marvin.talkback.TalkBackService";
    private static final String SELECT_TO_SPEAK_PACKAGE_NAME = "com.google.android.marvin.talkback";
    private static final String SELECT_TO_SPEAK_PREFERENCE = "select_to_speak_preference";
    private static final String SELECT_TO_SPEAK_SERVICE_NAME = "com.google.android.accessibility.selecttospeak.SelectToSpeakService";
    private Preference mDisplayMagnificationPreference;
    private Preference mScreenReaderPreference;
    private Preference mSelectToSpeakPreference;

    public int getMetricsCategory() {
        return 367;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.accessibility_settings_for_setup_wizard);
        this.mDisplayMagnificationPreference = findPreference(DISPLAY_MAGNIFICATION_PREFERENCE);
        this.mScreenReaderPreference = findPreference(SCREEN_READER_PREFERENCE);
        this.mSelectToSpeakPreference = findPreference(SELECT_TO_SPEAK_PREFERENCE);
    }

    public void onResume() {
        super.onResume();
        updateAccessibilityServicePreference(this.mScreenReaderPreference, findService("com.google.android.marvin.talkback", SCREEN_READER_SERVICE_NAME));
        updateAccessibilityServicePreference(this.mSelectToSpeakPreference, findService("com.google.android.marvin.talkback", SELECT_TO_SPEAK_SERVICE_NAME));
        configureMagnificationPreferenceIfNeeded(this.mDisplayMagnificationPreference);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (this.mDisplayMagnificationPreference == preference) {
            this.mDisplayMagnificationPreference.getExtras().putBoolean("from_suw", true);
        }
        return super.onPreferenceTreeClick(preference);
    }

    private AccessibilityServiceInfo findService(String packageName, String serviceName) {
        for (AccessibilityServiceInfo info : ((AccessibilityManager) getActivity().getSystemService(AccessibilityManager.class)).getInstalledAccessibilityServiceList()) {
            ServiceInfo serviceInfo = info.getResolveInfo().serviceInfo;
            if (packageName.equals(serviceInfo.packageName) && serviceName.equals(serviceInfo.name)) {
                return info;
            }
        }
        return null;
    }

    private void updateAccessibilityServicePreference(Preference preference, AccessibilityServiceInfo info) {
        if (info == null) {
            getPreferenceScreen().removePreference(preference);
            return;
        }
        ServiceInfo serviceInfo = info.getResolveInfo().serviceInfo;
        CharSequence title = info.getResolveInfo().loadLabel(getPackageManager()).toString();
        preference.setTitle(title);
        ComponentName componentName = new ComponentName(serviceInfo.packageName, serviceInfo.name);
        preference.setKey(componentName.flattenToString());
        Bundle extras = preference.getExtras();
        extras.putParcelable("component_name", componentName);
        extras.putString("preference_key", preference.getKey());
        extras.putString("title", title);
        String description = info.loadDescription(getPackageManager());
        if (TextUtils.isEmpty(description)) {
            description = getString(R.string.accessibility_service_default_description);
        }
        extras.putString("summary", description);
    }

    private static void configureMagnificationPreferenceIfNeeded(Preference preference) {
        Context context = preference.getContext();
        if (!MagnificationPreferenceFragment.isApplicable(context.getResources())) {
            preference.setFragment(ToggleScreenMagnificationPreferenceFragmentForSetupWizard.class.getName());
            MagnificationGesturesPreferenceController.populateMagnificationGesturesPreferenceExtras(preference.getExtras(), context);
        }
    }
}
