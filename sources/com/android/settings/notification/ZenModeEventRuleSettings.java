package com.android.settings.notification;

import android.app.AutomaticZenRule;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.CalendarContract.Calendars;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.EventInfo;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import com.oneplus.settings.utils.OPFirewallUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ZenModeEventRuleSettings extends ZenModeRuleSettingsBase {
    public static final String ACTION = "android.settings.ZEN_MODE_EVENT_RULE_SETTINGS";
    private static final Comparator<CalendarInfo> CALENDAR_NAME = new Comparator<CalendarInfo>() {
        public int compare(CalendarInfo lhs, CalendarInfo rhs) {
            return lhs.name.compareTo(rhs.name);
        }
    };
    private static final String KEY_CALENDAR = "calendar";
    private static final String KEY_REPLY = "reply";
    private DropDownPreference mCalendar;
    private List<CalendarInfo> mCalendars;
    private boolean mCreate;
    private EventInfo mEvent;
    private DropDownPreference mReply;

    public static class CalendarInfo {
        public String name;
        public int userId;
    }

    /* Access modifiers changed, original: protected */
    public boolean setRule(AutomaticZenRule rule) {
        EventInfo tryParseEventConditionId;
        if (rule != null) {
            tryParseEventConditionId = ZenModeConfig.tryParseEventConditionId(rule.getConditionId());
        } else {
            tryParseEventConditionId = null;
        }
        this.mEvent = tryParseEventConditionId;
        return this.mEvent != null;
    }

    public void onResume() {
        super.onResume();
        if (!isUiRestricted()) {
            if (!this.mCreate) {
                reloadCalendar();
            }
            this.mCreate = false;
        }
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.zen_mode_event_rule_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        this.mHeader = new ZenAutomaticRuleHeaderPreferenceController(context, this, getLifecycle());
        this.mSwitch = new ZenAutomaticRuleSwitchPreferenceController(context, this, getLifecycle());
        controllers.add(this.mHeader);
        controllers.add(this.mSwitch);
        return controllers;
    }

    private void reloadCalendar() {
        this.mCalendars = getCalendars(this.mContext);
        ArrayList<CharSequence> entries = new ArrayList();
        ArrayList<CharSequence> values = new ArrayList();
        entries.add(getString(R.string.zen_mode_event_rule_calendar_any));
        String eventCalendar = null;
        values.add(key(0, null));
        if (this.mEvent != null) {
            eventCalendar = this.mEvent.calendar;
        }
        boolean found = false;
        for (CalendarInfo calendar : this.mCalendars) {
            entries.add(calendar.name);
            values.add(key(calendar));
            if (eventCalendar != null && eventCalendar.equals(calendar.name)) {
                found = true;
            }
        }
        if (!(eventCalendar == null || found)) {
            entries.add(eventCalendar);
            values.add(key(this.mEvent.userId, eventCalendar));
        }
        this.mCalendar.setEntries((CharSequence[]) entries.toArray(new CharSequence[entries.size()]));
        this.mCalendar.setEntryValues((CharSequence[]) values.toArray(new CharSequence[values.size()]));
    }

    /* Access modifiers changed, original: protected */
    public void onCreateInternal() {
        this.mCreate = true;
        PreferenceScreen root = getPreferenceScreen();
        this.mCalendar = (DropDownPreference) root.findPreference(KEY_CALENDAR);
        this.mCalendar.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String calendarKey = (String) newValue;
                if (calendarKey.equals(ZenModeEventRuleSettings.key(ZenModeEventRuleSettings.this.mEvent))) {
                    return false;
                }
                int i = calendarKey.indexOf(58);
                ZenModeEventRuleSettings.this.mEvent.userId = Integer.parseInt(calendarKey.substring(0, i));
                ZenModeEventRuleSettings.this.mEvent.calendar = calendarKey.substring(i + 1);
                if (ZenModeEventRuleSettings.this.mEvent.calendar.isEmpty()) {
                    ZenModeEventRuleSettings.this.mEvent.calendar = null;
                }
                ZenModeEventRuleSettings.this.updateRule(ZenModeConfig.toEventConditionId(ZenModeEventRuleSettings.this.mEvent));
                return true;
            }
        });
        this.mReply = (DropDownPreference) root.findPreference(KEY_REPLY);
        this.mReply.setEntries(new CharSequence[]{getString(R.string.zen_mode_event_rule_reply_any_except_no), getString(R.string.zen_mode_event_rule_reply_yes_or_maybe), getString(R.string.zen_mode_event_rule_reply_yes)});
        this.mReply.setEntryValues(new CharSequence[]{Integer.toString(0), Integer.toString(1), Integer.toString(2)});
        this.mReply.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int reply = Integer.parseInt((String) newValue);
                if (reply == ZenModeEventRuleSettings.this.mEvent.reply) {
                    return false;
                }
                ZenModeEventRuleSettings.this.mEvent.reply = reply;
                ZenModeEventRuleSettings.this.updateRule(ZenModeConfig.toEventConditionId(ZenModeEventRuleSettings.this.mEvent));
                return true;
            }
        });
        reloadCalendar();
        updateControlsInternal();
    }

    /* Access modifiers changed, original: protected */
    public void updateControlsInternal() {
        this.mCalendar.setValue(key(this.mEvent));
        this.mReply.setValue(Integer.toString(this.mEvent.reply));
    }

    public int getMetricsCategory() {
        return Const.CODE_C1_SPL;
    }

    public static CalendarInfo findCalendar(Context context, EventInfo event) {
        if (context == null || event == null) {
            return null;
        }
        String eventKey = key(event);
        for (CalendarInfo calendar : getCalendars(context)) {
            if (eventKey.equals(key(calendar))) {
                return calendar;
            }
        }
        return null;
    }

    private static List<CalendarInfo> getCalendars(Context context) {
        List<CalendarInfo> calendars = new ArrayList();
        for (UserHandle user : UserManager.get(context).getUserProfiles()) {
            Context userContext = getContextForUser(context, user);
            if (userContext != null) {
                addCalendars(userContext, calendars);
            }
        }
        Collections.sort(calendars, CALENDAR_NAME);
        return calendars;
    }

    private static Context getContextForUser(Context context, UserHandle user) {
        try {
            return context.createPackageContextAsUser(context.getPackageName(), 0, user);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public static void addCalendars(Context context, List<CalendarInfo> outCalendars) {
        String primary = "\"primary\"";
        String selection = "\"primary\" = 1";
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Calendars.CONTENT_URI, new String[]{OPFirewallUtils._ID, "calendar_displayName", "(account_name=ownerAccount) AS \"primary\""}, "\"primary\" = 1", null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    CalendarInfo ci = new CalendarInfo();
                    ci.name = cursor.getString(1);
                    ci.userId = context.getUserId();
                    outCalendars.add(ci);
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static String key(CalendarInfo calendar) {
        return key(calendar.userId, calendar.name);
    }

    private static String key(EventInfo event) {
        return key(event.userId, event.calendar);
    }

    private static String key(int userId, String calendar) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(EventInfo.resolveUserId(userId));
        stringBuilder.append(":");
        stringBuilder.append(calendar == null ? "" : calendar);
        return stringBuilder.toString();
    }
}
