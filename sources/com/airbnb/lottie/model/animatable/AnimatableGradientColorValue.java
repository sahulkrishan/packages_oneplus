package com.airbnb.lottie.model.animatable;

import android.graphics.Color;
import android.support.annotation.IntRange;
import android.util.Log;
import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.GradientColorKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.StaticKeyframeAnimation;
import com.airbnb.lottie.model.content.GradientColor;
import com.airbnb.lottie.utils.MiscUtils;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class AnimatableGradientColorValue extends BaseAnimatableValue<GradientColor, GradientColor> {

    public static final class Factory {
        private Factory() {
        }

        public static AnimatableGradientColorValue newInstance(JSONObject json, LottieComposition composition) {
            Result<GradientColor> result = AnimatableValueParser.newInstance(json, 1.0f, composition, new ValueFactory(json.optInt("p", json.optJSONArray("k").length() / 4))).parseJson();
            return new AnimatableGradientColorValue(result.keyframes, result.initialValue);
        }
    }

    private static class ValueFactory implements com.airbnb.lottie.model.animatable.AnimatableValue.Factory<GradientColor> {
        private final int colorPoints;

        private ValueFactory(int colorPoints) {
            this.colorPoints = colorPoints;
        }

        public GradientColor valueFromObject(Object object, float scale) {
            JSONArray array = (JSONArray) object;
            float[] positions = new float[this.colorPoints];
            int[] colors = new int[this.colorPoints];
            GradientColor gradientColor = new GradientColor(positions, colors);
            int r = 0;
            int g = 0;
            if (array.length() != this.colorPoints * 4) {
                String str = L.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unexpected gradient length: ");
                stringBuilder.append(array.length());
                stringBuilder.append(". Expected ");
                stringBuilder.append(this.colorPoints * 4);
                stringBuilder.append(". This may affect the appearance of the gradient. Make sure to save your After Effects file before exporting an animation with gradients.");
                Log.w(str, stringBuilder.toString());
            }
            for (int i = 0; i < this.colorPoints * 4; i++) {
                int colorIndex = i / 4;
                double value = array.optDouble(i);
                switch (i % 4) {
                    case 0:
                        positions[colorIndex] = (float) value;
                        break;
                    case 1:
                        r = (int) (255.0d * value);
                        break;
                    case 2:
                        g = (int) (255.0d * value);
                        break;
                    case 3:
                        colors[colorIndex] = Color.argb(255, r, g, (int) (255.0d * value));
                        break;
                    default:
                        break;
                }
            }
            addOpacityStopsToGradientIfNeeded(gradientColor, array);
            return gradientColor;
        }

        private void addOpacityStopsToGradientIfNeeded(GradientColor gradientColor, JSONArray array) {
            int startIndex = this.colorPoints * 4;
            if (array.length() > startIndex) {
                int opacityStops = (array.length() - startIndex) / 2;
                double[] positions = new double[opacityStops];
                double[] opacities = new double[opacityStops];
                int i = 0;
                int j = 0;
                for (int i2 = startIndex; i2 < array.length(); i2++) {
                    if (i2 % 2 == 0) {
                        positions[j] = array.optDouble(i2);
                    } else {
                        opacities[j] = array.optDouble(i2);
                        j++;
                    }
                }
                while (true) {
                    j = i;
                    if (j < gradientColor.getSize()) {
                        i = gradientColor.getColors()[j];
                        gradientColor.getColors()[j] = Color.argb(getOpacityAtPosition((double) gradientColor.getPositions()[j], positions, opacities), Color.red(i), Color.green(i), Color.blue(i));
                        i = j + 1;
                    } else {
                        return;
                    }
                }
            }
        }

        @IntRange(from = 0, to = 255)
        private int getOpacityAtPosition(double position, double[] positions, double[] opacities) {
            double[] dArr = positions;
            double[] dArr2 = opacities;
            for (int i = 1; i < dArr.length; i++) {
                double lastPosition = dArr[i - 1];
                double thisPosition = dArr[i];
                if (dArr[i] >= position) {
                    return (int) (255.0d * MiscUtils.lerp(dArr2[i - 1], dArr2[i], (position - lastPosition) / (thisPosition - lastPosition)));
                }
            }
            return (int) (255.0d * dArr2[dArr2.length - 1]);
        }
    }

    private AnimatableGradientColorValue(List<Keyframe<GradientColor>> keyframes, GradientColor initialValue) {
        super(keyframes, initialValue);
    }

    public BaseKeyframeAnimation<GradientColor, GradientColor> createAnimation() {
        if (hasAnimation()) {
            return new GradientColorKeyframeAnimation(this.keyframes);
        }
        return new StaticKeyframeAnimation(this.initialValue);
    }
}
