package com.android.settingslib.applications;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.util.Slog;
import com.android.settingslib.accessibility.AccessibilityUtils;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class ServiceListing {
    private final List<Callback> mCallbacks;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private final HashSet<ComponentName> mEnabledServices;
    private final String mIntentAction;
    private boolean mListening;
    private final String mNoun;
    private final BroadcastReceiver mPackageReceiver;
    private final String mPermission;
    private final List<ServiceInfo> mServices;
    private final String mSetting;
    private final ContentObserver mSettingsObserver;
    private final String mTag;

    public static class Builder {
        private final Context mContext;
        private String mIntentAction;
        private String mNoun;
        private String mPermission;
        private String mSetting;
        private String mTag;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder setTag(String tag) {
            this.mTag = tag;
            return this;
        }

        public Builder setSetting(String setting) {
            this.mSetting = setting;
            return this;
        }

        public Builder setIntentAction(String intentAction) {
            this.mIntentAction = intentAction;
            return this;
        }

        public Builder setPermission(String permission) {
            this.mPermission = permission;
            return this;
        }

        public Builder setNoun(String noun) {
            this.mNoun = noun;
            return this;
        }

        public ServiceListing build() {
            return new ServiceListing(this.mContext, this.mTag, this.mSetting, this.mIntentAction, this.mPermission, this.mNoun, null);
        }
    }

    public interface Callback {
        void onServicesReloaded(List<ServiceInfo> list);
    }

    /* synthetic */ ServiceListing(Context x0, String x1, String x2, String x3, String x4, String x5, AnonymousClass1 x6) {
        this(x0, x1, x2, x3, x4, x5);
    }

    private ServiceListing(Context context, String tag, String setting, String intentAction, String permission, String noun) {
        this.mEnabledServices = new HashSet();
        this.mServices = new ArrayList();
        this.mCallbacks = new ArrayList();
        this.mSettingsObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange, Uri uri) {
                ServiceListing.this.reload();
            }
        };
        this.mPackageReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                ServiceListing.this.reload();
            }
        };
        this.mContentResolver = context.getContentResolver();
        this.mContext = context;
        this.mTag = tag;
        this.mSetting = setting;
        this.mIntentAction = intentAction;
        this.mPermission = permission;
        this.mNoun = noun;
    }

    public void addCallback(Callback callback) {
        this.mCallbacks.add(callback);
    }

    public void removeCallback(Callback callback) {
        this.mCallbacks.remove(callback);
    }

    public void setListening(boolean listening) {
        if (this.mListening != listening) {
            this.mListening = listening;
            if (this.mListening) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.PACKAGE_ADDED");
                filter.addAction("android.intent.action.PACKAGE_CHANGED");
                filter.addAction("android.intent.action.PACKAGE_REMOVED");
                filter.addAction("android.intent.action.PACKAGE_REPLACED");
                filter.addDataScheme("package");
                this.mContext.registerReceiver(this.mPackageReceiver, filter);
                this.mContentResolver.registerContentObserver(Secure.getUriFor(this.mSetting), false, this.mSettingsObserver);
            } else {
                this.mContext.unregisterReceiver(this.mPackageReceiver);
                this.mContentResolver.unregisterContentObserver(this.mSettingsObserver);
            }
        }
    }

    private void saveEnabledServices() {
        StringBuilder sb = null;
        Iterator it = this.mEnabledServices.iterator();
        while (it.hasNext()) {
            ComponentName cn = (ComponentName) it.next();
            if (sb == null) {
                sb = new StringBuilder();
            } else {
                sb.append(AccessibilityUtils.ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
            }
            sb.append(cn.flattenToString());
        }
        Secure.putString(this.mContentResolver, this.mSetting, sb != null ? sb.toString() : "");
    }

    private void loadEnabledServices() {
        this.mEnabledServices.clear();
        String flat = Secure.getString(this.mContentResolver, this.mSetting);
        if (flat != null && !"".equals(flat)) {
            for (String name : flat.split(":")) {
                ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    this.mEnabledServices.add(cn);
                }
            }
        }
    }

    public void reload() {
        loadEnabledServices();
        this.mServices.clear();
        for (ResolveInfo resolveInfo : new PackageManagerWrapper(this.mContext.getPackageManager()).queryIntentServicesAsUser(new Intent(this.mIntentAction), Const.CODE_C1_CW4, ActivityManager.getCurrentUser())) {
            ServiceInfo info = resolveInfo.serviceInfo;
            if (this.mPermission.equals(info.permission)) {
                this.mServices.add(info);
            } else {
                String str = this.mTag;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Skipping ");
                stringBuilder.append(this.mNoun);
                stringBuilder.append(" service ");
                stringBuilder.append(info.packageName);
                stringBuilder.append("/");
                stringBuilder.append(info.name);
                stringBuilder.append(": it does not require the permission ");
                stringBuilder.append(this.mPermission);
                Slog.w(str, stringBuilder.toString());
            }
        }
        for (Callback callback : this.mCallbacks) {
            callback.onServicesReloaded(this.mServices);
        }
    }

    public boolean isEnabled(ComponentName cn) {
        return this.mEnabledServices.contains(cn);
    }

    public void setEnabled(ComponentName cn, boolean enabled) {
        if (enabled) {
            this.mEnabledServices.add(cn);
        } else {
            this.mEnabledServices.remove(cn);
        }
        saveEnabledServices();
    }
}
