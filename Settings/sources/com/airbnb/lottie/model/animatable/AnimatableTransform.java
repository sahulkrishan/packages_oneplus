package com.airbnb.lottie.model.animatable;

import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.Log;
import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.ModifierContent;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;
import com.airbnb.lottie.model.ScaleXY;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.android.settings.network.ApnSettings;
import java.util.Collections;
import org.json.JSONObject;

public class AnimatableTransform implements ModifierContent, ContentModel {
    private final AnimatablePathValue anchorPoint;
    @Nullable
    private final AnimatableFloatValue endOpacity;
    private final AnimatableIntegerValue opacity;
    private final AnimatableValue<PointF, PointF> position;
    private final AnimatableFloatValue rotation;
    private final AnimatableScaleValue scale;
    @Nullable
    private final AnimatableFloatValue startOpacity;

    public static class Factory {
        private Factory() {
        }

        public static AnimatableTransform newInstance() {
            return new AnimatableTransform(new AnimatablePathValue(), new AnimatablePathValue(), Factory.newInstance(), com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(), com.airbnb.lottie.model.animatable.AnimatableIntegerValue.Factory.newInstance(), com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(), com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance());
        }

        public static AnimatableTransform newInstance(JSONObject json, LottieComposition composition) {
            AnimatablePathValue animatablePathValue;
            AnimatableScaleValue newInstance;
            AnimatableIntegerValue newInstance2;
            JSONObject jSONObject = json;
            LottieComposition lottieComposition = composition;
            AnimatableValue<PointF, PointF> position = null;
            AnimatableFloatValue rotation = null;
            AnimatableFloatValue startOpacity = null;
            AnimatableFloatValue endOpacity = null;
            JSONObject anchorJson = jSONObject.optJSONObject("a");
            if (anchorJson != null) {
                animatablePathValue = new AnimatablePathValue(anchorJson.opt("k"), lottieComposition);
            } else {
                Log.w(L.TAG, "Layer has no transform property. You may be using an unsupported layer type such as a camera.");
                animatablePathValue = new AnimatablePathValue();
            }
            AnimatablePathValue anchorPoint = animatablePathValue;
            JSONObject positionJson = jSONObject.optJSONObject("p");
            if (positionJson != null) {
                position = AnimatablePathValue.createAnimatablePathOrSplitDimensionPath(positionJson, lottieComposition);
            } else {
                throwMissingTransform(ApnSettings.EXTRA_POSITION);
            }
            JSONObject scaleJson = jSONObject.optJSONObject("s");
            if (scaleJson != null) {
                newInstance = Factory.newInstance(scaleJson, lottieComposition);
            } else {
                newInstance = new AnimatableScaleValue(Collections.emptyList(), new ScaleXY());
            }
            AnimatableScaleValue scale = newInstance;
            JSONObject rotationJson = jSONObject.optJSONObject("r");
            if (rotationJson == null) {
                rotationJson = jSONObject.optJSONObject("rz");
            }
            JSONObject rotationJson2 = rotationJson;
            if (rotationJson2 != null) {
                rotation = com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(rotationJson2, lottieComposition, false);
            } else {
                throwMissingTransform("rotation");
            }
            JSONObject opacityJson = jSONObject.optJSONObject("o");
            if (opacityJson != null) {
                newInstance2 = com.airbnb.lottie.model.animatable.AnimatableIntegerValue.Factory.newInstance(opacityJson, lottieComposition);
            } else {
                newInstance2 = new AnimatableIntegerValue(Collections.emptyList(), Integer.valueOf(100));
            }
            AnimatableIntegerValue opacity = newInstance2;
            JSONObject startOpacityJson = jSONObject.optJSONObject("so");
            if (startOpacityJson != null) {
                startOpacity = com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(startOpacityJson, lottieComposition, false);
            }
            rotationJson = jSONObject.optJSONObject("eo");
            if (rotationJson != null) {
                endOpacity = com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(rotationJson, lottieComposition, false);
            }
            return new AnimatableTransform(anchorPoint, position, scale, rotation, opacity, startOpacity, endOpacity);
        }

        private static void throwMissingTransform(String missingProperty) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Missing transform for ");
            stringBuilder.append(missingProperty);
            throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    private AnimatableTransform(AnimatablePathValue anchorPoint, AnimatableValue<PointF, PointF> position, AnimatableScaleValue scale, AnimatableFloatValue rotation, AnimatableIntegerValue opacity, @Nullable AnimatableFloatValue startOpacity, @Nullable AnimatableFloatValue endOpacity) {
        this.anchorPoint = anchorPoint;
        this.position = position;
        this.scale = scale;
        this.rotation = rotation;
        this.opacity = opacity;
        this.startOpacity = startOpacity;
        this.endOpacity = endOpacity;
    }

    public AnimatablePathValue getAnchorPoint() {
        return this.anchorPoint;
    }

    public AnimatableValue<PointF, PointF> getPosition() {
        return this.position;
    }

    public AnimatableScaleValue getScale() {
        return this.scale;
    }

    public AnimatableFloatValue getRotation() {
        return this.rotation;
    }

    public AnimatableIntegerValue getOpacity() {
        return this.opacity;
    }

    @Nullable
    public AnimatableFloatValue getStartOpacity() {
        return this.startOpacity;
    }

    @Nullable
    public AnimatableFloatValue getEndOpacity() {
        return this.endOpacity;
    }

    public TransformKeyframeAnimation createAnimation() {
        return new TransformKeyframeAnimation(this);
    }

    @Nullable
    public Content toContent(LottieDrawable drawable, BaseLayer layer) {
        return null;
    }
}
