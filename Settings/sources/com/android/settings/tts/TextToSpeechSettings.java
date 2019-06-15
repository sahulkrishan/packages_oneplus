package com.android.settings.tts;

import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.EngineInfo;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TtsEngines;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.widget.ActionButtonPreference;
import com.android.settings.widget.GearPreference;
import com.android.settings.widget.GearPreference.OnGearClickListener;
import com.android.settings.widget.SeekBarPreference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Set;

public class TextToSpeechSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnGearClickListener, Indexable {
    private static final boolean DBG = false;
    private static final int GET_SAMPLE_TEXT = 1983;
    private static final String KEY_ACTION_BUTTONS = "action_buttons";
    private static final String KEY_DEFAULT_PITCH = "tts_default_pitch";
    private static final String KEY_DEFAULT_RATE = "tts_default_rate";
    private static final String KEY_ENGINE_LOCALE = "tts_default_lang";
    private static final String KEY_TTS_ENGINE_PREFERENCE = "tts_engine_preference";
    private static final int MAX_SPEECH_PITCH = 400;
    private static final int MAX_SPEECH_RATE = 600;
    private static final int MIN_SPEECH_PITCH = 25;
    private static final int MIN_SPEECH_RATE = 10;
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.tts_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add(TextToSpeechSettings.KEY_TTS_ENGINE_PREFERENCE);
            return keys;
        }
    };
    private static final String STATE_KEY_LOCALE_ENTRIES = "locale_entries";
    private static final String STATE_KEY_LOCALE_ENTRY_VALUES = "locale_entry_values";
    private static final String STATE_KEY_LOCALE_VALUE = "locale_value";
    private static final String TAG = "TextToSpeechSettings";
    private static final int VOICE_DATA_INTEGRITY_CHECK = 1977;
    private ActionButtonPreference mActionButtons;
    private List<String> mAvailableStrLocals;
    private Locale mCurrentDefaultLocale;
    private String mCurrentEngine;
    private int mDefaultPitch = 100;
    private SeekBarPreference mDefaultPitchPref;
    private int mDefaultRate = 100;
    private SeekBarPreference mDefaultRatePref;
    private TtsEngines mEnginesHelper = null;
    private final OnInitListener mInitListener = new OnInitListener() {
        public void onInit(int status) {
            TextToSpeechSettings.this.onInitEngine(status);
        }
    };
    private ListPreference mLocalePreference;
    private String mSampleText = null;
    private int mSelectedLocaleIndex = -1;
    private TextToSpeech mTts = null;

    public int getMetricsCategory() {
        return 94;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tts_settings);
        getActivity().setVolumeControlStream(3);
        this.mEnginesHelper = new TtsEngines(getActivity().getApplicationContext());
        this.mLocalePreference = (ListPreference) findPreference(KEY_ENGINE_LOCALE);
        this.mLocalePreference.setOnPreferenceChangeListener(this);
        this.mDefaultPitchPref = (SeekBarPreference) findPreference(KEY_DEFAULT_PITCH);
        this.mDefaultRatePref = (SeekBarPreference) findPreference(KEY_DEFAULT_RATE);
        ActionButtonPreference button1OnClickListener = ((ActionButtonPreference) findPreference(KEY_ACTION_BUTTONS)).setButton1Text(R.string.tts_play).setButton1Positive(true).setButton1OnClickListener(new -$$Lambda$TextToSpeechSettings$-mqMfqhP2l_0b2lu0aliM8gSxIQ(this));
        boolean z = false;
        this.mActionButtons = button1OnClickListener.setButton1Enabled(false).setButton2Text(R.string.tts_reset).setButton2Positive(false).setButton2OnClickListener(new -$$Lambda$TextToSpeechSettings$-PSeoELUhAn9aTlkws2o7dPjqCc(this)).setButton1Enabled(true);
        if (savedInstanceState == null) {
            this.mLocalePreference.setEnabled(false);
            this.mLocalePreference.setEntries(new CharSequence[0]);
            this.mLocalePreference.setEntryValues(new CharSequence[0]);
        } else {
            CharSequence[] entries = savedInstanceState.getCharSequenceArray(STATE_KEY_LOCALE_ENTRIES);
            CharSequence[] entryValues = savedInstanceState.getCharSequenceArray(STATE_KEY_LOCALE_ENTRY_VALUES);
            CharSequence value = savedInstanceState.getCharSequence(STATE_KEY_LOCALE_VALUE);
            this.mLocalePreference.setEntries(entries);
            this.mLocalePreference.setEntryValues(entryValues);
            this.mLocalePreference.setValue(value != null ? value.toString() : null);
            ListPreference listPreference = this.mLocalePreference;
            if (entries.length > 0) {
                z = true;
            }
            listPreference.setEnabled(z);
        }
        this.mTts = new TextToSpeech(getActivity().getApplicationContext(), this.mInitListener);
        setTtsUtteranceProgressListener();
        initSettings();
        setRetainInstance(true);
    }

    public void onResume() {
        super.onResume();
        if (this.mTts != null && this.mCurrentDefaultLocale != null && this.mTts.getDefaultEngine() != null) {
            if (this.mTts.getDefaultEngine().equals(this.mTts.getCurrentEngine())) {
                this.mTts.setPitch(((float) Secure.getInt(getContentResolver(), KEY_DEFAULT_PITCH, 100)) / 100.0f);
            } else {
                try {
                    this.mTts.shutdown();
                    this.mTts = null;
                } catch (Exception e) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Error shutting down TTS engine");
                    stringBuilder.append(e);
                    Log.e(str, stringBuilder.toString());
                }
                this.mTts = new TextToSpeech(getActivity().getApplicationContext(), this.mInitListener);
                setTtsUtteranceProgressListener();
                initSettings();
            }
            Locale ttsDefaultLocale = this.mTts.getDefaultLanguage();
            if (!(this.mCurrentDefaultLocale == null || this.mCurrentDefaultLocale.equals(ttsDefaultLocale))) {
                updateWidgetState(false);
                checkDefaultLocale();
            }
        }
    }

    private void setTtsUtteranceProgressListener() {
        if (this.mTts != null) {
            this.mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                public void onStart(String utteranceId) {
                }

                public void onDone(String utteranceId) {
                }

                public void onError(String utteranceId) {
                    Log.e(TextToSpeechSettings.TAG, "Error while trying to synthesize sample text");
                }
            });
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mTts != null) {
            this.mTts.shutdown();
            this.mTts = null;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequenceArray(STATE_KEY_LOCALE_ENTRIES, this.mLocalePreference.getEntries());
        outState.putCharSequenceArray(STATE_KEY_LOCALE_ENTRY_VALUES, this.mLocalePreference.getEntryValues());
        outState.putCharSequence(STATE_KEY_LOCALE_VALUE, this.mLocalePreference.getValue());
    }

    private void initSettings() {
        ContentResolver resolver = getContentResolver();
        this.mDefaultRate = Secure.getInt(resolver, KEY_DEFAULT_RATE, 100);
        this.mDefaultPitch = Secure.getInt(resolver, KEY_DEFAULT_PITCH, 100);
        this.mDefaultRatePref.setProgress(getSeekBarProgressFromValue(KEY_DEFAULT_RATE, this.mDefaultRate));
        this.mDefaultRatePref.setOnPreferenceChangeListener(this);
        this.mDefaultRatePref.setMax(getSeekBarProgressFromValue(KEY_DEFAULT_RATE, 600));
        this.mDefaultPitchPref.setProgress(getSeekBarProgressFromValue(KEY_DEFAULT_PITCH, this.mDefaultPitch));
        this.mDefaultPitchPref.setOnPreferenceChangeListener(this);
        this.mDefaultPitchPref.setMax(getSeekBarProgressFromValue(KEY_DEFAULT_PITCH, 400));
        if (this.mTts != null) {
            this.mCurrentEngine = this.mTts.getCurrentEngine();
            this.mTts.setSpeechRate(((float) this.mDefaultRate) / 100.0f);
            this.mTts.setPitch(((float) this.mDefaultPitch) / 100.0f);
        }
        if (getActivity() instanceof SettingsActivity) {
            getActivity();
            if (this.mCurrentEngine != null) {
                EngineInfo info = this.mEnginesHelper.getEngineInfo(this.mCurrentEngine);
                Preference mEnginePreference = findPreference(KEY_TTS_ENGINE_PREFERENCE);
                ((GearPreference) mEnginePreference).setOnGearClickListener(this);
                mEnginePreference.setSummary(info.label);
            }
            checkVoiceData(this.mCurrentEngine);
            return;
        }
        throw new IllegalStateException("TextToSpeechSettings used outside a Settings");
    }

    private int getValueFromSeekBarProgress(String preferenceKey, int progress) {
        if (preferenceKey.equals(KEY_DEFAULT_RATE)) {
            return 10 + progress;
        }
        if (preferenceKey.equals(KEY_DEFAULT_PITCH)) {
            return 25 + progress;
        }
        return progress;
    }

    private int getSeekBarProgressFromValue(String preferenceKey, int value) {
        if (preferenceKey.equals(KEY_DEFAULT_RATE)) {
            return value - 10;
        }
        if (preferenceKey.equals(KEY_DEFAULT_PITCH)) {
            return value - 25;
        }
        return value;
    }

    public void onInitEngine(int status) {
        if (status == 0) {
            checkDefaultLocale();
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    TextToSpeechSettings.this.mLocalePreference.setEnabled(true);
                }
            });
            return;
        }
        updateWidgetState(false);
    }

    private void checkDefaultLocale() {
        Locale defaultLocale = this.mTts.getDefaultLanguage();
        if (defaultLocale == null) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to get default language from engine ");
            stringBuilder.append(this.mCurrentEngine);
            Log.e(str, stringBuilder.toString());
            updateWidgetState(false);
            return;
        }
        Locale oldDefaultLocale = this.mCurrentDefaultLocale;
        this.mCurrentDefaultLocale = this.mEnginesHelper.parseLocaleString(defaultLocale.toString());
        if (!Objects.equals(oldDefaultLocale, this.mCurrentDefaultLocale)) {
            this.mSampleText = null;
        }
        int defaultAvailable = this.mTts.setLanguage(defaultLocale);
        if (evaluateDefaultLocale() && this.mSampleText == null) {
            getSampleText();
        }
    }

    private boolean evaluateDefaultLocale() {
        if (this.mCurrentDefaultLocale == null || this.mAvailableStrLocals == null) {
            return false;
        }
        boolean notInAvailableLangauges = true;
        try {
            StringBuilder stringBuilder;
            String defaultLocaleStr = this.mCurrentDefaultLocale.getISO3Language();
            if (!TextUtils.isEmpty(this.mCurrentDefaultLocale.getISO3Country())) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(defaultLocaleStr);
                stringBuilder.append("-");
                stringBuilder.append(this.mCurrentDefaultLocale.getISO3Country());
                defaultLocaleStr = stringBuilder.toString();
            }
            if (!TextUtils.isEmpty(this.mCurrentDefaultLocale.getVariant())) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(defaultLocaleStr);
                stringBuilder.append("-");
                stringBuilder.append(this.mCurrentDefaultLocale.getVariant());
                defaultLocaleStr = stringBuilder.toString();
            }
            for (String loc : this.mAvailableStrLocals) {
                if (loc.equalsIgnoreCase(defaultLocaleStr)) {
                    notInAvailableLangauges = false;
                    break;
                }
            }
            int defaultAvailable = this.mTts.setLanguage(this.mCurrentDefaultLocale);
            if (defaultAvailable == -2 || defaultAvailable == -1 || notInAvailableLangauges) {
                updateWidgetState(false);
                return false;
            }
            updateWidgetState(true);
            return true;
        } catch (MissingResourceException e) {
            updateWidgetState(false);
            return false;
        }
    }

    private void getSampleText() {
        String currentEngine = this.mTts.getCurrentEngine();
        if (TextUtils.isEmpty(currentEngine)) {
            currentEngine = this.mTts.getDefaultEngine();
        }
        Intent intent = new Intent("android.speech.tts.engine.GET_SAMPLE_TEXT");
        intent.putExtra("language", this.mCurrentDefaultLocale.getLanguage());
        intent.putExtra("country", this.mCurrentDefaultLocale.getCountry());
        intent.putExtra("variant", this.mCurrentDefaultLocale.getVariant());
        intent.setPackage(currentEngine);
        try {
            startActivityForResult(intent, GET_SAMPLE_TEXT);
        } catch (ActivityNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to get sample text, no activity found for ");
            stringBuilder.append(intent);
            stringBuilder.append(")");
            Log.e(str, stringBuilder.toString());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_SAMPLE_TEXT) {
            onSampleTextReceived(resultCode, data);
        } else if (requestCode == VOICE_DATA_INTEGRITY_CHECK) {
            onVoiceDataIntegrityCheckDone(data);
            if (resultCode != 0) {
                updateDefaultLocalePref(data);
            }
        }
    }

    private void updateDefaultLocalePref(Intent data) {
        ArrayList<String> availableLangs = data.getStringArrayListExtra("availableVoices");
        ArrayList<String> unavailableLangs = data.getStringArrayListExtra("unavailableVoices");
        if (availableLangs == null || availableLangs.size() == 0) {
            this.mLocalePreference.setEnabled(false);
            return;
        }
        Locale currentLocale = null;
        if (!this.mEnginesHelper.isLocaleSetToDefaultForEngine(this.mTts.getCurrentEngine())) {
            currentLocale = this.mEnginesHelper.getLocalePrefForEngine(this.mTts.getCurrentEngine());
        }
        ArrayList<Pair<String, Locale>> entryPairs = new ArrayList(availableLangs.size());
        for (int i = 0; i < availableLangs.size(); i++) {
            Locale locale = this.mEnginesHelper.parseLocaleString((String) availableLangs.get(i));
            if (locale != null) {
                entryPairs.add(new Pair(locale.getDisplayName(), locale));
            }
        }
        Collections.sort(entryPairs, new Comparator<Pair<String, Locale>>() {
            public int compare(Pair<String, Locale> lhs, Pair<String, Locale> rhs) {
                return ((String) lhs.first).compareToIgnoreCase((String) rhs.first);
            }
        });
        this.mSelectedLocaleIndex = 0;
        CharSequence[] entries = new CharSequence[(availableLangs.size() + 1)];
        CharSequence[] entryValues = new CharSequence[(availableLangs.size() + 1)];
        entries[0] = getActivity().getString(R.string.tts_lang_use_system);
        entryValues[0] = "";
        int i2 = 1;
        Iterator it = entryPairs.iterator();
        while (it.hasNext()) {
            Pair<String, Locale> entry = (Pair) it.next();
            if (((Locale) entry.second).equals(currentLocale)) {
                this.mSelectedLocaleIndex = i2;
            }
            entries[i2] = (CharSequence) entry.first;
            int i3 = i2 + 1;
            entryValues[i2] = ((Locale) entry.second).toString();
            i2 = i3;
        }
        this.mLocalePreference.setEntries(entries);
        this.mLocalePreference.setEntryValues(entryValues);
        this.mLocalePreference.setEnabled(true);
        setLocalePreference(this.mSelectedLocaleIndex);
    }

    private void setLocalePreference(int index) {
        if (index < 0) {
            this.mLocalePreference.setValue("");
            this.mLocalePreference.setSummary((int) R.string.tts_lang_not_selected);
            return;
        }
        this.mLocalePreference.setValueIndex(index);
        this.mLocalePreference.setSummary(this.mLocalePreference.getEntries()[index]);
    }

    private String getDefaultSampleString() {
        if (!(this.mTts == null || this.mTts.getLanguage() == null)) {
            try {
                String currentLang = this.mTts.getLanguage().getISO3Language();
                String[] strings = getActivity().getResources().getStringArray(R.array.tts_demo_strings);
                String[] langs = getActivity().getResources().getStringArray(R.array.tts_demo_string_langs);
                for (int i = 0; i < strings.length; i++) {
                    if (langs[i].equals(currentLang)) {
                        return strings[i];
                    }
                }
            } catch (MissingResourceException e) {
            }
        }
        return getString(R.string.tts_default_sample_string);
    }

    private boolean isNetworkRequiredForSynthesis() {
        Set<String> features = this.mTts.getFeatures(this.mCurrentDefaultLocale);
        boolean z = false;
        if (features == null) {
            return false;
        }
        if (features.contains("networkTts") && !features.contains("embeddedTts")) {
            z = true;
        }
        return z;
    }

    private void onSampleTextReceived(int resultCode, Intent data) {
        String sample = getDefaultSampleString();
        if (!(resultCode != 0 || data == null || data == null || data.getStringExtra("sampleText") == null)) {
            sample = data.getStringExtra("sampleText");
        }
        this.mSampleText = sample;
        if (this.mSampleText != null) {
            updateWidgetState(true);
        } else {
            Log.e(TAG, "Did not have a sample string for the requested language. Using default");
        }
    }

    private void speakSampleText() {
        boolean networkRequired = isNetworkRequiredForSynthesis();
        if (!networkRequired || (networkRequired && this.mTts.isLanguageAvailable(this.mCurrentDefaultLocale) >= 0)) {
            HashMap<String, String> params = new HashMap();
            params.put("utteranceId", "Sample");
            this.mTts.speak(this.mSampleText, 0, params);
            return;
        }
        Log.w(TAG, "Network required for sample synthesis for requested language");
        displayNetworkAlert();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (KEY_DEFAULT_RATE.equals(preference.getKey())) {
            updateSpeechRate(((Integer) objValue).intValue());
        } else if (KEY_DEFAULT_PITCH.equals(preference.getKey())) {
            updateSpeechPitchValue(((Integer) objValue).intValue());
        } else if (preference == this.mLocalePreference) {
            String localeString = (String) objValue;
            Locale locale = null;
            if (!(TextUtils.isEmpty(localeString) || this.mEnginesHelper == null)) {
                locale = this.mEnginesHelper.parseLocaleString(localeString);
            }
            updateLanguageTo(locale);
            checkDefaultLocale();
            return true;
        }
        return true;
    }

    private void updateLanguageTo(Locale locale) {
        int selectedLocaleIndex = -1;
        String localeString = locale != null ? locale.toString() : "";
        for (int i = 0; i < this.mLocalePreference.getEntryValues().length; i++) {
            if (localeString.equalsIgnoreCase(this.mLocalePreference.getEntryValues()[i].toString())) {
                selectedLocaleIndex = i;
                break;
            }
        }
        if (selectedLocaleIndex == -1) {
            Log.w(TAG, "updateLanguageTo called with unknown locale argument");
            return;
        }
        this.mLocalePreference.setSummary(this.mLocalePreference.getEntries()[selectedLocaleIndex]);
        this.mSelectedLocaleIndex = selectedLocaleIndex;
        this.mEnginesHelper.updateLocalePrefForEngine(this.mTts.getCurrentEngine(), locale);
        this.mTts.setLanguage(locale != null ? locale : Locale.getDefault());
    }

    private void resetTts() {
        int speechRateSeekbarProgress = getSeekBarProgressFromValue(KEY_DEFAULT_RATE, 100);
        this.mDefaultRatePref.setProgress(speechRateSeekbarProgress);
        updateSpeechRate(speechRateSeekbarProgress);
        int pitchSeekbarProgress = getSeekBarProgressFromValue(KEY_DEFAULT_PITCH, 100);
        this.mDefaultPitchPref.setProgress(pitchSeekbarProgress);
        updateSpeechPitchValue(pitchSeekbarProgress);
    }

    private void updateSpeechRate(int speechRateSeekBarProgress) {
        this.mDefaultRate = getValueFromSeekBarProgress(KEY_DEFAULT_RATE, speechRateSeekBarProgress);
        try {
            Secure.putInt(getContentResolver(), KEY_DEFAULT_RATE, this.mDefaultRate);
            if (this.mTts != null) {
                this.mTts.setSpeechRate(((float) this.mDefaultRate) / 100.0f);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist default TTS rate setting", e);
        }
    }

    private void updateSpeechPitchValue(int speechPitchSeekBarProgress) {
        this.mDefaultPitch = getValueFromSeekBarProgress(KEY_DEFAULT_PITCH, speechPitchSeekBarProgress);
        try {
            Secure.putInt(getContentResolver(), KEY_DEFAULT_PITCH, this.mDefaultPitch);
            if (this.mTts != null) {
                this.mTts.setPitch(((float) this.mDefaultPitch) / 100.0f);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist default TTS pitch setting", e);
        }
    }

    private void updateWidgetState(boolean enable) {
        this.mActionButtons.setButton1Enabled(enable);
        this.mDefaultRatePref.setEnabled(enable);
        this.mDefaultPitchPref.setEnabled(enable);
    }

    private void displayNetworkAlert() {
        Builder builder = new Builder(getActivity());
        builder.setTitle(17039380).setMessage(getActivity().getString(R.string.tts_engine_network_required)).setCancelable(false).setPositiveButton(17039370, null);
        builder.create().show();
    }

    private void checkVoiceData(String engine) {
        Intent intent = new Intent("android.speech.tts.engine.CHECK_TTS_DATA");
        intent.setPackage(engine);
        try {
            startActivityForResult(intent, VOICE_DATA_INTEGRITY_CHECK);
        } catch (ActivityNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to check TTS data, no activity found for ");
            stringBuilder.append(intent);
            stringBuilder.append(")");
            Log.e(str, stringBuilder.toString());
        }
    }

    private void onVoiceDataIntegrityCheckDone(Intent data) {
        String engine = this.mTts.getCurrentEngine();
        if (engine == null) {
            Log.e(TAG, "Voice data check complete, but no engine bound");
        } else if (data == null) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Engine failed voice data integrity check (null return)");
            stringBuilder.append(this.mTts.getCurrentEngine());
            Log.e(str, stringBuilder.toString());
        } else {
            Secure.putString(getContentResolver(), "tts_default_synth", engine);
            this.mAvailableStrLocals = data.getStringArrayListExtra("availableVoices");
            if (this.mAvailableStrLocals == null) {
                Log.e(TAG, "Voice data check complete, but no available voices found");
                this.mAvailableStrLocals = new ArrayList();
            }
            if (evaluateDefaultLocale()) {
                getSampleText();
            }
        }
    }

    public void onGearClick(GearPreference p) {
        if (KEY_TTS_ENGINE_PREFERENCE.equals(p.getKey())) {
            Intent settingsIntent = this.mEnginesHelper.getSettingsIntent(this.mEnginesHelper.getEngineInfo(this.mCurrentEngine).name);
            if (settingsIntent != null) {
                startActivity(settingsIntent);
            } else {
                Log.e(TAG, "settingsIntent is null");
            }
        }
    }
}
