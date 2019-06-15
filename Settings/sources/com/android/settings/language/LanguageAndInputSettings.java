package com.android.settings.language;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.speech.tts.TtsEngines;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.android.settings.R;
import com.android.settings.applications.defaultapps.DefaultAutofillPreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.inputmethod.PhysicalKeyboardPreferenceController;
import com.android.settings.inputmethod.SpellCheckerPreferenceController;
import com.android.settings.inputmethod.VirtualKeyboardPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.widget.PreferenceCategoryController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LanguageAndInputSettings extends DashboardFragment {
    private static final String KEY_GAME_CONTROLLER_CATEGORY = "game_controller_settings_category";
    private static final String KEY_KEYBOARDS_CATEGORY = "keyboards_category";
    private static final String KEY_PHYSICAL_KEYBOARD = "physical_keyboard_pref";
    private static final String KEY_POINTER_AND_TTS_CATEGORY = "pointer_and_tts_category";
    private static final String KEY_TEXT_TO_SPEECH = "tts_settings_summary";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.language_and_input;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return LanguageAndInputSettings.buildPreferenceControllers(context, null);
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add(LanguageAndInputSettings.KEY_TEXT_TO_SPEECH);
            keys.add(LanguageAndInputSettings.KEY_PHYSICAL_KEYBOARD);
            return keys;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = -$$Lambda$LanguageAndInputSettings$VvwbgRiPWoRSuoMu5QPyPqZ5AEc.INSTANCE;
    private static final String TAG = "LangAndInputSettings";

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            if (listening) {
                String flattenComponent = Secure.getString(contentResolver, "default_input_method");
                if (!TextUtils.isEmpty(flattenComponent)) {
                    PackageManager packageManage = this.mContext.getPackageManager();
                    String pkg = ComponentName.unflattenFromString(flattenComponent).getPackageName();
                    for (InputMethodInfo imi : ((InputMethodManager) this.mContext.getSystemService("input_method")).getInputMethodList()) {
                        if (TextUtils.equals(imi.getPackageName(), pkg)) {
                            this.mSummaryLoader.setSummary(this, imi.loadLabel(packageManage));
                            return;
                        }
                    }
                }
                this.mSummaryLoader.setSummary(this, "");
            }
        }
    }

    public int getMetricsCategory() {
        return 750;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(R.string.language_settings);
        }
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.language_and_input;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(@NonNull Context context, @Nullable Lifecycle lifecycle) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new PhoneLanguagePreferenceController(context));
        controllers.add(new OPPhoneLanguageH2PreferenceController(context));
        VirtualKeyboardPreferenceController virtualKeyboardPreferenceController = new VirtualKeyboardPreferenceController(context);
        PhysicalKeyboardPreferenceController physicalKeyboardPreferenceController = new PhysicalKeyboardPreferenceController(context, lifecycle);
        controllers.add(virtualKeyboardPreferenceController);
        controllers.add(physicalKeyboardPreferenceController);
        controllers.add(new PreferenceCategoryController(context, KEY_KEYBOARDS_CATEGORY).setChildren(Arrays.asList(new AbstractPreferenceController[]{virtualKeyboardPreferenceController, physicalKeyboardPreferenceController})));
        controllers.add(new TtsPreferenceController(context, new TtsEngines(context)));
        controllers.add(new PointerSpeedController(context));
        controllers.add(new PreferenceCategoryController(context, KEY_POINTER_AND_TTS_CATEGORY).setChildren(Arrays.asList(new AbstractPreferenceController[]{pointerController, ttsPreferenceController})));
        controllers.add(new SpellCheckerPreferenceController(context));
        controllers.add(new DefaultAutofillPreferenceController(context));
        controllers.add(new UserDictionaryPreferenceController(context));
        return controllers;
    }
}
