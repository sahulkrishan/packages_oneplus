package com.android.settings.tts;

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.EngineInfo;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TtsEngines;
import android.support.v7.preference.PreferenceCategory;
import android.util.Log;
import android.widget.Checkable;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.tts.TtsEnginePreference.RadioButtonGroupState;
import java.util.Arrays;
import java.util.List;

public class TtsEnginePreferenceFragment extends SettingsPreferenceFragment implements RadioButtonGroupState, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.tts_engine_picker;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }
    };
    private static final String TAG = "TtsEnginePrefFragment";
    private static final int VOICE_DATA_INTEGRITY_CHECK = 1977;
    private Checkable mCurrentChecked;
    private String mCurrentEngine;
    private PreferenceCategory mEnginePreferenceCategory;
    private TtsEngines mEnginesHelper = null;
    private String mPreviousEngine;
    private TextToSpeech mTts = null;
    private final OnInitListener mUpdateListener = new OnInitListener() {
        public void onInit(int status) {
            TtsEnginePreferenceFragment.this.onUpdateEngine(status);
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tts_engine_picker);
        this.mEnginePreferenceCategory = (PreferenceCategory) findPreference("tts_engine_preference_category");
        this.mEnginesHelper = new TtsEngines(getActivity().getApplicationContext());
        this.mTts = new TextToSpeech(getActivity().getApplicationContext(), null);
        initSettings();
    }

    public int getMetricsCategory() {
        return 93;
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mTts != null) {
            this.mTts.shutdown();
            this.mTts = null;
        }
    }

    private void initSettings() {
        if (this.mTts != null) {
            this.mCurrentEngine = this.mTts.getCurrentEngine();
        }
        this.mEnginePreferenceCategory.removeAll();
        SettingsActivity activity = (SettingsActivity) getActivity();
        for (EngineInfo engine : this.mEnginesHelper.getEngines()) {
            this.mEnginePreferenceCategory.addPreference(new TtsEnginePreference(getPrefContext(), engine, this, activity));
        }
    }

    public Checkable getCurrentChecked() {
        return this.mCurrentChecked;
    }

    public String getCurrentKey() {
        return this.mCurrentEngine;
    }

    public void setCurrentChecked(Checkable current) {
        this.mCurrentChecked = current;
    }

    private void updateDefaultEngine(String engine) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Updating default synth to : ");
        stringBuilder.append(engine);
        Log.d(str, stringBuilder.toString());
        this.mPreviousEngine = this.mTts.getCurrentEngine();
        Log.i(TAG, "Shutting down current tts engine");
        if (this.mTts != null) {
            try {
                this.mTts.shutdown();
                this.mTts = null;
            } catch (Exception e) {
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Error shutting down TTS engine");
                stringBuilder2.append(e);
                Log.e(str2, stringBuilder2.toString());
            }
        }
        str = TAG;
        stringBuilder = new StringBuilder();
        stringBuilder.append("Updating engine : Attempting to connect to engine: ");
        stringBuilder.append(engine);
        Log.i(str, stringBuilder.toString());
        this.mTts = new TextToSpeech(getActivity().getApplicationContext(), this.mUpdateListener, engine);
        Log.i(TAG, "Success");
    }

    public void onUpdateEngine(int status) {
        if (status == 0) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Updating engine: Successfully bound to the engine: ");
            stringBuilder.append(this.mTts.getCurrentEngine());
            Log.d(str, stringBuilder.toString());
            Secure.putString(getContentResolver(), "tts_default_synth", this.mTts.getCurrentEngine());
            return;
        }
        Log.d(TAG, "Updating engine: Failed to bind to engine, reverting.");
        if (this.mPreviousEngine != null) {
            this.mTts = new TextToSpeech(getActivity().getApplicationContext(), null, this.mPreviousEngine);
        }
        this.mPreviousEngine = null;
    }

    public void setCurrentKey(String key) {
        this.mCurrentEngine = key;
        updateDefaultEngine(this.mCurrentEngine);
    }
}
