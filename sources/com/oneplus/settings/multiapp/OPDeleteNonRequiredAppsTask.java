package com.oneplus.settings.multiapp;

import android.app.AppGlobals;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver.Stub;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Xml;
import android.view.inputmethod.InputMethodInfo;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.view.IInputMethodManager;
import com.android.settings.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class OPDeleteNonRequiredAppsTask {
    private static final String ATTR_VALUE = "value";
    public static final int DEVICE_OWNER = 0;
    public static final int MANAGED_USER = 2;
    public static final int PROFILE_OWNER = 1;
    public static final String TAG = "DeleteNonRequiredAppsTask";
    private static final String TAG_PACKAGE_LIST_ITEM = "item";
    private static final String TAG_SYSTEM_APPS = "system-apps";
    private final Callback mCallback;
    private final Context mContext;
    private final List<String> mDisallowedAppsList;
    private final IInputMethodManager mIInputMethodManager;
    private final IPackageManager mIPackageManager;
    private final boolean mLeaveAllSystemAppsEnabled;
    private final String mMdmPackageName;
    private final boolean mNewProfile;
    private final PackageManager mPm;
    private final int mProvisioningType;
    private final List<String> mRequiredAppsList;
    private final int mUserId;
    private final List<String> mVendorDisallowedAppsList;
    private final List<String> mVendorRequiredAppsList;

    public static abstract class Callback {
        public abstract void onError();

        public abstract void onSuccess();
    }

    class PackageDeleteObserver extends Stub {
        private final AtomicInteger mPackageCount = new AtomicInteger(0);

        public PackageDeleteObserver(int packageCount) {
            this.mPackageCount.set(packageCount);
        }

        public void packageDeleted(String packageName, int returnCode) {
            if (returnCode != 1) {
                Log.e(OPDeleteNonRequiredAppsTask.TAG, "Could not finish the provisioning: package deletion failed");
                OPDeleteNonRequiredAppsTask.this.mCallback.onError();
                return;
            }
            if (this.mPackageCount.decrementAndGet() == 0) {
                Log.e(OPDeleteNonRequiredAppsTask.TAG, "All non-required system apps with launcher icon, and all disallowed apps have been uninstalled.");
                OPDeleteNonRequiredAppsTask.this.mCallback.onSuccess();
            }
        }
    }

    public OPDeleteNonRequiredAppsTask(Context context, String mdmPackageName, int provisioningType, boolean newProfile, int userId, boolean leaveAllSystemAppsEnabled, Callback callback) {
        this(context, AppGlobals.getPackageManager(), getIInputMethodManager(), mdmPackageName, provisioningType, newProfile, userId, leaveAllSystemAppsEnabled, callback);
    }

    @VisibleForTesting
    OPDeleteNonRequiredAppsTask(Context context, IPackageManager iPm, IInputMethodManager iimm, String mdmPackageName, int provisioningType, boolean newProfile, int userId, boolean leaveAllSystemAppsEnabled, Callback callback) {
        this.mCallback = callback;
        this.mContext = context;
        this.mMdmPackageName = mdmPackageName;
        this.mProvisioningType = provisioningType;
        this.mUserId = userId;
        this.mNewProfile = newProfile;
        this.mLeaveAllSystemAppsEnabled = leaveAllSystemAppsEnabled;
        this.mPm = context.getPackageManager();
        this.mIPackageManager = iPm;
        this.mIInputMethodManager = iimm;
        Resources resources = this.mContext.getResources();
        this.mRequiredAppsList = Arrays.asList(resources.getStringArray(R.array.required_apps_managed_profile));
        this.mDisallowedAppsList = Arrays.asList(resources.getStringArray(R.array.disallowed_apps_managed_profile));
        this.mVendorRequiredAppsList = null;
        this.mVendorDisallowedAppsList = null;
    }

    public void run() {
        if (this.mLeaveAllSystemAppsEnabled) {
            Log.e(TAG, "Not deleting non-required apps.");
            this.mCallback.onSuccess();
            return;
        }
        Log.e(TAG, "Deleting non required apps.");
        Set<String> packagesToDelete = getPackagesToDelete();
        removeNonInstalledPackages(packagesToDelete);
        if (packagesToDelete.isEmpty()) {
            this.mCallback.onSuccess();
            return;
        }
        PackageDeleteObserver packageDeleteObserver = new PackageDeleteObserver(packagesToDelete.size());
        for (String packageName : packagesToDelete) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Deleting package [");
            stringBuilder.append(packageName);
            stringBuilder.append("] as user ");
            stringBuilder.append(this.mUserId);
            Log.e(str, stringBuilder.toString());
            this.mPm.deletePackageAsUser(packageName, packageDeleteObserver, 4, this.mUserId);
        }
    }

    private Set<String> getPackagesToDelete() {
        Set<String> packagesToDelete = getCurrentAppsWithLauncher();
        packagesToDelete.removeAll(getRequiredApps());
        if (this.mProvisioningType == 0 || this.mProvisioningType == 2) {
            packagesToDelete.removeAll(getSystemInputMethods());
        }
        packagesToDelete.addAll(getDisallowedApps());
        return packagesToDelete;
    }

    private void removeNonInstalledPackages(Set<String> packages) {
        Set<String> toBeRemoved = new HashSet();
        for (String packageName : packages) {
            try {
                if (this.mPm.getPackageInfoAsUser(packageName, 0, this.mUserId) == null) {
                    toBeRemoved.add(packageName);
                }
            } catch (NameNotFoundException e) {
                toBeRemoved.add(packageName);
            }
        }
        packages.removeAll(toBeRemoved);
    }

    public static boolean shouldDeleteNonRequiredApps(Context context, int userId) {
        return getSystemAppsFile(context, userId).exists();
    }

    static File getSystemAppsFile(Context context, int userId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getFilesDir());
        stringBuilder.append(File.separator);
        stringBuilder.append("system_apps");
        stringBuilder.append(File.separator);
        stringBuilder.append("user");
        stringBuilder.append(userId);
        stringBuilder.append(".xml");
        return new File(stringBuilder.toString());
    }

    private Set<String> getCurrentAppsWithLauncher() {
        Intent launcherIntent = new Intent("android.intent.action.MAIN");
        launcherIntent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> resolveInfos = this.mPm.queryIntentActivitiesAsUser(launcherIntent, 1843712, this.mUserId);
        Log.d(TAG, "Oneplus-MATCH_SYSTEM_ONLY");
        Set<String> apps = new HashSet();
        for (ResolveInfo resolveInfo : resolveInfos) {
            apps.add(resolveInfo.activityInfo.packageName);
        }
        return apps;
    }

    private Set<String> getSystemInputMethods() {
        try {
            List<InputMethodInfo> inputMethods = this.mIInputMethodManager.getInputMethodList();
            Set<String> systemInputMethods = new HashSet();
            for (InputMethodInfo inputMethodInfo : inputMethods) {
                if ((inputMethodInfo.getServiceInfo().applicationInfo.flags & 1) != 0) {
                    systemInputMethods.add(inputMethodInfo.getPackageName());
                }
            }
            return systemInputMethods;
        } catch (RemoteException e) {
            Log.e(TAG, "Could not communicate with IInputMethodManager", e);
            return Collections.emptySet();
        }
    }

    private void writeSystemApps(Set<String> packageNames, File systemAppsFile) {
        try {
            FileOutputStream stream = new FileOutputStream(systemAppsFile, false);
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(stream, "utf-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.startTag(null, TAG_SYSTEM_APPS);
            for (String packageName : packageNames) {
                serializer.startTag(null, "item");
                serializer.attribute(null, ATTR_VALUE, packageName);
                serializer.endTag(null, "item");
            }
            serializer.endTag(null, TAG_SYSTEM_APPS);
            serializer.endDocument();
            stream.close();
        } catch (IOException e) {
            Log.e(TAG, "IOException trying to write the system apps", e);
        }
    }

    private Set<String> readSystemApps(File systemAppsFile) {
        Set<String> result = new HashSet();
        if (!systemAppsFile.exists()) {
            return result;
        }
        try {
            FileInputStream stream = new FileInputStream(systemAppsFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            int type = parser.next();
            int outerDepth = parser.getDepth();
            while (true) {
                int next = parser.next();
                type = next;
                if (next == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                    stream.close();
                } else if (type != 3) {
                    if (type != 4) {
                        String tag = parser.getName();
                        if (tag.equals("item")) {
                            result.add(parser.getAttributeValue(null, ATTR_VALUE));
                        } else {
                            String str = TAG;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Unknown tag: ");
                            stringBuilder.append(tag);
                            Log.e(str, stringBuilder.toString());
                        }
                    }
                }
            }
            stream.close();
        } catch (IOException e) {
            Log.e(TAG, "IOException trying to read the system apps", e);
        } catch (XmlPullParserException e2) {
            Log.e(TAG, "XmlPullParserException trying to read the system apps", e2);
        }
        return result;
    }

    /* Access modifiers changed, original: protected */
    public Set<String> getRequiredApps() {
        HashSet<String> requiredApps = new HashSet();
        requiredApps.addAll(this.mRequiredAppsList);
        return requiredApps;
    }

    private Set<String> getDisallowedApps() {
        HashSet<String> disallowedApps = new HashSet();
        disallowedApps.addAll(this.mDisallowedAppsList);
        return disallowedApps;
    }

    private static IInputMethodManager getIInputMethodManager() {
        return IInputMethodManager.Stub.asInterface(ServiceManager.getService("input_method"));
    }
}
