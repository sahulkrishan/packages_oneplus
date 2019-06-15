package com.android.settings;

import android.os.Bundle;
import com.android.settings.enterprise.EnterprisePrivacySettings;

public class Settings extends SettingsActivity {

    public static class AccessibilityContrastSettingsActivity extends SettingsActivity {
    }

    public static class AccessibilityDaltonizerSettingsActivity extends SettingsActivity {
    }

    public static class AccessibilityInversionSettingsActivity extends SettingsActivity {
    }

    public static class AccessibilitySettingsActivity extends SettingsActivity {
    }

    public static class AccountDashboardActivity extends SettingsActivity {
    }

    public static class AccountSyncSettingsActivity extends SettingsActivity {
    }

    public static class AccountSyncSettingsInAddAccountActivity extends SettingsActivity {
    }

    public static class AdvancedAppsActivity extends SettingsActivity {
    }

    public static class AdvancedConnectedDeviceActivity extends SettingsActivity {
    }

    public static class AllApplicationsActivity extends SettingsActivity {
    }

    public static class AndroidBeamSettingsActivity extends SettingsActivity {
    }

    public static class ApnEditorActivity extends SettingsActivity {
    }

    public static class ApnSettingsActivity extends SettingsActivity {
    }

    public static class AppAndNotificationDashboardActivity extends SettingsActivity {
    }

    public static class AppDrawOverlaySettingsActivity extends SettingsActivity {
    }

    public static class AppMemoryUsageActivity extends SettingsActivity {
    }

    public static class AppNotificationSettingsActivity extends SettingsActivity {
    }

    public static class AppPictureInPictureSettingsActivity extends SettingsActivity {
    }

    public static class AppWriteSettingsActivity extends SettingsActivity {
    }

    public static class ApplicationSettingsActivity extends SettingsActivity {
    }

    public static class AssistGestureSettingsActivity extends SettingsActivity {
    }

    public static class AutomaticStorageManagerSettingsActivity extends SettingsActivity {
    }

    public static class AvailableVirtualKeyboardActivity extends SettingsActivity {
    }

    public static class BackgroundCheckSummaryActivity extends SettingsActivity {
    }

    public static class BatterySaverSettingsActivity extends SettingsActivity {
    }

    public static class BgOptimizeAppListActivity extends SettingsActivity {
    }

    public static class BgOptimizeSwitchActivity extends SettingsActivity {
    }

    public static class BluetoothSettingsActivity extends SettingsActivity {
    }

    public static class CaptioningSettingsActivity extends SettingsActivity {
    }

    public static class ChangeWifiStateActivity extends SettingsActivity {
    }

    public static class ChannelGroupNotificationSettingsActivity extends SettingsActivity {
    }

    public static class ChannelNotificationSettingsActivity extends SettingsActivity {
    }

    public static class ChooseAccountActivity extends SettingsActivity {
    }

    public static class ConditionProviderSettingsActivity extends SettingsActivity {
    }

    public static class ConfigureNotificationSettingsActivity extends SettingsActivity {
    }

    public static class ConfigureWifiSettingsActivity extends SettingsActivity {
    }

    public static class ConnectedDeviceDashboardActivity extends SettingsActivity {
    }

    public static class CryptKeeperSettingsActivity extends SettingsActivity {
    }

    public static class DataUsageSummaryActivity extends SettingsActivity {
    }

    public static class DataUsageSummaryLegacyActivity extends SettingsActivity {
    }

    public static class DateTimeSettingsActivity extends SettingsActivity {
    }

    public static class DefaultAssistPickerActivity extends SettingsActivity {
    }

    public static class DeletionHelperActivity extends SettingsActivity {
    }

    public static class DevelopmentSettingsDashboardActivity extends SettingsActivity {
    }

    public static class DeviceAdminSettingsActivity extends SettingsActivity {
    }

    public static class DeviceInfoSettingsActivity extends SettingsActivity {
    }

    public static class DirectoryAccessSettingsActivity extends SettingsActivity {
    }

    public static class DisplaySettingsActivity extends SettingsActivity {
    }

    public static class DisplaySizeAdaptionAppListActivity extends SettingsActivity {
    }

    public static class DomainsURLsAppListActivity extends SettingsActivity {
    }

    public static class DreamSettingsActivity extends SettingsActivity {
    }

    public static class EnterprisePrivacySettingsActivity extends SettingsActivity {
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (!EnterprisePrivacySettings.isPageEnabled(this)) {
                finish();
            }
        }
    }

    public static class FactoryResetActivity extends SettingsActivity {
    }

    public static class GamesStorageActivity extends SettingsActivity {
    }

    public static class HighPowerApplicationsActivity extends SettingsActivity {
    }

    public static class IccLockSettingsActivity extends SettingsActivity {
    }

    public static class InputMethodAndSubtypeEnablerActivity extends SettingsActivity {
    }

    public static class KeyboardLayoutPickerActivity extends SettingsActivity {
    }

    public static class LanguageAndInputSettingsActivity extends SettingsActivity {
    }

    public static class LegalSettingsActivity extends SettingsActivity {
    }

    public static class LocalePickerActivity extends SettingsActivity {
    }

    public static class LocationSettingsActivity extends SettingsActivity {
    }

    public static class ManageAppExternalSourcesActivity extends SettingsActivity {
    }

    public static class ManageApplicationsActivity extends SettingsActivity {
    }

    public static class ManageAssistActivity extends SettingsActivity {
    }

    public static class ManageDomainUrlsActivity extends SettingsActivity {
    }

    public static class ManageExternalSourcesActivity extends SettingsActivity {
    }

    public static class ManagedProfileSettingsActivity extends SettingsActivity {
    }

    public static class MemorySettingsActivity extends SettingsActivity {
    }

    public static class MobileDataUsageListActivity extends SettingsActivity {
    }

    public static class MoviesStorageActivity extends SettingsActivity {
    }

    public static class MyDeviceInfoActivity extends SettingsActivity {
    }

    public static class MyDeviceInfoFragmentActivity extends SettingsActivity {
    }

    public static class NetworkDashboardActivity extends SettingsActivity {
    }

    public static class NightDisplaySettingsActivity extends SettingsActivity {
    }

    public static class NotificationAccessSettingsActivity extends SettingsActivity {
    }

    public static class NotificationAppListActivity extends SettingsActivity {
    }

    public static class NotificationStationActivity extends SettingsActivity {
    }

    public static class OPAboutPhoneActivity extends SettingsActivity {
    }

    public static class OPAppLockerActivity extends SettingsActivity {
    }

    public static class OPAppSecurityRecommeSettingsActivity extends SettingsActivity {
    }

    public static class OPBluetoothCarKitActivity extends SettingsActivity {
    }

    public static class OPButtonsAndGesturesSettingsActivity extends SettingsActivity {
    }

    public static class OPCarChargerActivity extends SettingsActivity {
    }

    public static class OPCloudServiceSettings extends SettingsActivity {
    }

    public static class OPCustomFingerprintAnimSettingsActivity extends SettingsActivity {
    }

    public static class OPDataSaverActivity extends SettingsActivity {
    }

    public static class OPDataUsageSummaryActivity extends SettingsActivity {
    }

    public static class OPDeviceNameActivity extends SettingsActivity {
    }

    public static class OPEarphoneModeActivity extends SettingsActivity {
    }

    public static class OPFaceUnlockSettings extends SettingsActivity {
    }

    public static class OPGamingModeActivity extends SettingsActivity {
    }

    public static class OPGestureAnswerSettings extends SettingsActivity {
    }

    public static class OPHapticFeedbackActivity extends SettingsActivity {
    }

    public static class OPNightModeActivity extends SettingsActivity {
    }

    public static class OPNotificationAndNotdisturbSettingsActivity extends SettingsActivity {
    }

    public static class OPPreInstalledAppListActivity extends SettingsActivity {
    }

    public static class OPProductInfoActivity extends SettingsActivity {
    }

    public static class OPQuickLaunchListSettings extends SettingsActivity {
    }

    public static class OPQuickLaunchSettings extends SettingsActivity {
    }

    public static class OPQuickPaySettingsActivity extends SettingsActivity {
    }

    public static class OPQuickReplySettingsActivity extends SettingsActivity {
    }

    public static class OPRamBoostSettingsActivity extends SettingsActivity {
    }

    public static class OPReadingModeActivity extends SettingsActivity {
    }

    public static class OPRingModeActivity extends SettingsActivity {
    }

    public static class OPSilentModeActivity extends SettingsActivity {
    }

    public static class OPToolsSettingsActivity extends SettingsActivity {
    }

    public static class OPVibrationModeActivity extends SettingsActivity {
    }

    public static class OPVoiceAssistantSettingsActivity extends SettingsActivity {
    }

    public static class OverlaySettingsActivity extends SettingsActivity {
    }

    public static class PaymentSettingsActivity extends SettingsActivity {
    }

    public static class PhotosStorageActivity extends SettingsActivity {
    }

    public static class PhysicalKeyboardActivity extends SettingsActivity {
    }

    public static class PictureInPictureSettingsActivity extends SettingsActivity {
    }

    public static class PowerUsageAdvancedActivity extends SettingsActivity {
    }

    public static class PowerUsageSummaryActivity extends SettingsActivity {
    }

    public static class PrintJobSettingsActivity extends SettingsActivity {
    }

    public static class PrintSettingsActivity extends SettingsActivity {
    }

    public static class PrivacySettingsActivity extends SettingsActivity {
    }

    public static class PrivateVolumeForgetActivity extends SettingsActivity {
    }

    public static class PrivateVolumeSettingsActivity extends SettingsActivity {
    }

    public static class PublicVolumeSettingsActivity extends SettingsActivity {
    }

    public static class RunningServicesActivity extends SettingsActivity {
    }

    public static class SMQQtiFeedbackActivity extends SettingsActivity {
    }

    public static class SavedAccessPointsSettingsActivity extends SettingsActivity {
    }

    public static class ScanningSettingsActivity extends SettingsActivity {
    }

    public static class SecurityDashboardActivity extends SettingsActivity {
    }

    public static class SimSettingsActivity extends SettingsActivity {
    }

    public static class SoundSettingsActivity extends SettingsActivity {
    }

    public static class SpellCheckersSettingsActivity extends SettingsActivity {
    }

    public static class StorageDashboardActivity extends SettingsActivity {
    }

    public static class StorageUseActivity extends SettingsActivity {
    }

    public static class SupportDashboardActivity extends SettingsActivity {
    }

    public static class SystemDashboardActivity extends SettingsActivity {
    }

    public static class TestingSettingsActivity extends SettingsActivity {
    }

    public static class TetherSettingsActivity extends SettingsActivity {
    }

    public static class TextToSpeechSettingsActivity extends SettingsActivity {
    }

    public static class TopLevelSettings extends SettingsActivity {
    }

    public static class TrustedCredentialsSettingsActivity extends SettingsActivity {
    }

    public static class UsageAccessSettingsActivity extends SettingsActivity {
    }

    public static class UsbDetailsActivity extends SettingsActivity {
    }

    public static class UsbSettingsActivity extends SettingsActivity {
    }

    public static class UserDictionarySettingsActivity extends SettingsActivity {
    }

    public static class UserSettingsActivity extends SettingsActivity {
    }

    public static class VpnSettingsActivity extends SettingsActivity {
    }

    public static class VrListenersSettingsActivity extends SettingsActivity {
    }

    public static class WallpaperSettingsActivity extends SettingsActivity {
    }

    public static class WebViewAppPickerActivity extends SettingsActivity {
    }

    public static class WifiAPITestActivity extends SettingsActivity {
    }

    public static class WifiCallingSettingsActivity extends SettingsActivity {
    }

    public static class WifiDisplaySettingsActivity extends SettingsActivity {
    }

    public static class WifiInfoActivity extends SettingsActivity {
    }

    public static class WifiP2pSettingsActivity extends SettingsActivity {
    }

    public static class WifiSettingsActivity extends SettingsActivity {
    }

    public static class WriteSettingsActivity extends SettingsActivity {
    }

    public static class ZenAccessSettingsActivity extends SettingsActivity {
    }

    public static class ZenModeAutomationSettingsActivity extends SettingsActivity {
    }

    public static class ZenModeBehaviorSettingsActivity extends SettingsActivity {
    }

    public static class ZenModeBlockedEffectsSettingsActivity extends SettingsActivity {
    }

    public static class ZenModeEventRuleSettingsActivity extends SettingsActivity {
    }

    public static class ZenModeScheduleRuleSettingsActivity extends SettingsActivity {
    }

    public static class ZenModeSettingsActivity extends SettingsActivity {
    }

    public static class NightDisplaySuggestionActivity extends NightDisplaySettingsActivity {
    }
}
