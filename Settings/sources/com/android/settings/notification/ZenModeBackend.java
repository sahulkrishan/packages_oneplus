package com.android.settings.notification;

import android.app.ActivityManager;
import android.app.AutomaticZenRule;
import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.content.Context;
import android.provider.Settings.Global;
import android.service.notification.ZenModeConfig;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.R;

public class ZenModeBackend {
    protected static final int SOURCE_NONE = -1;
    @VisibleForTesting
    protected static final String ZEN_MODE_FROM_ANYONE = "zen_mode_from_anyone";
    @VisibleForTesting
    protected static final String ZEN_MODE_FROM_CONTACTS = "zen_mode_from_contacts";
    @VisibleForTesting
    protected static final String ZEN_MODE_FROM_NONE = "zen_mode_from_none";
    @VisibleForTesting
    protected static final String ZEN_MODE_FROM_STARRED = "zen_mode_from_starred";
    private static ZenModeBackend sInstance;
    private String TAG = "ZenModeSettingsBackend";
    private final Context mContext;
    private final NotificationManager mNotificationManager;
    protected Policy mPolicy;
    protected int mZenMode;

    public static ZenModeBackend getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ZenModeBackend(context);
        }
        return sInstance;
    }

    public ZenModeBackend(Context context) {
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        updateZenMode();
        updatePolicy();
    }

    /* Access modifiers changed, original: protected */
    public void updatePolicy() {
        if (this.mNotificationManager != null) {
            this.mPolicy = this.mNotificationManager.getNotificationPolicy();
        }
    }

    /* Access modifiers changed, original: protected */
    public void updateZenMode() {
        this.mZenMode = Global.getInt(this.mContext.getContentResolver(), "zen_mode", this.mZenMode);
    }

    /* Access modifiers changed, original: protected */
    public boolean setZenRule(String id, AutomaticZenRule rule) {
        return NotificationManager.from(this.mContext).updateAutomaticZenRule(id, rule);
    }

    /* Access modifiers changed, original: protected */
    public void setZenMode(int zenMode) {
        NotificationManager.from(this.mContext).setZenMode(zenMode, null, this.TAG);
        this.mZenMode = getZenMode();
    }

    /* Access modifiers changed, original: protected */
    public void setZenModeForDuration(int minutes) {
        this.mNotificationManager.setZenMode(1, ZenModeConfig.toTimeCondition(this.mContext, minutes, ActivityManager.getCurrentUser(), true).id, this.TAG);
        this.mZenMode = getZenMode();
    }

    /* Access modifiers changed, original: protected */
    public int getZenMode() {
        this.mZenMode = Global.getInt(this.mContext.getContentResolver(), "zen_mode", this.mZenMode);
        return this.mZenMode;
    }

    /* Access modifiers changed, original: protected */
    public boolean isVisualEffectSuppressed(int visualEffect) {
        return (this.mPolicy.suppressedVisualEffects & visualEffect) != 0;
    }

    /* Access modifiers changed, original: protected */
    public boolean isPriorityCategoryEnabled(int categoryType) {
        return (this.mPolicy.priorityCategories & categoryType) != 0;
    }

    /* Access modifiers changed, original: protected */
    public int getNewPriorityCategories(boolean allow, int categoryType) {
        int priorityCategories = this.mPolicy.priorityCategories;
        if (allow) {
            return priorityCategories | categoryType;
        }
        return priorityCategories & (~categoryType);
    }

    /* Access modifiers changed, original: protected */
    public int getPriorityCallSenders() {
        if (isPriorityCategoryEnabled(8)) {
            return this.mPolicy.priorityCallSenders;
        }
        return -1;
    }

    /* Access modifiers changed, original: protected */
    public int getPriorityMessageSenders() {
        if (isPriorityCategoryEnabled(4)) {
            return this.mPolicy.priorityMessageSenders;
        }
        return -1;
    }

    /* Access modifiers changed, original: protected */
    public void saveVisualEffectsPolicy(int category, boolean suppress) {
        Global.putInt(this.mContext.getContentResolver(), "zen_settings_updated", 1);
        savePolicy(this.mPolicy.priorityCategories, this.mPolicy.priorityCallSenders, this.mPolicy.priorityMessageSenders, getNewSuppressedEffects(suppress, category));
    }

    /* Access modifiers changed, original: protected */
    public void saveSoundPolicy(int category, boolean allow) {
        savePolicy(getNewPriorityCategories(allow, category), this.mPolicy.priorityCallSenders, this.mPolicy.priorityMessageSenders, this.mPolicy.suppressedVisualEffects);
    }

    /* Access modifiers changed, original: protected */
    public void savePolicy(int priorityCategories, int priorityCallSenders, int priorityMessageSenders, int suppressedVisualEffects) {
        this.mPolicy = new Policy(priorityCategories, priorityCallSenders, priorityMessageSenders, suppressedVisualEffects);
        this.mNotificationManager.setNotificationPolicy(this.mPolicy);
    }

    private int getNewSuppressedEffects(boolean suppress, int effectType) {
        int effects = this.mPolicy.suppressedVisualEffects;
        if (suppress) {
            effects |= effectType;
        } else {
            effects &= ~effectType;
        }
        return clearDeprecatedEffects(effects);
    }

    private int clearDeprecatedEffects(int effects) {
        return effects & -4;
    }

    /* Access modifiers changed, original: protected */
    public boolean isEffectAllowed(int effect) {
        return (this.mPolicy.suppressedVisualEffects & effect) == 0;
    }

    /* Access modifiers changed, original: protected */
    public void saveSenders(int category, int val) {
        int priorityCallSenders = getPriorityCallSenders();
        int priorityMessagesSenders = getPriorityMessageSenders();
        int categorySenders = getPrioritySenders(category);
        boolean allowSenders = val != -1;
        int allowSendersFrom = val == -1 ? categorySenders : val;
        String stringCategory = "";
        if (category == 8) {
            stringCategory = "Calls";
            priorityCallSenders = allowSendersFrom;
        }
        if (category == 4) {
            stringCategory = "Messages";
            priorityMessagesSenders = allowSendersFrom;
        }
        savePolicy(getNewPriorityCategories(allowSenders, category), priorityCallSenders, priorityMessagesSenders, this.mPolicy.suppressedVisualEffects);
        if (ZenModeSettingsBase.DEBUG) {
            String str = this.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onPrefChange allow");
            stringBuilder.append(stringCategory);
            stringBuilder.append("=");
            stringBuilder.append(allowSenders);
            stringBuilder.append(" allow");
            stringBuilder.append(stringCategory);
            stringBuilder.append("From=");
            stringBuilder.append(ZenModeConfig.sourceToString(allowSendersFrom));
            Log.d(str, stringBuilder.toString());
        }
    }

    /* Access modifiers changed, original: protected */
    public String getSendersKey(int category) {
        int i = -1;
        switch (getZenMode()) {
            case 2:
            case 3:
                return getKeyFromSetting(-1);
            default:
                int prioritySenders = getPrioritySenders(category);
                if (isPriorityCategoryEnabled(category)) {
                    i = prioritySenders;
                }
                return getKeyFromSetting(i);
        }
    }

    private int getPrioritySenders(int category) {
        if (category == 8) {
            return getPriorityCallSenders();
        }
        if (category == 4) {
            return getPriorityMessageSenders();
        }
        return -1;
    }

    protected static String getKeyFromSetting(int contactType) {
        switch (contactType) {
            case 0:
                return ZEN_MODE_FROM_ANYONE;
            case 1:
                return ZEN_MODE_FROM_CONTACTS;
            case 2:
                return ZEN_MODE_FROM_STARRED;
            default:
                return ZEN_MODE_FROM_NONE;
        }
    }

    /* Access modifiers changed, original: protected */
    public int getContactsSummary(int category) {
        int contactType = -1;
        if (category == -1) {
            return R.string.zen_mode_from_none;
        }
        if (category == 4) {
            if (isPriorityCategoryEnabled(category)) {
                contactType = getPriorityMessageSenders();
            }
        } else if (category == 8 && isPriorityCategoryEnabled(category)) {
            contactType = getPriorityCallSenders();
        }
        switch (contactType) {
            case 0:
                return R.string.zen_mode_from_anyone;
            case 1:
                return R.string.zen_mode_from_contacts;
            case 2:
                return R.string.zen_mode_from_starred;
            default:
                return R.string.zen_mode_from_none;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0049 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004c A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004b A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004a A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0049 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004c A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004b A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004a A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0049 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004c A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004b A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004a A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0049 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004c A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004b A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004a A:{RETURN} */
    protected static int getSettingFromPrefKey(java.lang.String r6) {
        /*
        r0 = r6.hashCode();
        r1 = -946901971; // 0xffffffffc78f6c2d float:-73432.35 double:NaN;
        r2 = 1;
        r3 = 0;
        r4 = 2;
        r5 = -1;
        if (r0 == r1) goto L_0x003b;
    L_0x000d:
        r1 = -423126328; // 0xffffffffe6c79ac8 float:-4.7130307E23 double:NaN;
        if (r0 == r1) goto L_0x0031;
    L_0x0012:
        r1 = 187510959; // 0xb2d30af float:3.3355214E-32 double:9.2642723E-316;
        if (r0 == r1) goto L_0x0027;
    L_0x0017:
        r1 = 462773226; // 0x1b955bea float:2.470938E-22 double:2.28640353E-315;
        if (r0 == r1) goto L_0x001d;
    L_0x001c:
        goto L_0x0045;
    L_0x001d:
        r0 = "zen_mode_from_starred";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x0045;
    L_0x0025:
        r0 = r4;
        goto L_0x0046;
    L_0x0027:
        r0 = "zen_mode_from_anyone";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x0045;
    L_0x002f:
        r0 = r3;
        goto L_0x0046;
    L_0x0031:
        r0 = "zen_mode_from_contacts";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x0045;
    L_0x0039:
        r0 = r2;
        goto L_0x0046;
    L_0x003b:
        r0 = "zen_mode_from_none";
        r0 = r6.equals(r0);
        if (r0 == 0) goto L_0x0045;
    L_0x0043:
        r0 = 3;
        goto L_0x0046;
    L_0x0045:
        r0 = r5;
    L_0x0046:
        switch(r0) {
            case 0: goto L_0x004c;
            case 1: goto L_0x004b;
            case 2: goto L_0x004a;
            default: goto L_0x0049;
        };
    L_0x0049:
        return r5;
    L_0x004a:
        return r4;
    L_0x004b:
        return r2;
    L_0x004c:
        return r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.notification.ZenModeBackend.getSettingFromPrefKey(java.lang.String):int");
    }

    public boolean removeZenRule(String ruleId) {
        return NotificationManager.from(this.mContext).removeAutomaticZenRule(ruleId);
    }

    /* Access modifiers changed, original: protected */
    public String addZenRule(AutomaticZenRule rule) {
        try {
            String id = NotificationManager.from(this.mContext).addAutomaticZenRule(rule);
            NotificationManager.from(this.mContext).getAutomaticZenRule(id);
            return id;
        } catch (Exception e) {
            return null;
        }
    }
}
