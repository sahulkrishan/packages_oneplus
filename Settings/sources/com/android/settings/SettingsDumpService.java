package com.android.settings;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkTemplate;
import android.net.Uri;
import android.os.IBinder;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.applications.ProcStatsData;
import com.android.settings.applications.ProcStatsData.MemInfo;
import com.android.settings.fuelgauge.batterytip.AnomalyConfigJobService;
import com.android.settingslib.net.DataUsageController;
import com.android.settingslib.net.DataUsageController.DataUsageInfo;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingsDumpService extends Service {
    @VisibleForTesting
    static final Intent BROWSER_INTENT = new Intent("android.intent.action.VIEW", Uri.parse("http://"));
    @VisibleForTesting
    static final String KEY_ANOMALY_DETECTION = "anomaly_detection";
    @VisibleForTesting
    static final String KEY_DATAUSAGE = "datausage";
    @VisibleForTesting
    static final String KEY_DEFAULT_BROWSER_APP = "default_browser_app";
    @VisibleForTesting
    static final String KEY_MEMORY = "memory";
    @VisibleForTesting
    static final String KEY_SERVICE = "service";
    @VisibleForTesting
    static final String KEY_STORAGE = "storage";

    public IBinder onBind(Intent intent) {
        return null;
    }

    /* Access modifiers changed, original: protected */
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        JSONObject dump = new JSONObject();
        try {
            dump.put("service", "Settings State");
            dump.put(KEY_STORAGE, dumpStorage());
            dump.put(KEY_DATAUSAGE, dumpDataUsage());
            dump.put(KEY_MEMORY, dumpMemory());
            dump.put(KEY_DEFAULT_BROWSER_APP, dumpDefaultBrowser());
            dump.put(KEY_ANOMALY_DETECTION, dumpAnomalyDetection());
        } catch (Exception e) {
            e.printStackTrace();
        }
        writer.println(dump);
    }

    private JSONObject dumpMemory() throws JSONException {
        JSONObject obj = new JSONObject();
        ProcStatsData statsManager = new ProcStatsData(this, false);
        statsManager.refreshStats(true);
        MemInfo memInfo = statsManager.getMemInfo();
        obj.put("used", String.valueOf(memInfo.realUsedRam));
        obj.put("free", String.valueOf(memInfo.realFreeRam));
        obj.put("total", String.valueOf(memInfo.realTotalRam));
        obj.put("state", statsManager.getMemState());
        return obj;
    }

    private JSONObject dumpDataUsage() throws JSONException {
        JSONObject obj = new JSONObject();
        DataUsageController controller = new DataUsageController(this);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(ConnectivityManager.class);
        SubscriptionManager manager = SubscriptionManager.from(this);
        TelephonyManager telephonyManager = TelephonyManager.from(this);
        if (connectivityManager.isNetworkSupported(0)) {
            JSONArray array = new JSONArray();
            for (SubscriptionInfo info : manager.getAllSubscriptionInfoList()) {
                JSONObject usage = dumpDataUsage(NetworkTemplate.buildTemplateMobileAll(telephonyManager.getSubscriberId(info.getSubscriptionId())), controller);
                usage.put("subId", info.getSubscriptionId());
                array.put(usage);
            }
            obj.put("cell", array);
        }
        if (connectivityManager.isNetworkSupported(1)) {
            obj.put("wifi", dumpDataUsage(NetworkTemplate.buildTemplateWifiWildcard(), controller));
        }
        if (connectivityManager.isNetworkSupported(9)) {
            obj.put("ethernet", dumpDataUsage(NetworkTemplate.buildTemplateEthernet(), controller));
        }
        return obj;
    }

    private JSONObject dumpDataUsage(NetworkTemplate template, DataUsageController controller) throws JSONException {
        JSONObject obj = new JSONObject();
        DataUsageInfo usage = controller.getDataUsageInfo(template);
        obj.put("carrier", usage.carrier);
        obj.put("start", usage.startDate);
        obj.put("usage", usage.usageLevel);
        obj.put("warning", usage.warningLevel);
        obj.put("limit", usage.limitLevel);
        return obj;
    }

    private JSONObject dumpStorage() throws JSONException {
        JSONObject obj = new JSONObject();
        for (VolumeInfo volume : ((StorageManager) getSystemService(StorageManager.class)).getVolumes()) {
            JSONObject volObj = new JSONObject();
            if (volume.isMountedReadable()) {
                File path = volume.getPath();
                volObj.put("used", String.valueOf(path.getTotalSpace() - path.getFreeSpace()));
                volObj.put("total", String.valueOf(path.getTotalSpace()));
            }
            volObj.put("path", volume.getInternalPath());
            volObj.put("state", volume.getState());
            volObj.put("stateDesc", volume.getStateDescription());
            volObj.put("description", volume.getDescription());
            obj.put(volume.getId(), volObj);
        }
        return obj;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String dumpDefaultBrowser() {
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(BROWSER_INTENT, 65536);
        if (resolveInfo == null || resolveInfo.activityInfo.packageName.equals("android")) {
            return null;
        }
        return resolveInfo.activityInfo.packageName;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public JSONObject dumpAnomalyDetection() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(AnomalyConfigJobService.KEY_ANOMALY_CONFIG_VERSION, String.valueOf(getSharedPreferences(AnomalyConfigJobService.PREF_DB, 0).getInt(AnomalyConfigJobService.KEY_ANOMALY_CONFIG_VERSION, 0)));
        return obj;
    }
}
