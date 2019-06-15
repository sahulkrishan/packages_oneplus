package com.airbnb.lottie.animation;

import android.graphics.PointF;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.utils.JsonUtils;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.utils.Utils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class Keyframe<T> {
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final float MAX_CP_VALUE = 100.0f;
    private final LottieComposition composition;
    @Nullable
    public Float endFrame;
    private float endProgress = Float.MIN_VALUE;
    @Nullable
    public final T endValue;
    @Nullable
    public final Interpolator interpolator;
    public final float startFrame;
    private float startProgress = Float.MIN_VALUE;
    @Nullable
    public final T startValue;

    public static class Factory {
        private static SparseArrayCompat<WeakReference<Interpolator>> pathInterpolatorCache;

        private static SparseArrayCompat<WeakReference<Interpolator>> pathInterpolatorCache() {
            if (pathInterpolatorCache == null) {
                pathInterpolatorCache = new SparseArrayCompat();
            }
            return pathInterpolatorCache;
        }

        @Nullable
        private static WeakReference<Interpolator> getInterpolator(int hash) {
            WeakReference weakReference;
            synchronized (Factory.class) {
                weakReference = (WeakReference) pathInterpolatorCache().get(hash);
            }
            return weakReference;
        }

        private static void putInterpolator(int hash, WeakReference<Interpolator> interpolator) {
            synchronized (Factory.class) {
                pathInterpolatorCache.put(hash, interpolator);
            }
        }

        private Factory() {
        }

        public static <T> Keyframe<T> newInstance(JSONObject json, LottieComposition composition, float scale, com.airbnb.lottie.model.animatable.AnimatableValue.Factory<T> valueFactory) {
            float startFrame;
            T startValue;
            JSONObject jSONObject = json;
            float f = scale;
            com.airbnb.lottie.model.animatable.AnimatableValue.Factory<T> factory = valueFactory;
            PointF cp1 = null;
            PointF cp12 = null;
            T startValue2 = null;
            T endValue = null;
            Interpolator interpolator = null;
            if (jSONObject.has("t")) {
                float startFrame2 = (float) jSONObject.optDouble("t", 0.0d);
                Object startValueJson = jSONObject.opt("s");
                if (startValueJson != null) {
                    startValue2 = factory.valueFromObject(startValueJson, f);
                }
                Object endValueJson = jSONObject.opt("e");
                if (endValueJson != null) {
                    endValue = factory.valueFromObject(endValueJson, f);
                }
                JSONObject cp1Json = jSONObject.optJSONObject("o");
                JSONObject cp2Json = jSONObject.optJSONObject("i");
                if (!(cp1Json == null || cp2Json == null)) {
                    cp1 = JsonUtils.pointFromJsonObject(cp1Json, f);
                    cp12 = JsonUtils.pointFromJsonObject(cp2Json, f);
                }
                PointF cp2 = cp12;
                cp12 = cp1;
                boolean hold = false;
                if (jSONObject.optInt("h", 0) == 1) {
                    hold = true;
                }
                PointF cp13;
                if (hold) {
                    endValue = startValue2;
                    interpolator = Keyframe.LINEAR_INTERPOLATOR;
                    cp13 = cp12;
                    startFrame = startFrame2;
                    startValue = startValue2;
                } else if (cp12 != null) {
                    cp12.x = MiscUtils.clamp(cp12.x, -f, f);
                    startFrame = startFrame2;
                    cp12.y = MiscUtils.clamp(cp12.y, -100.0f, Keyframe.MAX_CP_VALUE);
                    cp2.x = MiscUtils.clamp(cp2.x, -f, f);
                    cp2.y = MiscUtils.clamp(cp2.y, -100.0f, Keyframe.MAX_CP_VALUE);
                    startValue = startValue2;
                    startFrame2 = Utils.hashFor(cp12.x, cp12.y, cp2.x, cp2.y);
                    WeakReference<Interpolator> interpolatorRef = getInterpolator(startFrame2);
                    if (interpolatorRef != null) {
                        interpolator = (Interpolator) interpolatorRef.get();
                    }
                    if (interpolatorRef == null || interpolator == null) {
                        cp13 = cp12;
                        interpolator = PathInterpolatorCompat.create(cp12.x / f, cp12.y / f, cp2.x / f, cp2.y / f);
                        try {
                            putInterpolator(startFrame2, new WeakReference(interpolator));
                        } catch (ArrayIndexOutOfBoundsException e) {
                        }
                    } else {
                        cp13 = cp12;
                    }
                } else {
                    cp13 = cp12;
                    startFrame = startFrame2;
                    startValue = startValue2;
                    interpolator = Keyframe.LINEAR_INTERPOLATOR;
                }
            } else {
                startValue2 = factory.valueFromObject(jSONObject, f);
                endValue = startValue2;
                startFrame = 0.0f;
                startValue = startValue2;
            }
            return new Keyframe(composition, startValue, endValue, interpolator, startFrame, null);
        }

        public static <T> List<Keyframe<T>> parseKeyframes(JSONArray json, LottieComposition composition, float scale, com.airbnb.lottie.model.animatable.AnimatableValue.Factory<T> valueFactory) {
            int length = json.length();
            if (length == 0) {
                return Collections.emptyList();
            }
            List<Keyframe<T>> keyframes = new ArrayList();
            for (int i = 0; i < length; i++) {
                keyframes.add(newInstance(json.optJSONObject(i), composition, scale, valueFactory));
            }
            Keyframe.setEndFrames(keyframes);
            return keyframes;
        }
    }

    public static void setEndFrames(List<? extends Keyframe<?>> keyframes) {
        int size = keyframes.size();
        for (int i = 0; i < size - 1; i++) {
            ((Keyframe) keyframes.get(i)).endFrame = Float.valueOf(((Keyframe) keyframes.get(i + 1)).startFrame);
        }
        Keyframe<?> lastKeyframe = (Keyframe) keyframes.get(size - 1);
        if (lastKeyframe.startValue == null) {
            keyframes.remove(lastKeyframe);
        }
    }

    public Keyframe(LottieComposition composition, @Nullable T startValue, @Nullable T endValue, @Nullable Interpolator interpolator, float startFrame, @Nullable Float endFrame) {
        this.composition = composition;
        this.startValue = startValue;
        this.endValue = endValue;
        this.interpolator = interpolator;
        this.startFrame = startFrame;
        this.endFrame = endFrame;
    }

    public float getStartProgress() {
        if (this.startProgress == Float.MIN_VALUE) {
            this.startProgress = (this.startFrame - ((float) this.composition.getStartFrame())) / this.composition.getDurationFrames();
        }
        return this.startProgress;
    }

    public float getEndProgress() {
        if (this.endProgress == Float.MIN_VALUE) {
            if (this.endFrame == null) {
                this.endProgress = 1.0f;
            } else {
                this.endProgress = getStartProgress() + ((this.endFrame.floatValue() - this.startFrame) / this.composition.getDurationFrames());
            }
        }
        return this.endProgress;
    }

    public boolean isStatic() {
        return this.interpolator == null;
    }

    public boolean containsProgress(@FloatRange(from = 0.0d, to = 1.0d) float progress) {
        return progress >= getStartProgress() && progress <= getEndProgress();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Keyframe{startValue=");
        stringBuilder.append(this.startValue);
        stringBuilder.append(", endValue=");
        stringBuilder.append(this.endValue);
        stringBuilder.append(", startFrame=");
        stringBuilder.append(this.startFrame);
        stringBuilder.append(", endFrame=");
        stringBuilder.append(this.endFrame);
        stringBuilder.append(", interpolator=");
        stringBuilder.append(this.interpolator);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
