package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import java.util.ArrayList;
import java.util.List;

public class CompositionLayer extends BaseLayer {
    @Nullable
    private Boolean hasMasks;
    @Nullable
    private Boolean hasMatte;
    private final List<BaseLayer> layers = new ArrayList();
    private final RectF newClipRect = new RectF();
    private final RectF rect = new RectF();
    @Nullable
    private final BaseKeyframeAnimation<Float, Float> timeRemapping;

    public CompositionLayer(LottieDrawable lottieDrawable, Layer layerModel, List<Layer> layerModels, LottieComposition composition) {
        super(lottieDrawable, layerModel);
        AnimatableFloatValue timeRemapping = layerModel.getTimeRemapping();
        if (timeRemapping != null) {
            this.timeRemapping = timeRemapping.createAnimation();
            addAnimation(this.timeRemapping);
            this.timeRemapping.addUpdateListener(this);
        } else {
            this.timeRemapping = null;
        }
        LongSparseArray<BaseLayer> layerMap = new LongSparseArray(composition.getLayers().size());
        BaseLayer mattedLayer = null;
        int i = layerModels.size();
        while (true) {
            i--;
            int i2 = 0;
            BaseLayer layer;
            if (i >= 0) {
                Layer lm = (Layer) layerModels.get(i);
                layer = BaseLayer.forModel(lm, lottieDrawable, composition);
                if (layer != null) {
                    layerMap.put(layer.getLayerModel().getId(), layer);
                    if (mattedLayer == null) {
                        this.layers.add(0, layer);
                        switch (lm.getMatteType()) {
                            case Add:
                            case Invert:
                                mattedLayer = layer;
                                break;
                            default:
                                break;
                        }
                    }
                    mattedLayer.setMatteLayer(layer);
                    mattedLayer = null;
                }
            } else {
                while (true) {
                    i = i2;
                    if (i < layerMap.size()) {
                        layer = (BaseLayer) layerMap.get(layerMap.keyAt(i));
                        BaseLayer parentLayer = (BaseLayer) layerMap.get(layer.getLayerModel().getParentId());
                        if (parentLayer != null) {
                            layer.setParentLayer(parentLayer);
                        }
                        i2 = i + 1;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        L.beginSection("CompositionLayer#draw");
        canvas.save();
        this.newClipRect.set(0.0f, 0.0f, (float) this.layerModel.getPreCompWidth(), (float) this.layerModel.getPreCompHeight());
        parentMatrix.mapRect(this.newClipRect);
        for (int i = this.layers.size() - 1; i >= 0; i--) {
            boolean nonEmptyClip = true;
            if (!this.newClipRect.isEmpty()) {
                nonEmptyClip = canvas.clipRect(this.newClipRect);
            }
            if (nonEmptyClip) {
                ((BaseLayer) this.layers.get(i)).draw(canvas, parentMatrix, parentAlpha);
            }
        }
        canvas.restore();
        L.endSection("CompositionLayer#draw");
    }

    public void getBounds(RectF outBounds, Matrix parentMatrix) {
        super.getBounds(outBounds, parentMatrix);
        this.rect.set(0.0f, 0.0f, 0.0f, 0.0f);
        for (int i = this.layers.size() - 1; i >= 0; i--) {
            ((BaseLayer) this.layers.get(i)).getBounds(this.rect, this.boundsMatrix);
            if (outBounds.isEmpty()) {
                outBounds.set(this.rect);
            } else {
                outBounds.set(Math.min(outBounds.left, this.rect.left), Math.min(outBounds.top, this.rect.top), Math.max(outBounds.right, this.rect.right), Math.max(outBounds.bottom, this.rect.bottom));
            }
        }
    }

    public void setProgress(@FloatRange(from = 0.0d, to = 1.0d) float progress) {
        super.setProgress(progress);
        if (this.timeRemapping != null) {
            progress = ((float) ((long) (((Float) this.timeRemapping.getValue()).floatValue() * 1000.0f))) / ((float) this.lottieDrawable.getComposition().getDuration());
        }
        if (this.layerModel.getTimeStretch() != 0.0f) {
            progress /= this.layerModel.getTimeStretch();
        }
        progress -= this.layerModel.getStartProgress();
        for (int i = this.layers.size() - 1; i >= 0; i--) {
            ((BaseLayer) this.layers.get(i)).setProgress(progress);
        }
    }

    public boolean hasMasks() {
        if (this.hasMasks == null) {
            for (int i = this.layers.size() - 1; i >= 0; i--) {
                BaseLayer layer = (BaseLayer) this.layers.get(i);
                if (layer instanceof ShapeLayer) {
                    if (layer.hasMasksOnThisLayer()) {
                        this.hasMasks = Boolean.valueOf(true);
                        return true;
                    }
                } else if ((layer instanceof CompositionLayer) && ((CompositionLayer) layer).hasMasks()) {
                    this.hasMasks = Boolean.valueOf(true);
                    return true;
                }
            }
            this.hasMasks = Boolean.valueOf(false);
        }
        return this.hasMasks.booleanValue();
    }

    public boolean hasMatte() {
        if (this.hasMatte == null) {
            if (hasMatteOnThisLayer()) {
                this.hasMatte = Boolean.valueOf(true);
                return true;
            }
            for (int i = this.layers.size() - 1; i >= 0; i--) {
                if (((BaseLayer) this.layers.get(i)).hasMatteOnThisLayer()) {
                    this.hasMatte = Boolean.valueOf(true);
                    return true;
                }
            }
            this.hasMatte = Boolean.valueOf(false);
        }
        return this.hasMatte.booleanValue();
    }

    public void addColorFilter(@Nullable String layerName, @Nullable String contentName, @Nullable ColorFilter colorFilter) {
        for (int i = 0; i < this.layers.size(); i++) {
            BaseLayer layer = (BaseLayer) this.layers.get(i);
            String name = layer.getLayerModel().getName();
            if (layerName == null) {
                layer.addColorFilter(null, null, colorFilter);
            } else if (name.equals(layerName)) {
                layer.addColorFilter(layerName, contentName, colorFilter);
            }
        }
    }
}
