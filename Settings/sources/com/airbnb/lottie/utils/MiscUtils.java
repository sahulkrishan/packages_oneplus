package com.airbnb.lottie.utils;

import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.FloatRange;
import com.airbnb.lottie.model.CubicCurveData;
import com.airbnb.lottie.model.content.ShapeData;

public class MiscUtils {
    public static PointF addPoints(PointF p1, PointF p2) {
        return new PointF(p1.x + p2.x, p1.y + p2.y);
    }

    public static void getPathFromData(ShapeData shapeData, Path outPath) {
        Path path = outPath;
        outPath.reset();
        PointF initialPoint = shapeData.getInitialPoint();
        path.moveTo(initialPoint.x, initialPoint.y);
        PointF currentPoint = new PointF(initialPoint.x, initialPoint.y);
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= shapeData.getCurves().size()) {
                break;
            }
            CubicCurveData curveData = (CubicCurveData) shapeData.getCurves().get(i2);
            PointF cp1 = curveData.getControlPoint1();
            PointF cp2 = curveData.getControlPoint2();
            PointF vertex = curveData.getVertex();
            if (cp1.equals(currentPoint) && cp2.equals(vertex)) {
                path.lineTo(vertex.x, vertex.y);
            } else {
                path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, vertex.x, vertex.y);
            }
            currentPoint.set(vertex.x, vertex.y);
            i = i2 + 1;
        }
        if (shapeData.isClosed()) {
            outPath.close();
        }
    }

    public static float lerp(float a, float b, @FloatRange(from = 0.0d, to = 1.0d) float percentage) {
        return ((b - a) * percentage) + a;
    }

    public static double lerp(double a, double b, @FloatRange(from = 0.0d, to = 1.0d) double percentage) {
        return ((b - a) * percentage) + a;
    }

    public static int lerp(int a, int b, @FloatRange(from = 0.0d, to = 1.0d) float percentage) {
        return (int) (((float) a) + (((float) (b - a)) * percentage));
    }

    public static int floorMod(float x, float y) {
        return floorMod((int) x, (int) y);
    }

    public static int floorMod(int x, int y) {
        return x - (floorDiv(x, y) * y);
    }

    private static int floorDiv(int x, int y) {
        int r = x / y;
        if ((x ^ y) >= 0 || r * y == x) {
            return r;
        }
        return r - 1;
    }

    public static float clamp(float number, float min, float max) {
        return Math.max(min, Math.min(max, number));
    }
}
