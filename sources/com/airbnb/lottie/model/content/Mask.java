package com.airbnb.lottie.model.content;

import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatableShapeValue;

public class Mask {
    private final MaskMode maskMode;
    private final AnimatableShapeValue maskPath;
    private final AnimatableIntegerValue opacity;

    public static class Factory {
        private Factory() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:17:0x0039  */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x0042  */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x003f  */
        /* JADX WARNING: Removed duplicated region for block: B:18:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x0039  */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x0042  */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x003f  */
        /* JADX WARNING: Removed duplicated region for block: B:18:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x0039  */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x0042  */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x003f  */
        /* JADX WARNING: Removed duplicated region for block: B:18:0x003c  */
        public static com.airbnb.lottie.model.content.Mask newMask(org.json.JSONObject r6, com.airbnb.lottie.LottieComposition r7) {
            /*
            r0 = "mode";
            r0 = r6.optString(r0);
            r1 = r0.hashCode();
            r2 = 97;
            if (r1 == r2) goto L_0x002b;
        L_0x000e:
            r2 = 105; // 0x69 float:1.47E-43 double:5.2E-322;
            if (r1 == r2) goto L_0x0021;
        L_0x0012:
            r2 = 115; // 0x73 float:1.61E-43 double:5.7E-322;
            if (r1 == r2) goto L_0x0017;
        L_0x0016:
            goto L_0x0035;
        L_0x0017:
            r1 = "s";
            r0 = r0.equals(r1);
            if (r0 == 0) goto L_0x0035;
        L_0x001f:
            r0 = 1;
            goto L_0x0036;
        L_0x0021:
            r1 = "i";
            r0 = r0.equals(r1);
            if (r0 == 0) goto L_0x0035;
        L_0x0029:
            r0 = 2;
            goto L_0x0036;
        L_0x002b:
            r1 = "a";
            r0 = r0.equals(r1);
            if (r0 == 0) goto L_0x0035;
        L_0x0033:
            r0 = 0;
            goto L_0x0036;
        L_0x0035:
            r0 = -1;
        L_0x0036:
            switch(r0) {
                case 0: goto L_0x0042;
                case 1: goto L_0x003f;
                case 2: goto L_0x003c;
                default: goto L_0x0039;
            };
        L_0x0039:
            r0 = com.airbnb.lottie.model.content.Mask.MaskMode.MaskModeUnknown;
            goto L_0x0045;
        L_0x003c:
            r0 = com.airbnb.lottie.model.content.Mask.MaskMode.MaskModeIntersect;
            goto L_0x0045;
        L_0x003f:
            r0 = com.airbnb.lottie.model.content.Mask.MaskMode.MaskModeSubtract;
            goto L_0x0045;
        L_0x0042:
            r0 = com.airbnb.lottie.model.content.Mask.MaskMode.MaskModeAdd;
            r1 = "pt";
            r1 = r6.optJSONObject(r1);
            r1 = com.airbnb.lottie.model.animatable.AnimatableShapeValue.Factory.newInstance(r1, r7);
            r2 = "o";
            r2 = r6.optJSONObject(r2);
            r3 = com.airbnb.lottie.model.animatable.AnimatableIntegerValue.Factory.newInstance(r2, r7);
            r4 = new com.airbnb.lottie.model.content.Mask;
            r5 = 0;
            r4.<init>(r0, r1, r3);
            return r4;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.airbnb.lottie.model.content.Mask$Factory.newMask(org.json.JSONObject, com.airbnb.lottie.LottieComposition):com.airbnb.lottie.model.content.Mask");
        }
    }

    public enum MaskMode {
        MaskModeAdd,
        MaskModeSubtract,
        MaskModeIntersect,
        MaskModeUnknown
    }

    private Mask(MaskMode maskMode, AnimatableShapeValue maskPath, AnimatableIntegerValue opacity) {
        this.maskMode = maskMode;
        this.maskPath = maskPath;
        this.opacity = opacity;
    }

    public MaskMode getMaskMode() {
        return this.maskMode;
    }

    public AnimatableShapeValue getMaskPath() {
        return this.maskPath;
    }

    public AnimatableIntegerValue getOpacity() {
        return this.opacity;
    }
}
