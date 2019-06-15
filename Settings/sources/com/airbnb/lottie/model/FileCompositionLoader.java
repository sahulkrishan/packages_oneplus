package com.airbnb.lottie.model;

import android.content.res.Resources;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieComposition.Factory;
import com.airbnb.lottie.OnCompositionLoadedListener;
import java.io.InputStream;

public final class FileCompositionLoader extends CompositionLoader<InputStream> {
    private final OnCompositionLoadedListener loadedListener;
    private final Resources res;

    public FileCompositionLoader(Resources res, OnCompositionLoadedListener loadedListener) {
        this.res = res;
        this.loadedListener = loadedListener;
    }

    /* Access modifiers changed, original: protected|varargs */
    public LottieComposition doInBackground(InputStream... params) {
        return Factory.fromInputStream(this.res, params[0]);
    }

    /* Access modifiers changed, original: protected */
    public void onPostExecute(LottieComposition composition) {
        this.loadedListener.onCompositionLoaded(composition);
    }
}
