package com.android.settings.utils;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.util.ArraySet;
import android.util.Slog;
import com.android.settings.utils.ManagedServiceSettings.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ZenServiceListing {
    private final Set<ServiceInfo> mApprovedServices = new ArraySet();
    private final Config mConfig;
    private final Context mContext;
    private final NotificationManager mNm;
    private final List<Callback> mZenCallbacks = new ArrayList();

    public interface Callback {
        void onServicesReloaded(Set<ServiceInfo> set);
    }

    public ZenServiceListing(Context context, Config config) {
        this.mContext = context;
        this.mConfig = config;
        this.mNm = (NotificationManager) context.getSystemService("notification");
    }

    public ServiceInfo findService(ComponentName cn) {
        for (ServiceInfo service : this.mApprovedServices) {
            if (new ComponentName(service.packageName, service.name).equals(cn)) {
                return service;
            }
        }
        return null;
    }

    public void addZenCallback(Callback callback) {
        this.mZenCallbacks.add(callback);
    }

    public void removeZenCallback(Callback callback) {
        this.mZenCallbacks.remove(callback);
    }

    public void reloadApprovedServices() {
        this.mApprovedServices.clear();
        List<String> enabledNotificationListenerPkgs = this.mNm.getEnabledNotificationListenerPackages();
        List<ServiceInfo> services = new ArrayList();
        getServices(this.mConfig, services, this.mContext.getPackageManager());
        for (ServiceInfo service : services) {
            String servicePackage = service.getComponentName().getPackageName();
            if (this.mNm.isNotificationPolicyAccessGrantedForPackage(servicePackage) || enabledNotificationListenerPkgs.contains(servicePackage)) {
                this.mApprovedServices.add(service);
            }
        }
        if (!this.mApprovedServices.isEmpty()) {
            for (Callback callback : this.mZenCallbacks) {
                callback.onServicesReloaded(this.mApprovedServices);
            }
        }
    }

    private static int getServices(Config c, List<ServiceInfo> list, PackageManager pm) {
        int services = 0;
        if (list != null) {
            list.clear();
        }
        List<ResolveInfo> installedServices = pm.queryIntentServicesAsUser(new Intent(c.intentAction), Const.CODE_C1_CW4, ActivityManager.getCurrentUser());
        int count = installedServices.size();
        for (int i = 0; i < count; i++) {
            ServiceInfo info = ((ResolveInfo) installedServices.get(i)).serviceInfo;
            if (c.permission.equals(info.permission)) {
                if (list != null) {
                    list.add(info);
                }
                services++;
            } else {
                String str = c.tag;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Skipping ");
                stringBuilder.append(c.noun);
                stringBuilder.append(" service ");
                stringBuilder.append(info.packageName);
                stringBuilder.append("/");
                stringBuilder.append(info.name);
                stringBuilder.append(": it does not require the permission ");
                stringBuilder.append(c.permission);
                Slog.w(str, stringBuilder.toString());
            }
        }
        return services;
    }
}
