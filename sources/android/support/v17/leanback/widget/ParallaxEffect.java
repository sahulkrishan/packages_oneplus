package android.support.v17.leanback.widget;

import android.animation.PropertyValuesHolder;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.widget.Parallax.FloatProperty;
import android.support.v17.leanback.widget.Parallax.IntProperty;
import android.support.v17.leanback.widget.Parallax.PropertyMarkerValue;
import android.support.v17.leanback.widget.ParallaxTarget.DirectPropertyTarget;
import android.support.v17.leanback.widget.ParallaxTarget.PropertyValuesHolderTarget;
import android.util.Property;
import java.util.ArrayList;
import java.util.List;

public abstract class ParallaxEffect {
    final List<PropertyMarkerValue> mMarkerValues = new ArrayList(2);
    final List<ParallaxTarget> mTargets = new ArrayList(4);
    final List<Float> mTotalWeights = new ArrayList(2);
    final List<Float> mWeights = new ArrayList(2);

    static final class FloatEffect extends ParallaxEffect {
        FloatEffect() {
        }

        /* Access modifiers changed, original: 0000 */
        public Number calculateDirectValue(Parallax source) {
            if (this.mMarkerValues.size() != 2) {
                throw new RuntimeException("Must use two marker values for direct mapping");
            } else if (((PropertyMarkerValue) this.mMarkerValues.get(0)).getProperty() == ((PropertyMarkerValue) this.mMarkerValues.get(1)).getProperty()) {
                float value1 = ((FloatPropertyMarkerValue) this.mMarkerValues.get(0)).getMarkerValue(source);
                float value2 = ((FloatPropertyMarkerValue) this.mMarkerValues.get(1)).getMarkerValue(source);
                if (value1 > value2) {
                    float swapValue = value2;
                    value2 = value1;
                    value1 = swapValue;
                }
                Number currentValue = ((FloatProperty) ((PropertyMarkerValue) this.mMarkerValues.get(0)).getProperty()).get(source);
                if (currentValue.floatValue() < value1) {
                    return Float.valueOf(value1);
                }
                if (currentValue.floatValue() > value2) {
                    return Float.valueOf(value2);
                }
                return currentValue;
            } else {
                throw new RuntimeException("Marker value must use same Property for direct mapping");
            }
        }

        /* Access modifiers changed, original: 0000 */
        public float calculateFraction(Parallax source) {
            int lastIndex = 0;
            float lastValue = 0.0f;
            float lastMarkerValue = 0.0f;
            for (int i = 0; i < this.mMarkerValues.size(); i++) {
                FloatPropertyMarkerValue k = (FloatPropertyMarkerValue) this.mMarkerValues.get(i);
                int index = ((FloatProperty) k.getProperty()).getIndex();
                float markerValue = k.getMarkerValue(source);
                float currentValue = source.getFloatPropertyValue(index);
                if (i == 0) {
                    if (currentValue >= markerValue) {
                        return 0.0f;
                    }
                } else if (lastIndex == index && lastMarkerValue < markerValue) {
                    throw new IllegalStateException("marker value of same variable must be descendant order");
                } else if (currentValue == Float.MAX_VALUE) {
                    return getFractionWithWeightAdjusted((lastMarkerValue - lastValue) / source.getMaxValue(), i);
                } else {
                    if (currentValue >= markerValue) {
                        float fraction;
                        if (lastIndex == index) {
                            fraction = (lastMarkerValue - currentValue) / (lastMarkerValue - markerValue);
                        } else if (lastValue != -3.4028235E38f) {
                            lastMarkerValue += currentValue - lastValue;
                            fraction = (lastMarkerValue - currentValue) / (lastMarkerValue - markerValue);
                        } else {
                            fraction = 1.0f - ((currentValue - markerValue) / source.getMaxValue());
                        }
                        return getFractionWithWeightAdjusted(fraction, i);
                    }
                }
                lastValue = currentValue;
                lastIndex = index;
                lastMarkerValue = markerValue;
            }
            return 1.0f;
        }
    }

    static final class IntEffect extends ParallaxEffect {
        IntEffect() {
        }

        /* Access modifiers changed, original: 0000 */
        public Number calculateDirectValue(Parallax source) {
            if (this.mMarkerValues.size() != 2) {
                throw new RuntimeException("Must use two marker values for direct mapping");
            } else if (((PropertyMarkerValue) this.mMarkerValues.get(0)).getProperty() == ((PropertyMarkerValue) this.mMarkerValues.get(1)).getProperty()) {
                int value1 = ((IntPropertyMarkerValue) this.mMarkerValues.get(0)).getMarkerValue(source);
                int value2 = ((IntPropertyMarkerValue) this.mMarkerValues.get(1)).getMarkerValue(source);
                if (value1 > value2) {
                    int swapValue = value2;
                    value2 = value1;
                    value1 = swapValue;
                }
                Number currentValue = ((IntProperty) ((PropertyMarkerValue) this.mMarkerValues.get(0)).getProperty()).get(source);
                if (currentValue.intValue() < value1) {
                    return Integer.valueOf(value1);
                }
                if (currentValue.intValue() > value2) {
                    return Integer.valueOf(value2);
                }
                return currentValue;
            } else {
                throw new RuntimeException("Marker value must use same Property for direct mapping");
            }
        }

        /* Access modifiers changed, original: 0000 */
        public float calculateFraction(Parallax source) {
            int lastIndex = 0;
            int lastValue = 0;
            int lastMarkerValue = 0;
            for (int i = 0; i < this.mMarkerValues.size(); i++) {
                IntPropertyMarkerValue k = (IntPropertyMarkerValue) this.mMarkerValues.get(i);
                int index = ((IntProperty) k.getProperty()).getIndex();
                int markerValue = k.getMarkerValue(source);
                int currentValue = source.getIntPropertyValue(index);
                if (i == 0) {
                    if (currentValue >= markerValue) {
                        return 0.0f;
                    }
                } else if (lastIndex == index && lastMarkerValue < markerValue) {
                    throw new IllegalStateException("marker value of same variable must be descendant order");
                } else if (currentValue == Integer.MAX_VALUE) {
                    return getFractionWithWeightAdjusted(((float) (lastMarkerValue - lastValue)) / source.getMaxValue(), i);
                } else {
                    if (currentValue >= markerValue) {
                        float fraction;
                        if (lastIndex == index) {
                            fraction = ((float) (lastMarkerValue - currentValue)) / ((float) (lastMarkerValue - markerValue));
                        } else if (lastValue != Integer.MIN_VALUE) {
                            lastMarkerValue += currentValue - lastValue;
                            fraction = ((float) (lastMarkerValue - currentValue)) / ((float) (lastMarkerValue - markerValue));
                        } else {
                            fraction = 1.0f - (((float) (currentValue - markerValue)) / source.getMaxValue());
                        }
                        return getFractionWithWeightAdjusted(fraction, i);
                    }
                }
                lastValue = currentValue;
                lastIndex = index;
                lastMarkerValue = markerValue;
            }
            return 1.0f;
        }
    }

    public abstract Number calculateDirectValue(Parallax parallax);

    public abstract float calculateFraction(Parallax parallax);

    ParallaxEffect() {
    }

    public final List<PropertyMarkerValue> getPropertyRanges() {
        return this.mMarkerValues;
    }

    @RestrictTo({Scope.LIBRARY})
    public final List<Float> getWeights() {
        return this.mWeights;
    }

    public final void setPropertyRanges(PropertyMarkerValue... markerValues) {
        this.mMarkerValues.clear();
        for (PropertyMarkerValue markerValue : markerValues) {
            this.mMarkerValues.add(markerValue);
        }
    }

    @RestrictTo({Scope.LIBRARY})
    public final void setWeights(float... weights) {
        int length = weights.length;
        int i = 0;
        int i2 = 0;
        while (i2 < length) {
            if (weights[i2] > 0.0f) {
                i2++;
            } else {
                throw new IllegalArgumentException();
            }
        }
        this.mWeights.clear();
        this.mTotalWeights.clear();
        float totalWeight = 0.0f;
        i2 = weights.length;
        while (i < i2) {
            float weight = weights[i];
            this.mWeights.add(Float.valueOf(weight));
            totalWeight += weight;
            this.mTotalWeights.add(Float.valueOf(totalWeight));
            i++;
        }
    }

    @RestrictTo({Scope.LIBRARY})
    public final ParallaxEffect weights(float... weights) {
        setWeights(weights);
        return this;
    }

    public final void addTarget(ParallaxTarget target) {
        this.mTargets.add(target);
    }

    public final ParallaxEffect target(ParallaxTarget target) {
        this.mTargets.add(target);
        return this;
    }

    public final ParallaxEffect target(Object targetObject, PropertyValuesHolder values) {
        this.mTargets.add(new PropertyValuesHolderTarget(targetObject, values));
        return this;
    }

    public final <T, V extends Number> ParallaxEffect target(T targetObject, Property<T, V> targetProperty) {
        this.mTargets.add(new DirectPropertyTarget(targetObject, targetProperty));
        return this;
    }

    public final List<ParallaxTarget> getTargets() {
        return this.mTargets;
    }

    public final void removeTarget(ParallaxTarget target) {
        this.mTargets.remove(target);
    }

    public final void performMapping(Parallax source) {
        if (this.mMarkerValues.size() >= 2) {
            if (this instanceof IntEffect) {
                source.verifyIntProperties();
            } else {
                source.verifyFloatProperties();
            }
            boolean fractionCalculated = false;
            float fraction = 0.0f;
            Number directValue = null;
            for (int i = 0; i < this.mTargets.size(); i++) {
                ParallaxTarget target = (ParallaxTarget) this.mTargets.get(i);
                if (target.isDirectMapping()) {
                    if (directValue == null) {
                        directValue = calculateDirectValue(source);
                    }
                    target.directUpdate(directValue);
                } else {
                    if (!fractionCalculated) {
                        fractionCalculated = true;
                        fraction = calculateFraction(source);
                    }
                    target.update(fraction);
                }
            }
        }
    }

    /* Access modifiers changed, original: final */
    public final float getFractionWithWeightAdjusted(float fraction, int markerValueIndex) {
        if (this.mMarkerValues.size() < 3) {
            return fraction;
        }
        float allWeights;
        if (this.mWeights.size() == this.mMarkerValues.size() - 1) {
            allWeights = ((Float) this.mTotalWeights.get(this.mTotalWeights.size() - 1)).floatValue();
            float fraction2 = (((Float) this.mWeights.get(markerValueIndex - 1)).floatValue() * fraction) / allWeights;
            if (markerValueIndex >= 2) {
                fraction2 += ((Float) this.mTotalWeights.get(markerValueIndex - 2)).floatValue() / allWeights;
            }
            return fraction2;
        }
        allWeights = (float) (this.mMarkerValues.size() - 1);
        fraction /= allWeights;
        if (markerValueIndex >= 2) {
            return fraction + (((float) (markerValueIndex - 1)) / allWeights);
        }
        return fraction;
    }
}
