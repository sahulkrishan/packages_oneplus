package com.android.settings.notification;

import android.app.AutomaticZenRule;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.content.Context;
import android.icu.text.ListFormatter;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.service.notification.ZenModeConfig;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.PreferenceGroup;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class ZenModeSettings extends ZenModeSettingsBase {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.zen_mode_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add("zen_mode_duration_settings");
            keys.add("zen_mode_settings_button_container");
            return keys;
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return ZenModeSettings.buildPreferenceControllers(context, null, null);
        }
    };

    public static class SummaryBuilder {
        private static final int[] ALL_PRIORITY_CATEGORIES = new int[]{32, 64, 128, 4, 2, 1, 8, 16};
        private Context mContext;

        public SummaryBuilder(Context context) {
            this.mContext = context;
        }

        /* Access modifiers changed, original: 0000 */
        public String getSoundSettingSummary(Policy policy) {
            int numCategories = getEnabledCategories(policy, -$$Lambda$ZenModeSettings$SummaryBuilder$-hUbn9epxyVxqc9qNo66a-LO5Ug.INSTANCE).size();
            if (numCategories == 0) {
                return this.mContext.getString(R.string.zen_sound_all_muted);
            }
            if (numCategories == 1) {
                return this.mContext.getString(R.string.zen_sound_one_allowed, new Object[]{((String) enabledCategories.get(0)).toLowerCase()});
            } else if (numCategories == 2) {
                return this.mContext.getString(R.string.zen_sound_two_allowed, new Object[]{((String) enabledCategories.get(0)).toLowerCase(), ((String) enabledCategories.get(1)).toLowerCase()});
            } else if (numCategories != 3) {
                return this.mContext.getString(R.string.zen_sound_none_muted);
            } else {
                return this.mContext.getString(R.string.zen_sound_three_allowed, new Object[]{((String) enabledCategories.get(0)).toLowerCase(), ((String) enabledCategories.get(1)).toLowerCase(), ((String) enabledCategories.get(2)).toLowerCase()});
            }
        }

        static /* synthetic */ boolean lambda$getSoundSettingSummary$0(Integer category) {
            return 32 == category.intValue() || 64 == category.intValue() || 128 == category.intValue();
        }

        /* Access modifiers changed, original: 0000 */
        public String getCallsSettingSummary(Policy policy) {
            int numCategories = getEnabledCategories(policy, -$$Lambda$ZenModeSettings$SummaryBuilder$_Gea8GbwXN997GXaupRdGPPi1FA.INSTANCE).size();
            if (numCategories == 0) {
                return this.mContext.getString(R.string.zen_mode_no_exceptions);
            }
            if (numCategories == 1) {
                return this.mContext.getString(R.string.zen_mode_calls_summary_one, new Object[]{((String) enabledCategories.get(0)).toLowerCase()});
            }
            return this.mContext.getString(R.string.zen_mode_calls_summary_two, new Object[]{((String) enabledCategories.get(0)).toLowerCase(), ((String) enabledCategories.get(1)).toLowerCase()});
        }

        static /* synthetic */ boolean lambda$getCallsSettingSummary$1(Integer category) {
            return 8 == category.intValue() || 16 == category.intValue();
        }

        /* Access modifiers changed, original: 0000 */
        public String getMsgEventReminderSettingSummary(Policy policy) {
            List<String> enabledCategories = getEnabledCategories(policy, -$$Lambda$ZenModeSettings$SummaryBuilder$Ydm8DmhkL6wV0O584-hfIH59p1A.INSTANCE);
            int numCategories = enabledCategories.size();
            if (numCategories == 0) {
                return this.mContext.getString(R.string.zen_mode_no_exceptions);
            }
            if (numCategories == 1) {
                return (String) enabledCategories.get(0);
            }
            List<String> summaries;
            if (numCategories == 2) {
                return this.mContext.getString(R.string.join_two_items, new Object[]{enabledCategories.get(0), ((String) enabledCategories.get(1)).toLowerCase()});
            } else if (numCategories == 3) {
                summaries = new ArrayList();
                summaries.add((String) enabledCategories.get(0));
                summaries.add(((String) enabledCategories.get(1)).toLowerCase());
                summaries.add(((String) enabledCategories.get(2)).toLowerCase());
                return ListFormatter.getInstance().format(summaries);
            } else {
                summaries = new ArrayList();
                summaries.add((String) enabledCategories.get(0));
                summaries.add(((String) enabledCategories.get(1)).toLowerCase());
                summaries.add(((String) enabledCategories.get(2)).toLowerCase());
                summaries.add(this.mContext.getString(R.string.zen_mode_other_options));
                return ListFormatter.getInstance().format(summaries);
            }
        }

        static /* synthetic */ boolean lambda$getMsgEventReminderSettingSummary$2(Integer category) {
            if (2 == category.intValue() || 1 == category.intValue() || 4 == category.intValue()) {
                return true;
            }
            return false;
        }

        /* Access modifiers changed, original: 0000 */
        public String getSoundSummary() {
            if (NotificationManager.from(this.mContext).getZenMode() != 0) {
                if (ZenModeConfig.getDescription(this.mContext, true, NotificationManager.from(this.mContext).getZenModeConfig(), false) == null) {
                    return this.mContext.getString(R.string.zen_mode_sound_summary_on);
                }
                return this.mContext.getString(R.string.zen_mode_sound_summary_on_with_info, new Object[]{description});
            }
            int count = getEnabledAutomaticRulesCount();
            if (count <= 0) {
                return this.mContext.getString(R.string.zen_mode_sound_summary_off);
            }
            Context context = this.mContext;
            Object[] objArr = new Object[1];
            objArr[0] = this.mContext.getResources().getQuantityString(R.plurals.zen_mode_sound_summary_summary_off_info, count, new Object[]{Integer.valueOf(count)});
            return context.getString(R.string.zen_mode_sound_summary_off_with_info, objArr);
        }

        /* Access modifiers changed, original: 0000 */
        public String getBlockedEffectsSummary(Policy policy) {
            if (policy.suppressedVisualEffects == 0) {
                return this.mContext.getResources().getString(R.string.zen_mode_restrict_notifications_summary_muted);
            }
            if (Policy.areAllVisualEffectsSuppressed(policy.suppressedVisualEffects)) {
                return this.mContext.getResources().getString(R.string.zen_mode_restrict_notifications_summary_hidden);
            }
            return this.mContext.getResources().getString(R.string.zen_mode_restrict_notifications_summary_custom);
        }

        /* Access modifiers changed, original: 0000 */
        public String getAutomaticRulesSummary() {
            int count = getEnabledAutomaticRulesCount();
            if (count == 0) {
                return this.mContext.getString(R.string.zen_mode_settings_summary_off);
            }
            return this.mContext.getResources().getQuantityString(R.plurals.zen_mode_settings_summary_on, count, new Object[]{Integer.valueOf(count)});
        }

        /* Access modifiers changed, original: 0000 */
        @VisibleForTesting
        public int getEnabledAutomaticRulesCount() {
            int count = 0;
            Map<String, AutomaticZenRule> ruleMap = NotificationManager.from(this.mContext).getAutomaticZenRules();
            if (ruleMap != null) {
                for (Entry<String, AutomaticZenRule> ruleEntry : ruleMap.entrySet()) {
                    AutomaticZenRule rule = (AutomaticZenRule) ruleEntry.getValue();
                    if (rule != null && rule.isEnabled()) {
                        count++;
                    }
                }
            }
            return count;
        }

        private List<String> getEnabledCategories(Policy policy, Predicate<Integer> filteredCategories) {
            List<String> enabledCategories = new ArrayList();
            for (int category : ALL_PRIORITY_CATEGORIES) {
                if (filteredCategories.test(Integer.valueOf(category)) && isCategoryEnabled(policy, category)) {
                    if (category == 32) {
                        enabledCategories.add(this.mContext.getString(R.string.zen_mode_alarms));
                    } else if (category == 64) {
                        enabledCategories.add(this.mContext.getString(R.string.zen_mode_media));
                    } else if (category == 128) {
                        enabledCategories.add(this.mContext.getString(R.string.zen_mode_system));
                    } else if (category == 4) {
                        if (policy.priorityMessageSenders == 0) {
                            enabledCategories.add(this.mContext.getString(R.string.zen_mode_all_messages));
                        } else {
                            enabledCategories.add(this.mContext.getString(R.string.zen_mode_selected_messages));
                        }
                    } else if (category == 2) {
                        enabledCategories.add(this.mContext.getString(R.string.zen_mode_events));
                    } else if (category == 1) {
                        enabledCategories.add(this.mContext.getString(R.string.zen_mode_reminders));
                    } else if (category == 8) {
                        if (policy.priorityCallSenders == 0) {
                            enabledCategories.add(this.mContext.getString(R.string.zen_mode_all_callers));
                        } else if (policy.priorityCallSenders == 1) {
                            enabledCategories.add(this.mContext.getString(R.string.zen_mode_contacts_callers));
                        } else {
                            enabledCategories.add(this.mContext.getString(R.string.zen_mode_starred_callers));
                        }
                    } else if (category == 16 && !enabledCategories.contains(this.mContext.getString(R.string.zen_mode_all_callers))) {
                        enabledCategories.add(this.mContext.getString(R.string.zen_mode_repeat_callers));
                    }
                }
            }
            return enabledCategories;
        }

        private boolean isCategoryEnabled(Policy policy, int categoryType) {
            return (policy.priorityCategories & categoryType) != 0;
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        PreferenceGroup mSchedulePreferenceGroup = (PreferenceGroup) findPreference("schedule");
        if (mSchedulePreferenceGroup != null && !OPUtils.isSupportSocTriState()) {
            mSchedulePreferenceGroup.setVisible(false);
        }
    }

    public void onResume() {
        super.onResume();
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.zen_mode_settings;
    }

    public int getMetricsCategory() {
        return 76;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle(), getFragmentManager());
    }

    public int getHelpResource() {
        return R.string.help_uri_interruptions;
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Lifecycle lifecycle, FragmentManager fragmentManager) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new OPZenModeTurnOnSettings(context, lifecycle, fragmentManager));
        controllers.add(new ZenModeBehaviorMsgEventReminderPreferenceController(context, lifecycle));
        controllers.add(new ZenModeBehaviorSoundPreferenceController(context, lifecycle));
        controllers.add(new ZenModeBehaviorCallsPreferenceController(context, lifecycle));
        controllers.add(new ZenModeBlockedEffectsPreferenceController(context, lifecycle));
        controllers.add(new ZenModeDurationPreferenceController(context, lifecycle, fragmentManager));
        controllers.add(new ZenModeAutomationPreferenceController(context));
        controllers.add(new ZenModeButtonPreferenceController(context, lifecycle, fragmentManager));
        controllers.add(new ZenModeSettingsFooterPreferenceController(context, lifecycle));
        return controllers;
    }
}
