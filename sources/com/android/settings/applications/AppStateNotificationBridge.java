package com.android.settings.applications;

import android.app.usage.IUsageStatsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.content.Context;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.support.v7.preference.VibratorSceneUtils;
import android.util.ArrayMap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Switch;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settings.notification.NotificationBackend;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import com.android.settingslib.utils.StringUtil;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AppStateNotificationBridge extends AppStateBaseBridge {
    private static final int DAYS_TO_CHECK = 7;
    public static final AppFilter FILTER_APP_NOTIFICATION_FREQUENCY = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            NotificationsSentState state = AppStateNotificationBridge.getNotificationsSentState(info);
            boolean z = false;
            if (state == null) {
                return false;
            }
            if (state.sentCount != 0) {
                z = true;
            }
            return z;
        }
    };
    public static final AppFilter FILTER_APP_NOTIFICATION_RECENCY = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            NotificationsSentState state = AppStateNotificationBridge.getNotificationsSentState(info);
            boolean z = false;
            if (state == null) {
                return false;
            }
            if (state.lastSent != 0) {
                z = true;
            }
            return z;
        }
    };
    public static final Comparator<AppEntry> FREQUENCY_NOTIFICATION_COMPARATOR = new Comparator<AppEntry>() {
        public int compare(AppEntry object1, AppEntry object2) {
            NotificationsSentState state1 = AppStateNotificationBridge.getNotificationsSentState(object1);
            NotificationsSentState state2 = AppStateNotificationBridge.getNotificationsSentState(object2);
            if (state1 == null && state2 != null) {
                return -1;
            }
            if (state1 != null && state2 == null) {
                return 1;
            }
            if (!(state1 == null || state2 == null)) {
                if (state1.sentCount < state2.sentCount) {
                    return 1;
                }
                if (state1.sentCount > state2.sentCount) {
                    return -1;
                }
            }
            return ApplicationsState.ALPHA_COMPARATOR.compare(object1, object2);
        }
    };
    public static final Comparator<AppEntry> RECENT_NOTIFICATION_COMPARATOR = new Comparator<AppEntry>() {
        public int compare(AppEntry object1, AppEntry object2) {
            NotificationsSentState state1 = AppStateNotificationBridge.getNotificationsSentState(object1);
            NotificationsSentState state2 = AppStateNotificationBridge.getNotificationsSentState(object2);
            if (state1 == null && state2 != null) {
                return -1;
            }
            if (state1 != null && state2 == null) {
                return 1;
            }
            if (!(state1 == null || state2 == null)) {
                if (state1.lastSent < state2.lastSent) {
                    return 1;
                }
                if (state1.lastSent > state2.lastSent) {
                    return -1;
                }
            }
            return ApplicationsState.ALPHA_COMPARATOR.compare(object1, object2);
        }
    };
    private NotificationBackend mBackend;
    private final Context mContext;
    private IUsageStatsManager mUsageStatsManager;
    protected List<Integer> mUserIds = new ArrayList();
    protected long[] mVibratePattern;
    protected Vibrator mVibrator;

    public static class NotificationsSentState {
        public int avgSentDaily = 0;
        public int avgSentWeekly = 0;
        public boolean blockable;
        public boolean blocked;
        public long lastSent = 0;
        public int sentCount = 0;
        public boolean systemApp;
    }

    public AppStateNotificationBridge(Context context, ApplicationsState appState, Callback callback, IUsageStatsManager usageStatsManager, UserManager userManager, NotificationBackend backend) {
        super(appState, callback);
        this.mContext = context;
        this.mUsageStatsManager = usageStatsManager;
        this.mBackend = backend;
        this.mUserIds.add(Integer.valueOf(this.mContext.getUserId()));
        int workUserId = Utils.getManagedProfileId(userManager, this.mContext.getUserId());
        if (workUserId != -10000) {
            this.mUserIds.add(Integer.valueOf(workUserId));
        }
        if (OPUtils.isSupportXVibrate()) {
            this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        }
    }

    /* Access modifiers changed, original: protected */
    public void loadAllExtraInfo() {
        ArrayList<AppEntry> apps = this.mAppSession.getAllApps();
        if (apps != null) {
            Map<String, NotificationsSentState> map = getAggregatedUsageEvents();
            Iterator it = apps.iterator();
            while (it.hasNext()) {
                AppEntry entry = (AppEntry) it.next();
                NotificationsSentState stats = (NotificationsSentState) map.get(getKey(UserHandle.getUserId(entry.info.uid), entry.info.packageName));
                calculateAvgSentCounts(stats);
                addBlockStatus(entry, stats);
                entry.extraInfo = stats;
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void updateExtraInfo(AppEntry entry, String pkg, int uid) {
        NotificationsSentState stats = getAggregatedUsageEvents(UserHandle.getUserId(entry.info.uid), entry.info.packageName);
        calculateAvgSentCounts(stats);
        addBlockStatus(entry, stats);
        entry.extraInfo = stats;
    }

    public static CharSequence getSummary(Context context, NotificationsSentState state, boolean sortByRecency) {
        if (sortByRecency) {
            if (state.lastSent == 0) {
                return context.getString(R.string.notifications_sent_never);
            }
            return StringUtil.formatRelativeTime(context, (double) (System.currentTimeMillis() - state.lastSent), true);
        } else if (state.avgSentWeekly > 0) {
            return context.getString(R.string.notifications_sent_weekly, new Object[]{Integer.valueOf(state.avgSentWeekly)});
        } else {
            return context.getString(R.string.notifications_sent_daily, new Object[]{Integer.valueOf(state.avgSentDaily)});
        }
    }

    private void addBlockStatus(AppEntry entry, NotificationsSentState stats) {
        if (stats != null) {
            stats.blocked = this.mBackend.getNotificationsBanned(entry.info.packageName, entry.info.uid);
            stats.systemApp = this.mBackend.isSystemApp(this.mContext, entry.info);
            boolean z = !stats.systemApp || (stats.systemApp && stats.blocked);
            stats.blockable = z;
        }
    }

    private void calculateAvgSentCounts(NotificationsSentState stats) {
        if (stats != null) {
            stats.avgSentDaily = Math.round(((float) stats.sentCount) / 7.0f);
            if (stats.sentCount < 7) {
                stats.avgSentWeekly = stats.sentCount;
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public Map<String, NotificationsSentState> getAggregatedUsageEvents() {
        ArrayMap<String, NotificationsSentState> aggregatedStats = new ArrayMap();
        long now = System.currentTimeMillis();
        long startTime = now - 604800000;
        for (Integer intValue : this.mUserIds) {
            int userId = intValue.intValue();
            UsageEvents events = null;
            try {
                events = this.mUsageStatsManager.queryEventsForUser(startTime, now, userId, this.mContext.getPackageName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (events != null) {
                Event event = new Event();
                while (events.hasNextEvent()) {
                    events.getNextEvent(event);
                    NotificationsSentState stats = (NotificationsSentState) aggregatedStats.get(getKey(userId, event.getPackageName()));
                    if (stats == null) {
                        stats = new NotificationsSentState();
                        aggregatedStats.put(getKey(userId, event.getPackageName()), stats);
                    }
                    if (event.getEventType() == 12) {
                        if (event.getTimeStamp() > stats.lastSent) {
                            stats.lastSent = event.getTimeStamp();
                        }
                        stats.sentCount++;
                    }
                }
            }
        }
        return aggregatedStats;
    }

    /* Access modifiers changed, original: protected */
    public NotificationsSentState getAggregatedUsageEvents(int userId, String pkg) {
        NotificationsSentState stats = null;
        long now = System.currentTimeMillis();
        UsageEvents events = null;
        try {
            events = this.mUsageStatsManager.queryEventsForPackageForUser(now - 604800000, now, userId, pkg, this.mContext.getPackageName());
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (SecurityException e2) {
            e2.printStackTrace();
        }
        if (events != null) {
            Event event = new Event();
            while (events.hasNextEvent()) {
                events.getNextEvent(event);
                if (event.getEventType() == 12) {
                    if (stats == null) {
                        stats = new NotificationsSentState();
                    }
                    if (event.getTimeStamp() > stats.lastSent) {
                        stats.lastSent = event.getTimeStamp();
                    }
                    stats.sentCount++;
                }
            }
        }
        return stats;
    }

    /* JADX WARNING: Missing block: B:9:0x0014, code skipped:
            return null;
     */
    private static com.android.settings.applications.AppStateNotificationBridge.NotificationsSentState getNotificationsSentState(com.android.settingslib.applications.ApplicationsState.AppEntry r2) {
        /*
        r0 = 0;
        if (r2 == 0) goto L_0x0014;
    L_0x0003:
        r1 = r2.extraInfo;
        if (r1 != 0) goto L_0x0008;
    L_0x0007:
        goto L_0x0014;
    L_0x0008:
        r1 = r2.extraInfo;
        r1 = r1 instanceof com.android.settings.applications.AppStateNotificationBridge.NotificationsSentState;
        if (r1 == 0) goto L_0x0013;
    L_0x000e:
        r0 = r2.extraInfo;
        r0 = (com.android.settings.applications.AppStateNotificationBridge.NotificationsSentState) r0;
        return r0;
    L_0x0013:
        return r0;
    L_0x0014:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.applications.AppStateNotificationBridge.getNotificationsSentState(com.android.settingslib.applications.ApplicationsState$AppEntry):com.android.settings.applications.AppStateNotificationBridge$NotificationsSentState");
    }

    protected static String getKey(int userId, String pkg) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(userId);
        stringBuilder.append("|");
        stringBuilder.append(pkg);
        return stringBuilder.toString();
    }

    public OnClickListener getSwitchOnClickListener(AppEntry entry) {
        return entry != null ? new -$$Lambda$AppStateNotificationBridge$3yb6PrF82n91FG3YEHY_Ccl1JyI(this, entry) : null;
    }

    public static /* synthetic */ void lambda$getSwitchOnClickListener$0(AppStateNotificationBridge appStateNotificationBridge, AppEntry entry, View v) {
        Switch toggle = (Switch) ((ViewGroup) v).findViewById(R.id.switchWidget);
        if (toggle != null && toggle.isEnabled()) {
            if (VibratorSceneUtils.systemVibrateEnabled(appStateNotificationBridge.mContext)) {
                appStateNotificationBridge.mVibratePattern = VibratorSceneUtils.getVibratorScenePattern(appStateNotificationBridge.mContext, appStateNotificationBridge.mVibrator, 1003);
                VibratorSceneUtils.vibrateIfNeeded(appStateNotificationBridge.mVibratePattern, appStateNotificationBridge.mVibrator);
            }
            toggle.toggle();
            appStateNotificationBridge.mBackend.setNotificationsEnabledForPackage(entry.info.packageName, entry.info.uid, toggle.isChecked());
            NotificationsSentState stats = getNotificationsSentState(entry);
            if (stats != null) {
                stats.blocked = toggle.isChecked() ^ 1;
            }
        }
    }

    public static final boolean enableSwitch(AppEntry entry) {
        NotificationsSentState stats = getNotificationsSentState(entry);
        if (stats == null) {
            return false;
        }
        return stats.blockable;
    }

    public static final boolean checkSwitch(AppEntry entry) {
        NotificationsSentState stats = getNotificationsSentState(entry);
        if (stats == null) {
            return false;
        }
        return stats.blocked ^ 1;
    }
}
