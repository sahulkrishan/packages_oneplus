package com.airbnb.lottie.model;

import android.content.res.Resources;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieComposition.Factory;
import com.airbnb.lottie.OnCompositionLoadedListener;
import org.json.JSONObject;

public final class JsonCompositionLoader extends CompositionLoader<JSONObject> {
    private final OnCompositionLoadedListener loadedListener;
    private final Resources res;

    public JsonCompositionLoader(Resources res, OnCompositionLoadedListener loadedListener) {
        this.res = res;
        this.loadedListener = loadedListener;
    }

    /* Access modifiers changed, original: protected|varargs */
    public LottieComposition doInBackground(JSONObject... params) {
        return Factory.fromJsonSync(this.res, params[0]);
    }

    /* Access modifiers changed, original: protected */
    public void onPostExecute(LottieComposition composition) {
        this.loadedListener.onCompositionLoaded(composition);
    }
}
