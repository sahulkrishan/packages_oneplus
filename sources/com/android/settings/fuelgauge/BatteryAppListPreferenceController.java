package com.android.settings.fuelgauge;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.BatteryStats;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.PointerIconCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatterySipper.DrainType;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.os.PowerProfile;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.fuelgauge.anomaly.Anomaly;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnDestroy;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.utils.StringUtil;
import com.oneplus.settings.backgroundoptimize.Logutil;
import com.oneplus.settings.highpowerapp.HighPowerApp;
import com.oneplus.settings.highpowerapp.HighPowerAppModel;
import com.oneplus.settings.highpowerapp.IHighPowerAppObserver;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BatteryAppListPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnPause, OnDestroy, OnResume, IHighPowerAppObserver {
    private static final int MAX_ITEMS_TO_LIST = 10;
    private static final int MIN_AVERAGE_POWER_THRESHOLD_MILLI_AMP = 10;
    private static final int MIN_POWER_THRESHOLD_MILLI_AMP = 5;
    private static final int MSG_INTERVAL = 10000;
    private static final int MSG_UPDATE = 1000;
    private static final int SECONDS_IN_HOUR = 3600;
    private static final int STATS_TYPE = 0;
    static final String TAG = "BatteryAppList";
    @VisibleForTesting
    static final boolean USE_FAKE_DATA = false;
    private SettingsActivity mActivity;
    SparseArray<List<Anomaly>> mAnomalySparseArray;
    @VisibleForTesting
    PreferenceGroup mAppListGroup;
    private BatteryStatsHelper mBatteryStatsHelper;
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    private OnClickListener mForceCloseListener = new OnClickListener() {
        public void onClick(View v) {
            Object o = v.getTag();
            if (o != null && (o instanceof String)) {
                BatteryAppListPreferenceController.this.mHighPowerAppModel.stopApp((String) o);
                BatteryAppListPreferenceController.this.mHighPowerAppModel.update();
            }
        }
    };
    private InstrumentedPreferenceFragment mFragment;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 1000) {
                switch (i) {
                    case 1:
                        BatteryEntry entry = msg.obj;
                        PowerGaugePreference pgp = (PowerGaugePreference) BatteryAppListPreferenceController.this.mAppListGroup.findPreference(Integer.toString(entry.sipper.uidObj.getUid()));
                        if (pgp != null) {
                            pgp.setIcon(BatteryAppListPreferenceController.this.mUserManager.getBadgedIconForUser(entry.getIcon(), new UserHandle(UserHandle.getUserId(entry.sipper.getUid()))));
                            pgp.setTitle((CharSequence) entry.name);
                            if (entry.sipper.drainType == DrainType.APP) {
                                pgp.setContentDescription(entry.name);
                                break;
                            }
                        }
                        break;
                    case 2:
                        Activity activity = BatteryAppListPreferenceController.this.mActivity;
                        if (activity != null) {
                            activity.reportFullyDrawn();
                            break;
                        }
                        break;
                }
            }
            BatteryAppListPreferenceController.this.mHighPowerAppModel.update();
            if (!BatteryAppListPreferenceController.this.mPauseUpdate) {
                BatteryAppListPreferenceController.this.nextUpdate();
            }
            super.handleMessage(msg);
        }
    };
    private HighPowerAppModel mHighPowerAppModel;
    private boolean mPauseUpdate;
    private Context mPrefContext;
    private ArrayMap<String, Preference> mPreferenceCache;
    private final String mPreferenceKey;
    private int mStatsType = 0;
    private UserManager mUserManager;

    public BatteryAppListPreferenceController(Context context, String preferenceKey, Lifecycle lifecycle, SettingsActivity activity, InstrumentedPreferenceFragment fragment) {
        super(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
        this.mPreferenceKey = preferenceKey;
        this.mBatteryUtils = BatteryUtils.getInstance(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mActivity = activity;
        this.mFragment = fragment;
        this.mHighPowerAppModel = new HighPowerAppModel(context, null);
    }

    public void onPause() {
        BatteryEntry.stopRequestQueue();
        this.mHandler.removeMessages(1);
        this.mPauseUpdate = true;
        this.mHighPowerAppModel.unregisterObserver(this);
        this.mHandler.removeMessages(1000);
    }

    public void onResume() {
        this.mPauseUpdate = false;
        this.mHighPowerAppModel.registerObserver(this);
        this.mHighPowerAppModel.update();
        nextUpdate();
    }

    public void onDestroy() {
        if (this.mActivity.isChangingConfigurations()) {
            BatteryEntry.clearUidCache();
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPrefContext = screen.getContext();
        this.mAppListGroup = (PreferenceGroup) screen.findPreference(this.mPreferenceKey);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return this.mPreferenceKey;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!(preference instanceof PowerGaugePreference)) {
            return false;
        }
        List list;
        PowerGaugePreference pgp = (PowerGaugePreference) preference;
        BatteryEntry entry = pgp.getInfo();
        SettingsActivity settingsActivity = this.mActivity;
        InstrumentedPreferenceFragment instrumentedPreferenceFragment = this.mFragment;
        BatteryStatsHelper batteryStatsHelper = this.mBatteryStatsHelper;
        String percent = pgp.getPercent();
        if (this.mAnomalySparseArray != null) {
            list = (List) this.mAnomalySparseArray.get(entry.sipper.getUid());
        } else {
            list = null;
        }
        AdvancedPowerUsageDetail.startBatteryDetailPage(settingsActivity, instrumentedPreferenceFragment, batteryStatsHelper, 0, entry, percent, list);
        return true;
    }

    public void refreshAnomalyIcon(SparseArray<List<Anomaly>> anomalySparseArray) {
        if (isAvailable()) {
            this.mAnomalySparseArray = anomalySparseArray;
            int size = anomalySparseArray.size();
            for (int i = 0; i < size; i++) {
                PowerGaugePreference pref = (PowerGaugePreference) this.mAppListGroup.findPreference(extractKeyFromUid(anomalySparseArray.keyAt(i)));
                if (pref != null) {
                    pref.shouldShowAnomalyIcon(true);
                }
            }
        }
    }

    public void refreshAppListGroup(BatteryStatsHelper statsHelper, boolean showAllApps) {
        if (isAvailable()) {
            this.mBatteryStatsHelper = statsHelper;
            this.mAppListGroup.setTitle((int) R.string.power_usage_list_summary);
            PowerProfile powerProfile = statsHelper.getPowerProfile();
            BatteryStats stats = statsHelper.getStats();
            double averagePower = powerProfile.getAveragePower("screen.full");
            boolean addedSome = false;
            List<BatterySipper> usageList = getCoalescedUsageList(new ArrayList(statsHelper.getUsageList()));
            if (!OPUtils.isGuestMode()) {
                usageList = concatHighPowerApp(usageList, stats);
            }
            Map<Integer, PowerGaugePreference> apps = new HashMap();
            Set<Integer> uids = new HashSet();
            BatteryStats batteryStats;
            double d;
            PowerProfile powerProfile2;
            if (this.mHighPowerAppModel == null || this.mActivity == null) {
                batteryStats = stats;
                d = averagePower;
            } else if (this.mActivity.isFinishing()) {
                powerProfile2 = powerProfile;
                batteryStats = stats;
                d = averagePower;
            } else {
                List<HighPowerApp> list = this.mHighPowerAppModel.getDataList();
                int i = 0;
                while (list != null && i < list.size()) {
                    uids.add(Integer.valueOf(((HighPowerApp) list.get(i)).uid));
                    i++;
                }
                String uninstalled = this.mPrefContext.getString(R.string.op_app_already_uninstalled);
                int dischargeAmount = stats != null ? stats.getDischargeAmount(0) : 0;
                cacheRemoveAllPrefs(this.mAppListGroup);
                this.mAppListGroup.setOrderingAsAdded(false);
                List<HighPowerApp> list2;
                String str;
                if (averagePower < 10.0d) {
                    powerProfile2 = powerProfile;
                    batteryStats = stats;
                    d = averagePower;
                    list2 = list;
                    str = uninstalled;
                } else {
                    double d2;
                    int numSippers;
                    if (showAllApps) {
                        d2 = 0.0d;
                    } else {
                        d2 = this.mBatteryUtils.removeHiddenBatterySippers(usageList);
                    }
                    double hiddenPowerMah = d2;
                    this.mBatteryUtils.sortUsageList(usageList);
                    int numSippers2 = usageList.size();
                    boolean addedSome2 = false;
                    int i2 = 0;
                    while (i2 < numSippers2) {
                        int numSippers3;
                        int i3;
                        int i4;
                        BatteryStatsHelper batteryStatsHelper;
                        BatterySipper sipper = (BatterySipper) usageList.get(i2);
                        str = uninstalled;
                        if (sipper.totalPowerMah * 3600.0d >= 5.0d || uids.contains(Integer.valueOf(sipper.getUid()))) {
                            double totalPower = statsHelper.getTotalPower();
                            BatteryUtils batteryUtils = this.mBatteryUtils;
                            powerProfile2 = powerProfile;
                            batteryStats = stats;
                            powerProfile = sipper.totalPowerMah;
                            d = averagePower;
                            BatterySipper sipper2 = sipper;
                            BatteryUtils batteryUtils2 = batteryUtils;
                            numSippers3 = numSippers2;
                            powerProfile = batteryUtils2.calculateBatteryPercent(powerProfile, totalPower, hiddenPowerMah, dischargeAmount);
                            list2 = list;
                            if (((int) (powerProfile + 4602678819172646912)) < 1 && uids.contains(Integer.valueOf(sipper2.getUid()))) {
                                powerProfile = 0;
                            }
                            if (((int) (0.5d + powerProfile)) >= 1 && !shouldHideSipper(sipper2)) {
                                PowerGaugePreference pref;
                                UserHandle userHandle = new UserHandle(UserHandle.getUserId(sipper2.getUid()));
                                BatteryEntry entry = new BatteryEntry(this.mActivity, this.mHandler, this.mUserManager, sipper2);
                                Drawable badgedIcon = this.mUserManager.getBadgedIconForUser(entry.getIcon(), userHandle);
                                CharSequence contentDescription = this.mUserManager.getBadgedLabelForUser(entry.getLabel(), userHandle);
                                String key = extractKeyFromSipper(sipper2);
                                PowerGaugePreference pref2 = (PowerGaugePreference) getCachedPreference(key);
                                if (pref2 == null) {
                                    numSippers = numSippers3;
                                    pref = new PowerGaugePreference(this.mPrefContext, badgedIcon, contentDescription, entry);
                                    pref.setKey(key);
                                } else {
                                    numSippers = numSippers3;
                                    pref = pref2;
                                }
                                sipper2.percent = powerProfile;
                                pref.setTitle((CharSequence) entry.getLabel());
                                pref.setOrder(i2 + 1);
                                pref.setPercent(powerProfile);
                                pref.shouldShowAnomalyIcon(false);
                                pref.setOnButtonClickListener(this.mForceCloseListener);
                                apps.put(Integer.valueOf(sipper2.getUid()), pref);
                                double percentOfTotal = powerProfile;
                                if (sipper2.usageTimeMs == 0 && sipper2.drainType == DrainType.APP) {
                                    i3 = 0;
                                    sipper2.usageTimeMs = this.mBatteryUtils.getProcessTimeMs(1, sipper2.uidObj, 0);
                                } else {
                                    i3 = 0;
                                }
                                addedSome2 = true;
                                this.mAppListGroup.addPreference(pref);
                                this.mAppListGroup.getPreferenceCount();
                                getCachedCount();
                                i2++;
                                i4 = i3;
                                uninstalled = str;
                                powerProfile = powerProfile2;
                                stats = batteryStats;
                                averagePower = d;
                                list = list2;
                                numSippers2 = numSippers;
                                batteryStatsHelper = statsHelper;
                            }
                        } else {
                            powerProfile2 = powerProfile;
                            batteryStats = stats;
                            d = averagePower;
                            list2 = list;
                            numSippers3 = numSippers2;
                        }
                        numSippers = numSippers3;
                        i3 = 0;
                        i2++;
                        i4 = i3;
                        uninstalled = str;
                        powerProfile = powerProfile2;
                        stats = batteryStats;
                        averagePower = d;
                        list = list2;
                        numSippers2 = numSippers;
                        batteryStatsHelper = statsHelper;
                    }
                    batteryStats = stats;
                    d = averagePower;
                    list2 = list;
                    str = uninstalled;
                    numSippers = numSippers2;
                    setPowerState(apps);
                    addedSome = addedSome2;
                }
                if (!addedSome) {
                    addNotAvailableMessage();
                }
                removeCachedPrefs(this.mAppListGroup);
                BatteryEntry.startRequestQueue();
            }
        }
    }

    private List<BatterySipper> getCoalescedUsageList(List<BatterySipper> sippers) {
        int i;
        SparseArray<BatterySipper> uidList = new SparseArray();
        ArrayList<BatterySipper> results = new ArrayList();
        int numSippers = sippers.size();
        int i2 = 0;
        for (i = 0; i < numSippers; i++) {
            BatterySipper sipper = (BatterySipper) sippers.get(i);
            if (sipper.getUid() > 0) {
                int realUid = sipper.getUid();
                if (isSharedGid(sipper.getUid())) {
                    realUid = UserHandle.getUid(0, UserHandle.getAppIdFromSharedAppGid(sipper.getUid()));
                }
                if (isSystemUid(realUid) && !"mediaserver".equals(sipper.packageWithHighestDrain)) {
                    realUid = 1000;
                }
                if (realUid != sipper.getUid()) {
                    BatterySipper newSipper = new BatterySipper(sipper.drainType, new FakeUid(realUid), 0.0d);
                    newSipper.add(sipper);
                    newSipper.packageWithHighestDrain = sipper.packageWithHighestDrain;
                    newSipper.mPackages = sipper.mPackages;
                    sipper = newSipper;
                }
                int index = uidList.indexOfKey(realUid);
                if (index < 0) {
                    uidList.put(realUid, sipper);
                } else {
                    BatterySipper existingSipper = (BatterySipper) uidList.valueAt(index);
                    existingSipper.add(sipper);
                    if (existingSipper.packageWithHighestDrain == null && sipper.packageWithHighestDrain != null) {
                        existingSipper.packageWithHighestDrain = sipper.packageWithHighestDrain;
                    }
                    int existingPackageLen = existingSipper.mPackages != null ? existingSipper.mPackages.length : 0;
                    int newPackageLen = sipper.mPackages != null ? sipper.mPackages.length : 0;
                    if (newPackageLen > 0) {
                        String[] newPackages = new String[(existingPackageLen + newPackageLen)];
                        if (existingPackageLen > 0) {
                            System.arraycopy(existingSipper.mPackages, 0, newPackages, 0, existingPackageLen);
                        }
                        System.arraycopy(sipper.mPackages, 0, newPackages, existingPackageLen, newPackageLen);
                        existingSipper.mPackages = newPackages;
                    }
                }
            } else {
                results.add(sipper);
            }
        }
        i = uidList.size();
        while (i2 < i) {
            results.add((BatterySipper) uidList.valueAt(i2));
            i2++;
        }
        this.mBatteryUtils.sortUsageList(results);
        return results;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setUsageSummary(Preference preference, BatterySipper sipper) {
        long usageTimeMs = sipper.usageTimeMs;
        if (usageTimeMs >= 60000) {
            CharSequence charSequence;
            CharSequence timeSequence = StringUtil.formatElapsedTime(this.mContext, (double) usageTimeMs, false);
            if (sipper.drainType != DrainType.APP || this.mBatteryUtils.shouldHideSipper(sipper)) {
                charSequence = timeSequence;
            } else {
                charSequence = TextUtils.expandTemplate(this.mContext.getText(R.string.battery_used_for), new CharSequence[]{timeSequence});
            }
            preference.setSummary(charSequence);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean shouldHideSipper(BatterySipper sipper) {
        return sipper.drainType == DrainType.OVERCOUNTED || sipper.drainType == DrainType.UNACCOUNTED;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String extractKeyFromSipper(BatterySipper sipper) {
        if (sipper.uidObj != null) {
            return extractKeyFromUid(sipper.getUid());
        }
        if (sipper.drainType == DrainType.USER) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(sipper.drainType.toString());
            stringBuilder.append(sipper.userId);
            return stringBuilder.toString();
        } else if (sipper.drainType != DrainType.APP) {
            return sipper.drainType.toString();
        } else {
            if (sipper.getPackages() != null) {
                return TextUtils.concat(sipper.getPackages()).toString();
            }
            String str = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Inappropriate BatterySipper without uid and package names: ");
            stringBuilder2.append(sipper);
            Log.w(str, stringBuilder2.toString());
            return "-1";
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String extractKeyFromUid(int uid) {
        return Integer.toString(uid);
    }

    private void cacheRemoveAllPrefs(PreferenceGroup group) {
        this.mPreferenceCache = new ArrayMap();
        int N = group.getPreferenceCount();
        for (int i = 0; i < N; i++) {
            Preference p = group.getPreference(i);
            if (!TextUtils.isEmpty(p.getKey())) {
                this.mPreferenceCache.put(p.getKey(), p);
            }
        }
    }

    private static boolean isSharedGid(int uid) {
        return UserHandle.getAppIdFromSharedAppGid(uid) > 0;
    }

    private static boolean isSystemUid(int uid) {
        int appUid = UserHandle.getAppId(uid);
        return appUid >= 1000 && appUid < 10000;
    }

    private static List<BatterySipper> getFakeStats() {
        int use;
        ArrayList<BatterySipper> stats = new ArrayList();
        float use2 = 5.0f;
        for (DrainType type : DrainType.values()) {
            if (type != DrainType.APP) {
                stats.add(new BatterySipper(type, null, (double) use2));
                use2 += 5.0f;
            }
        }
        for (use = 0; use < 100; use++) {
            stats.add(new BatterySipper(DrainType.APP, new FakeUid(10000 + use), (double) use2));
        }
        stats.add(new BatterySipper(DrainType.APP, new FakeUid(0), (double) use2));
        BatterySipper sipper = new BatterySipper(DrainType.APP, new FakeUid(UserHandle.getSharedAppGid(10000)), 10.0d);
        sipper.packageWithHighestDrain = "dex2oat";
        stats.add(sipper);
        sipper = new BatterySipper(DrainType.APP, new FakeUid(UserHandle.getSharedAppGid(10001)), 10.0d);
        sipper.packageWithHighestDrain = "dex2oat";
        stats.add(sipper);
        stats.add(new BatterySipper(DrainType.APP, new FakeUid(UserHandle.getSharedAppGid(PointerIconCompat.TYPE_CROSSHAIR)), 9.0d));
        return stats;
    }

    private Preference getCachedPreference(String key) {
        return this.mPreferenceCache != null ? (Preference) this.mPreferenceCache.remove(key) : null;
    }

    private void removeCachedPrefs(PreferenceGroup group) {
        for (Preference p : this.mPreferenceCache.values()) {
            group.removePreference(p);
        }
        this.mPreferenceCache = null;
    }

    private int getCachedCount() {
        return this.mPreferenceCache != null ? this.mPreferenceCache.size() : 0;
    }

    private void addNotAvailableMessage() {
        String NOT_AVAILABLE = "not_available";
        if (getCachedPreference("not_available") == null) {
            Preference notAvailable = new Preference(this.mPrefContext);
            notAvailable.setKey("not_available");
            notAvailable.setTitle((int) R.string.power_usage_not_available);
            notAvailable.setSelectable(false);
            this.mAppListGroup.addPreference(notAvailable);
        }
    }

    public void OnDataChanged() {
        Log.d(TAG, "OnDataChanged");
        if (this.mActivity == null) {
            Log.e(TAG, "null activity");
        } else {
            this.mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    ((PowerUsageAdvanced) BatteryAppListPreferenceController.this.mFragment).restartBatteryStatsLoader(0);
                }
            });
        }
    }

    private void nextUpdate() {
        if (!OPUtils.isGuestMode()) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1000), 10000);
        }
    }

    private List<BatterySipper> concatHighPowerApp(List<BatterySipper> usageList, BatteryStats stats) {
        List<BatterySipper> usageList2;
        BatteryStats batteryStats = stats;
        if (usageList == null) {
            usageList2 = new ArrayList();
        } else {
            usageList2 = usageList;
        }
        Map<Integer, BatterySipper> uids = new HashMap();
        for (BatterySipper sipper : usageList2) {
            uids.put(Integer.valueOf(sipper.getUid()), sipper);
        }
        if (this.mHighPowerAppModel != null) {
            List<HighPowerApp> list = this.mHighPowerAppModel.getDataList();
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("HighPowerApp list.size=");
            stringBuilder.append(list != null ? Integer.valueOf(list.size()) : null);
            Logutil.loge(str, stringBuilder.toString());
            if (list != null && list.size() > 0) {
                int dischargeAmount = batteryStats != null ? batteryStats.getDischargeAmount(this.mStatsType) : 0;
                double totalPower = this.mBatteryStatsHelper.getTotalPower();
                double totalPowerMah = (dischargeAmount == 0 || 0.0d == totalPower) ? 1.0d : (0.6d / ((double) dischargeAmount)) * totalPower;
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("concatHighPowerApp list.size=");
                stringBuilder2.append(list.size());
                Logutil.loge(str2, stringBuilder2.toString());
                for (HighPowerApp app : list) {
                    BatterySipper sipper2;
                    if (uids.containsKey(Integer.valueOf(app.uid))) {
                        String str3 = TAG;
                        StringBuilder stringBuilder3 = new StringBuilder();
                        stringBuilder3.append("concatHighPowerApp exist pkg=");
                        stringBuilder3.append(app.pkgName);
                        stringBuilder3.append(", uid=");
                        stringBuilder3.append(app.uid);
                        Logutil.loge(str3, stringBuilder3.toString());
                        sipper2 = (BatterySipper) uids.get(Integer.valueOf(app.uid));
                        if (sipper2.totalPowerMah < totalPowerMah) {
                            sipper2.totalPowerMah = totalPowerMah;
                        }
                    } else {
                        String str4 = TAG;
                        StringBuilder stringBuilder4 = new StringBuilder();
                        stringBuilder4.append("concatHighPowerApp add pkg=");
                        stringBuilder4.append(app.pkgName);
                        stringBuilder4.append(", uid=");
                        stringBuilder4.append(app.uid);
                        Logutil.loge(str4, stringBuilder4.toString());
                        sipper2 = new BatterySipper(DrainType.APP, new FakeUid(app.uid), 1.0d);
                        sipper2.totalPowerMah = totalPowerMah;
                        usageList2.add(sipper2);
                    }
                    batteryStats = stats;
                }
            }
        }
        return usageList2;
    }

    private void setPowerState(Map<Integer, PowerGaugePreference> apps) {
        if (apps != null) {
            for (PowerGaugePreference pref : apps.values()) {
                pref.setState(-1);
            }
            if (this.mHighPowerAppModel != null) {
                List<HighPowerApp> list = this.mHighPowerAppModel.getDataList();
                if (list != null && list.size() > 0) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("setPowerState list.size=");
                    stringBuilder.append(list.size());
                    Logutil.loge(str, stringBuilder.toString());
                    for (HighPowerApp app : list) {
                        if (apps.containsKey(Integer.valueOf(app.uid))) {
                            String str2 = TAG;
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("setPowerState pkg=");
                            stringBuilder2.append(app.pkgName);
                            stringBuilder2.append(", uid=");
                            stringBuilder2.append(app.uid);
                            Logutil.loge(str2, stringBuilder2.toString());
                            ((PowerGaugePreference) apps.get(Integer.valueOf(app.uid))).setState(app.getState());
                        }
                    }
                }
            }
        }
    }
}
