package com.android.settings.tts;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.speech.tts.TextToSpeech.EngineInfo;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import com.android.settings.R;
import com.android.settings.SettingsActivity;

public class TtsEnginePreference extends Preference {
    private static final String TAG = "TtsEnginePreference";
    private final EngineInfo mEngineInfo;
    private volatile boolean mPreventRadioButtonCallbacks;
    private RadioButton mRadioButton;
    private final OnCheckedChangeListener mRadioChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            TtsEnginePreference.this.onRadioButtonClicked(buttonView, isChecked);
        }
    };
    private final RadioButtonGroupState mSharedState;
    private Intent mVoiceCheckData;

    public interface RadioButtonGroupState {
        Checkable getCurrentChecked();

        String getCurrentKey();

        void setCurrentChecked(Checkable checkable);

        void setCurrentKey(String str);
    }

    public TtsEnginePreference(Context context, EngineInfo info, RadioButtonGroupState state, SettingsActivity prefActivity) {
        super(context);
        setLayoutResource(R.layout.preference_tts_engine);
        this.mSharedState = state;
        this.mEngineInfo = info;
        this.mPreventRadioButtonCallbacks = false;
        setKey(this.mEngineInfo.name);
        setTitle((CharSequence) this.mEngineInfo.label);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        if (this.mSharedState != null) {
            RadioButton rb = (RadioButton) view.findViewById(R.id.tts_engine_radiobutton);
            rb.setOnCheckedChangeListener(this.mRadioChangeListener);
            rb.setText(this.mEngineInfo.label);
            boolean isChecked = getKey().equals(this.mSharedState.getCurrentKey());
            if (isChecked) {
                this.mSharedState.setCurrentChecked(rb);
            }
            this.mPreventRadioButtonCallbacks = true;
            rb.setChecked(isChecked);
            this.mPreventRadioButtonCallbacks = false;
            this.mRadioButton = rb;
            return;
        }
        throw new IllegalStateException("Call to getView() before a call tosetSharedState()");
    }

    public void setVoiceDataDetails(Intent data) {
        this.mVoiceCheckData = data;
    }

    private boolean shouldDisplayDataAlert() {
        return this.mEngineInfo.system ^ 1;
    }

    private void displayDataAlert(OnClickListener positiveOnClickListener, OnClickListener negativeOnClickListener) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Displaying data alert for :");
        stringBuilder.append(this.mEngineInfo.name);
        Log.i(str, stringBuilder.toString());
        Builder builder = new Builder(getContext());
        builder.setTitle(17039380).setMessage(getContext().getString(R.string.tts_engine_security_warning, new Object[]{this.mEngineInfo.label})).setCancelable(true).setPositiveButton(17039370, positiveOnClickListener).setNegativeButton(17039360, negativeOnClickListener);
        builder.create().show();
    }

    /* JADX WARNING: Missing block: B:10:0x0027, code skipped:
            return;
     */
    private void onRadioButtonClicked(final android.widget.CompoundButton r3, boolean r4) {
        /*
        r2 = this;
        r0 = r2.mPreventRadioButtonCallbacks;
        if (r0 != 0) goto L_0x0027;
    L_0x0004:
        r0 = r2.mSharedState;
        r0 = r0.getCurrentChecked();
        if (r0 != r3) goto L_0x000d;
    L_0x000c:
        goto L_0x0027;
    L_0x000d:
        if (r4 == 0) goto L_0x0026;
    L_0x000f:
        r0 = r2.shouldDisplayDataAlert();
        if (r0 == 0) goto L_0x0023;
    L_0x0015:
        r0 = new com.android.settings.tts.TtsEnginePreference$2;
        r0.<init>(r3);
        r1 = new com.android.settings.tts.TtsEnginePreference$3;
        r1.<init>(r3);
        r2.displayDataAlert(r0, r1);
        goto L_0x0026;
    L_0x0023:
        r2.makeCurrentEngine(r3);
    L_0x0026:
        return;
    L_0x0027:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.tts.TtsEnginePreference.onRadioButtonClicked(android.widget.CompoundButton, boolean):void");
    }

    private void makeCurrentEngine(Checkable current) {
        if (this.mSharedState.getCurrentChecked() != null) {
            this.mSharedState.getCurrentChecked().setChecked(false);
        }
        this.mSharedState.setCurrentChecked(current);
        this.mSharedState.setCurrentKey(getKey());
        callChangeListener(this.mSharedState.getCurrentKey());
    }
}
