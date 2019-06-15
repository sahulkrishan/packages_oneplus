package com.android.settings.notification;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.INotificationManager;
import android.app.INotificationManager.Stub;
import android.app.Notification;
import android.app.Notification.Action;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.NotificationListenerService.Ranking;
import android.service.notification.NotificationListenerService.RankingMap;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DateTimeView;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationStation extends SettingsPreferenceFragment {
    private static final boolean DEBUG = true;
    private static final boolean DUMP_EXTRAS = true;
    private static final boolean DUMP_PARCEL = true;
    private static final String TAG = NotificationStation.class.getSimpleName();
    private Context mContext;
    private Handler mHandler;
    private final NotificationListenerService mListener = new NotificationListenerService() {
        public void onNotificationPosted(StatusBarNotification sbn, RankingMap ranking) {
            String str = "onNotificationPosted: %s, with update for %d";
            r1 = new Object[2];
            int i = 0;
            r1[0] = sbn.getNotification();
            if (ranking != null) {
                i = ranking.getOrderedKeys().length;
            }
            r1[1] = Integer.valueOf(i);
            NotificationStation.logd(str, r1);
            NotificationStation.this.mRanking = ranking;
            NotificationStation.this.scheduleRefreshList();
        }

        public void onNotificationRemoved(StatusBarNotification notification, RankingMap ranking) {
            String str = "onNotificationRankingUpdate with update for %d";
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(ranking == null ? 0 : ranking.getOrderedKeys().length);
            NotificationStation.logd(str, objArr);
            NotificationStation.this.mRanking = ranking;
            NotificationStation.this.scheduleRefreshList();
        }

        public void onNotificationRankingUpdate(RankingMap ranking) {
            String str = "onNotificationRankingUpdate with update for %d";
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(ranking == null ? 0 : ranking.getOrderedKeys().length);
            NotificationStation.logd(str, objArr);
            NotificationStation.this.mRanking = ranking;
            NotificationStation.this.scheduleRefreshList();
        }

        public void onListenerConnected() {
            NotificationStation.this.mRanking = getCurrentRanking();
            String str = "onListenerConnected with update for %d";
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(NotificationStation.this.mRanking == null ? 0 : NotificationStation.this.mRanking.getOrderedKeys().length);
            NotificationStation.logd(str, objArr);
            NotificationStation.this.scheduleRefreshList();
        }
    };
    private INotificationManager mNoMan;
    private final Comparator<HistoricalNotificationInfo> mNotificationSorter = new Comparator<HistoricalNotificationInfo>() {
        public int compare(HistoricalNotificationInfo lhs, HistoricalNotificationInfo rhs) {
            return Long.compare(rhs.timestamp, lhs.timestamp);
        }
    };
    private PackageManager mPm;
    private RankingMap mRanking;
    private Runnable mRefreshListRunnable = new Runnable() {
        public void run() {
            NotificationStation.this.refreshList();
        }
    };

    private static class HistoricalNotificationInfo {
        public boolean active;
        public String channel;
        public CharSequence extra;
        public Drawable icon;
        public String key;
        public String pkg;
        public Drawable pkgicon;
        public CharSequence pkgname;
        public int priority;
        public long timestamp;
        public CharSequence title;
        public int user;

        private HistoricalNotificationInfo() {
        }

        /* synthetic */ HistoricalNotificationInfo(AnonymousClass1 x0) {
            this();
        }
    }

    private static class HistoricalNotificationPreference extends Preference {
        private static long sLastExpandedTimestamp;
        private final HistoricalNotificationInfo mInfo;

        public HistoricalNotificationPreference(Context context, HistoricalNotificationInfo info) {
            super(context);
            setLayoutResource(R.layout.notification_log_row);
            this.mInfo = info;
        }

        public void onBindViewHolder(PreferenceViewHolder row) {
            super.onBindViewHolder(row);
            if (this.mInfo.icon != null) {
                ((ImageView) row.findViewById(R.id.icon)).setImageDrawable(this.mInfo.icon);
            }
            if (this.mInfo.pkgicon != null) {
                ((ImageView) row.findViewById(R.id.pkgicon)).setImageDrawable(this.mInfo.pkgicon);
            }
            ((DateTimeView) row.findViewById(R.id.timestamp)).setTime(this.mInfo.timestamp);
            ((TextView) row.findViewById(R.id.title)).setText(this.mInfo.title);
            ((TextView) row.findViewById(R.id.pkgname)).setText(this.mInfo.pkgname);
            final TextView extra = (TextView) row.findViewById(R.id.extra);
            extra.setText(this.mInfo.extra);
            extra.setVisibility(this.mInfo.timestamp == sLastExpandedTimestamp ? 0 : 8);
            row.itemView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    extra.setVisibility(extra.getVisibility() == 0 ? 8 : 0);
                    HistoricalNotificationPreference.sLastExpandedTimestamp = HistoricalNotificationPreference.this.mInfo.timestamp;
                }
            });
            row.itemView.setAlpha(this.mInfo.active ? 1.0f : 0.5f);
        }

        public void performClick() {
        }
    }

    private void scheduleRefreshList() {
        if (this.mHandler != null) {
            this.mHandler.removeCallbacks(this.mRefreshListRunnable);
            this.mHandler.postDelayed(this.mRefreshListRunnable, 100);
        }
    }

    public void onAttach(Activity activity) {
        logd("onAttach(%s)", activity.getClass().getSimpleName());
        super.onAttach(activity);
        this.mHandler = new Handler(activity.getMainLooper());
        this.mContext = activity;
        this.mPm = this.mContext.getPackageManager();
        this.mNoMan = Stub.asInterface(ServiceManager.getService("notification"));
    }

    public void onDetach() {
        logd("onDetach()", new Object[0]);
        this.mHandler.removeCallbacks(this.mRefreshListRunnable);
        this.mHandler = null;
        super.onDetach();
    }

    public void onPause() {
        try {
            this.mListener.unregisterAsSystemService();
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot unregister listener", e);
        }
        super.onPause();
    }

    public int getMetricsCategory() {
        return 75;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        logd("onActivityCreated(%s)", savedInstanceState);
        super.onActivityCreated(savedInstanceState);
        Utils.forceCustomPadding(getListView(), false);
    }

    public void onResume() {
        logd("onResume()", new Object[0]);
        super.onResume();
        try {
            this.mListener.registerAsSystemService(this.mContext, new ComponentName(this.mContext.getPackageName(), getClass().getCanonicalName()), ActivityManager.getCurrentUser());
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot register listener", e);
        }
        refreshList();
    }

    private void refreshList() {
        List<HistoricalNotificationInfo> infos = loadNotifications();
        if (infos != null) {
            int N = infos.size();
            Object[] objArr = new Object[1];
            int i = 0;
            objArr[0] = Integer.valueOf(N);
            logd("adding %d infos", objArr);
            Collections.sort(infos, this.mNotificationSorter);
            if (getPreferenceScreen() == null) {
                setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getContext()));
            }
            getPreferenceScreen().removeAll();
            while (true) {
                int i2 = i;
                if (i2 < N) {
                    getPreferenceScreen().addPreference(new HistoricalNotificationPreference(getPrefContext(), (HistoricalNotificationInfo) infos.get(i2)));
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }
    }

    private static void logd(String msg, Object... args) {
        String str = TAG;
        String format = (args == null || args.length == 0) ? msg : String.format(msg, args);
        Log.d(str, format);
    }

    private static CharSequence bold(CharSequence cs) {
        if (cs.length() == 0) {
            return cs;
        }
        SpannableString ss = new SpannableString(cs);
        ss.setSpan(new StyleSpan(1), 0, cs.length(), 0);
        return ss;
    }

    private static String getTitleString(Notification n) {
        CharSequence title = null;
        if (n.extras != null) {
            title = n.extras.getCharSequence(NotificationCompat.EXTRA_TITLE);
            if (TextUtils.isEmpty(title)) {
                title = n.extras.getCharSequence(NotificationCompat.EXTRA_TEXT);
            }
        }
        if (TextUtils.isEmpty(title) && !TextUtils.isEmpty(n.tickerText)) {
            title = n.tickerText;
        }
        return String.valueOf(title);
    }

    private static String formatPendingIntent(PendingIntent pi) {
        StringBuilder sb = new StringBuilder();
        IntentSender is = pi.getIntentSender();
        sb.append("Intent(pkg=");
        sb.append(is.getCreatorPackage());
        try {
            if (ActivityManager.getService().isIntentSenderAnActivity(is.getTarget())) {
                sb.append(" (activity)");
            }
        } catch (RemoteException e) {
        }
        sb.append(")");
        return sb.toString();
    }

    private List<HistoricalNotificationInfo> loadNotifications() {
        RemoteException e;
        NotificationStation notificationStation = this;
        int currentUserId = ActivityManager.getCurrentUser();
        AnonymousClass1 anonymousClass1 = null;
        int currentUserId2;
        try {
            StatusBarNotification[] active = notificationStation.mNoMan.getActiveNotifications(notificationStation.mContext.getPackageName());
            StatusBarNotification[] dismissed = notificationStation.mNoMan.getHistoricalNotifications(notificationStation.mContext.getPackageName(), 50);
            List<HistoricalNotificationInfo> list = new ArrayList(active.length + dismissed.length);
            int i = 2;
            r7 = new StatusBarNotification[2][];
            int i2 = 0;
            r7[0] = active;
            int i3 = 1;
            r7[1] = dismissed;
            int length = r7.length;
            int i4 = 0;
            while (i4 < length) {
                StatusBarNotification[] active2;
                int i5;
                int i6;
                StatusBarNotification[] resultset = r7[i4];
                int length2 = resultset.length;
                int i7 = i2;
                while (i7 < length2) {
                    StatusBarNotification sbn = resultset[i7];
                    if (((sbn.getUserId() != -1 ? 1 : i2) & (sbn.getUserId() != currentUserId ? 1 : i2)) != 0) {
                        active2 = active;
                        currentUserId2 = currentUserId;
                        i5 = i2;
                        i2 = 1;
                        i6 = 2;
                    } else {
                        Notification n = sbn.getNotification();
                        HistoricalNotificationInfo info = new HistoricalNotificationInfo(anonymousClass1);
                        info.pkg = sbn.getPackageName();
                        info.user = sbn.getUserId();
                        currentUserId2 = currentUserId;
                        try {
                            info.icon = notificationStation.loadIconDrawable(info.pkg, info.user, n.icon);
                            info.pkgicon = notificationStation.loadPackageIconDrawable(info.pkg, info.user);
                            info.pkgname = notificationStation.loadPackageName(info.pkg);
                            info.title = getTitleString(n);
                            if (TextUtils.isEmpty(info.title)) {
                                info.title = notificationStation.getString(R.string.notification_log_no_title);
                            }
                            info.timestamp = sbn.getPostTime();
                            info.priority = n.priority;
                            info.channel = n.getChannelId();
                            info.key = sbn.getKey();
                            info.active = resultset == active;
                            info.extra = notificationStation.generateExtraText(sbn, info);
                            r3 = new Object[3];
                            active2 = active;
                            i5 = 0;
                            r3[0] = Long.valueOf(info.timestamp);
                            i2 = 1;
                            r3[1] = info.pkg;
                            i6 = 2;
                            r3[2] = info.title;
                            logd("   [%d] %s: %s", r3);
                            list.add(info);
                        } catch (RemoteException e2) {
                            e = e2;
                            Log.e(TAG, "Cannot load Notifications: ", e);
                            return null;
                        }
                    }
                    i7++;
                    i3 = i2;
                    i = i6;
                    currentUserId = currentUserId2;
                    active = active2;
                    anonymousClass1 = null;
                    i2 = i5;
                    notificationStation = this;
                }
                active2 = active;
                currentUserId2 = currentUserId;
                i6 = i;
                i5 = i2;
                i2 = i3;
                i4++;
                anonymousClass1 = null;
                i2 = i5;
                notificationStation = this;
            }
            currentUserId2 = currentUserId;
            return list;
        } catch (RemoteException e3) {
            e = e3;
            currentUserId2 = currentUserId;
            Log.e(TAG, "Cannot load Notifications: ", e);
            return null;
        }
    }

    private CharSequence generateExtraText(StatusBarNotification sbn, HistoricalNotificationInfo info) {
        int vi;
        Ranking rank = new Ranking();
        Notification n = sbn.getNotification();
        SpannableStringBuilder sb = new SpannableStringBuilder();
        String delim = getString(R.string.notification_log_details_delimiter);
        sb.append(bold(getString(R.string.notification_log_details_package))).append(delim).append(info.pkg).append("\n").append(bold(getString(R.string.notification_log_details_key))).append(delim).append(sbn.getKey());
        sb.append("\n").append(bold(getString(R.string.notification_log_details_icon))).append(delim).append(String.valueOf(n.getSmallIcon()));
        sb.append("\n").append(bold("channelId")).append(delim).append(String.valueOf(n.getChannelId()));
        sb.append("\n").append(bold("postTime")).append(delim).append(String.valueOf(sbn.getPostTime()));
        if (n.getTimeoutAfter() != 0) {
            sb.append("\n").append(bold("timeoutAfter")).append(delim).append(String.valueOf(n.getTimeoutAfter()));
        }
        if (sbn.isGroup()) {
            sb.append("\n").append(bold(getString(R.string.notification_log_details_group))).append(delim).append(String.valueOf(sbn.getGroupKey()));
            if (n.isGroupSummary()) {
                sb.append(bold(getString(R.string.notification_log_details_group_summary)));
            }
        }
        sb.append("\n").append(bold(getString(R.string.notification_log_details_sound))).append(delim);
        if ((n.defaults & 1) != 0) {
            sb.append(getString(R.string.notification_log_details_default));
        } else if (n.sound != null) {
            sb.append(n.sound.toString());
        } else {
            sb.append(getString(R.string.notification_log_details_none));
        }
        sb.append("\n").append(bold(getString(R.string.notification_log_details_vibrate))).append(delim);
        if ((n.defaults & 2) != 0) {
            sb.append(getString(R.string.notification_log_details_default));
        } else if (n.vibrate != null) {
            for (vi = 0; vi < n.vibrate.length; vi++) {
                if (vi > 0) {
                    sb.append(',');
                }
                sb.append(String.valueOf(n.vibrate[vi]));
            }
        } else {
            sb.append(getString(R.string.notification_log_details_none));
        }
        sb.append("\n").append(bold(getString(R.string.notification_log_details_visibility))).append(delim).append(Notification.visibilityToString(n.visibility));
        if (n.publicVersion != null) {
            sb.append("\n").append(bold(getString(R.string.notification_log_details_public_version))).append(delim).append(getTitleString(n.publicVersion));
        }
        sb.append("\n").append(bold(getString(R.string.notification_log_details_priority))).append(delim).append(Notification.priorityToString(n.priority));
        if (info.active) {
            if (this.mRanking != null && this.mRanking.getRanking(sbn.getKey(), rank)) {
                sb.append("\n").append(bold(getString(R.string.notification_log_details_importance))).append(delim).append(Ranking.importanceToString(rank.getImportance()));
                if (rank.getImportanceExplanation() != null) {
                    sb.append("\n").append(bold(getString(R.string.notification_log_details_explanation))).append(delim).append(rank.getImportanceExplanation());
                }
                sb.append("\n").append(bold(getString(R.string.notification_log_details_badge))).append(delim).append(Boolean.toString(rank.canShowBadge()));
            } else if (this.mRanking == null) {
                sb.append("\n").append(bold(getString(R.string.notification_log_details_ranking_null)));
            } else {
                sb.append("\n").append(bold(getString(R.string.notification_log_details_ranking_none)));
            }
        }
        if (n.contentIntent != null) {
            sb.append("\n").append(bold(getString(R.string.notification_log_details_content_intent))).append(delim).append(formatPendingIntent(n.contentIntent));
        }
        if (n.deleteIntent != null) {
            sb.append("\n").append(bold(getString(R.string.notification_log_details_delete_intent))).append(delim).append(formatPendingIntent(n.deleteIntent));
        }
        if (n.fullScreenIntent != null) {
            sb.append("\n").append(bold(getString(R.string.notification_log_details_full_screen_intent))).append(delim).append(formatPendingIntent(n.fullScreenIntent));
        }
        if (n.actions != null && n.actions.length > 0) {
            sb.append("\n").append(bold(getString(R.string.notification_log_details_actions)));
            for (vi = 0; vi < n.actions.length; vi++) {
                Action action = n.actions[vi];
                sb.append("\n  ").append(String.valueOf(vi)).append(' ').append(bold(getString(R.string.notification_log_details_title))).append(delim).append(action.title);
                if (action.actionIntent != null) {
                    sb.append("\n    ").append(bold(getString(R.string.notification_log_details_content_intent))).append(delim).append(formatPendingIntent(action.actionIntent));
                }
                if (action.getRemoteInputs() != null) {
                    sb.append("\n    ").append(bold(getString(R.string.notification_log_details_remoteinput))).append(delim).append(String.valueOf(action.getRemoteInputs().length));
                }
            }
        }
        if (n.contentView != null) {
            sb.append("\n").append(bold(getString(R.string.notification_log_details_content_view))).append(delim).append(n.contentView.toString());
        }
        if (n.extras != null && n.extras.size() > 0) {
            sb.append("\n").append(bold(getString(R.string.notification_log_details_extras)));
            for (String extraKey : n.extras.keySet()) {
                String val = String.valueOf(n.extras.get(extraKey));
                if (val.length() > 100) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(val.substring(0, 100));
                    stringBuilder.append("...");
                    val = stringBuilder.toString();
                }
                sb.append("\n  ").append(extraKey).append(delim).append(val);
            }
        }
        Parcel p = Parcel.obtain();
        n.writeToParcel(p, 0);
        sb.append("\n").append(bold(getString(R.string.notification_log_details_parcel))).append(delim).append(String.valueOf(p.dataPosition())).append(' ').append(bold(getString(R.string.notification_log_details_ashmem))).append(delim).append(String.valueOf(p.getBlobAshmemSize())).append("\n");
        return sb;
    }

    private Resources getResourcesForUserPackage(String pkg, int userId) {
        Resources r;
        if (pkg != null) {
            if (userId == -1) {
                userId = 0;
            }
            try {
                r = this.mPm.getResourcesForApplicationAsUser(pkg, userId);
            } catch (NameNotFoundException ex) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Icon package not found: ");
                stringBuilder.append(pkg);
                Log.e(str, stringBuilder.toString(), ex);
                return null;
            }
        }
        r = this.mContext.getResources();
        return r;
    }

    private Drawable loadPackageIconDrawable(String pkg, int userId) {
        try {
            return this.mPm.getApplicationIcon(pkg);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Cannot get application icon", e);
            return null;
        }
    }

    private CharSequence loadPackageName(String pkg) {
        try {
            ApplicationInfo info = this.mPm.getApplicationInfo(pkg, 4194304);
            if (info != null) {
                return this.mPm.getApplicationLabel(info);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Cannot load package name", e);
        }
        return pkg;
    }

    private Drawable loadIconDrawable(String pkg, int userId, int resId) {
        Resources r = getResourcesForUserPackage(pkg, userId);
        if (resId == 0 || r == null) {
            return null;
        }
        try {
            return r.getDrawable(resId, null);
        } catch (RuntimeException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Icon not found in ");
            stringBuilder.append(pkg != null ? Integer.valueOf(resId) : "<system>");
            stringBuilder.append(": ");
            stringBuilder.append(Integer.toHexString(resId));
            Log.w(str, stringBuilder.toString(), e);
            return null;
        }
    }
}
