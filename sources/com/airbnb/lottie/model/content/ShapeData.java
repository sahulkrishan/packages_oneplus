package com.airbnb.lottie.model.content;

import android.graphics.PointF;
import android.support.annotation.FloatRange;
import com.airbnb.lottie.model.CubicCurveData;
import com.airbnb.lottie.utils.MiscUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class ShapeData {
    private boolean closed;
    private final List<CubicCurveData> curves;
    private PointF initialPoint;

    public static class Factory implements com.airbnb.lottie.model.animatable.AnimatableValue.Factory<ShapeData> {
        public static final Factory INSTANCE = new Factory();

        private Factory() {
        }

        public ShapeData valueFromObject(Object object, float scale) {
            JSONObject jSONObject = object;
            JSONObject pointsData = null;
            if (jSONObject instanceof JSONArray) {
                JSONObject firstObject = ((JSONArray) jSONObject).opt(0);
                if ((firstObject instanceof JSONObject) && firstObject.has("v")) {
                    pointsData = firstObject;
                }
            } else if ((jSONObject instanceof JSONObject) && jSONObject.has("v")) {
                pointsData = jSONObject;
            }
            if (pointsData == null) {
                return null;
            }
            JSONArray pointsArray = pointsData.optJSONArray("v");
            JSONArray inTangents = pointsData.optJSONArray("i");
            JSONArray outTangents = pointsData.optJSONArray("o");
            boolean closed = pointsData.optBoolean("c", false);
            if (pointsArray == null || inTangents == null || outTangents == null || pointsArray.length() != inTangents.length() || pointsArray.length() != outTangents.length()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to process points array or tangents. ");
                stringBuilder.append(pointsData);
                throw new IllegalStateException(stringBuilder.toString());
            } else if (pointsArray.length() == 0) {
                return new ShapeData(new PointF(), false, Collections.emptyList());
            } else {
                PointF previousVertex;
                PointF cp2;
                PointF shapeCp1;
                PointF shapeCp2;
                int length = pointsArray.length();
                PointF vertex = vertexAtIndex(0, pointsArray);
                vertex.x *= scale;
                vertex.y *= scale;
                PointF initialPoint = vertex;
                List<CubicCurveData> curves = new ArrayList(length);
                int i = 1;
                while (i < length) {
                    vertex = vertexAtIndex(i, pointsArray);
                    previousVertex = vertexAtIndex(i - 1, pointsArray);
                    PointF cp1 = vertexAtIndex(i - 1, outTangents);
                    cp2 = vertexAtIndex(i, inTangents);
                    shapeCp1 = MiscUtils.addPoints(previousVertex, cp1);
                    shapeCp2 = MiscUtils.addPoints(vertex, cp2);
                    shapeCp1.x *= scale;
                    shapeCp1.y *= scale;
                    shapeCp2.x *= scale;
                    shapeCp2.y *= scale;
                    vertex.x *= scale;
                    vertex.y *= scale;
                    curves.add(new CubicCurveData(shapeCp1, shapeCp2, vertex));
                    i++;
                    Object obj = object;
                }
                if (closed) {
                    vertex = vertexAtIndex(0, pointsArray);
                    cp2 = vertexAtIndex(length - 1, pointsArray);
                    shapeCp1 = vertexAtIndex(length - 1, outTangents);
                    shapeCp2 = vertexAtIndex(0, inTangents);
                    PointF shapeCp12 = MiscUtils.addPoints(cp2, shapeCp1);
                    previousVertex = MiscUtils.addPoints(vertex, shapeCp2);
                    if (scale != 1.0f) {
                        shapeCp12.x *= scale;
                        shapeCp12.y *= scale;
                        previousVertex.x *= scale;
                        previousVertex.y *= scale;
                        vertex.x *= scale;
                        vertex.y *= scale;
                    }
                    curves.add(new CubicCurveData(shapeCp12, previousVertex, vertex));
                }
                return new ShapeData(initialPoint, closed, curves);
            }
        }

        private static PointF vertexAtIndex(int idx, JSONArray points) {
            if (idx < points.length()) {
                JSONArray pointArray = points.optJSONArray(idx);
                Object x = pointArray.opt(null);
                Object y = pointArray.opt(1);
                return new PointF(x instanceof Double ? ((Double) x).floatValue() : (float) ((Integer) x).intValue(), y instanceof Double ? ((Double) y).floatValue() : (float) ((Integer) y).intValue());
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid index ");
            stringBuilder.append(idx);
            stringBuilder.append(". There are only ");
            stringBuilder.append(points.length());
            stringBuilder.append(" points.");
            throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    private ShapeData(PointF initialPoint, boolean closed, List<CubicCurveData> curves) {
        this.curves = new ArrayList();
        this.initialPoint = initialPoint;
        this.closed = closed;
        this.curves.addAll(curves);
    }

    public ShapeData() {
        this.curves = new ArrayList();
    }

    private void setInitialPoint(float x, float y) {
        if (this.initialPoint == null) {
            this.initialPoint = new PointF();
        }
        this.initialPoint.set(x, y);
    }

    public PointF getInitialPoint() {
        return this.initialPoint;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public List<CubicCurveData> getCurves() {
        return this.curves;
    }

    public void interpolateBetween(ShapeData shapeData1, ShapeData shapeData2, @FloatRange(from = 0.0d, to = 1.0d) float percentage) {
        float f = percentage;
        if (this.initialPoint == null) {
            this.initialPoint = new PointF();
        }
        boolean z = shapeData1.isClosed() || shapeData2.isClosed();
        this.closed = z;
        if (this.curves.isEmpty() || this.curves.size() == shapeData1.getCurves().size() || this.curves.size() == shapeData2.getCurves().size()) {
            if (this.curves.isEmpty()) {
                for (int i = shapeData1.getCurves().size() - 1; i >= 0; i--) {
                    this.curves.add(new CubicCurveData());
                }
            }
            PointF initialPoint1 = shapeData1.getInitialPoint();
            PointF initialPoint2 = shapeData2.getInitialPoint();
            setInitialPoint(MiscUtils.lerp(initialPoint1.x, initialPoint2.x, f), MiscUtils.lerp(initialPoint1.y, initialPoint2.y, f));
            int i2 = this.curves.size() - 1;
            while (true) {
                int i3 = i2;
                if (i3 >= 0) {
                    CubicCurveData curve1 = (CubicCurveData) shapeData1.getCurves().get(i3);
                    CubicCurveData curve2 = (CubicCurveData) shapeData2.getCurves().get(i3);
                    PointF cp11 = curve1.getControlPoint1();
                    PointF cp21 = curve1.getControlPoint2();
                    PointF vertex1 = curve1.getVertex();
                    PointF cp12 = curve2.getControlPoint1();
                    PointF cp22 = curve2.getControlPoint2();
                    PointF vertex2 = curve2.getVertex();
                    PointF initialPoint12 = initialPoint1;
                    ((CubicCurveData) this.curves.get(i3)).setControlPoint1(MiscUtils.lerp(cp11.x, cp12.x, f), MiscUtils.lerp(cp11.y, cp12.y, f));
                    ((CubicCurveData) this.curves.get(i3)).setControlPoint2(MiscUtils.lerp(cp21.x, cp22.x, f), MiscUtils.lerp(cp21.y, cp22.y, f));
                    ((CubicCurveData) this.curves.get(i3)).setVertex(MiscUtils.lerp(vertex1.x, vertex2.x, f), MiscUtils.lerp(vertex1.y, vertex2.y, f));
                    i2 = i3 - 1;
                    initialPoint1 = initialPoint12;
                } else {
                    return;
                }
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Curves must have the same number of control points. This: ");
        stringBuilder.append(getCurves().size());
        stringBuilder.append("\tShape 1: ");
        stringBuilder.append(shapeData1.getCurves().size());
        stringBuilder.append("\tShape 2: ");
        stringBuilder.append(shapeData2.getCurves().size());
        throw new IllegalStateException(stringBuilder.toString());
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ShapeData{numCurves=");
        stringBuilder.append(this.curves.size());
        stringBuilder.append("closed=");
        stringBuilder.append(this.closed);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
