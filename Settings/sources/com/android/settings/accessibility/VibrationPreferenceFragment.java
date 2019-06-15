package com.android.settings.accessibility;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes.Builder;
import android.net.Uri;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings.System;
import android.support.annotation.VisibleForTesting;
import android.util.ArrayMap;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.widget.RadioButtonPickerFragment;
import com.android.settingslib.widget.CandidateInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public abstract class VibrationPreferenceFragment extends RadioButtonPickerFragment {
    @VisibleForTesting
    static final String KEY_INTENSITY_HIGH = "intensity_high";
    @VisibleForTesting
    static final String KEY_INTENSITY_LOW = "intensity_low";
    @VisibleForTesting
    static final String KEY_INTENSITY_MEDIUM = "intensity_medium";
    @VisibleForTesting
    static final String KEY_INTENSITY_OFF = "intensity_off";
    @VisibleForTesting
    static final String KEY_INTENSITY_ON = "intensity_on";
    private static final String TAG = "VibrationPreferenceFragment";
    private final Map<String, VibrationIntensityCandidateInfo> mCandidates = new ArrayMap();
    private final SettingsObserver mSettingsObserver = new SettingsObserver();

    private class SettingsObserver extends ContentObserver {
        public SettingsObserver() {
            super(new Handler());
        }

        public void register() {
            VibrationPreferenceFragment.this.getContext().getContentResolver().registerContentObserver(System.getUriFor(VibrationPreferenceFragment.this.getVibrationIntensitySetting()), false, this);
        }

        public void unregister() {
            VibrationPreferenceFragment.this.getContext().getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            VibrationPreferenceFragment.this.updateCandidates();
            VibrationPreferenceFragment.this.playVibrationPreview();
        }
    }

    @VisibleForTesting
    class VibrationIntensityCandidateInfo extends CandidateInfo {
        private int mIntensity;
        private String mKey;
        private int mLabelId;

        public VibrationIntensityCandidateInfo(String key, int labelId, int intensity) {
            super(true);
            this.mKey = key;
            this.mLabelId = labelId;
            this.mIntensity = intensity;
        }

        public CharSequence loadLabel() {
            return VibrationPreferenceFragment.this.getContext().getString(this.mLabelId);
        }

        public Drawable loadIcon() {
            return null;
        }

        public String getKey() {
            return this.mKey;
        }

        public int getIntensity() {
            return this.mIntensity;
        }
    }

    public abstract int getDefaultVibrationIntensity();

    public abstract String getVibrationIntensitySetting();

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mSettingsObserver.register();
        if (this.mCandidates.isEmpty()) {
            loadCandidates(context);
        }
    }

    private void loadCandidates(Context context) {
        if (context.getResources().getBoolean(R.bool.config_vibration_supports_multiple_intensities)) {
            this.mCandidates.put(KEY_INTENSITY_OFF, new VibrationIntensityCandidateInfo(KEY_INTENSITY_OFF, R.string.accessibility_vibration_intensity_off, 0));
            this.mCandidates.put(KEY_INTENSITY_LOW, new VibrationIntensityCandidateInfo(KEY_INTENSITY_LOW, R.string.accessibility_vibration_intensity_low, 1));
            this.mCandidates.put(KEY_INTENSITY_MEDIUM, new VibrationIntensityCandidateInfo(KEY_INTENSITY_MEDIUM, R.string.accessibility_vibration_intensity_medium, 2));
            this.mCandidates.put(KEY_INTENSITY_HIGH, new VibrationIntensityCandidateInfo(KEY_INTENSITY_HIGH, R.string.accessibility_vibration_intensity_high, 3));
            return;
        }
        this.mCandidates.put(KEY_INTENSITY_OFF, new VibrationIntensityCandidateInfo(KEY_INTENSITY_OFF, R.string.switch_off_text, 0));
        this.mCandidates.put(KEY_INTENSITY_ON, new VibrationIntensityCandidateInfo(KEY_INTENSITY_ON, R.string.switch_on_text, getDefaultVibrationIntensity()));
    }

    public void onDetach() {
        super.onDetach();
        this.mSettingsObserver.unregister();
    }

    /* Access modifiers changed, original: protected */
    public void onVibrationIntensitySelected(int intensity) {
    }

    /* Access modifiers changed, original: protected */
    public void playVibrationPreview() {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Vibrator.class);
        VibrationEffect effect = VibrationEffect.get(null);
        Builder builder = new Builder();
        builder.setUsage(getPreviewVibrationAudioAttributesUsage());
        vibrator.vibrate(effect, builder.build());
    }

    /* Access modifiers changed, original: protected */
    public int getPreviewVibrationAudioAttributesUsage() {
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public List<? extends CandidateInfo> getCandidates() {
        List<VibrationIntensityCandidateInfo> candidates = new ArrayList(this.mCandidates.values());
        candidates.sort(Comparator.comparing(-$$Lambda$_Oh9z60fg9jQX72D1CuzQSHZqtM.INSTANCE).reversed());
        return candidates;
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        int vibrationIntensity = System.getInt(getContext().getContentResolver(), getVibrationIntensitySetting(), getDefaultVibrationIntensity());
        for (VibrationIntensityCandidateInfo candidate : this.mCandidates.values()) {
            boolean matchesOn = false;
            boolean matchesIntensity = candidate.getIntensity() == vibrationIntensity;
            if (candidate.getKey().equals(KEY_INTENSITY_ON) && vibrationIntensity != 0) {
                matchesOn = true;
            }
            if (matchesIntensity || matchesOn) {
                return candidate.getKey();
            }
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String key) {
        VibrationIntensityCandidateInfo candidate = (VibrationIntensityCandidateInfo) this.mCandidates.get(key);
        if (candidate == null) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Tried to set unknown intensity (key=");
            stringBuilder.append(key);
            stringBuilder.append(")!");
            Log.e(str, stringBuilder.toString());
            return false;
        }
        System.putInt(getContext().getContentResolver(), getVibrationIntensitySetting(), candidate.getIntensity());
        onVibrationIntensitySelected(candidate.getIntensity());
        return true;
    }
}
