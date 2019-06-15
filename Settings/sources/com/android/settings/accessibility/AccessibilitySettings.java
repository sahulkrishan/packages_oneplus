package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.KeyCharacterMap;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;
import com.android.internal.accessibility.AccessibilityShortcutController;
import com.android.internal.accessibility.AccessibilityShortcutController.ToggleableFrameworkFeatureInfo;
import com.android.internal.content.PackageMonitor;
import com.android.internal.view.RotationPolicy;
import com.android.internal.view.RotationPolicy.RotationPolicyListener;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.wifi.ConfigureWifiSettings;
import com.android.settingslib.accessibility.AccessibilityUtils;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codeaurora.ims.utils.QtiImsExtUtils;

public class AccessibilitySettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable {
    private static final String ACCESSIBILITY_SHORTCUT_PREFERENCE = "accessibility_shortcut_preference";
    private static final String ANIMATION_OFF_VALUE = "0";
    private static final String ANIMATION_ON_VALUE = "1";
    private static final String AUTOCLICK_PREFERENCE_SCREEN = "autoclick_preference_screen";
    private static final String CAPTIONING_PREFERENCE_SCREEN = "captioning_preference_screen";
    private static final String[] CATEGORIES = new String[]{CATEGORY_SCREEN_READER, CATEGORY_AUDIO_AND_CAPTIONS, CATEGORY_DISPLAY, CATEGORY_INTERACTION_CONTROL, CATEGORY_EXPERIMENTAL, CATEGORY_DOWNLOADED_SERVICES};
    private static final String CATEGORY_AUDIO_AND_CAPTIONS = "audio_and_captions_category";
    private static final String CATEGORY_DISPLAY = "display_category";
    private static final String CATEGORY_DOWNLOADED_SERVICES = "user_installed_services_category";
    private static final String CATEGORY_EXPERIMENTAL = "experimental_category";
    private static final String CATEGORY_INTERACTION_CONTROL = "interaction_control_category";
    private static final String CATEGORY_SCREEN_READER = "screen_reader_category";
    private static final long DELAY_UPDATE_SERVICES_MILLIS = 1000;
    private static final String DISPLAY_DALTONIZER_PREFERENCE_SCREEN = "daltonizer_preference_screen";
    private static final String DISPLAY_MAGNIFICATION_PREFERENCE_SCREEN = "magnification_preference_screen";
    static final String EXTRA_CHECKED = "checked";
    static final String EXTRA_COMPONENT_NAME = "component_name";
    static final String EXTRA_LAUNCHED_FROM_SUW = "from_suw";
    static final String EXTRA_PREFERENCE_KEY = "preference_key";
    static final String EXTRA_RESOLVE_INFO = "resolve_info";
    static final String EXTRA_SETTINGS_COMPONENT_NAME = "settings_component_name";
    static final String EXTRA_SETTINGS_TITLE = "settings_title";
    static final String EXTRA_SUMMARY = "summary";
    static final String EXTRA_SUMMARY_RES = "summary_res";
    static final String EXTRA_TITLE = "title";
    static final String EXTRA_TITLE_RES = "title_res";
    static final String EXTRA_VIDEO_RAW_RESOURCE_ID = "video_resource";
    private static final int FIRST_PREFERENCE_IN_CATEGORY_INDEX = -1;
    private static final String FONT_SIZE_PREFERENCE_SCREEN = "font_size_preference_screen";
    private static final String HEARING_AID_ENABLED = "hac_enabled=true";
    private static final String HEARING_AID_KEY = "hearing_aid";
    private static final String RTT_FEATURE_KEY = "rtt_feature";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public static final String KEY_DISPLAY_SIZE = "accessibility_settings_screen_zoom";

        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> indexables = new ArrayList();
            SearchIndexableResource indexable = new SearchIndexableResource(context);
            indexable.xmlResId = R.xml.accessibility_settings;
            indexables.add(indexable);
            return indexables;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add(AccessibilitySettings.FONT_SIZE_PREFERENCE_SCREEN);
            keys.add(KEY_DISPLAY_SIZE);
            keys.add(AccessibilitySettings.TTS_SETTINGS_PREFERENCE);
            if (!OPUtils.isSupportHearingAid()) {
                keys.add(AccessibilitySettings.HEARING_AID_KEY);
            }
            if (!OPUtils.isProductRTTSupport()) {
                keys.add(AccessibilitySettings.RTT_FEATURE_KEY);
            }
            return keys;
        }
    };
    private static final String SELECT_LONG_PRESS_TIMEOUT_PREFERENCE = "select_long_press_timeout_preference";
    private static final String[] TOGGLE_ANIMATION_TARGETS = new String[]{"window_animation_scale", "transition_animation_scale", "animator_duration_scale"};
    private static final String TOGGLE_DISABLE_ANIMATIONS = "toggle_disable_animations";
    private static final String TOGGLE_HIGH_TEXT_CONTRAST_PREFERENCE = "toggle_high_text_contrast_preference";
    private static final String TOGGLE_INVERSION_PREFERENCE = "toggle_inversion_preference";
    private static final String TOGGLE_LARGE_POINTER_ICON = "toggle_large_pointer_icon";
    private static final String TOGGLE_LOCK_SCREEN_ROTATION_PREFERENCE = "toggle_lock_screen_rotation_preference";
    private static final String TOGGLE_MASTER_MONO = "toggle_master_mono";
    private static final String TOGGLE_POWER_BUTTON_ENDS_CALL_PREFERENCE = "toggle_power_button_ends_call_preference";
    private static final String TTS_SETTINGS_PREFERENCE = "tts_settings_preference";
    private static final String VIBRATION_PREFERENCE_SCREEN = "vibration_preference_screen";
    private Preference mAccessibilityShortcutPreferenceScreen;
    private AudioManager mAudioManager;
    private Preference mAutoclickPreferenceScreen;
    private Preference mCaptioningPreferenceScreen;
    private final Map<String, PreferenceCategory> mCategoryToPrefCategoryMap = new ArrayMap();
    private Preference mDisplayDaltonizerPreferenceScreen;
    private Preference mDisplayMagnificationPreferenceScreen;
    private DevicePolicyManager mDpm;
    private Preference mFontSizePreferenceScreen;
    private final Handler mHandler = new Handler();
    private SwitchPreference mHearingAidPreference;
    private int mLongPressTimeoutDefault;
    private final Map<String, String> mLongPressTimeoutValueToTitleMap = new HashMap();
    private final Map<ComponentName, PreferenceCategory> mPreBundledServiceComponentToCategoryMap = new ArrayMap();
    private Preference mRTTPreference;
    private final RotationPolicyListener mRotationPolicyListener = new RotationPolicyListener() {
        public void onChange() {
            AccessibilitySettings.this.updateLockScreenRotationCheckbox();
        }
    };
    private ListPreference mSelectLongPressTimeoutPreference;
    private final Map<Preference, PreferenceCategory> mServicePreferenceToPreferenceCategoryMap = new ArrayMap();
    private final SettingsContentObserver mSettingsContentObserver;
    private final PackageMonitor mSettingsPackageMonitor = new PackageMonitor() {
        public void onPackageAdded(String packageName, int uid) {
            sendUpdate();
        }

        public void onPackageAppeared(String packageName, int reason) {
            sendUpdate();
        }

        public void onPackageDisappeared(String packageName, int reason) {
            sendUpdate();
        }

        public void onPackageRemoved(String packageName, int uid) {
            sendUpdate();
        }

        private void sendUpdate() {
            AccessibilitySettings.this.mHandler.postDelayed(AccessibilitySettings.this.mUpdateRunnable, 1000);
        }
    };
    private SwitchPreference mToggleDisableAnimationsPreference;
    private SwitchPreference mToggleHighTextContrastPreference;
    private ContentObserver mToggleInversionObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            if (AccessibilitySettings.this.mToggleInversionPreference != null) {
                boolean z = false;
                if (Secure.getInt(AccessibilitySettings.this.getContentResolver(), "accessibility_display_inversion_enabled", 0) != 0) {
                    z = true;
                }
                AccessibilitySettings.this.mToggleInversionPreference.setChecked(z);
            }
        }
    };
    private SwitchPreference mToggleInversionPreference;
    private SwitchPreference mToggleLargePointerIconPreference;
    private SwitchPreference mToggleLockScreenRotationPreference;
    private SwitchPreference mToggleMasterMonoPreference;
    private SwitchPreference mTogglePowerButtonEndsCallPreference;
    private final Runnable mUpdateRunnable = new Runnable() {
        public void run() {
            if (AccessibilitySettings.this.getActivity() != null) {
                AccessibilitySettings.this.updateServicePreferences();
            }
        }
    };
    private Preference mVibrationPreferenceScreen;

    public static boolean isColorTransformAccelerated(Context context) {
        return context.getResources().getBoolean(17957019);
    }

    public AccessibilitySettings() {
        Collection<ToggleableFrameworkFeatureInfo> features = AccessibilityShortcutController.getFrameworkShortcutFeaturesMap().values();
        List<String> shortcutFeatureKeys = new ArrayList(features.size());
        for (ToggleableFrameworkFeatureInfo feature : features) {
            shortcutFeatureKeys.add(feature.getSettingKey());
        }
        this.mSettingsContentObserver = new SettingsContentObserver(this.mHandler, shortcutFeatureKeys) {
            public void onChange(boolean selfChange, Uri uri) {
                AccessibilitySettings.this.updateAllPreferences();
            }
        };
    }

    public int getMetricsCategory() {
        return 2;
    }

    public int getHelpResource() {
        return R.string.help_uri_accessibility;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.accessibility_settings);
        initializeAllPreferences();
        this.mDpm = (DevicePolicyManager) getActivity().getSystemService("device_policy");
        this.mAudioManager = (AudioManager) getActivity().getSystemService("audio");
    }

    public void onResume() {
        super.onResume();
        updateAllPreferences();
        this.mSettingsPackageMonitor.register(getActivity(), getActivity().getMainLooper(), false);
        this.mSettingsContentObserver.register(getContentResolver());
        if (RotationPolicy.isRotationSupported(getActivity())) {
            RotationPolicy.registerRotationPolicyListener(getActivity(), this.mRotationPolicyListener);
        }
        getContentResolver().registerContentObserver(Secure.getUriFor("accessibility_display_inversion_enabled"), true, this.mToggleInversionObserver, -1);
    }

    public void onPause() {
        this.mSettingsPackageMonitor.unregister();
        this.mSettingsContentObserver.unregister(getContentResolver());
        if (RotationPolicy.isRotationSupported(getActivity())) {
            RotationPolicy.unregisterRotationPolicyListener(getActivity(), this.mRotationPolicyListener);
        }
        getContentResolver().unregisterContentObserver(this.mToggleInversionObserver);
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mSelectLongPressTimeoutPreference == preference) {
            handleLongPressTimeoutPreferenceChange((String) newValue);
            return true;
        } else if (this.mToggleInversionPreference == preference) {
            handleToggleInversionPreferenceChange(((Boolean) newValue).booleanValue());
            return true;
        } else if (this.mHearingAidPreference != preference) {
            return false;
        } else {
            this.mAudioManager.setParameters(((Boolean) newValue).booleanValue() ? HEARING_AID_ENABLED : "hac_enabled=false");
            return true;
        }
    }

    private void handleLongPressTimeoutPreferenceChange(String stringValue) {
        Secure.putInt(getContentResolver(), "long_press_timeout", Integer.parseInt(stringValue));
        this.mSelectLongPressTimeoutPreference.setSummary((CharSequence) this.mLongPressTimeoutValueToTitleMap.get(stringValue));
    }

    private void handleToggleInversionPreferenceChange(boolean checked) {
        Secure.putInt(getContentResolver(), "accessibility_display_inversion_enabled", checked);
        if (checked) {
            Toast.makeText(getPrefContext(), R.string.oneplus_screen_features_not_available_toast, 1).show();
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (this.mToggleHighTextContrastPreference == preference) {
            handleToggleTextContrastPreferenceClick();
            return true;
        } else if (this.mTogglePowerButtonEndsCallPreference == preference) {
            handleTogglePowerButtonEndsCallPreferenceClick();
            return true;
        } else if (this.mToggleLockScreenRotationPreference == preference) {
            handleLockScreenRotationPreferenceClick();
            return true;
        } else if (this.mToggleLargePointerIconPreference == preference) {
            handleToggleLargePointerIconPreferenceClick();
            return true;
        } else if (this.mToggleDisableAnimationsPreference == preference) {
            handleToggleDisableAnimations();
            return true;
        } else if (this.mToggleMasterMonoPreference == preference) {
            handleToggleMasterMonoPreferenceClick();
            return true;
        } else {
            if (this.mRTTPreference == preference) {
                gotoRTTSettingsActivity();
            }
            return super.onPreferenceTreeClick(preference);
        }
    }

    private void updateRTTPreference() {
        boolean z = false;
        if (Secure.getInt(getContentResolver(), "rtt_calling_mode", 0) != 0) {
            z = true;
        }
        boolean enableRTT = z;
        if (this.mRTTPreference != null) {
            this.mRTTPreference.setSummary(enableRTT ? R.string.switch_on_text : R.string.switch_off_text);
        }
    }

    private void gotoRTTSettingsActivity() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.settings.OPRTTSettingsActivity"));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            finish();
        }
    }

    public static CharSequence getServiceSummary(Context context, AccessibilityServiceInfo info, boolean serviceEnabled) {
        String serviceState;
        if (serviceEnabled) {
            serviceState = context.getString(R.string.accessibility_summary_state_enabled);
        } else {
            serviceState = context.getString(R.string.accessibility_summary_state_disabled);
        }
        return TextUtils.isEmpty(info.loadSummary(context.getPackageManager())) ? serviceState : context.getString(R.string.preference_summary_default_combination, new Object[]{serviceState, info.loadSummary(context.getPackageManager())});
    }

    private void handleToggleTextContrastPreferenceClick() {
        Secure.putInt(getContentResolver(), "high_text_contrast_enabled", this.mToggleHighTextContrastPreference.isChecked());
    }

    private void handleTogglePowerButtonEndsCallPreferenceClick() {
        int i;
        ContentResolver contentResolver = getContentResolver();
        String str = "incall_power_button_behavior";
        if (this.mTogglePowerButtonEndsCallPreference.isChecked()) {
            i = 2;
        } else {
            i = 1;
        }
        Secure.putInt(contentResolver, str, i);
    }

    private void handleLockScreenRotationPreferenceClick() {
        RotationPolicy.setRotationLockForAccessibility(getActivity(), this.mToggleLockScreenRotationPreference.isChecked() ^ 1);
    }

    private void handleToggleLargePointerIconPreferenceClick() {
        Secure.putInt(getContentResolver(), "accessibility_large_pointer_icon", this.mToggleLargePointerIconPreference.isChecked());
    }

    private void handleToggleDisableAnimations() {
        String newAnimationValue = this.mToggleDisableAnimationsPreference.isChecked() ? ANIMATION_OFF_VALUE : ANIMATION_ON_VALUE;
        for (String animationPreference : TOGGLE_ANIMATION_TARGETS) {
            Global.putString(getContentResolver(), animationPreference, newAnimationValue);
        }
    }

    private void handleToggleMasterMonoPreferenceClick() {
        System.putIntForUser(getContentResolver(), "master_mono", this.mToggleMasterMonoPreference.isChecked(), -2);
    }

    private void initializeAllPreferences() {
        int i = 0;
        for (int i2 = 0; i2 < CATEGORIES.length; i2++) {
            this.mCategoryToPrefCategoryMap.put(CATEGORIES[i2], (PreferenceCategory) findPreference(CATEGORIES[i2]));
        }
        this.mToggleHighTextContrastPreference = (SwitchPreference) findPreference(TOGGLE_HIGH_TEXT_CONTRAST_PREFERENCE);
        this.mToggleInversionPreference = (SwitchPreference) findPreference(TOGGLE_INVERSION_PREFERENCE);
        this.mToggleInversionPreference.setOnPreferenceChangeListener(this);
        this.mHearingAidPreference = (SwitchPreference) findPreference(HEARING_AID_KEY);
        this.mHearingAidPreference.setOnPreferenceChangeListener(this);
        if (!OPUtils.isSupportHearingAid()) {
            this.mHearingAidPreference.setVisible(false);
        }
        this.mRTTPreference = findPreference(RTT_FEATURE_KEY);
        if (!(OPUtils.isProductRTTSupport() && QtiImsExtUtils.isRttSupported(SubscriptionManager.getDefaultVoicePhoneId(), SettingsBaseApplication.mApplication.getApplicationContext()) && !OPUtils.isGuestMode())) {
            this.mRTTPreference.setVisible(false);
        }
        this.mTogglePowerButtonEndsCallPreference = (SwitchPreference) findPreference(TOGGLE_POWER_BUTTON_ENDS_CALL_PREFERENCE);
        if (!(KeyCharacterMap.deviceHasKey(26) && Utils.isVoiceCapable(getActivity()))) {
            ((PreferenceCategory) this.mCategoryToPrefCategoryMap.get(CATEGORY_INTERACTION_CONTROL)).removePreference(this.mTogglePowerButtonEndsCallPreference);
        }
        this.mToggleLockScreenRotationPreference = (SwitchPreference) findPreference(TOGGLE_LOCK_SCREEN_ROTATION_PREFERENCE);
        if (!RotationPolicy.isRotationSupported(getActivity())) {
            ((PreferenceCategory) this.mCategoryToPrefCategoryMap.get(CATEGORY_INTERACTION_CONTROL)).removePreference(this.mToggleLockScreenRotationPreference);
        }
        this.mToggleLargePointerIconPreference = (SwitchPreference) findPreference(TOGGLE_LARGE_POINTER_ICON);
        this.mToggleDisableAnimationsPreference = (SwitchPreference) findPreference(TOGGLE_DISABLE_ANIMATIONS);
        this.mToggleMasterMonoPreference = (SwitchPreference) findPreference(TOGGLE_MASTER_MONO);
        this.mSelectLongPressTimeoutPreference = (ListPreference) findPreference(SELECT_LONG_PRESS_TIMEOUT_PREFERENCE);
        this.mSelectLongPressTimeoutPreference.setOnPreferenceChangeListener(this);
        if (this.mLongPressTimeoutValueToTitleMap.size() == 0) {
            String[] timeoutValues = getResources().getStringArray(R.array.long_press_timeout_selector_values);
            this.mLongPressTimeoutDefault = Integer.parseInt(timeoutValues[0]);
            String[] timeoutTitles = getResources().getStringArray(R.array.long_press_timeout_selector_titles);
            int timeoutValueCount = timeoutValues.length;
            while (i < timeoutValueCount) {
                this.mLongPressTimeoutValueToTitleMap.put(timeoutValues[i], timeoutTitles[i]);
                i++;
            }
        }
        this.mCaptioningPreferenceScreen = findPreference(CAPTIONING_PREFERENCE_SCREEN);
        this.mDisplayMagnificationPreferenceScreen = findPreference(DISPLAY_MAGNIFICATION_PREFERENCE_SCREEN);
        configureMagnificationPreferenceIfNeeded(this.mDisplayMagnificationPreferenceScreen);
        this.mFontSizePreferenceScreen = findPreference(FONT_SIZE_PREFERENCE_SCREEN);
        this.mAutoclickPreferenceScreen = findPreference(AUTOCLICK_PREFERENCE_SCREEN);
        this.mDisplayDaltonizerPreferenceScreen = findPreference(DISPLAY_DALTONIZER_PREFERENCE_SCREEN);
        this.mAccessibilityShortcutPreferenceScreen = findPreference(ACCESSIBILITY_SHORTCUT_PREFERENCE);
        this.mVibrationPreferenceScreen = findPreference(VIBRATION_PREFERENCE_SCREEN);
    }

    private void updateAllPreferences() {
        updateSystemPreferences();
        updateServicePreferences();
        updateRTTPreference();
        if (this.mHearingAidPreference != null) {
            this.mHearingAidPreference.setChecked(HEARING_AID_ENABLED.equals(this.mAudioManager.getParameters("hac_enabled")));
        }
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x01bb  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x019e  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x01d4 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x01cb  */
    public void updateServicePreferences() {
        /*
        r28 = this;
        r0 = r28;
        r1 = new java.util.ArrayList;
        r2 = r0.mServicePreferenceToPreferenceCategoryMap;
        r2 = r2.keySet();
        r1.<init>(r2);
        r3 = 0;
    L_0x000e:
        r4 = r1.size();
        if (r3 >= r4) goto L_0x0028;
    L_0x0014:
        r4 = r1.get(r3);
        r4 = (android.support.v7.preference.Preference) r4;
        r5 = r0.mServicePreferenceToPreferenceCategoryMap;
        r5 = r5.get(r4);
        r5 = (android.support.v7.preference.PreferenceCategory) r5;
        r5.removePreference(r4);
        r3 = r3 + 1;
        goto L_0x000e;
    L_0x0028:
        r3 = "screen_reader_category";
        r4 = 2130903107; // 0x7f030043 float:1.7413023E38 double:1.05280602E-314;
        r0.initializePreBundledServicesMapFromArray(r3, r4);
        r3 = "audio_and_captions_category";
        r4 = 2130903104; // 0x7f030040 float:1.7413017E38 double:1.0528060183E-314;
        r0.initializePreBundledServicesMapFromArray(r3, r4);
        r3 = "display_category";
        r4 = 2130903105; // 0x7f030041 float:1.7413019E38 double:1.052806019E-314;
        r0.initializePreBundledServicesMapFromArray(r3, r4);
        r3 = "interaction_control_category";
        r4 = 2130903106; // 0x7f030042 float:1.741302E38 double:1.0528060193E-314;
        r0.initializePreBundledServicesMapFromArray(r3, r4);
        r3 = r28.getActivity();
        r3 = android.view.accessibility.AccessibilityManager.getInstance(r3);
        r4 = r3.getInstalledAccessibilityServiceList();
        r5 = -1;
        r6 = r3.getEnabledAccessibilityServiceList(r5);
        r7 = r28.getActivity();
        r7 = com.android.settingslib.accessibility.AccessibilityUtils.getEnabledServicesFromSettings(r7);
        r8 = r0.mDpm;
        r9 = android.os.UserHandle.myUserId();
        r8 = r8.getPermittedAccessibilityServices(r9);
        r9 = r0.mCategoryToPrefCategoryMap;
        r10 = "user_installed_services_category";
        r9 = r9.get(r10);
        r9 = (android.support.v7.preference.PreferenceCategory) r9;
        r10 = "user_installed_services_category";
        r10 = r0.findPreference(r10);
        if (r10 != 0) goto L_0x0087;
    L_0x0080:
        r10 = r28.getPreferenceScreen();
        r10.addPreference(r9);
    L_0x0087:
        r10 = 0;
        r11 = r4.size();
    L_0x008c:
        if (r10 >= r11) goto L_0x01ef;
    L_0x008e:
        r12 = r4.get(r10);
        r12 = (android.accessibilityservice.AccessibilityServiceInfo) r12;
        r13 = r12.getResolveInfo();
        r14 = new com.android.settingslib.RestrictedPreference;
        r15 = r9.getContext();
        r14.<init>(r15);
        r15 = r28.getPackageManager();
        r15 = r13.loadLabel(r15);
        r15 = r15.toString();
        r16 = r13.getIconResource();
        if (r16 != 0) goto L_0x00be;
    L_0x00b3:
        r5 = r28.getContext();
        r2 = 2131689472; // 0x7f0f0000 float:1.900796E38 double:1.0531945357E-314;
        r2 = android.support.v4.content.ContextCompat.getDrawable(r5, r2);
        goto L_0x00c6;
    L_0x00be:
        r2 = r28.getPackageManager();
        r2 = r13.loadIcon(r2);
    L_0x00c6:
        r5 = r13.serviceInfo;
        r17 = r1;
        r1 = r5.packageName;
        r18 = r3;
        r3 = new android.content.ComponentName;
        r19 = r4;
        r4 = r5.name;
        r3.<init>(r1, r4);
        r4 = r3.flattenToString();
        r14.setKey(r4);
        r14.setTitle(r15);
        com.android.settings.Utils.setSafeIcon(r14, r2);
        r4 = r7.contains(r3);
        r20 = r2;
        r2 = r28.getPackageManager();
        r2 = r12.loadDescription(r2);
        r16 = android.text.TextUtils.isEmpty(r2);
        if (r16 == 0) goto L_0x0102;
    L_0x00f8:
        r21 = r2;
        r2 = 2131886207; // 0x7f12007f float:1.9406986E38 double:1.0532917357E-314;
        r2 = r0.getString(r2);
        goto L_0x0104;
    L_0x0102:
        r21 = r2;
    L_0x0104:
        if (r4 == 0) goto L_0x011e;
    L_0x0106:
        r22 = r2;
        r2 = r5.name;
        r2 = com.android.settingslib.accessibility.AccessibilityUtils.hasServiceCrashed(r1, r2, r6);
        if (r2 == 0) goto L_0x0120;
    L_0x0110:
        r2 = 2131886217; // 0x7f120089 float:1.9407007E38 double:1.0532917407E-314;
        r14.setSummary(r2);
        r2 = 2131886171; // 0x7f12005b float:1.9406913E38 double:1.053291718E-314;
        r2 = r0.getString(r2);
        goto L_0x012d;
    L_0x011e:
        r22 = r2;
    L_0x0120:
        r2 = r28.getContext();
        r2 = getServiceSummary(r2, r12, r4);
        r14.setSummary(r2);
        r2 = r22;
    L_0x012d:
        r23 = r5;
        if (r8 == 0) goto L_0x013b;
    L_0x0131:
        r16 = r8.contains(r1);
        if (r16 == 0) goto L_0x0138;
    L_0x0137:
        goto L_0x013b;
    L_0x0138:
        r16 = 0;
        goto L_0x013d;
    L_0x013b:
        r16 = 1;
    L_0x013d:
        if (r16 != 0) goto L_0x015e;
    L_0x013f:
        if (r4 != 0) goto L_0x015e;
        r5 = r28.getActivity();
        r24 = r6;
        r6 = android.os.UserHandle.myUserId();
        r5 = com.android.settingslib.RestrictedLockUtils.checkIfAccessibilityServiceDisallowed(r5, r1, r6);
        if (r5 == 0) goto L_0x0157;
    L_0x0152:
        r14.setDisabledByAdmin(r5);
        r6 = 0;
        goto L_0x015b;
    L_0x0157:
        r6 = 0;
        r14.setEnabled(r6);
        r5 = 1;
        goto L_0x0165;
    L_0x015e:
        r24 = r6;
        r6 = 0;
        r5 = 1;
        r14.setEnabled(r5);
    L_0x0165:
        r6 = com.android.settings.accessibility.ToggleAccessibilityServicePreferenceFragment.class;
        r6 = r6.getName();
        r14.setFragment(r6);
        r14.setPersistent(r5);
        r5 = r14.getExtras();
        r6 = "preference_key";
        r25 = r7;
        r7 = r14.getKey();
        r5.putString(r6, r7);
        r6 = "checked";
        r5.putBoolean(r6, r4);
        r6 = "title";
        r5.putString(r6, r15);
        r6 = "resolve_info";
        r5.putParcelable(r6, r13);
        r6 = "summary";
        r5.putString(r6, r2);
        r6 = r12.getSettingsActivityName();
        r7 = android.text.TextUtils.isEmpty(r6);
        if (r7 != 0) goto L_0x01bb;
    L_0x019e:
        r7 = "settings_title";
        r26 = r2;
        r2 = 2131886190; // 0x7f12006e float:1.9406952E38 double:1.0532917273E-314;
        r2 = r0.getString(r2);
        r5.putString(r7, r2);
        r2 = "settings_component_name";
        r7 = new android.content.ComponentName;
        r7.<init>(r1, r6);
        r7 = r7.flattenToString();
        r5.putString(r2, r7);
        goto L_0x01bd;
    L_0x01bb:
        r26 = r2;
    L_0x01bd:
        r2 = "component_name";
        r5.putParcelable(r2, r3);
        r2 = r9;
        r7 = r0.mPreBundledServiceComponentToCategoryMap;
        r7 = r7.containsKey(r3);
        if (r7 == 0) goto L_0x01d4;
    L_0x01cb:
        r7 = r0.mPreBundledServiceComponentToCategoryMap;
        r7 = r7.get(r3);
        r2 = r7;
        r2 = (android.support.v7.preference.PreferenceCategory) r2;
    L_0x01d4:
        r7 = -1;
        r14.setOrder(r7);
        r2.addPreference(r14);
        r7 = r0.mServicePreferenceToPreferenceCategoryMap;
        r7.put(r14, r2);
        r10 = r10 + 1;
        r1 = r17;
        r3 = r18;
        r4 = r19;
        r6 = r24;
        r7 = r25;
        r5 = -1;
        goto L_0x008c;
    L_0x01ef:
        r17 = r1;
        r18 = r3;
        r19 = r4;
        r24 = r6;
        r25 = r7;
        r1 = r9.getPreferenceCount();
        if (r1 != 0) goto L_0x0206;
    L_0x01ff:
        r1 = r28.getPreferenceScreen();
        r1.removePreference(r9);
    L_0x0206:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.accessibility.AccessibilitySettings.updateServicePreferences():void");
    }

    private void initializePreBundledServicesMapFromArray(String categoryKey, int key) {
        String[] services = getResources().getStringArray(key);
        PreferenceCategory category = (PreferenceCategory) this.mCategoryToPrefCategoryMap.get(categoryKey);
        for (ComponentName component : services) {
            this.mPreBundledServiceComponentToCategoryMap.put(ComponentName.unflattenFromString(component), category);
        }
    }

    /* Access modifiers changed, original: protected */
    public void updateSystemPreferences() {
        if (isColorTransformAccelerated(getContext())) {
            PreferenceCategory experimentalCategory = (PreferenceCategory) this.mCategoryToPrefCategoryMap.get(CATEGORY_EXPERIMENTAL);
            PreferenceCategory displayCategory = (PreferenceCategory) this.mCategoryToPrefCategoryMap.get(CATEGORY_DISPLAY);
            experimentalCategory.removePreference(this.mToggleInversionPreference);
            experimentalCategory.removePreference(this.mDisplayDaltonizerPreferenceScreen);
            this.mToggleInversionPreference.setOrder(this.mToggleLargePointerIconPreference.getOrder());
            this.mDisplayDaltonizerPreferenceScreen.setOrder(this.mToggleInversionPreference.getOrder());
            this.mToggleInversionPreference.setSummary((int) R.string.summary_empty);
            displayCategory.addPreference(this.mToggleInversionPreference);
            displayCategory.addPreference(this.mDisplayDaltonizerPreferenceScreen);
        }
        boolean z = true;
        this.mToggleHighTextContrastPreference.setChecked(Secure.getInt(getContentResolver(), "high_text_contrast_enabled", 0) == 1);
        this.mToggleInversionPreference.setChecked(Secure.getInt(getContentResolver(), "accessibility_display_inversion_enabled", 0) == 1);
        if (KeyCharacterMap.deviceHasKey(26) && Utils.isVoiceCapable(getActivity())) {
            this.mTogglePowerButtonEndsCallPreference.setChecked(Secure.getInt(getContentResolver(), "incall_power_button_behavior", 1) == 2);
        }
        updateLockScreenRotationCheckbox();
        SwitchPreference switchPreference = this.mToggleLargePointerIconPreference;
        if (Secure.getInt(getContentResolver(), "accessibility_large_pointer_icon", 0) == 0) {
            z = false;
        }
        switchPreference.setChecked(z);
        updateDisableAnimationsToggle();
        updateMasterMono();
        String value = String.valueOf(Secure.getInt(getContentResolver(), "long_press_timeout", this.mLongPressTimeoutDefault));
        this.mSelectLongPressTimeoutPreference.setValue(value);
        this.mSelectLongPressTimeoutPreference.setSummary((CharSequence) this.mLongPressTimeoutValueToTitleMap.get(value));
        updateVibrationSummary(this.mVibrationPreferenceScreen);
        updateFeatureSummary("accessibility_captioning_enabled", this.mCaptioningPreferenceScreen);
        updateFeatureSummary("accessibility_display_daltonizer_enabled", this.mDisplayDaltonizerPreferenceScreen);
        updateMagnificationSummary(this.mDisplayMagnificationPreferenceScreen);
        updateFontSizeSummary(this.mFontSizePreferenceScreen);
        updateAutoclickSummary(this.mAutoclickPreferenceScreen);
        updateAccessibilityShortcut(this.mAccessibilityShortcutPreferenceScreen);
    }

    private void updateMagnificationSummary(Preference pref) {
        int summaryResId;
        boolean buttonEnabled = true;
        boolean tripleTapEnabled = Secure.getInt(getContentResolver(), "accessibility_display_magnification_enabled", 0) == 1;
        if (Secure.getInt(getContentResolver(), "accessibility_display_magnification_navbar_enabled", 0) != 1) {
            buttonEnabled = false;
        }
        if (!tripleTapEnabled && !buttonEnabled) {
            summaryResId = R.string.accessibility_feature_state_off;
        } else if (!tripleTapEnabled && buttonEnabled) {
            summaryResId = R.string.accessibility_screen_magnification_navbar_title;
        } else if (!tripleTapEnabled || buttonEnabled) {
            summaryResId = R.string.accessibility_screen_magnification_state_navbar_gesture;
        } else {
            summaryResId = R.string.accessibility_screen_magnification_gestures_title;
        }
        pref.setSummary(summaryResId);
    }

    private void updateFeatureSummary(String prefKey, Preference pref) {
        int i;
        boolean z = false;
        if (Secure.getInt(getContentResolver(), prefKey, 0) == 1) {
            z = true;
        }
        if (z) {
            i = R.string.accessibility_feature_state_on;
        } else {
            i = R.string.accessibility_feature_state_off;
        }
        pref.setSummary(i);
    }

    private void updateAutoclickSummary(Preference pref) {
        boolean z = true;
        if (Secure.getInt(getContentResolver(), "accessibility_autoclick_enabled", 0) != 1) {
            z = false;
        }
        if (z) {
            pref.setSummary(ToggleAutoclickPreferenceFragment.getAutoclickPreferenceSummary(getResources(), Secure.getInt(getContentResolver(), "accessibility_autoclick_delay", ConfigureWifiSettings.WIFI_WAKEUP_REQUEST_CODE)));
            return;
        }
        pref.setSummary((int) R.string.accessibility_feature_state_off);
    }

    private void updateFontSizeSummary(Preference pref) {
        float currentScale = System.getFloat(getContext().getContentResolver(), "font_scale", 1.0f);
        Resources res = getContext().getResources();
        pref.setSummary(res.getStringArray(2130903123)[ToggleFontSizePreferenceFragment.fontSizeValueToIndex(currentScale, res.getStringArray(2130903124))]);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting(otherwise = 2)
    public void updateVibrationSummary(Preference pref) {
        Context context = getContext();
        Vibrator vibrator = (Vibrator) context.getSystemService(Vibrator.class);
        int ringIntensity = System.getInt(context.getContentResolver(), "notification_vibration_intensity", vibrator.getDefaultNotificationVibrationIntensity());
        CharSequence ringIntensityString = VibrationIntensityPreferenceController.getIntensityString(context, ringIntensity);
        int touchIntensity = System.getInt(context.getContentResolver(), "haptic_feedback_intensity", vibrator.getDefaultHapticFeedbackIntensity());
        CharSequence touchIntensityString = VibrationIntensityPreferenceController.getIntensityString(context, touchIntensity);
        if (this.mVibrationPreferenceScreen == null) {
            this.mVibrationPreferenceScreen = findPreference(VIBRATION_PREFERENCE_SCREEN);
        }
        if (ringIntensity == touchIntensity) {
            this.mVibrationPreferenceScreen.setSummary(ringIntensityString);
            return;
        }
        this.mVibrationPreferenceScreen.setSummary(getString(R.string.accessibility_vibration_summary, new Object[]{ringIntensityString, touchIntensityString}));
    }

    private String getVibrationSummary(Context context, int intensity) {
        if (context.getResources().getBoolean(R.bool.config_vibration_supports_multiple_intensities)) {
            switch (intensity) {
                case 0:
                    return context.getString(R.string.accessibility_vibration_summary_off);
                case 1:
                    return context.getString(R.string.accessibility_vibration_summary_low);
                case 2:
                    return context.getString(R.string.accessibility_vibration_summary_medium);
                case 3:
                    return context.getString(R.string.accessibility_vibration_summary_high);
                default:
                    return "";
            }
        } else if (intensity == 0) {
            return context.getString(R.string.switch_on_text);
        } else {
            return context.getString(R.string.switch_off_text);
        }
    }

    private void updateLockScreenRotationCheckbox() {
        Context context = getActivity();
        if (context != null) {
            this.mToggleLockScreenRotationPreference.setChecked(RotationPolicy.isRotationLocked(context) ^ 1);
        }
    }

    private void updateDisableAnimationsToggle() {
        boolean allAnimationsDisabled = true;
        for (String animationSetting : TOGGLE_ANIMATION_TARGETS) {
            if (!TextUtils.equals(Global.getString(getContentResolver(), animationSetting), ANIMATION_OFF_VALUE)) {
                allAnimationsDisabled = false;
                break;
            }
        }
        this.mToggleDisableAnimationsPreference.setChecked(allAnimationsDisabled);
    }

    private void updateMasterMono() {
        boolean z = true;
        if (System.getIntForUser(getContentResolver(), "master_mono", 0, -2) != 1) {
            z = false;
        }
        this.mToggleMasterMonoPreference.setChecked(z);
    }

    private void updateAccessibilityShortcut(Preference preference) {
        if (AccessibilityManager.getInstance(getActivity()).getInstalledAccessibilityServiceList().isEmpty()) {
            this.mAccessibilityShortcutPreferenceScreen.setSummary(getString(R.string.accessibility_no_services_installed));
            this.mAccessibilityShortcutPreferenceScreen.setEnabled(false);
            return;
        }
        CharSequence summary;
        this.mAccessibilityShortcutPreferenceScreen.setEnabled(true);
        if (AccessibilityUtils.isShortcutEnabled(getContext(), UserHandle.myUserId())) {
            summary = AccessibilityShortcutPreferenceFragment.getServiceName(getContext());
        } else {
            summary = getString(R.string.accessibility_feature_state_off);
        }
        this.mAccessibilityShortcutPreferenceScreen.setSummary(summary);
    }

    private static void configureMagnificationPreferenceIfNeeded(Preference preference) {
        Context context = preference.getContext();
        if (!MagnificationPreferenceFragment.isApplicable(context.getResources())) {
            preference.setFragment(ToggleScreenMagnificationPreferenceFragment.class.getName());
            MagnificationGesturesPreferenceController.populateMagnificationGesturesPreferenceExtras(preference.getExtras(), context);
        }
    }
}
