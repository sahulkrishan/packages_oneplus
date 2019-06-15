package com.android.settings.location;

import android.R;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.IconDrawableFactory;
import android.util.Log;
import android.util.Xml;
import com.android.settings.location.InjectedSetting.Builder;
import com.android.settings.search.IndexDatabaseHelper.IndexColumns;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.xmlpull.v1.XmlPullParserException;

class SettingsInjector {
    private static final long INJECTED_STATUS_UPDATE_TIMEOUT_MILLIS = 1000;
    static final String TAG = "SettingsInjector";
    private static final int WHAT_RECEIVED_STATUS = 2;
    private static final int WHAT_RELOAD = 1;
    private static final int WHAT_TIMEOUT = 3;
    private final Context mContext;
    private final Handler mHandler = new StatusLoadingHandler();
    private final Set<Setting> mSettings = new HashSet();

    private final class Setting {
        public final Preference preference;
        public final InjectedSetting setting;
        public long startMillis;

        private Setting(InjectedSetting setting, Preference preference) {
            this.setting = setting;
            this.preference = preference;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Setting{setting=");
            stringBuilder.append(this.setting);
            stringBuilder.append(", preference=");
            stringBuilder.append(this.preference);
            stringBuilder.append('}');
            return stringBuilder.toString();
        }

        public boolean equals(Object o) {
            return this == o || ((o instanceof Setting) && this.setting.equals(((Setting) o).setting));
        }

        public int hashCode() {
            return this.setting.hashCode();
        }

        public void startService() {
            if (((ActivityManager) SettingsInjector.this.mContext.getSystemService("activity")).isUserRunning(this.setting.mUserHandle.getIdentifier())) {
                Handler handler = new Handler() {
                    public void handleMessage(Message msg) {
                        Bundle bundle = msg.getData();
                        boolean enabled = bundle.getBoolean(IndexColumns.ENABLED, true);
                        if (Log.isLoggable(SettingsInjector.TAG, 3)) {
                            String str = SettingsInjector.TAG;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(Setting.this.setting);
                            stringBuilder.append(": received ");
                            stringBuilder.append(msg);
                            stringBuilder.append(", bundle: ");
                            stringBuilder.append(bundle);
                            Log.d(str, stringBuilder.toString());
                        }
                        Setting.this.preference.setSummary(null);
                        Setting.this.preference.setEnabled(enabled);
                        SettingsInjector.this.mHandler.sendMessage(SettingsInjector.this.mHandler.obtainMessage(2, Setting.this));
                    }
                };
                Messenger messenger = new Messenger(handler);
                Intent intent = this.setting.getServiceIntent();
                intent.putExtra("messenger", messenger);
                if (Log.isLoggable(SettingsInjector.TAG, 3)) {
                    String str = SettingsInjector.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(this.setting);
                    stringBuilder.append(": sending update intent: ");
                    stringBuilder.append(intent);
                    stringBuilder.append(", handler: ");
                    stringBuilder.append(handler);
                    Log.d(str, stringBuilder.toString());
                    this.startMillis = SystemClock.elapsedRealtime();
                } else {
                    this.startMillis = 0;
                }
                SettingsInjector.this.mContext.startServiceAsUser(intent, this.setting.mUserHandle);
                return;
            }
            if (Log.isLoggable(SettingsInjector.TAG, 2)) {
                String str2 = SettingsInjector.TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Cannot start service as user ");
                stringBuilder2.append(this.setting.mUserHandle.getIdentifier());
                stringBuilder2.append(" is not running");
                Log.v(str2, stringBuilder2.toString());
            }
        }

        public long getElapsedTime() {
            return SystemClock.elapsedRealtime() - this.startMillis;
        }

        public void maybeLogElapsedTime() {
            if (Log.isLoggable(SettingsInjector.TAG, 3) && this.startMillis != 0) {
                long elapsed = getElapsedTime();
                String str = SettingsInjector.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(this);
                stringBuilder.append(" update took ");
                stringBuilder.append(elapsed);
                stringBuilder.append(" millis");
                Log.d(str, stringBuilder.toString());
            }
        }
    }

    private final class StatusLoadingHandler extends Handler {
        private boolean mReloadRequested;
        private Set<Setting> mSettingsBeingLoaded;
        private Set<Setting> mSettingsToLoad;
        private Set<Setting> mTimedOutSettings;

        private StatusLoadingHandler() {
            super(Looper.getMainLooper());
            this.mSettingsToLoad = new HashSet();
            this.mSettingsBeingLoaded = new HashSet();
            this.mTimedOutSettings = new HashSet();
        }

        public void handleMessage(Message msg) {
            String str;
            StringBuilder stringBuilder;
            StringBuilder stringBuilder2;
            if (Log.isLoggable(SettingsInjector.TAG, 3)) {
                str = SettingsInjector.TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("handleMessage start: ");
                stringBuilder.append(msg);
                stringBuilder.append(", ");
                stringBuilder.append(this);
                Log.d(str, stringBuilder.toString());
            }
            Setting receivedSetting;
            switch (msg.what) {
                case 1:
                    this.mReloadRequested = true;
                    break;
                case 2:
                    receivedSetting = msg.obj;
                    receivedSetting.maybeLogElapsedTime();
                    this.mSettingsBeingLoaded.remove(receivedSetting);
                    this.mTimedOutSettings.remove(receivedSetting);
                    removeMessages(3, receivedSetting);
                    break;
                case 3:
                    receivedSetting = msg.obj;
                    this.mSettingsBeingLoaded.remove(receivedSetting);
                    this.mTimedOutSettings.add(receivedSetting);
                    if (Log.isLoggable(SettingsInjector.TAG, 5)) {
                        String str2 = SettingsInjector.TAG;
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("Timed out after ");
                        stringBuilder2.append(receivedSetting.getElapsedTime());
                        stringBuilder2.append(" millis trying to get status for: ");
                        stringBuilder2.append(receivedSetting);
                        Log.w(str2, stringBuilder2.toString());
                        break;
                    }
                    break;
                default:
                    str = SettingsInjector.TAG;
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("Unexpected what: ");
                    stringBuilder3.append(msg);
                    Log.wtf(str, stringBuilder3.toString());
                    break;
            }
            if (this.mSettingsBeingLoaded.size() > 0 || this.mTimedOutSettings.size() > 1) {
                if (Log.isLoggable(SettingsInjector.TAG, 2)) {
                    str = SettingsInjector.TAG;
                    StringBuilder stringBuilder4 = new StringBuilder();
                    stringBuilder4.append("too many services already live for ");
                    stringBuilder4.append(msg);
                    stringBuilder4.append(", ");
                    stringBuilder4.append(this);
                    Log.v(str, stringBuilder4.toString());
                }
                return;
            }
            if (this.mReloadRequested && this.mSettingsToLoad.isEmpty() && this.mSettingsBeingLoaded.isEmpty() && this.mTimedOutSettings.isEmpty()) {
                if (Log.isLoggable(SettingsInjector.TAG, 2)) {
                    str = SettingsInjector.TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("reloading because idle and reload requesteed ");
                    stringBuilder.append(msg);
                    stringBuilder.append(", ");
                    stringBuilder.append(this);
                    Log.v(str, stringBuilder.toString());
                }
                this.mSettingsToLoad.addAll(SettingsInjector.this.mSettings);
                this.mReloadRequested = false;
            }
            Iterator<Setting> iter = this.mSettingsToLoad.iterator();
            String str3;
            if (iter.hasNext()) {
                Setting setting = (Setting) iter.next();
                iter.remove();
                setting.startService();
                this.mSettingsBeingLoaded.add(setting);
                sendMessageDelayed(obtainMessage(3, setting), 1000);
                if (Log.isLoggable(SettingsInjector.TAG, 3)) {
                    str3 = SettingsInjector.TAG;
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("handleMessage end ");
                    stringBuilder2.append(msg);
                    stringBuilder2.append(", ");
                    stringBuilder2.append(this);
                    stringBuilder2.append(", started loading ");
                    stringBuilder2.append(setting);
                    Log.d(str3, stringBuilder2.toString());
                }
                return;
            }
            if (Log.isLoggable(SettingsInjector.TAG, 2)) {
                str3 = SettingsInjector.TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("nothing left to do for ");
                stringBuilder.append(msg);
                stringBuilder.append(", ");
                stringBuilder.append(this);
                Log.v(str3, stringBuilder.toString());
            }
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("StatusLoadingHandler{mSettingsToLoad=");
            stringBuilder.append(this.mSettingsToLoad);
            stringBuilder.append(", mSettingsBeingLoaded=");
            stringBuilder.append(this.mSettingsBeingLoaded);
            stringBuilder.append(", mTimedOutSettings=");
            stringBuilder.append(this.mTimedOutSettings);
            stringBuilder.append(", mReloadRequested=");
            stringBuilder.append(this.mReloadRequested);
            stringBuilder.append('}');
            return stringBuilder.toString();
        }
    }

    private class ServiceSettingClickedListener implements OnPreferenceClickListener {
        private InjectedSetting mInfo;

        public ServiceSettingClickedListener(InjectedSetting info) {
            this.mInfo = info;
        }

        public boolean onPreferenceClick(Preference preference) {
            Intent settingIntent = new Intent();
            settingIntent.setClassName(this.mInfo.packageName, this.mInfo.settingsActivity);
            settingIntent.setFlags(268468224);
            SettingsInjector.this.mContext.startActivityAsUser(settingIntent, this.mInfo.mUserHandle);
            return true;
        }
    }

    public SettingsInjector(Context context) {
        this.mContext = context;
    }

    private List<InjectedSetting> getSettings(UserHandle userHandle) {
        StringBuilder stringBuilder;
        PackageManager pm = this.mContext.getPackageManager();
        Intent intent = new Intent("android.location.SettingInjectorService");
        int profileId = userHandle.getIdentifier();
        List<ResolveInfo> resolveInfos = pm.queryIntentServicesAsUser(intent, 128, profileId);
        if (Log.isLoggable(TAG, 3)) {
            String str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Found services for profile id ");
            stringBuilder.append(profileId);
            stringBuilder.append(": ");
            stringBuilder.append(resolveInfos);
            Log.d(str, stringBuilder.toString());
        }
        List<InjectedSetting> settings = new ArrayList(resolveInfos.size());
        for (ResolveInfo resolveInfo : resolveInfos) {
            String str2;
            StringBuilder stringBuilder2;
            try {
                InjectedSetting setting = parseServiceInfo(resolveInfo, userHandle, pm);
                if (setting == null) {
                    str2 = TAG;
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Unable to load service info ");
                    stringBuilder2.append(resolveInfo);
                    Log.w(str2, stringBuilder2.toString());
                } else {
                    settings.add(setting);
                }
            } catch (XmlPullParserException e) {
                str2 = TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Unable to load service info ");
                stringBuilder2.append(resolveInfo);
                Log.w(str2, stringBuilder2.toString(), e);
            } catch (IOException e2) {
                str2 = TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Unable to load service info ");
                stringBuilder2.append(resolveInfo);
                Log.w(str2, stringBuilder2.toString(), e2);
            }
        }
        if (Log.isLoggable(TAG, 3)) {
            String str3 = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Loaded settings for profile id ");
            stringBuilder.append(profileId);
            stringBuilder.append(": ");
            stringBuilder.append(settings);
            Log.d(str3, stringBuilder.toString());
        }
        return settings;
    }

    private InjectedSetting parseServiceInfo(ResolveInfo service, UserHandle userHandle, PackageManager pm) throws XmlPullParserException, IOException {
        ServiceInfo si = service.serviceInfo;
        if ((si.applicationInfo.flags & 1) == 0) {
            if (Log.isLoggable(TAG, 5)) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Ignoring attempt to inject setting from app not in system image: ");
                stringBuilder.append(service);
                Log.w(str, stringBuilder.toString());
                return null;
            }
        } else if (!DimmableIZatIconPreference.showIzat(this.mContext, si.packageName)) {
            return null;
        }
        XmlResourceParser parser = null;
        try {
            parser = si.loadXmlMetaData(pm, "android.location.SettingInjectorService");
            if (parser != null) {
                AttributeSet attrs = Xml.asAttributeSet(parser);
                while (true) {
                    int next = parser.next();
                    int type = next;
                    if (next == 1 || type == 2) {
                    }
                }
                if ("injected-location-setting".equals(parser.getName())) {
                    InjectedSetting parseAttributes = parseAttributes(si.packageName, si.name, userHandle, pm.getResourcesForApplicationAsUser(si.packageName, userHandle.getIdentifier()), attrs);
                    if (parser != null) {
                        parser.close();
                    }
                    return parseAttributes;
                }
                throw new XmlPullParserException("Meta-data does not start with injected-location-setting tag");
            }
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("No android.location.SettingInjectorService meta-data for ");
            stringBuilder2.append(service);
            stringBuilder2.append(": ");
            stringBuilder2.append(si);
            throw new XmlPullParserException(stringBuilder2.toString());
        } catch (NameNotFoundException e) {
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("Unable to load resources for package ");
            stringBuilder3.append(si.packageName);
            throw new XmlPullParserException(stringBuilder3.toString());
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
        }
    }

    private static InjectedSetting parseAttributes(String packageName, String className, UserHandle userHandle, Resources res, AttributeSet attrs) {
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.SettingInjectorService);
        try {
            String title = sa.getString(1);
            int iconId = sa.getResourceId(0, 0);
            String settingsActivity = sa.getString(2);
            String userRestriction = sa.getString(3);
            if (Log.isLoggable(TAG, 3)) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("parsed title: ");
                stringBuilder.append(title);
                stringBuilder.append(", iconId: ");
                stringBuilder.append(iconId);
                stringBuilder.append(", settingsActivity: ");
                stringBuilder.append(settingsActivity);
                Log.d(str, stringBuilder.toString());
            }
            InjectedSetting build = new Builder().setPackageName(packageName).setClassName(className).setTitle(title).setIconId(iconId).setUserHandle(userHandle).setSettingsActivity(settingsActivity).setUserRestriction(userRestriction).build();
            return build;
        } finally {
            sa.recycle();
        }
    }

    public List<Preference> getInjectedSettings(Context prefContext, int profileId) {
        Context context;
        int i = profileId;
        List<UserHandle> profiles = ((UserManager) this.mContext.getSystemService("user")).getUserProfiles();
        ArrayList<Preference> prefs = new ArrayList();
        int profileCount = profiles.size();
        int i2 = 0;
        while (i2 < profileCount) {
            UserHandle userHandle = (UserHandle) profiles.get(i2);
            if (userHandle.getIdentifier() != 999 && (i == -2 || i == userHandle.getIdentifier())) {
                for (InjectedSetting setting : getSettings(userHandle)) {
                    this.mSettings.add(new Setting(setting, addServiceSetting(prefContext, prefs, setting)));
                    i = profileId;
                }
            }
            context = prefContext;
            i2++;
            i = profileId;
        }
        context = prefContext;
        reloadStatusMessages();
        return prefs;
    }

    public boolean hasInjectedSettings(int profileId) {
        List<UserHandle> profiles = ((UserManager) this.mContext.getSystemService("user")).getUserProfiles();
        int profileCount = profiles.size();
        for (int i = 0; i < profileCount; i++) {
            UserHandle userHandle = (UserHandle) profiles.get(i);
            if (profileId == -2 || profileId == userHandle.getIdentifier()) {
                Iterator it = getSettings(userHandle).iterator();
                if (it.hasNext()) {
                    it.next();
                    return true;
                }
            }
        }
        return false;
    }

    public void reloadStatusMessages() {
        if (Log.isLoggable(TAG, 3)) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("reloadingStatusMessages: ");
            stringBuilder.append(this.mSettings);
            Log.d(str, stringBuilder.toString());
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
    }

    private Preference addServiceSetting(Context prefContext, List<Preference> prefs, InjectedSetting info) {
        Preference pref;
        PackageManager pm = this.mContext.getPackageManager();
        Drawable appIcon = null;
        try {
            PackageItemInfo itemInfo = new PackageItemInfo();
            itemInfo.icon = info.iconId;
            itemInfo.packageName = info.packageName;
            appIcon = IconDrawableFactory.newInstance(this.mContext).getBadgedIcon(itemInfo, pm.getApplicationInfo(info.packageName, 128), info.mUserHandle.getIdentifier());
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Can't get ApplicationInfo for ");
            stringBuilder.append(info.packageName);
            Log.e(str, stringBuilder.toString(), e);
        }
        if (TextUtils.isEmpty(info.userRestriction)) {
            pref = DimmableIZatIconPreference.getAppPreference(prefContext, info);
        } else {
            pref = DimmableIZatIconPreference.getRestrictedAppPreference(prefContext, info);
        }
        pref.setTitle(info.title);
        pref.setSummary(null);
        pref.setIcon(appIcon);
        pref.setOnPreferenceClickListener(new ServiceSettingClickedListener(info));
        prefs.add(pref);
        return pref;
    }
}
