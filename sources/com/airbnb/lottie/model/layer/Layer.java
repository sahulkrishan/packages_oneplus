package com.airbnb.lottie.model.layer;

import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableTextFrame;
import com.airbnb.lottie.model.animatable.AnimatableTextProperties;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.content.Mask;
import com.airbnb.lottie.model.content.ShapeGroup;
import com.airbnb.lottie.utils.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

public class Layer {
    private static final String TAG = Layer.class.getSimpleName();
    private final LottieComposition composition;
    private final List<Keyframe<Float>> inOutKeyframes;
    private final long layerId;
    private final String layerName;
    private final LayerType layerType;
    private final List<Mask> masks;
    private final MatteType matteType;
    private final long parentId;
    private final int preCompHeight;
    private final int preCompWidth;
    @Nullable
    private final String refId;
    private final List<ContentModel> shapes;
    private final int solidColor;
    private final int solidHeight;
    private final int solidWidth;
    private final float startProgress;
    @Nullable
    private final AnimatableTextFrame text;
    @Nullable
    private final AnimatableTextProperties textProperties;
    @Nullable
    private final AnimatableFloatValue timeRemapping;
    private final float timeStretch;
    private final AnimatableTransform transform;

    public static class Factory {
        private Factory() {
        }

        public static Layer newInstance(LottieComposition composition) {
            Rect bounds = composition.getBounds();
            return new Layer(Collections.emptyList(), composition, "root", -1, LayerType.PreComp, -1, null, Collections.emptyList(), com.airbnb.lottie.model.animatable.AnimatableTransform.Factory.newInstance(), 0, 0, 0, 0.0f, 0.0f, bounds.width(), bounds.height(), null, null, Collections.emptyList(), MatteType.None, null);
        }

        public static Layer newInstance(JSONObject json, LottieComposition composition) {
            LayerType layerType;
            AnimatableTextFrame text;
            AnimatableTextProperties textProperties;
            int preCompWidth;
            int preCompHeight;
            float inFrame;
            List<ContentModel> shapes;
            JSONObject jSONObject = json;
            LottieComposition lottieComposition = composition;
            String layerName = jSONObject.optString("nm");
            String refId = jSONObject.optString("refId");
            if (layerName.endsWith(".ai") || jSONObject.optString("cl", "").equals("ai")) {
                lottieComposition.addWarning("Convert your Illustrator layers to shape layers.");
            }
            long layerId = jSONObject.optLong("ind");
            int solidWidth = 0;
            int solidHeight = 0;
            int solidColor = 0;
            int layerTypeInt = jSONObject.optInt("ty", -1);
            if (layerTypeInt < LayerType.Unknown.ordinal()) {
                layerType = LayerType.values()[layerTypeInt];
            } else {
                layerType = LayerType.Unknown;
            }
            if (layerType == LayerType.Text && !Utils.isAtLeastVersion(lottieComposition, 4, 8, 0)) {
                layerType = LayerType.Unknown;
                lottieComposition.addWarning("Text is only supported on bodymovin >= 4.8.0");
            }
            LayerType layerType2 = layerType;
            long parentId = jSONObject.optLong("parent", -1);
            if (layerType2 == LayerType.Solid) {
                solidWidth = (int) (((float) jSONObject.optInt("sw")) * composition.getDpScale());
                solidHeight = (int) (((float) jSONObject.optInt("sh")) * composition.getDpScale());
                solidColor = Color.parseColor(jSONObject.optString("sc"));
            }
            int solidWidth2 = solidWidth;
            int solidHeight2 = solidHeight;
            int solidColor2 = solidColor;
            AnimatableTransform transform = com.airbnb.lottie.model.animatable.AnimatableTransform.Factory.newInstance(jSONObject.optJSONObject("ks"), lottieComposition);
            MatteType matteType = MatteType.values()[jSONObject.optInt("tt")];
            List<Mask> arrayList = new ArrayList();
            List<Keyframe<Float>> inOutKeyframes = new ArrayList();
            JSONArray jsonMasks = jSONObject.optJSONArray("masksProperties");
            if (jsonMasks != null) {
                for (solidWidth = 0; solidWidth < jsonMasks.length(); solidWidth++) {
                    arrayList.add(com.airbnb.lottie.model.content.Mask.Factory.newMask(jsonMasks.optJSONObject(solidWidth), lottieComposition));
                }
            }
            List<ContentModel> shapes2 = new ArrayList();
            JSONArray shapesJson = jSONObject.optJSONArray("shapes");
            if (shapesJson != null) {
                for (solidWidth = 0; solidWidth < shapesJson.length(); solidWidth++) {
                    ContentModel shape = ShapeGroup.shapeItemWithJson(shapesJson.optJSONObject(solidWidth), lottieComposition);
                    if (shape != null) {
                        shapes2.add(shape);
                    }
                }
            }
            JSONObject textJson = jSONObject.optJSONObject("t");
            if (textJson != null) {
                text = com.airbnb.lottie.model.animatable.AnimatableTextFrame.Factory.newInstance(textJson.optJSONObject("d"), lottieComposition);
                textProperties = com.airbnb.lottie.model.animatable.AnimatableTextProperties.Factory.newInstance(textJson.optJSONArray("a").optJSONObject(null), lottieComposition);
            } else {
                text = null;
                textProperties = null;
            }
            if (jSONObject.has("ef")) {
                lottieComposition.addWarning("Lottie doesn't support layer effects. If you are using them for  fills, strokes, trim paths etc. then try adding them directly as contents  in your shape.");
            }
            float timeStretch = (float) jSONObject.optDouble("sr", 1.0d);
            float startFrame = (float) jSONObject.optDouble("st");
            float startProgress = startFrame / composition.getDurationFrames();
            if (layerType2 == LayerType.PreComp) {
                preCompWidth = (int) (((float) jSONObject.optInt("w")) * composition.getDpScale());
                preCompHeight = (int) (((float) jSONObject.optInt("h")) * composition.getDpScale());
            } else {
                preCompWidth = 0;
                preCompHeight = 0;
            }
            float inFrame2 = ((float) jSONObject.optLong("ip")) / timeStretch;
            float outFrame = ((float) jSONObject.optLong("op")) / timeStretch;
            if (inFrame2 > 0.0f) {
                inFrame = inFrame2;
                shapes = shapes2;
                inOutKeyframes.add(new Keyframe(lottieComposition, Float.valueOf(0.0f), Float.valueOf(0.0f), null, 0.0f, Float.valueOf(inFrame2)));
            } else {
                inFrame = inFrame2;
                JSONArray jSONArray = shapesJson;
                shapes = shapes2;
            }
            float outFrame2 = outFrame > 0.0f ? outFrame : (float) (composition.getEndFrame() + 1);
            LottieComposition lottieComposition2 = lottieComposition;
            float outFrame3 = outFrame2;
            float timeStretch2 = timeStretch;
            timeStretch = 0.0f;
            Keyframe<Float> visibleKeyframe = new Keyframe(lottieComposition2, Float.valueOf(1.0f), Float.valueOf(1.0f), null, inFrame, Float.valueOf(outFrame2));
            inOutKeyframes.add(visibleKeyframe);
            Keyframe<Float> outKeyframe = new Keyframe(lottieComposition2, Float.valueOf(timeStretch), Float.valueOf(timeStretch), null, outFrame3, Float.valueOf(Float.MAX_VALUE));
            inOutKeyframes.add(outKeyframe);
            AnimatableFloatValue timeRemapping = null;
            if (jSONObject.has("tm")) {
                timeRemapping = com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(jSONObject.optJSONObject("tm"), lottieComposition, false);
            }
            float timeStretch3 = timeStretch2;
            List<Mask> masks = arrayList;
            return new Layer(shapes, lottieComposition, layerName, layerId, layerType2, parentId, refId, arrayList, transform, solidWidth2, solidHeight2, solidColor2, timeStretch3, startProgress, preCompWidth, preCompHeight, text, textProperties, inOutKeyframes, matteType, timeRemapping);
        }
    }

    public enum LayerType {
        PreComp,
        Solid,
        Image,
        Null,
        Shape,
        Text,
        Unknown
    }

    enum MatteType {
        None,
        Add,
        Invert,
        Unknown
    }

    private Layer(List<ContentModel> shapes, LottieComposition composition, String layerName, long layerId, LayerType layerType, long parentId, @Nullable String refId, List<Mask> masks, AnimatableTransform transform, int solidWidth, int solidHeight, int solidColor, float timeStretch, float startProgress, int preCompWidth, int preCompHeight, @Nullable AnimatableTextFrame text, @Nullable AnimatableTextProperties textProperties, List<Keyframe<Float>> inOutKeyframes, MatteType matteType, @Nullable AnimatableFloatValue timeRemapping) {
        this.shapes = shapes;
        this.composition = composition;
        this.layerName = layerName;
        this.layerId = layerId;
        this.layerType = layerType;
        this.parentId = parentId;
        this.refId = refId;
        this.masks = masks;
        this.transform = transform;
        this.solidWidth = solidWidth;
        this.solidHeight = solidHeight;
        this.solidColor = solidColor;
        this.timeStretch = timeStretch;
        this.startProgress = startProgress;
        this.preCompWidth = preCompWidth;
        this.preCompHeight = preCompHeight;
        this.text = text;
        this.textProperties = textProperties;
        this.inOutKeyframes = inOutKeyframes;
        this.matteType = matteType;
        this.timeRemapping = timeRemapping;
    }

    /* Access modifiers changed, original: 0000 */
    public LottieComposition getComposition() {
        return this.composition;
    }

    /* Access modifiers changed, original: 0000 */
    public float getTimeStretch() {
        return this.timeStretch;
    }

    /* Access modifiers changed, original: 0000 */
    public float getStartProgress() {
        return this.startProgress;
    }

    /* Access modifiers changed, original: 0000 */
    public List<Keyframe<Float>> getInOutKeyframes() {
        return this.inOutKeyframes;
    }

    public long getId() {
        return this.layerId;
    }

    /* Access modifiers changed, original: 0000 */
    public String getName() {
        return this.layerName;
    }

    /* Access modifiers changed, original: 0000 */
    @Nullable
    public String getRefId() {
        return this.refId;
    }

    /* Access modifiers changed, original: 0000 */
    public int getPreCompWidth() {
        return this.preCompWidth;
    }

    /* Access modifiers changed, original: 0000 */
    public int getPreCompHeight() {
        return this.preCompHeight;
    }

    /* Access modifiers changed, original: 0000 */
    public List<Mask> getMasks() {
        return this.masks;
    }

    public LayerType getLayerType() {
        return this.layerType;
    }

    /* Access modifiers changed, original: 0000 */
    public MatteType getMatteType() {
        return this.matteType;
    }

    /* Access modifiers changed, original: 0000 */
    public long getParentId() {
        return this.parentId;
    }

    /* Access modifiers changed, original: 0000 */
    public List<ContentModel> getShapes() {
        return this.shapes;
    }

    /* Access modifiers changed, original: 0000 */
    public AnimatableTransform getTransform() {
        return this.transform;
    }

    /* Access modifiers changed, original: 0000 */
    public int getSolidColor() {
        return this.solidColor;
    }

    /* Access modifiers changed, original: 0000 */
    public int getSolidHeight() {
        return this.solidHeight;
    }

    /* Access modifiers changed, original: 0000 */
    public int getSolidWidth() {
        return this.solidWidth;
    }

    /* Access modifiers changed, original: 0000 */
    @Nullable
    public AnimatableTextFrame getText() {
        return this.text;
    }

    /* Access modifiers changed, original: 0000 */
    @Nullable
    public AnimatableTextProperties getTextProperties() {
        return this.textProperties;
    }

    /* Access modifiers changed, original: 0000 */
    @Nullable
    public AnimatableFloatValue getTimeRemapping() {
        return this.timeRemapping;
    }

    public String toString() {
        return toString("");
    }

    public String toString(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(getName());
        sb.append("\n");
        Layer parent = this.composition.layerModelForId(getParentId());
        if (parent != null) {
            sb.append("\t\tParents: ");
            sb.append(parent.getName());
            parent = this.composition.layerModelForId(parent.getParentId());
            while (parent != null) {
                sb.append("->");
                sb.append(parent.getName());
                parent = this.composition.layerModelForId(parent.getParentId());
            }
            sb.append(prefix);
            sb.append("\n");
        }
        if (!getMasks().isEmpty()) {
            sb.append(prefix);
            sb.append("\tMasks: ");
            sb.append(getMasks().size());
            sb.append("\n");
        }
        if (!(getSolidWidth() == 0 || getSolidHeight() == 0)) {
            sb.append(prefix);
            sb.append("\tBackground: ");
            sb.append(String.format(Locale.US, "%dx%d %X\n", new Object[]{Integer.valueOf(getSolidWidth()), Integer.valueOf(getSolidHeight()), Integer.valueOf(getSolidColor())}));
        }
        if (!this.shapes.isEmpty()) {
            sb.append(prefix);
            sb.append("\tShapes:\n");
            for (Object shape : this.shapes) {
                sb.append(prefix);
                sb.append("\t\t");
                sb.append(shape);
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
