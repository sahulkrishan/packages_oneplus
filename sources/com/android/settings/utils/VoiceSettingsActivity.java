package com.android.settings.utils;

import android.app.Activity;
import android.app.VoiceInteractor.AbortVoiceRequest;
import android.app.VoiceInteractor.CompleteVoiceRequest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public abstract class VoiceSettingsActivity extends Activity {
    private static final String TAG = "VoiceSettingsActivity";

    public abstract boolean onVoiceSettingInteraction(Intent intent);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isVoiceInteractionRoot()) {
            Log.v(TAG, "Cannot modify settings without voice interaction");
            finish();
        } else if (onVoiceSettingInteraction(getIntent())) {
            finish();
        }
    }

    /* Access modifiers changed, original: protected */
    public void notifySuccess(CharSequence prompt) {
        if (getVoiceInteractor() != null) {
            getVoiceInteractor().submitRequest(new CompleteVoiceRequest(prompt, null) {
                public void onCompleteResult(Bundle options) {
                    VoiceSettingsActivity.this.finish();
                }
            });
        }
    }

    /* Access modifiers changed, original: protected */
    public void notifyFailure(CharSequence prompt) {
        if (getVoiceInteractor() != null) {
            getVoiceInteractor().submitRequest(new AbortVoiceRequest(prompt, null));
        }
    }
}
