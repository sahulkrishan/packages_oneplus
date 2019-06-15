package com.airbnb.lottie.model.content;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.ContentGroup;
import com.airbnb.lottie.model.layer.BaseLayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class ShapeGroup implements ContentModel {
    private final List<ContentModel> items;
    private final String name;

    static class Factory {
        private Factory() {
        }

        private static ShapeGroup newInstance(JSONObject json, LottieComposition composition) {
            JSONArray jsonItems = json.optJSONArray("it");
            String name = json.optString("nm");
            List<ContentModel> items = new ArrayList();
            for (int i = 0; i < jsonItems.length(); i++) {
                ContentModel newItem = ShapeGroup.shapeItemWithJson(jsonItems.optJSONObject(i), composition);
                if (newItem != null) {
                    items.add(newItem);
                }
            }
            return new ShapeGroup(name, items);
        }
    }

    @android.support.annotation.Nullable
    public static com.airbnb.lottie.model.content.ContentModel shapeItemWithJson(org.json.JSONObject r4, com.airbnb.lottie.LottieComposition r5) {
        /*
        r0 = "ty";
        r0 = r4.optString(r0);
        r1 = r0.hashCode();
        switch(r1) {
            case 3239: goto L_0x008f;
            case 3270: goto L_0x0085;
            case 3295: goto L_0x007b;
            case 3307: goto L_0x0071;
            case 3308: goto L_0x0067;
            case 3488: goto L_0x005c;
            case 3633: goto L_0x0051;
            case 3646: goto L_0x0046;
            case 3669: goto L_0x003c;
            case 3679: goto L_0x0031;
            case 3681: goto L_0x0026;
            case 3705: goto L_0x001a;
            case 3710: goto L_0x000f;
            default: goto L_0x000d;
        };
    L_0x000d:
        goto L_0x0099;
    L_0x000f:
        r1 = "tr";
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x0099;
    L_0x0017:
        r1 = 5;
        goto L_0x009a;
    L_0x001a:
        r1 = "tm";
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x0099;
    L_0x0022:
        r1 = 9;
        goto L_0x009a;
    L_0x0026:
        r1 = "st";
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x0099;
    L_0x002e:
        r1 = 1;
        goto L_0x009a;
    L_0x0031:
        r1 = "sr";
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x0099;
    L_0x0039:
        r1 = 10;
        goto L_0x009a;
    L_0x003c:
        r1 = "sh";
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x0099;
    L_0x0044:
        r1 = 6;
        goto L_0x009a;
    L_0x0046:
        r1 = "rp";
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x0099;
    L_0x004e:
        r1 = 12;
        goto L_0x009a;
    L_0x0051:
        r1 = "rc";
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x0099;
    L_0x0059:
        r1 = 8;
        goto L_0x009a;
    L_0x005c:
        r1 = "mm";
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x0099;
    L_0x0064:
        r1 = 11;
        goto L_0x009a;
    L_0x0067:
        r1 = "gs";
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x0099;
    L_0x006f:
        r1 = 2;
        goto L_0x009a;
    L_0x0071:
        r1 = "gr";
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x0099;
    L_0x0079:
        r1 = 0;
        goto L_0x009a;
    L_0x007b:
        r1 = "gf";
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x0099;
    L_0x0083:
        r1 = 4;
        goto L_0x009a;
    L_0x0085:
        r1 = "fl";
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x0099;
    L_0x008d:
        r1 = 3;
        goto L_0x009a;
    L_0x008f:
        r1 = "el";
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x0099;
    L_0x0097:
        r1 = 7;
        goto L_0x009a;
    L_0x0099:
        r1 = -1;
    L_0x009a:
        switch(r1) {
            case 0: goto L_0x00f1;
            case 1: goto L_0x00ec;
            case 2: goto L_0x00e7;
            case 3: goto L_0x00e2;
            case 4: goto L_0x00dd;
            case 5: goto L_0x00d8;
            case 6: goto L_0x00d3;
            case 7: goto L_0x00ce;
            case 8: goto L_0x00c9;
            case 9: goto L_0x00c4;
            case 10: goto L_0x00bf;
            case 11: goto L_0x00ba;
            case 12: goto L_0x00b5;
            default: goto L_0x009d;
        };
    L_0x009d:
        r1 = "LOTTIE";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "Unknown shape type ";
        r2.append(r3);
        r2.append(r0);
        r2 = r2.toString();
        android.util.Log.w(r1, r2);
        r1 = 0;
        return r1;
    L_0x00b5:
        r1 = com.airbnb.lottie.model.content.Repeater.Factory.newInstance(r4, r5);
        return r1;
    L_0x00ba:
        r1 = com.airbnb.lottie.model.content.MergePaths.Factory.newInstance(r4);
        return r1;
    L_0x00bf:
        r1 = com.airbnb.lottie.model.content.PolystarShape.Factory.newInstance(r4, r5);
        return r1;
    L_0x00c4:
        r1 = com.airbnb.lottie.model.content.ShapeTrimPath.Factory.newInstance(r4, r5);
        return r1;
    L_0x00c9:
        r1 = com.airbnb.lottie.model.content.RectangleShape.Factory.newInstance(r4, r5);
        return r1;
    L_0x00ce:
        r1 = com.airbnb.lottie.model.content.CircleShape.Factory.newInstance(r4, r5);
        return r1;
    L_0x00d3:
        r1 = com.airbnb.lottie.model.content.ShapePath.Factory.newInstance(r4, r5);
        return r1;
    L_0x00d8:
        r1 = com.airbnb.lottie.model.animatable.AnimatableTransform.Factory.newInstance(r4, r5);
        return r1;
    L_0x00dd:
        r1 = com.airbnb.lottie.model.content.GradientFill.Factory.newInstance(r4, r5);
        return r1;
    L_0x00e2:
        r1 = com.airbnb.lottie.model.content.ShapeFill.Factory.newInstance(r4, r5);
        return r1;
    L_0x00e7:
        r1 = com.airbnb.lottie.model.content.GradientStroke.Factory.newInstance(r4, r5);
        return r1;
    L_0x00ec:
        r1 = com.airbnb.lottie.model.content.ShapeStroke.Factory.newInstance(r4, r5);
        return r1;
    L_0x00f1:
        r1 = com.airbnb.lottie.model.content.ShapeGroup.Factory.newInstance(r4, r5);
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airbnb.lottie.model.content.ShapeGroup.shapeItemWithJson(org.json.JSONObject, com.airbnb.lottie.LottieComposition):com.airbnb.lottie.model.content.ContentModel");
    }

    public ShapeGroup(String name, List<ContentModel> items) {
        this.name = name;
        this.items = items;
    }

    public String getName() {
        return this.name;
    }

    public List<ContentModel> getItems() {
        return this.items;
    }

    public Content toContent(LottieDrawable drawable, BaseLayer layer) {
        return new ContentGroup(drawable, layer, this);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ShapeGroup{name='");
        stringBuilder.append(this.name);
        stringBuilder.append("' Shapes: ");
        stringBuilder.append(Arrays.toString(this.items.toArray()));
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
