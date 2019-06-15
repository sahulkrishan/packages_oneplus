package com.airbnb.lottie.animation.content;

import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation.AnimationListener;
import com.airbnb.lottie.model.content.PolystarShape;
import com.airbnb.lottie.model.content.PolystarShape.Type;
import com.airbnb.lottie.model.content.ShapeTrimPath;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.Utils;
import java.util.List;

public class PolystarContent implements PathContent, AnimationListener {
    private static final float POLYGON_MAGIC_NUMBER = 0.25f;
    private static final float POLYSTAR_MAGIC_NUMBER = 0.47829f;
    @Nullable
    private final BaseKeyframeAnimation<?, Float> innerRadiusAnimation;
    @Nullable
    private final BaseKeyframeAnimation<?, Float> innerRoundednessAnimation;
    private boolean isPathValid;
    private final LottieDrawable lottieDrawable;
    private final String name;
    private final BaseKeyframeAnimation<?, Float> outerRadiusAnimation;
    private final BaseKeyframeAnimation<?, Float> outerRoundednessAnimation;
    private final Path path = new Path();
    private final BaseKeyframeAnimation<?, Float> pointsAnimation;
    private final BaseKeyframeAnimation<?, PointF> positionAnimation;
    private final BaseKeyframeAnimation<?, Float> rotationAnimation;
    @Nullable
    private TrimPathContent trimPath;
    private final Type type;

    public PolystarContent(LottieDrawable lottieDrawable, BaseLayer layer, PolystarShape polystarShape) {
        this.lottieDrawable = lottieDrawable;
        this.name = polystarShape.getName();
        this.type = polystarShape.getType();
        this.pointsAnimation = polystarShape.getPoints().createAnimation();
        this.positionAnimation = polystarShape.getPosition().createAnimation();
        this.rotationAnimation = polystarShape.getRotation().createAnimation();
        this.outerRadiusAnimation = polystarShape.getOuterRadius().createAnimation();
        this.outerRoundednessAnimation = polystarShape.getOuterRoundedness().createAnimation();
        if (this.type == Type.Star) {
            this.innerRadiusAnimation = polystarShape.getInnerRadius().createAnimation();
            this.innerRoundednessAnimation = polystarShape.getInnerRoundedness().createAnimation();
        } else {
            this.innerRadiusAnimation = null;
            this.innerRoundednessAnimation = null;
        }
        layer.addAnimation(this.pointsAnimation);
        layer.addAnimation(this.positionAnimation);
        layer.addAnimation(this.rotationAnimation);
        layer.addAnimation(this.outerRadiusAnimation);
        layer.addAnimation(this.outerRoundednessAnimation);
        if (this.type == Type.Star) {
            layer.addAnimation(this.innerRadiusAnimation);
            layer.addAnimation(this.innerRoundednessAnimation);
        }
        this.pointsAnimation.addUpdateListener(this);
        this.positionAnimation.addUpdateListener(this);
        this.rotationAnimation.addUpdateListener(this);
        this.outerRadiusAnimation.addUpdateListener(this);
        this.outerRoundednessAnimation.addUpdateListener(this);
        if (this.type == Type.Star) {
            this.outerRadiusAnimation.addUpdateListener(this);
            this.outerRoundednessAnimation.addUpdateListener(this);
        }
    }

    public void onValueChanged() {
        invalidate();
    }

    private void invalidate() {
        this.isPathValid = false;
        this.lottieDrawable.invalidateSelf();
    }

    public void setContents(List<Content> contentsBefore, List<Content> list) {
        for (int i = 0; i < contentsBefore.size(); i++) {
            Content content = (Content) contentsBefore.get(i);
            if ((content instanceof TrimPathContent) && ((TrimPathContent) content).getType() == ShapeTrimPath.Type.Simultaneously) {
                this.trimPath = (TrimPathContent) content;
                this.trimPath.addListener(this);
            }
        }
    }

    public Path getPath() {
        if (this.isPathValid) {
            return this.path;
        }
        this.path.reset();
        switch (this.type) {
            case Star:
                createStarPath();
                break;
            case Polygon:
                createPolygonPath();
                break;
        }
        this.path.close();
        Utils.applyTrimPathIfNeeded(this.path, this.trimPath);
        this.isPathValid = true;
        return this.path;
    }

    public String getName() {
        return this.name;
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x01b1  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x01ae  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x01b8  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x01b5  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x01bf  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x01bc  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x01c6  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x01c3  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0204  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x01e9  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0224  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0222  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0135  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x012a  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x015b  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x01ae  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x01b1  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x01b5  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x01b8  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x01bc  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x01bf  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x01c3  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x01c6  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x01e9  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0204  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0222  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0224  */
    private void createStarPath() {
        /*
        r53 = this;
        r0 = r53;
        r1 = r0.pointsAnimation;
        r1 = r1.getValue();
        r1 = (java.lang.Float) r1;
        r1 = r1.floatValue();
        r2 = r0.rotationAnimation;
        if (r2 != 0) goto L_0x0015;
    L_0x0012:
        r2 = 0;
        goto L_0x0022;
    L_0x0015:
        r2 = r0.rotationAnimation;
        r2 = r2.getValue();
        r2 = (java.lang.Float) r2;
        r2 = r2.floatValue();
        r2 = (double) r2;
    L_0x0022:
        r4 = 4636033603912859648; // 0x4056800000000000 float:0.0 double:90.0;
        r2 = r2 - r4;
        r2 = java.lang.Math.toRadians(r2);
        r4 = 4618760256179416344; // 0x401921fb54442d18 float:3.37028055E12 double:6.283185307179586;
        r6 = (double) r1;
        r4 = r4 / r6;
        r4 = (float) r4;
        r5 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = r4 / r5;
        r7 = (int) r1;
        r7 = (float) r7;
        r7 = r1 - r7;
        r8 = 0;
        r9 = (r7 > r8 ? 1 : (r7 == r8 ? 0 : -1));
        if (r9 == 0) goto L_0x0047;
    L_0x0041:
        r9 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r9 = r9 - r7;
        r9 = r9 * r6;
        r9 = (double) r9;
        r2 = r2 + r9;
    L_0x0047:
        r9 = r0.outerRadiusAnimation;
        r9 = r9.getValue();
        r9 = (java.lang.Float) r9;
        r9 = r9.floatValue();
        r10 = r0.innerRadiusAnimation;
        r10 = r10.getValue();
        r10 = (java.lang.Float) r10;
        r10 = r10.floatValue();
        r11 = 0;
        r12 = r0.innerRoundednessAnimation;
        r13 = 1120403456; // 0x42c80000 float:100.0 double:5.53552857E-315;
        if (r12 == 0) goto L_0x0074;
    L_0x0066:
        r12 = r0.innerRoundednessAnimation;
        r12 = r12.getValue();
        r12 = (java.lang.Float) r12;
        r12 = r12.floatValue();
        r11 = r12 / r13;
    L_0x0074:
        r12 = 0;
        r14 = r0.outerRoundednessAnimation;
        if (r14 == 0) goto L_0x0087;
    L_0x0079:
        r14 = r0.outerRoundednessAnimation;
        r14 = r14.getValue();
        r14 = (java.lang.Float) r14;
        r14 = r14.floatValue();
        r14 = r14 / r13;
        goto L_0x0088;
    L_0x0087:
        r14 = r12;
    L_0x0088:
        r12 = 0;
        r13 = (r7 > r8 ? 1 : (r7 == r8 ? 0 : -1));
        if (r13 == 0) goto L_0x00bb;
    L_0x008d:
        r13 = r9 - r10;
        r13 = r13 * r7;
        r12 = r10 + r13;
        r16 = r9;
        r8 = (double) r12;
        r17 = java.lang.Math.cos(r2);
        r8 = r8 * r17;
        r8 = (float) r8;
        r20 = r6;
        r5 = (double) r12;
        r17 = java.lang.Math.sin(r2);
        r5 = r5 * r17;
        r5 = (float) r5;
        r6 = r0.path;
        r6.moveTo(r8, r5);
        r6 = r4 * r7;
        r9 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = r6 / r9;
        r21 = r5;
        r5 = (double) r6;
        r2 = r2 + r5;
        r22 = r10;
        r23 = r16;
        r9 = r20;
        goto L_0x00e2;
    L_0x00bb:
        r20 = r6;
        r16 = r9;
        r5 = r16;
        r8 = (double) r5;
        r16 = java.lang.Math.cos(r2);
        r8 = r8 * r16;
        r8 = (float) r8;
        r22 = r10;
        r9 = (double) r5;
        r16 = java.lang.Math.sin(r2);
        r9 = r9 * r16;
        r6 = (float) r9;
        r9 = r0.path;
        r9.moveTo(r8, r6);
        r23 = r5;
        r24 = r6;
        r9 = r20;
        r5 = (double) r9;
        r2 = r2 + r5;
        r21 = r24;
    L_0x00e2:
        r5 = 0;
        r25 = r2;
        r2 = (double) r1;
        r2 = java.lang.Math.ceil(r2);
        r16 = 4611686018427387904; // 0x4000000000000000 float:0.0 double:2.0;
        r2 = r2 * r16;
        r10 = r8;
        r27 = r25;
        r8 = r5;
        r5 = 0;
    L_0x00f3:
        r29 = r7;
        r6 = (double) r5;
        r6 = (r6 > r2 ? 1 : (r6 == r2 ? 0 : -1));
        if (r6 >= 0) goto L_0x0239;
    L_0x00fa:
        if (r8 == 0) goto L_0x00ff;
    L_0x00fc:
        r6 = r23;
        goto L_0x0101;
    L_0x00ff:
        r6 = r22;
    L_0x0101:
        r7 = r9;
        r13 = 0;
        r18 = (r12 > r13 ? 1 : (r12 == r13 ? 0 : -1));
        if (r18 == 0) goto L_0x011b;
    L_0x0107:
        r30 = r6;
        r31 = r7;
        r6 = (double) r5;
        r24 = r2 - r16;
        r6 = (r6 > r24 ? 1 : (r6 == r24 ? 0 : -1));
        if (r6 != 0) goto L_0x0118;
    L_0x0112:
        r7 = r4 * r29;
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r7 = r7 / r6;
        goto L_0x0123;
    L_0x0118:
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        goto L_0x0121;
    L_0x011b:
        r30 = r6;
        r31 = r7;
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
    L_0x0121:
        r7 = r31;
    L_0x0123:
        r13 = 0;
        r18 = (r12 > r13 ? 1 : (r12 == r13 ? 0 : -1));
        r19 = 4607182418800017408; // 0x3ff0000000000000 float:0.0 double:1.0;
        if (r18 == 0) goto L_0x0135;
    L_0x012a:
        r32 = r7;
        r6 = (double) r5;
        r24 = r2 - r19;
        r6 = (r6 > r24 ? 1 : (r6 == r24 ? 0 : -1));
        if (r6 != 0) goto L_0x0137;
    L_0x0133:
        r6 = r12;
        goto L_0x0139;
    L_0x0135:
        r32 = r7;
    L_0x0137:
        r6 = r30;
    L_0x0139:
        r7 = r10;
        r13 = r21;
        r33 = r9;
        r34 = r10;
        r9 = (double) r6;
        r35 = r1;
        r36 = r2;
        r1 = r27;
        r24 = java.lang.Math.cos(r1);
        r9 = r9 * r24;
        r3 = (float) r9;
        r9 = (double) r6;
        r24 = java.lang.Math.sin(r1);
        r9 = r9 * r24;
        r9 = (float) r9;
        r10 = 0;
        r15 = (r11 > r10 ? 1 : (r11 == r10 ? 0 : -1));
        if (r15 != 0) goto L_0x0170;
    L_0x015b:
        r18 = (r14 > r10 ? 1 : (r14 == r10 ? 0 : -1));
        if (r18 != 0) goto L_0x0170;
    L_0x015f:
        r10 = r0.path;
        r10.lineTo(r3, r9);
        r47 = r1;
        r49 = r9;
        r45 = r11;
        r46 = r14;
        r25 = 0;
        goto L_0x021b;
    L_0x0170:
        r45 = r11;
        r10 = (double) r13;
        r46 = r14;
        r14 = (double) r7;
        r10 = java.lang.Math.atan2(r10, r14);
        r14 = 4609753056924675352; // 0x3ff921fb54442d18 float:3.37028055E12 double:1.5707963267948966;
        r10 = r10 - r14;
        r10 = (float) r10;
        r14 = (double) r10;
        r14 = java.lang.Math.cos(r14);
        r11 = (float) r14;
        r14 = (double) r10;
        r14 = java.lang.Math.sin(r14);
        r14 = (float) r14;
        r47 = r1;
        r1 = (double) r9;
        r49 = r9;
        r50 = r10;
        r9 = (double) r3;
        r1 = java.lang.Math.atan2(r1, r9);
        r9 = 4609753056924675352; // 0x3ff921fb54442d18 float:3.37028055E12 double:1.5707963267948966;
        r1 = r1 - r9;
        r1 = (float) r1;
        r9 = (double) r1;
        r9 = java.lang.Math.cos(r9);
        r2 = (float) r9;
        r9 = (double) r1;
        r9 = java.lang.Math.sin(r9);
        r9 = (float) r9;
        if (r8 == 0) goto L_0x01b1;
    L_0x01ae:
        r10 = r45;
        goto L_0x01b3;
    L_0x01b1:
        r10 = r46;
    L_0x01b3:
        if (r8 == 0) goto L_0x01b8;
    L_0x01b5:
        r15 = r46;
        goto L_0x01ba;
    L_0x01b8:
        r15 = r45;
    L_0x01ba:
        if (r8 == 0) goto L_0x01bf;
    L_0x01bc:
        r18 = r22;
        goto L_0x01c1;
    L_0x01bf:
        r18 = r23;
    L_0x01c1:
        if (r8 == 0) goto L_0x01c6;
    L_0x01c3:
        r21 = r23;
        goto L_0x01c8;
    L_0x01c6:
        r21 = r22;
    L_0x01c8:
        r24 = r18 * r10;
        r25 = 1056236141; // 0x3ef4e26d float:0.47829 double:5.21849991E-315;
        r24 = r24 * r25;
        r24 = r24 * r11;
        r26 = r18 * r10;
        r26 = r26 * r25;
        r26 = r26 * r14;
        r27 = r21 * r15;
        r27 = r27 * r25;
        r27 = r27 * r2;
        r28 = r21 * r15;
        r28 = r28 * r25;
        r28 = r28 * r9;
        r25 = 0;
        r30 = (r29 > r25 ? 1 : (r29 == r25 ? 0 : -1));
        if (r30 == 0) goto L_0x0204;
    L_0x01e9:
        if (r5 != 0) goto L_0x01f4;
    L_0x01eb:
        r24 = r24 * r29;
        r26 = r26 * r29;
        r51 = r1;
        r52 = r2;
        goto L_0x0208;
    L_0x01f4:
        r51 = r1;
        r52 = r2;
        r1 = (double) r5;
        r19 = r36 - r19;
        r1 = (r1 > r19 ? 1 : (r1 == r19 ? 0 : -1));
        if (r1 != 0) goto L_0x0208;
    L_0x01ff:
        r27 = r27 * r29;
        r28 = r28 * r29;
        goto L_0x0208;
    L_0x0204:
        r51 = r1;
        r52 = r2;
    L_0x0208:
        r1 = r0.path;
        r39 = r7 - r24;
        r40 = r13 - r26;
        r41 = r3 + r27;
        r42 = r49 + r28;
        r38 = r1;
        r43 = r3;
        r44 = r49;
        r38.cubicTo(r39, r40, r41, r42, r43, r44);
    L_0x021b:
        r1 = r32;
        r9 = (double) r1;
        r27 = r47 + r9;
        if (r8 != 0) goto L_0x0224;
    L_0x0222:
        r2 = 1;
        goto L_0x0225;
    L_0x0224:
        r2 = 0;
    L_0x0225:
        r8 = r2;
        r5 = r5 + 1;
        r10 = r3;
        r7 = r29;
        r9 = r33;
        r1 = r35;
        r2 = r36;
        r11 = r45;
        r14 = r46;
        r21 = r49;
        goto L_0x00f3;
    L_0x0239:
        r35 = r1;
        r36 = r2;
        r33 = r9;
        r34 = r10;
        r45 = r11;
        r46 = r14;
        r47 = r27;
        r1 = r0.positionAnimation;
        r1 = r1.getValue();
        r1 = (android.graphics.PointF) r1;
        r2 = r0.path;
        r3 = r1.x;
        r5 = r1.y;
        r2.offset(r3, r5);
        r2 = r0.path;
        r2.close();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airbnb.lottie.animation.content.PolystarContent.createStarPath():void");
    }

    private void createPolygonPath() {
        double numPoints;
        double currentAngle;
        int points = (int) Math.floor((double) ((Float) this.pointsAnimation.getValue()).floatValue());
        double currentAngle2 = Math.toRadians((this.rotationAnimation == null ? 0.0d : (double) ((Float) this.rotationAnimation.getValue()).floatValue()) - 90.0d);
        int anglePerPoint = (float) (6.283185307179586d / ((double) points));
        float roundedness = ((Float) this.outerRoundednessAnimation.getValue()).floatValue() / 100.0f;
        float radius = ((Float) this.outerRadiusAnimation.getValue()).floatValue();
        float x = (float) (((double) radius) * Math.cos(currentAngle2));
        float y = (float) (((double) radius) * Math.sin(currentAngle2));
        this.path.moveTo(x, y);
        currentAngle2 += (double) anglePerPoint;
        double numPoints2 = Math.ceil((double) points);
        int i = 0;
        while (((double) i) < numPoints2) {
            int points2;
            int anglePerPoint2;
            float previousX = x;
            float previousY = y;
            x = (float) (((double) radius) * Math.cos(currentAngle2));
            y = (float) (((double) radius) * Math.sin(currentAngle2));
            if (roundedness != 0.0f) {
                numPoints = numPoints2;
                numPoints2 = (float) (Math.atan2((double) previousY, (double) previousX) - 1.5707963267948966d);
                points2 = points;
                currentAngle = currentAngle2;
                anglePerPoint2 = anglePerPoint;
                points = (float) (Math.atan2((double) y, (double) x) - 1.5707963267948966d);
                int cp2Theta = points;
                this.path.cubicTo(previousX - (((radius * roundedness) * POLYGON_MAGIC_NUMBER) * ((float) Math.cos((double) numPoints2))), previousY - (((radius * roundedness) * POLYGON_MAGIC_NUMBER) * ((float) Math.sin((double) numPoints2))), x + (((radius * roundedness) * POLYGON_MAGIC_NUMBER) * ((float) Math.cos((double) points))), y + (((radius * roundedness) * POLYGON_MAGIC_NUMBER) * ((float) Math.sin((double) points))), x, y);
            } else {
                points2 = points;
                currentAngle = currentAngle2;
                anglePerPoint2 = anglePerPoint;
                numPoints = numPoints2;
                this.path.lineTo(x, y);
            }
            points = anglePerPoint2;
            currentAngle2 = currentAngle + ((double) points);
            i++;
            anglePerPoint = points;
            numPoints2 = numPoints;
            points = points2;
        }
        currentAngle = currentAngle2;
        numPoints = numPoints2;
        PointF position = (PointF) this.positionAnimation.getValue();
        this.path.offset(position.x, position.y);
        this.path.close();
    }
}
