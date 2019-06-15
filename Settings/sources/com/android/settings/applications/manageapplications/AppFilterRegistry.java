package com.android.settings.applications.manageapplications;

import com.android.settings.R;
import com.android.settings.applications.AppStateDirectoryAccessBridge;
import com.android.settings.applications.AppStateInstallAppsBridge;
import com.android.settings.applications.AppStateNotificationBridge;
import com.android.settings.applications.AppStateOverlayBridge;
import com.android.settings.applications.AppStatePowerBridge;
import com.android.settings.applications.AppStateUsageBridge;
import com.android.settings.applications.AppStateWriteSettingsBridge;
import com.android.settings.wifi.AppStateChangeWifiStateBridge;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import com.android.settingslib.applications.ApplicationsState.CompoundFilter;
import com.oneplus.settings.backgroundoptimize.AppBgOptimizeBridge;
import com.oneplus.settings.displaysizeadaption.DisplaySizeAdaptionBridge;
import com.oneplus.settings.utils.OPUtils;

public class AppFilterRegistry {
    public static final int FILTER_APPS_ALL = 2;
    public static final int FILTER_APPS_BACKGROUND_OPTIMIZE_ALL = 19;
    public static final int FILTER_APPS_BACKGROUND_OPTIMIZE_NOT = 20;
    public static final int FILTER_APPS_DISABLED = 5;
    public static final int FILTER_APPS_DISPLAY_SIZE_ADAPTION_ALL = 16;
    public static final int FILTER_APPS_DISPLAY_SIZE_ADAPTION_FULL_SCREEN = 17;
    public static final int FILTER_APPS_DISPLAY_SIZE_ADAPTION_ORIGINAL_SIZE = 18;
    public static final int FILTER_APPS_ENABLED = 3;
    public static final int FILTER_APPS_FREQUENT = 7;
    public static final int FILTER_APPS_INSTALL_SOURCES = 13;
    public static final int FILTER_APPS_INSTANT = 4;
    public static final int FILTER_APPS_PERSONAL = 8;
    public static final int FILTER_APPS_POWER_WHITELIST = 0;
    public static final int FILTER_APPS_POWER_WHITELIST_ALL = 1;
    public static final int FILTER_APPS_RECENT = 6;
    public static final int FILTER_APPS_USAGE_ACCESS = 10;
    public static final int FILTER_APPS_WITH_OVERLAY = 11;
    public static final int FILTER_APPS_WORK = 9;
    public static final int FILTER_APPS_WRITE_SETTINGS = 12;
    public static final int FILTER_APP_CAN_CHANGE_WIFI_STATE = 15;
    public static final int FILTER_APP_HAS_DIRECTORY_ACCESS = 14;
    private static AppFilterRegistry sRegistry;
    private final AppFilterItem[] mFilters = new AppFilterItem[21];

    @interface FilterType {
    }

    private AppFilterRegistry() {
        AppFilter appFilter;
        AppFilter appFilter2;
        this.mFilters[0] = new AppFilterItem(new CompoundFilter(AppStatePowerBridge.FILTER_POWER_WHITELISTED, ApplicationsState.FILTER_ALL_ENABLED), 0, R.string.high_power_filter_on);
        this.mFilters[1] = new AppFilterItem(new CompoundFilter(ApplicationsState.FILTER_WITHOUT_DISABLED_UNTIL_USED, ApplicationsState.FILTER_ALL_ENABLED), 1, R.string.filter_all_apps);
        this.mFilters[2] = new AppFilterItem(ApplicationsState.FILTER_EVERYTHING, 2, R.string.filter_all_apps);
        this.mFilters[3] = new AppFilterItem(ApplicationsState.FILTER_ALL_ENABLED, 3, R.string.filter_enabled_apps);
        this.mFilters[5] = new AppFilterItem(ApplicationsState.FILTER_DISABLED, 5, R.string.filter_apps_disabled);
        this.mFilters[4] = new AppFilterItem(ApplicationsState.FILTER_INSTANT, 4, R.string.filter_instant_apps);
        this.mFilters[6] = new AppFilterItem(AppStateNotificationBridge.FILTER_APP_NOTIFICATION_RECENCY, 6, R.string.sort_order_recent_notification);
        this.mFilters[7] = new AppFilterItem(AppStateNotificationBridge.FILTER_APP_NOTIFICATION_FREQUENCY, 7, R.string.sort_order_frequent_notification);
        this.mFilters[8] = new AppFilterItem(ApplicationsState.FILTER_PERSONAL, 8, R.string.filter_personal_apps);
        this.mFilters[9] = new AppFilterItem(ApplicationsState.FILTER_WORK, 9, R.string.filter_work_apps);
        this.mFilters[10] = new AppFilterItem(AppStateUsageBridge.FILTER_APP_USAGE, 10, R.string.filter_all_apps);
        this.mFilters[11] = new AppFilterItem(AppStateOverlayBridge.FILTER_SYSTEM_ALERT_WINDOW, 11, R.string.filter_overlay_apps);
        this.mFilters[12] = new AppFilterItem(AppStateWriteSettingsBridge.FILTER_WRITE_SETTINGS, 12, R.string.filter_write_settings_apps);
        this.mFilters[13] = new AppFilterItem(AppStateInstallAppsBridge.FILTER_APP_SOURCES, 13, R.string.filter_install_sources_apps);
        this.mFilters[14] = new AppFilterItem(AppStateDirectoryAccessBridge.FILTER_APP_HAS_DIRECTORY_ACCESS, 14, R.string.filter_install_sources_apps);
        this.mFilters[15] = new AppFilterItem(AppStateChangeWifiStateBridge.FILTER_CHANGE_WIFI_STATE, 15, R.string.filter_write_settings_apps);
        this.mFilters[16] = new AppFilterItem(DisplaySizeAdaptionBridge.FILTER_APP_All, 16, R.string.filter_all_apps);
        AppFilterItem[] appFilterItemArr = this.mFilters;
        if (OPUtils.isSupportScreenCutting()) {
            appFilter = DisplaySizeAdaptionBridge.FILTER_APP_DEFAULT;
        } else {
            appFilter = DisplaySizeAdaptionBridge.FILTER_APP_FULL_SCREEN;
        }
        appFilterItemArr[17] = new AppFilterItem(appFilter, 17, R.string.oneplus_app_display_fullscreen);
        appFilterItemArr = this.mFilters;
        if (OPUtils.isSupportScreenCutting()) {
            appFilter2 = DisplaySizeAdaptionBridge.FILTER_APP_FULL_SCREEN;
        } else {
            appFilter2 = DisplaySizeAdaptionBridge.FILTER_APP_ORIGINAL_SIZE;
        }
        appFilterItemArr[18] = new AppFilterItem(appFilter2, 18, !OPUtils.isSupportScreenCutting() ? R.string.oneplus_app_display_compatibility : R.string.oneplus_screen_color_mode_default);
        this.mFilters[19] = new AppFilterItem(AppBgOptimizeBridge.FILTER_APP_BG_All, 19, R.string.filter_all_apps);
        this.mFilters[20] = new AppFilterItem(AppBgOptimizeBridge.FILTER_APP_BG_NOT_OPTIMIZE, 20, R.string.not_optimized_apps);
    }

    public static AppFilterRegistry getInstance() {
        if (sRegistry == null) {
            sRegistry = new AppFilterRegistry();
        }
        return sRegistry;
    }

    @FilterType
    public int getDefaultFilterType(int listType) {
        if (listType == 1) {
            return 6;
        }
        switch (listType) {
            case 4:
                return 10;
            case 5:
                return 0;
            case 6:
                return 11;
            case 7:
                return 12;
            case 8:
                return 13;
            default:
                switch (listType) {
                    case 12:
                        return 14;
                    case 13:
                        return 15;
                    default:
                        return 2;
                }
        }
    }

    public AppFilterItem get(@FilterType int filterType) {
        return this.mFilters[filterType];
    }
}
