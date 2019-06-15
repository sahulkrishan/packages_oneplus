package com.android.settings.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Process;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.Log;
import android.widget.RemoteViews;
import com.android.settings.DisplaySettings;
import com.android.settings.R;
import com.android.settings.bluetooth.Utils;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class SettingsAppWidgetProvider extends AppWidgetProvider {
    private static final int BUTTON_BLUETOOTH = 4;
    private static final int BUTTON_BRIGHTNESS = 1;
    private static final int BUTTON_LOCATION = 3;
    private static final int BUTTON_SYNC = 2;
    private static final int BUTTON_WIFI = 0;
    private static final float FULL_BRIGHTNESS_THRESHOLD = 0.8f;
    private static final float HALF_BRIGHTNESS_THRESHOLD = 0.3f;
    private static final int[] IND_DRAWABLE_MID = new int[]{R.drawable.appwidget_settings_ind_mid_l_holo, R.drawable.appwidget_settings_ind_mid_c_holo, R.drawable.appwidget_settings_ind_mid_r_holo};
    private static final int[] IND_DRAWABLE_OFF = new int[]{R.drawable.appwidget_settings_ind_off_l_holo, R.drawable.appwidget_settings_ind_off_c_holo, R.drawable.appwidget_settings_ind_off_r_holo};
    private static final int[] IND_DRAWABLE_ON = new int[]{R.drawable.appwidget_settings_ind_on_l_holo, R.drawable.appwidget_settings_ind_on_c_holo, R.drawable.appwidget_settings_ind_on_r_holo};
    private static final int POS_CENTER = 1;
    private static final int POS_LEFT = 0;
    private static final int POS_RIGHT = 2;
    private static final int STATE_DISABLED = 0;
    private static final int STATE_ENABLED = 1;
    private static final int STATE_INTERMEDIATE = 5;
    private static final int STATE_TURNING_OFF = 3;
    private static final int STATE_TURNING_ON = 2;
    private static final int STATE_UNKNOWN = 4;
    static final String TAG = "SettingsAppWidgetProvider";
    static final ComponentName THIS_APPWIDGET = new ComponentName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
    private static final StateTracker sBluetoothState = new BluetoothStateTracker();
    private static LocalBluetoothAdapter sLocalBluetoothAdapter = null;
    private static final StateTracker sLocationState = new LocationStateTracker();
    private static SettingsObserver sSettingsObserver;
    private static final StateTracker sSyncState = new SyncStateTracker();
    private static final StateTracker sWifiState = new WifiStateTracker();

    private static class SettingsObserver extends ContentObserver {
        private Context mContext;

        SettingsObserver(Handler handler, Context context) {
            super(handler);
            this.mContext = context;
        }

        /* Access modifiers changed, original: 0000 */
        public void startObserving() {
            ContentResolver resolver = this.mContext.getContentResolver();
            resolver.registerContentObserver(System.getUriFor("screen_brightness"), false, this);
            resolver.registerContentObserver(System.getUriFor("screen_brightness_mode"), false, this);
            resolver.registerContentObserver(System.getUriFor(DisplaySettings.SCREEN_AUTO_BRIGHTNESS_ADJ), false, this);
        }

        /* Access modifiers changed, original: 0000 */
        public void stopObserving() {
            this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange) {
            SettingsAppWidgetProvider.updateWidget(this.mContext);
        }
    }

    private static abstract class StateTracker {
        private Boolean mActualState;
        private boolean mDeferredStateChangeRequestNeeded;
        private boolean mInTransition;
        private Boolean mIntendedState;

        public abstract int getActualState(Context context);

        public abstract int getButtonDescription();

        public abstract int getButtonId();

        public abstract int getButtonImageId(boolean z);

        public abstract int getContainerId();

        public abstract int getIndicatorId();

        public abstract void onActualStateChange(Context context, Intent intent);

        public abstract void requestStateChange(Context context, boolean z);

        private StateTracker() {
            this.mInTransition = false;
            this.mActualState = null;
            this.mIntendedState = null;
            this.mDeferredStateChangeRequestNeeded = false;
        }

        public final void toggleState(Context context) {
            int currentState = getTriState(context);
            boolean newState = false;
            if (currentState != 5) {
                switch (currentState) {
                    case 0:
                        newState = true;
                        break;
                    case 1:
                        newState = false;
                        break;
                }
            } else if (this.mIntendedState != null) {
                newState = this.mIntendedState.booleanValue() ^ 1;
            }
            this.mIntendedState = Boolean.valueOf(newState);
            if (this.mInTransition) {
                this.mDeferredStateChangeRequestNeeded = true;
                return;
            }
            this.mInTransition = true;
            requestStateChange(context, newState);
        }

        public int getPosition() {
            return 1;
        }

        public final void setImageViewResources(Context context, RemoteViews views) {
            int containerId = getContainerId();
            int buttonId = getButtonId();
            int indicatorId = getIndicatorId();
            int pos = getPosition();
            int triState = getTriState(context);
            if (triState != 5) {
                switch (triState) {
                    case 0:
                        views.setContentDescription(containerId, getContentDescription(context, R.string.gadget_state_off));
                        views.setImageViewResource(buttonId, getButtonImageId(false));
                        views.setImageViewResource(indicatorId, SettingsAppWidgetProvider.IND_DRAWABLE_OFF[pos]);
                        return;
                    case 1:
                        views.setContentDescription(containerId, getContentDescription(context, R.string.gadget_state_on));
                        views.setImageViewResource(buttonId, getButtonImageId(true));
                        views.setImageViewResource(indicatorId, SettingsAppWidgetProvider.IND_DRAWABLE_ON[pos]);
                        return;
                    default:
                        return;
                }
            } else if (isTurningOn()) {
                views.setContentDescription(containerId, getContentDescription(context, R.string.gadget_state_turning_on));
                views.setImageViewResource(buttonId, getButtonImageId(true));
                views.setImageViewResource(indicatorId, SettingsAppWidgetProvider.IND_DRAWABLE_MID[pos]);
            } else {
                views.setContentDescription(containerId, getContentDescription(context, R.string.gadget_state_turning_off));
                views.setImageViewResource(buttonId, getButtonImageId(false));
                views.setImageViewResource(indicatorId, SettingsAppWidgetProvider.IND_DRAWABLE_OFF[pos]);
            }
        }

        private final String getContentDescription(Context context, int stateResId) {
            String gadget = context.getString(getButtonDescription());
            String state = context.getString(stateResId);
            return context.getString(R.string.gadget_state_template, new Object[]{gadget, state});
        }

        /* Access modifiers changed, original: protected|final */
        public final void setCurrentState(Context context, int newState) {
            boolean wasInTransition = this.mInTransition;
            switch (newState) {
                case 0:
                    this.mInTransition = false;
                    this.mActualState = Boolean.valueOf(false);
                    break;
                case 1:
                    this.mInTransition = false;
                    this.mActualState = Boolean.valueOf(true);
                    break;
                case 2:
                    this.mInTransition = true;
                    this.mActualState = Boolean.valueOf(false);
                    break;
                case 3:
                    this.mInTransition = true;
                    this.mActualState = Boolean.valueOf(true);
                    break;
            }
            if (wasInTransition && !this.mInTransition && this.mDeferredStateChangeRequestNeeded) {
                Log.v(SettingsAppWidgetProvider.TAG, "processing deferred state change");
                if (this.mActualState != null && this.mIntendedState != null && this.mIntendedState.equals(this.mActualState)) {
                    Log.v(SettingsAppWidgetProvider.TAG, "... but intended state matches, so no changes.");
                } else if (this.mIntendedState != null) {
                    this.mInTransition = true;
                    requestStateChange(context, this.mIntendedState.booleanValue());
                }
                this.mDeferredStateChangeRequestNeeded = false;
            }
        }

        public final boolean isTurningOn() {
            return this.mIntendedState != null && this.mIntendedState.booleanValue();
        }

        public final int getTriState(Context context) {
            if (this.mInTransition) {
                return 5;
            }
            switch (getActualState(context)) {
                case 0:
                    return 0;
                case 1:
                    return 1;
                default:
                    return 5;
            }
        }
    }

    private static final class BluetoothStateTracker extends StateTracker {
        private BluetoothStateTracker() {
            super();
        }

        public int getContainerId() {
            return R.id.btn_bluetooth;
        }

        public int getButtonId() {
            return R.id.img_bluetooth;
        }

        public int getIndicatorId() {
            return R.id.ind_bluetooth;
        }

        public int getButtonDescription() {
            return R.string.gadget_bluetooth;
        }

        public int getButtonImageId(boolean on) {
            if (on) {
                return R.drawable.ic_appwidget_settings_bluetooth_on_holo;
            }
            return R.drawable.ic_appwidget_settings_bluetooth_off_holo;
        }

        public int getActualState(Context context) {
            if (SettingsAppWidgetProvider.sLocalBluetoothAdapter == null) {
                LocalBluetoothManager manager = Utils.getLocalBtManager(context);
                if (manager == null) {
                    return 4;
                }
                SettingsAppWidgetProvider.sLocalBluetoothAdapter = manager.getBluetoothAdapter();
                if (SettingsAppWidgetProvider.sLocalBluetoothAdapter == null) {
                    return 4;
                }
            }
            return bluetoothStateToFiveState(SettingsAppWidgetProvider.sLocalBluetoothAdapter.getBluetoothState());
        }

        /* Access modifiers changed, original: protected */
        public void requestStateChange(Context context, final boolean desiredState) {
            if (SettingsAppWidgetProvider.sLocalBluetoothAdapter == null) {
                Log.d(SettingsAppWidgetProvider.TAG, "No LocalBluetoothManager");
            } else {
                new AsyncTask<Void, Void, Void>() {
                    /* Access modifiers changed, original: protected|varargs */
                    public Void doInBackground(Void... args) {
                        SettingsAppWidgetProvider.sLocalBluetoothAdapter.setBluetoothEnabled(desiredState);
                        return null;
                    }
                }.execute(new Void[0]);
            }
        }

        public void onActualStateChange(Context context, Intent intent) {
            if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                setCurrentState(context, bluetoothStateToFiveState(intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1)));
            }
        }

        private static int bluetoothStateToFiveState(int bluetoothState) {
            switch (bluetoothState) {
                case 10:
                    return 0;
                case 11:
                    return 2;
                case 12:
                    return 1;
                case 13:
                    return 3;
                default:
                    return 4;
            }
        }
    }

    private static final class LocationStateTracker extends StateTracker {
        private int mCurrentLocationMode;

        private LocationStateTracker() {
            super();
            this.mCurrentLocationMode = 0;
        }

        public int getContainerId() {
            return R.id.btn_location;
        }

        public int getButtonId() {
            return R.id.img_location;
        }

        public int getIndicatorId() {
            return R.id.ind_location;
        }

        public int getButtonDescription() {
            return R.string.gadget_location;
        }

        public int getButtonImageId(boolean on) {
            if (!on) {
                return R.drawable.ic_appwidget_settings_location_off_holo;
            }
            int i = this.mCurrentLocationMode;
            if (i == 1 || i == 3) {
                return R.drawable.ic_appwidget_settings_location_on_holo;
            }
            return R.drawable.ic_appwidget_settings_location_saving_holo;
        }

        public int getActualState(Context context) {
            this.mCurrentLocationMode = Secure.getInt(context.getContentResolver(), "location_mode", 0);
            if (this.mCurrentLocationMode == 0) {
                return 0;
            }
            return 1;
        }

        public void onActualStateChange(Context context, Intent unused) {
            setCurrentState(context, getActualState(context));
        }

        public void requestStateChange(final Context context, boolean desiredState) {
            ContentResolver resolver = context.getContentResolver();
            new AsyncTask<Void, Void, Boolean>() {
                /* Access modifiers changed, original: protected|varargs */
                public Boolean doInBackground(Void... args) {
                    if (((UserManager) context.getSystemService("user")).hasUserRestriction("no_share_location")) {
                        boolean z = true;
                        if (LocationStateTracker.this.getActualState(context) != 1) {
                            z = false;
                        }
                        return Boolean.valueOf(z);
                    }
                    LocationManager lm = (LocationManager) context.getSystemService("location");
                    lm.setLocationEnabledForUser(lm.isLocationEnabled() ^ 1, Process.myUserHandle());
                    return Boolean.valueOf(lm.isLocationEnabled());
                }

                /* Access modifiers changed, original: protected */
                public void onPostExecute(Boolean result) {
                    LocationStateTracker.this.setCurrentState(context, result.booleanValue());
                    SettingsAppWidgetProvider.updateWidget(context);
                }
            }.execute(new Void[0]);
        }
    }

    private static final class SyncStateTracker extends StateTracker {
        private SyncStateTracker() {
            super();
        }

        public int getContainerId() {
            return R.id.btn_sync;
        }

        public int getButtonId() {
            return R.id.img_sync;
        }

        public int getIndicatorId() {
            return R.id.ind_sync;
        }

        public int getButtonDescription() {
            return R.string.gadget_sync;
        }

        public int getButtonImageId(boolean on) {
            if (on) {
                return R.drawable.ic_appwidget_settings_sync_on_holo;
            }
            return R.drawable.ic_appwidget_settings_sync_off_holo;
        }

        public int getActualState(Context context) {
            return ContentResolver.getMasterSyncAutomatically();
        }

        public void onActualStateChange(Context context, Intent unused) {
            setCurrentState(context, getActualState(context));
        }

        public void requestStateChange(final Context context, final boolean desiredState) {
            context.getSystemService("connectivity");
            final boolean sync = ContentResolver.getMasterSyncAutomatically();
            new AsyncTask<Void, Void, Boolean>() {
                /* Access modifiers changed, original: protected|varargs */
                public Boolean doInBackground(Void... args) {
                    if (desiredState) {
                        if (!sync) {
                            ContentResolver.setMasterSyncAutomatically(true);
                        }
                        return Boolean.valueOf(true);
                    }
                    if (sync) {
                        ContentResolver.setMasterSyncAutomatically(false);
                    }
                    return Boolean.valueOf(false);
                }

                /* Access modifiers changed, original: protected */
                public void onPostExecute(Boolean result) {
                    SyncStateTracker.this.setCurrentState(context, result.booleanValue());
                    SettingsAppWidgetProvider.updateWidget(context);
                }
            }.execute(new Void[0]);
        }
    }

    private static final class WifiStateTracker extends StateTracker {
        private WifiStateTracker() {
            super();
        }

        public int getContainerId() {
            return R.id.btn_wifi;
        }

        public int getButtonId() {
            return R.id.img_wifi;
        }

        public int getIndicatorId() {
            return R.id.ind_wifi;
        }

        public int getButtonDescription() {
            return R.string.gadget_wifi;
        }

        public int getButtonImageId(boolean on) {
            if (on) {
                return R.drawable.ic_appwidget_settings_wifi_on_holo;
            }
            return R.drawable.ic_appwidget_settings_wifi_off_holo;
        }

        public int getPosition() {
            return 0;
        }

        public int getActualState(Context context) {
            WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
            if (wifiManager != null) {
                return wifiStateToFiveState(wifiManager.getWifiState());
            }
            return 4;
        }

        /* Access modifiers changed, original: protected */
        public void requestStateChange(Context context, final boolean desiredState) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
            if (wifiManager == null) {
                Log.d(SettingsAppWidgetProvider.TAG, "No wifiManager.");
            } else {
                new AsyncTask<Void, Void, Void>() {
                    /* Access modifiers changed, original: protected|varargs */
                    public Void doInBackground(Void... args) {
                        wifiManager.setWifiEnabled(desiredState);
                        return null;
                    }
                }.execute(new Void[0]);
            }
        }

        public void onActualStateChange(Context context, Intent intent) {
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) {
                setCurrentState(context, wifiStateToFiveState(intent.getIntExtra("wifi_state", -1)));
            }
        }

        private static int wifiStateToFiveState(int wifiState) {
            switch (wifiState) {
                case 0:
                    return 3;
                case 1:
                    return 0;
                case 2:
                    return 2;
                case 3:
                    return 1;
                default:
                    return 4;
            }
        }
    }

    private static void checkObserver(Context context) {
        if (sSettingsObserver == null) {
            sSettingsObserver = new SettingsObserver(new Handler(), context.getApplicationContext());
            sSettingsObserver.startObserving();
        }
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews view = buildUpdate(context);
        for (int updateAppWidget : appWidgetIds) {
            appWidgetManager.updateAppWidget(updateAppWidget, view);
        }
    }

    public void onEnabled(Context context) {
        checkObserver(context);
    }

    public void onDisabled(Context context) {
        if (sSettingsObserver != null) {
            sSettingsObserver.stopObserving();
            sSettingsObserver = null;
        }
    }

    static RemoteViews buildUpdate(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setOnClickPendingIntent(R.id.btn_wifi, getLaunchPendingIntent(context, 0));
        views.setOnClickPendingIntent(R.id.btn_brightness, getLaunchPendingIntent(context, 1));
        views.setOnClickPendingIntent(R.id.btn_sync, getLaunchPendingIntent(context, 2));
        views.setOnClickPendingIntent(R.id.btn_location, getLaunchPendingIntent(context, 3));
        views.setOnClickPendingIntent(R.id.btn_bluetooth, getLaunchPendingIntent(context, 4));
        updateButtons(views, context);
        return views;
    }

    public static void updateWidget(Context context) {
        AppWidgetManager.getInstance(context).updateAppWidget(THIS_APPWIDGET, buildUpdate(context));
        checkObserver(context);
    }

    private static void updateButtons(RemoteViews views, Context context) {
        sWifiState.setImageViewResources(context, views);
        sBluetoothState.setImageViewResources(context, views);
        sLocationState.setImageViewResources(context, views);
        sSyncState.setImageViewResources(context, views);
        if (getBrightnessMode(context)) {
            views.setContentDescription(R.id.btn_brightness, context.getString(R.string.gadget_brightness_template, new Object[]{context.getString(R.string.gadget_brightness_state_auto)}));
            views.setImageViewResource(R.id.img_brightness, R.drawable.ic_appwidget_settings_brightness_auto_holo);
            views.setImageViewResource(R.id.ind_brightness, R.drawable.appwidget_settings_ind_on_r_holo);
            return;
        }
        int brightness = getBrightness(context);
        PowerManager pm = (PowerManager) context.getSystemService(PowerManager.class);
        int half = (int) (((float) pm.getMaximumScreenBrightnessSetting()) * 1050253722);
        if (brightness > ((int) (((float) pm.getMaximumScreenBrightnessSetting()) * 1061997773))) {
            views.setContentDescription(R.id.btn_brightness, context.getString(R.string.gadget_brightness_template, new Object[]{context.getString(R.string.gadget_brightness_state_full)}));
            views.setImageViewResource(R.id.img_brightness, R.drawable.ic_appwidget_settings_brightness_full_holo);
        } else if (brightness > half) {
            views.setContentDescription(R.id.btn_brightness, context.getString(R.string.gadget_brightness_template, new Object[]{context.getString(R.string.gadget_brightness_state_half)}));
            views.setImageViewResource(R.id.img_brightness, R.drawable.ic_appwidget_settings_brightness_half_holo);
        } else {
            views.setContentDescription(R.id.btn_brightness, context.getString(R.string.gadget_brightness_template, new Object[]{context.getString(R.string.gadget_brightness_state_off)}));
            views.setImageViewResource(R.id.img_brightness, R.drawable.ic_appwidget_settings_brightness_off_holo);
        }
        if (brightness > half) {
            views.setImageViewResource(R.id.ind_brightness, R.drawable.appwidget_settings_ind_on_r_holo);
        } else {
            views.setImageViewResource(R.id.ind_brightness, R.drawable.appwidget_settings_ind_off_r_holo);
        }
    }

    private static PendingIntent getLaunchPendingIntent(Context context, int buttonId) {
        Intent launchIntent = new Intent();
        launchIntent.setClass(context, SettingsAppWidgetProvider.class);
        launchIntent.addCategory("android.intent.category.ALTERNATIVE");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("custom:");
        stringBuilder.append(buttonId);
        launchIntent.setData(Uri.parse(stringBuilder.toString()));
        return PendingIntent.getBroadcast(context, 0, launchIntent, 0);
    }

    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
            sWifiState.onActualStateChange(context, intent);
        } else if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
            sBluetoothState.onActualStateChange(context, intent);
        } else if ("android.location.MODE_CHANGED".equals(action)) {
            sLocationState.onActualStateChange(context, intent);
        } else if (ContentResolver.ACTION_SYNC_CONN_STATUS_CHANGED.equals(action)) {
            sSyncState.onActualStateChange(context, intent);
        } else if (intent.hasCategory("android.intent.category.ALTERNATIVE")) {
            int buttonId = Integer.parseInt(intent.getData().getSchemeSpecificPart());
            if (buttonId == 0) {
                sWifiState.toggleState(context);
            } else if (buttonId == 1) {
                toggleBrightness(context);
            } else if (buttonId == 2) {
                sSyncState.toggleState(context);
            } else if (buttonId == 3) {
                sLocationState.toggleState(context);
            } else if (buttonId == 4) {
                sBluetoothState.toggleState(context);
            }
        } else {
            return;
        }
        updateWidget(context);
    }

    private static int getBrightness(Context context) {
        try {
            return System.getInt(context.getContentResolver(), "screen_brightness");
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean getBrightnessMode(Context context) {
        boolean z = false;
        try {
            if (System.getInt(context.getContentResolver(), "screen_brightness_mode") == 1) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("getBrightnessMode: ");
            stringBuilder.append(e);
            Log.d(str, stringBuilder.toString());
            return false;
        }
    }

    private void toggleBrightness(Context context) {
        try {
            DisplayManager dm = (DisplayManager) context.getSystemService(DisplayManager.class);
            PowerManager pm = (PowerManager) context.getSystemService(PowerManager.class);
            ContentResolver cr = context.getContentResolver();
            int brightness = System.getInt(cr, "screen_brightness");
            int brightnessMode = 0;
            if (context.getResources().getBoolean(17956895)) {
                brightnessMode = System.getInt(cr, "screen_brightness_mode");
            }
            if (brightnessMode == 1) {
                brightness = pm.getMinimumScreenBrightnessSetting();
                brightnessMode = 0;
            } else if (brightness < pm.getDefaultScreenBrightnessSetting()) {
                brightness = pm.getDefaultScreenBrightnessSetting();
            } else if (brightness < pm.getMaximumScreenBrightnessSetting()) {
                brightness = pm.getMaximumScreenBrightnessSetting();
            } else {
                brightnessMode = 1;
                brightness = pm.getMinimumScreenBrightnessSetting();
            }
            if (context.getResources().getBoolean(17956895)) {
                System.putInt(context.getContentResolver(), "screen_brightness_mode", brightnessMode);
            } else {
                brightnessMode = 0;
            }
            if (brightnessMode == 0) {
                dm.setTemporaryBrightness(brightness);
                System.putInt(cr, "screen_brightness", brightness);
            }
        } catch (SettingNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("toggleBrightness: ");
            stringBuilder.append(e);
            Log.d(str, stringBuilder.toString());
        }
    }
}